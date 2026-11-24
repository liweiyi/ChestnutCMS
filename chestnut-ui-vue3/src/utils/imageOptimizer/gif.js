import { parseGIF, decompressFrame } from 'gifuct-js'
import { GIFEncoder } from 'gifenc/src/index.js'
import { resolveCompressionOptions } from './compression.js'
import { quantizeRgba } from './quantize.js'
import { getOrientedSize, transformPixels } from './transforms.js'
import { hasGeometryChanges } from './model.js'

const MAX_PIXELS = 16_000_000
const MAX_FRAME_PIXELS = 250_000_000
const MAX_FRAMES = 3000
const MAX_PREVIEW_EDGE = 1600

function fail(code) {
  throw Object.assign(new Error(code), { code })
}

function bytesOf(buffer) {
  return buffer instanceof ArrayBuffer
    ? new Uint8Array(buffer)
    : new Uint8Array(buffer.buffer, buffer.byteOffset, buffer.byteLength)
}

// Preflight before gifuct allocates frame data. Also read the unmodified timing:
// gifuct's public decompressor substitutes 100 ms for a zero-delay frame.
function scanGif(buffer) {
  const bytes = bytesOf(buffer)
  const word = (offset) => bytes[offset] | (bytes[offset + 1] << 8)
  const requireBytes = (offset, count) => {
    if (offset < 0 || offset + count > bytes.length) fail('GIF_INVALID_DATA')
  }
  requireBytes(0, 13)
  const signature = String.fromCharCode(...bytes.subarray(0, 6))
  if (signature !== 'GIF87a' && signature !== 'GIF89a') fail('GIF_INVALID_DATA')
  const width = word(6)
  const height = word(8)
  if (!width || !height) fail('GIF_INVALID_SIZE')
  if (width * height > MAX_PIXELS) fail('GIF_IMAGE_TOO_LARGE')
  let offset = 13 + (bytes[10] & 0x80 ? 3 * (2 << (bytes[10] & 7)) : 0)
  requireBytes(0, offset)
  let repeat = -1
  let control = { delay: 0, disposal: 0, transparent: false }
  const frames = []
  let totalPatchPixels = 0
  const skipBlocks = () => {
    let first
    while (true) {
      requireBytes(offset, 1)
      const size = bytes[offset++]
      if (!size) return first
      requireBytes(offset, size)
      if (!first) first = bytes.subarray(offset, offset + size)
      offset += size
    }
  }
  while (offset < bytes.length) {
    const marker = bytes[offset++]
    if (marker === 0x3b) {
      if (!frames.length) fail('GIF_INVALID_DATA')
      return { width, height, repeat, frames }
    }
    if (marker === 0x21) {
      requireBytes(offset, 1)
      const label = bytes[offset++]
      if (label === 0xf9) {
        requireBytes(offset, 6)
        if (bytes[offset] !== 4 || bytes[offset + 5] !== 0) fail('GIF_INVALID_DATA')
        const flags = bytes[offset + 1]
        control = { delay: word(offset + 2) * 10, disposal: (flags >> 2) & 7, transparent: Boolean(flags & 1), transparentIndex: bytes[offset + 4] }
        offset += 6
      } else if (label === 0xff) {
        requireBytes(offset, 1)
        const size = bytes[offset++]
        requireBytes(offset, size)
        const id = String.fromCharCode(...bytes.subarray(offset, offset + size))
        offset += size
        const data = skipBlocks()
        if ((id === 'NETSCAPE2.0' || id === 'ANIMEXTS1.0') && data?.length >= 3 && data[0] === 1) {
          repeat = data[1] | (data[2] << 8)
        }
      } else {
        skipBlocks()
        // A plain-text rendering block consumes its graphic control extension.
        if (label === 0x01) control = { delay: 0, disposal: 0, transparent: false }
      }
    } else if (marker === 0x2c) {
      requireBytes(offset, 9)
      const patchWidth = word(offset + 4)
      const patchHeight = word(offset + 6)
      const patchPixels = patchWidth * patchHeight
      if (!patchPixels || patchPixels > MAX_PIXELS) fail('GIF_IMAGE_TOO_LARGE')
      totalPatchPixels += patchPixels
      if (totalPatchPixels > MAX_FRAME_PIXELS || frames.length >= MAX_FRAMES) fail('GIF_TOO_MANY_FRAMES')
      frames.push(control)
      control = { delay: 0, disposal: 0, transparent: false }
      const flags = bytes[offset + 8]
      offset += 9 + (flags & 0x80 ? 3 * (2 << (flags & 7)) : 0)
      requireBytes(offset, 1)
      if (bytes[offset] < 2 || bytes[offset] > 8) fail('GIF_INVALID_DATA')
      offset++
      skipBlocks()
    } else {
      fail('GIF_INVALID_DATA')
    }
  }
  fail('GIF_INVALID_DATA')
}

function readGif(buffer) {
  const metadata = scanGif(buffer)
  const bytes = bytesOf(buffer)
  const parsed = parseGIF(bytes.byteOffset === 0 && bytes.byteLength === bytes.buffer.byteLength
    ? bytes.buffer : bytes.slice().buffer)
  const frames = parsed.frames.filter((frame) => frame.image)
  if (frames.length !== metadata.frames.length) fail('GIF_INVALID_DATA')
  return { ...metadata, parsed, images: frames }
}

function decodeFrame(gif, index) {
  const frame = decompressFrame(gif.images[index], gif.parsed.gct, false)
  // A GCE can be separated from its image by several comment/application
  // extensions. Its scope follows GIF blocks, rather than parser object groups.
  frame.transparentIndex = gif.frames[index].transparent ? gif.frames[index].transparentIndex : undefined
  return frame
}

function clippedRect(dims, width, height) {
  const x = Math.min(width, Math.max(0, dims.left))
  const y = Math.min(height, Math.max(0, dims.top))
  return { x, y, width: Math.max(0, Math.min(width, dims.left + dims.width) - x), height: Math.max(0, Math.min(height, dims.top + dims.height) - y) }
}

function fillRect(canvas, width, rect, color) {
  for (let y = rect.y; y < rect.y + rect.height; y++) {
    for (let x = rect.x; x < rect.x + rect.width; x++) {
      const pos = (y * width + x) * 4
      canvas[pos] = color[0]
      canvas[pos + 1] = color[1]
      canvas[pos + 2] = color[2]
      canvas[pos + 3] = color[3]
    }
  }
}

function copyRect(canvas, width, rect) {
  const copy = new Uint8ClampedArray(rect.width * rect.height * 4)
  for (let y = 0; y < rect.height; y++) {
    const start = ((rect.y + y) * width + rect.x) * 4
    copy.set(canvas.subarray(start, start + rect.width * 4), y * rect.width * 4)
  }
  return copy
}

function restoreRect(canvas, width, rect, copy) {
  for (let y = 0; y < rect.height; y++) {
    canvas.set(copy.subarray(y * rect.width * 4, (y + 1) * rect.width * 4), ((rect.y + y) * width + rect.x) * 4)
  }
}

function drawFrame(canvas, width, height, frame) {
  const { dims, colorTable, pixels, transparentIndex } = frame
  const rect = clippedRect(dims, width, height)
  for (let y = rect.y; y < rect.y + rect.height; y++) {
    for (let x = rect.x; x < rect.x + rect.width; x++) {
      const index = pixels[(y - dims.top) * dims.width + x - dims.left]
      if (index === transparentIndex) continue
      const color = colorTable?.[index]
      if (!color) fail('GIF_INVALID_DATA')
      const offset = (y * width + x) * 4
      canvas[offset] = color[0]
      canvas[offset + 1] = color[1]
      canvas[offset + 2] = color[2]
      canvas[offset + 3] = 255
    }
  }
  return rect
}

function backgroundColor(gif, transparent) {
  const rgb = gif.parsed.gct?.[gif.parsed.lsd.backgroundColorIndex]
  return transparent || !rgb ? [0, 0, 0, 0] : [...rgb, 255]
}

function initialCanvas(gif) {
  const canvas = new Uint8ClampedArray(gif.width * gif.height * 4)
  fillRect(canvas, gif.width, { x: 0, y: 0, width: gif.width, height: gif.height }, backgroundColor(gif, gif.frames[0].transparent))
  return canvas
}

function geometry(gif, options) {
  const source = getOrientedSize(gif.width, gif.height, options.orientation ?? 1)
  const crop = options.crop || { x: 0, y: 0, width: source.width, height: source.height }
  const rect = Object.fromEntries(Object.entries(crop).map(([key, value]) => [key, Math.round(Number(value))]))
  const width = Math.round(Number(options.width ?? rect.width))
  const height = Math.round(Number(options.height ?? rect.height))
  if (![rect.x, rect.y, rect.width, rect.height, width, height].every(Number.isFinite)
      || rect.x < 0 || rect.y < 0 || rect.width < 1 || rect.height < 1
      || rect.x + rect.width > source.width || rect.y + rect.height > source.height
      || width < 1 || height < 1 || width > rect.width || height > rect.height) fail('GIF_INVALID_SIZE')
  if (gif.width * gif.height * gif.frames.length > MAX_FRAME_PIXELS) fail('GIF_TOO_MANY_FRAMES')
  return { crop: rect, width, height }
}

// Area sampling in premultiplied alpha space keeps transparent edges clean and
// works in a Worker and Node without requiring a Canvas implementation.
function transform(data, sourceWidth, crop, width, height) {
  if (width === crop.width && height === crop.height) return copyRect(data, sourceWidth, crop)
  const output = new Uint8ClampedArray(width * height * 4)
  const sx = crop.width / width
  const sy = crop.height / height
  const area = sx * sy
  for (let y = 0; y < height; y++) {
    const top = crop.y + y * sy
    const bottom = Math.min(crop.y + crop.height, crop.y + (y + 1) * sy)
    for (let x = 0; x < width; x++) {
      const left = crop.x + x * sx
      const right = Math.min(crop.x + crop.width, crop.x + (x + 1) * sx)
      let red = 0, green = 0, blue = 0, alpha = 0
      for (let yy = Math.floor(top); yy < Math.ceil(bottom); yy++) {
        const wy = Math.min(bottom, yy + 1) - Math.max(top, yy)
        for (let xx = Math.floor(left); xx < Math.ceil(right); xx++) {
          const weight = wy * (Math.min(right, xx + 1) - Math.max(left, xx))
          const pos = (yy * sourceWidth + xx) * 4
          const coverage = data[pos + 3] * weight
          red += data[pos] * coverage
          green += data[pos + 1] * coverage
          blue += data[pos + 2] * coverage
          alpha += coverage
        }
      }
      const pos = (y * width + x) * 4
      if (alpha) {
        output[pos] = red / alpha
        output[pos + 1] = green / alpha
        output[pos + 2] = blue / alpha
        output[pos + 3] = alpha / area
      }
    }
  }
  return output
}

function previewOf(data, width, height) {
  const scale = Math.min(1, MAX_PREVIEW_EDGE / Math.max(width, height))
  const previewWidth = Math.max(1, Math.round(width * scale))
  const previewHeight = Math.max(1, Math.round(height * scale))
  const preview = transform(data, width, { x: 0, y: 0, width, height }, previewWidth, previewHeight)
  return { data: preview.buffer, width: previewWidth, height: previewHeight }
}

export function inspectGif(buffer) {
  const gif = readGif(buffer)
  const canvas = initialCanvas(gif)
  drawFrame(canvas, gif.width, gif.height, decodeFrame(gif, 0))
  return { width: gif.width, height: gif.height, animated: gif.frames.length > 1, frames: gif.frames.length, repeat: gif.repeat, preview: previewOf(canvas, gif.width, gif.height) }
}

// Decode on demand for canvas playback. The returned pixels remain the current
// frame until next/reset is called; pausing therefore needs no extra snapshot.
// Callers must paint/copy them before advancing, since the canvas is reused.
export function createGifFrameReader(buffer) {
  const gif = readGif(buffer)
  const canvas = initialCanvas(gif)
  let index = 0
  let pending = null
  const reset = () => {
    fillRect(canvas, gif.width, { x: 0, y: 0, width: gif.width, height: gif.height }, backgroundColor(gif, gif.frames[0].transparent))
    index = 0
    pending = null
  }
  const next = () => {
    if (index >= gif.images.length) reset()
    if (pending?.control.disposal === 2) {
      fillRect(canvas, gif.width, pending.rect, backgroundColor(gif, pending.control.transparent))
    } else if (pending?.control.disposal === 3) {
      restoreRect(canvas, gif.width, pending.rect, pending.previous)
    }
    const frame = decodeFrame(gif, index)
    const control = gif.frames[index]
    const rect = clippedRect(frame.dims, gif.width, gif.height)
    const previous = control.disposal === 3 ? copyRect(canvas, gif.width, rect) : null
    drawFrame(canvas, gif.width, gif.height, frame)
    pending = { control, rect, previous }
    return { data: canvas, width: gif.width, height: gif.height, delay: control.delay, index: index++ }
  }
  return { width: gif.width, height: gif.height, repeat: gif.repeat, frameCount: gif.images.length, reset, next }
}

function getPaletteSize(options) {
  return options.strategy === 'palette' ? Math.max(2, Math.min(256, Math.round(Number(options.colors) || 256))) : 256
}

export async function encodeStaticGif(image, options, onStage) {
  const { width, height } = image
  if (width > 0xffff || height > 0xffff || width * height > MAX_PIXELS) fail('INVALID_DIMENSIONS')
  onStage('quantize')
  const compression = resolveCompressionOptions('gif', options)
  const { palette, indices } = await quantizeRgba(image, { colors: getPaletteSize(compression), binaryAlpha: true })
  onStage('encode')
  const encoder = GIFEncoder()
  encoder.writeFrame(indices, width, height, {
    palette,
    transparent: palette[0][3] === 0,
    transparentIndex: 0,
    repeat: -1
  })
  encoder.finish()
  return encoder.bytes().buffer
}

export async function processGif(buffer, options = {}, onStage = () => {}) {
  options = resolveCompressionOptions('gif', options)
  onStage('decode', 0)
  const gif = readGif(buffer)
  const { crop, width, height } = geometry(gif, options)
  const geometryChanged = hasGeometryChanges(options, gif.width, gif.height)
  const canvas = initialCanvas(gif)
  const encoder = GIFEncoder()
  const colors = getPaletteSize(options)
  // Full independent frames must clear to transparent between images whenever
  // any source disposal can expose transparency. Reserve index 0 consistently.
  const transparent = !gif.parsed.gct || gif.frames[0].transparent
    || gif.frames.some((frame) => frame.disposal === 2 && frame.transparent)
  let reference
  for (let i = 0; i < gif.images.length; i++) {
    const progress = i / gif.images.length
    onStage('decode', progress)
    const frame = decodeFrame(gif, i)
    const control = gif.frames[i]
    const rect = clippedRect(frame.dims, gif.width, gif.height)
    const previous = control.disposal === 3 ? copyRect(canvas, gif.width, rect) : null
    drawFrame(canvas, gif.width, gif.height, frame)
    onStage('crop', progress)
    onStage('resize', progress)
    const oriented = transformPixels({ data: canvas, width: gif.width, height: gif.height }, options.orientation ?? 1)
    const transformed = transform(oriented.data, oriented.width, crop, width, height)
    if (!reference) reference = previewOf(transformed, width, height)
    onStage('quantize', progress)
    const quantized = await quantizeRgba({ data: transformed, width, height }, {
      ...options, colors, binaryAlpha: true, reserveTransparent: transparent
    })
    const { palette, indices } = quantized
    onStage('encode', progress)
    encoder.writeFrame(indices, width, height, {
      palette,
      transparent,
      transparentIndex: 0,
      dispose: transparent ? 2 : 1,
      delay: control.delay,
      repeat: gif.repeat
    })
    if (control.disposal === 2) fillRect(canvas, gif.width, rect, backgroundColor(gif, control.transparent))
    else if (control.disposal === 3) restoreRect(canvas, gif.width, rect, previous)
  }
  encoder.finish()
  const output = encoder.bytes()
  onStage('preview', 1)
  const result = inspectGif(output)
  return { buffer: output.buffer, width, height, preview: result.preview, reference, frames: gif.frames.length, repeat: gif.repeat, delays: gif.frames.map((frame) => frame.delay), geometryChanged }
}
