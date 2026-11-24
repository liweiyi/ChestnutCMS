import { buildUpngPalette } from './vendor/upng-quantize.js'

const SAMPLE_LIMIT = 65536
const CACHE_LIMIT = 65536
const MAX_PIXELS = 64 * 1024 * 1024

function fail(code = 'INVALID_COMPRESSION_OPTIONS') {
  throw Object.assign(new Error(code), { code })
}

function packedColor(r, g, b, a) {
  return (r | (g << 8) | (b << 16) | (a << 24)) >>> 0
}

function unpackColor(value) {
  return [value & 255, (value >>> 8) & 255, (value >>> 16) & 255, value >>> 24]
}

// Balanced palette tree in the same premultiplied RGBA space used for training.
// The point and search state are reused, so mapping allocates nothing per pixel.
function createNearestMapper(palette, firstVisibleIndex) {
  const points = palette.slice(firstVisibleIndex).map((color, offset) => {
    const alpha = color[3] / 255
    return { index: firstVisibleIndex + offset, values: [color[0] * alpha, color[1] * alpha, color[2] * alpha, color[3]] }
  })
  function build(items) {
    if (!items.length) return null
    let axis = 0
    let largestSpread = -1
    for (let channel = 0; channel < 4; channel++) {
      let min = Infinity
      let max = -Infinity
      for (const point of items) {
        min = Math.min(min, point.values[channel])
        max = Math.max(max, point.values[channel])
      }
      if (max - min > largestSpread) { largestSpread = max - min; axis = channel }
    }
    items.sort((a, b) => a.values[axis] - b.values[axis] || a.index - b.index)
    const middle = items.length >>> 1
    return { point: items[middle], axis, left: build(items.slice(0, middle)), right: build(items.slice(middle + 1)) }
  }
  const root = build(points)
  const current = new Float64Array(4)
  let bestIndex = firstVisibleIndex
  let bestDistance = Infinity
  function visit(node) {
    if (!node) return
    const values = node.point.values
    const dr = current[0] - values[0]
    const dg = current[1] - values[1]
    const db = current[2] - values[2]
    const da = current[3] - values[3]
    const distance = dr * dr + dg * dg + db * db + da * da
    if (distance < bestDistance || (distance === bestDistance && node.point.index < bestIndex)) {
      bestDistance = distance
      bestIndex = node.point.index
    }
    const delta = current[node.axis] - values[node.axis]
    visit(delta <= 0 ? node.left : node.right)
    if (delta * delta <= bestDistance) visit(delta <= 0 ? node.right : node.left)
  }
  return (r, g, b, a) => {
    const alpha = a / 255
    current[0] = r * alpha
    current[1] = g * alpha
    current[2] = b * alpha
    current[3] = a
    bestIndex = firstVisibleIndex
    bestDistance = Infinity
    visit(root)
    return bestIndex
  }
}

/**
 * Fixed palette quantization for PNG/GIF. There is no quality score, target
 * search, dithering or WASM dependency. Source pixels are never modified.
 */
export async function quantizeRgba(image, options = {}) {
  const { width, height, data } = image || {}
  const colors = options.maxColors ?? options.colors ?? 256
  if (![width, height].every(value => Number.isSafeInteger(value) && value > 0)
    || width * height > MAX_PIXELS || !(data instanceof Uint8Array || data instanceof Uint8ClampedArray)
    || data.length !== width * height * 4 || !Number.isInteger(colors) || colors < 2 || colors > 256) fail()

  const binaryAlpha = !!options.binaryAlpha
  const count = width * height
  const capacity = Math.min(count, SAMPLE_LIMIT)
  const samples = new Uint8Array(capacity * 4)
  let visibleCount = 0
  let hasTransparency = false
  let exactColors = new Map()
  let randomState = 0x9e3779b9

  // Deterministic reservoir sampling sees the entire image, including positions
  // between regular strides. Its memory stays bounded even for large frames.
  for (let offset = 0; offset < data.length; offset += 4) {
    const r = data[offset]
    const g = data[offset + 1]
    const b = data[offset + 2]
    const a = binaryAlpha ? (data[offset + 3] < 128 ? 0 : 255) : data[offset + 3]
    if (a === 0) { hasTransparency = true; continue }
    if (exactColors) {
      const key = packedColor(r, g, b, a)
      if (!exactColors.has(key)) {
        if (exactColors.size < colors) exactColors.set(key, [r, g, b, a])
        else exactColors = null
      }
    }
    visibleCount++
    let slot = visibleCount - 1
    if (visibleCount > capacity) {
      randomState ^= randomState << 13
      randomState ^= randomState >>> 17
      randomState ^= randomState << 5
      slot = Math.floor((randomState >>> 0) / 4294967296 * visibleCount)
    }
    if (slot < capacity) {
      const position = slot * 4
      samples[position] = r
      samples[position + 1] = g
      samples[position + 2] = b
      samples[position + 3] = a
    }
  }

  // Transparent pixels cannot be assigned to a visible cluster, and visible
  // pixels cannot accidentally disappear into the transparent entry.
  const reserveTransparent = hasTransparency || (binaryAlpha && !!options.reserveTransparent)
  const firstVisibleIndex = reserveTransparent ? 1 : 0
  const palette = reserveTransparent ? [[0, 0, 0, 0]] : []
  const indices = new Uint8Array(count)
  const rgba = new Uint8ClampedArray(data.length)
  if (!visibleCount) return { palette, indices, rgba, data: rgba, width, height }

  const budget = colors - firstVisibleIndex
  let exactIndices = null
  if (exactColors && exactColors.size <= budget) {
    exactIndices = new Map()
    for (const [key, color] of exactColors) {
      exactIndices.set(key, palette.length)
      palette.push(color)
    }
  } else {
    const length = Math.min(visibleCount, capacity) * 4
    const buffer = length === samples.length ? samples.buffer : samples.slice(0, length).buffer
    const seen = new Set()
    for (const packed of buildUpngPalette(buffer, budget)) {
      const color = unpackColor(packed)
      if (binaryAlpha) color[3] = 255
      if (color[3] === 0) continue
      const key = packedColor(...color)
      if (!seen.has(key)) { seen.add(key); palette.push(color) }
    }
    if (palette.length === firstVisibleIndex || palette.length > colors) fail('PROCESS_FAILED')
  }

  const nearest = exactIndices ? null : createNearestMapper(palette, firstVisibleIndex)
  const cache = new Map()
  const cacheOrder = nearest ? new Uint32Array(CACHE_LIMIT) : null
  let eviction = 0
  for (let offset = 0, pixel = 0; offset < data.length; offset += 4, pixel++) {
    const r = data[offset]
    const g = data[offset + 1]
    const b = data[offset + 2]
    const a = binaryAlpha ? (data[offset + 3] < 128 ? 0 : 255) : data[offset + 3]
    if (a === 0) continue // index/RGBA buffers already contain transparent zero
    const key = packedColor(r, g, b, a)
    let index = exactIndices ? exactIndices.get(key) : cache.get(key)
    if (index === undefined) {
      if (!nearest) fail('PROCESS_FAILED')
      index = nearest(r, g, b, a)
      if (cache.size < CACHE_LIMIT) cacheOrder[cache.size] = key
      else {
        cache.delete(cacheOrder[eviction])
        cacheOrder[eviction] = key
        eviction = (eviction + 1) & (CACHE_LIMIT - 1)
      }
      cache.set(key, index)
    }
    const color = palette[index]
    indices[pixel] = index
    rgba[offset] = color[0]
    rgba[offset + 1] = color[1]
    rgba[offset + 2] = color[2]
    rgba[offset + 3] = color[3]
  }
  return { palette, indices, rgba, data: rgba, width, height }
}
