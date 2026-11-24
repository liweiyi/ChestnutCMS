import { resolveCompressionOptions } from './compression.js'
import { transformPixels } from './transforms.js'

const decoders = new Map()
const encoders = new Map()

// Import the single-thread factories directly. The package entry points may select
// pthread builds when SharedArrayBuffer is available, creating nested Workers.
async function emscripten(factory, url) {
  return factory.default({ noInitialRun: true, locateFile: () => url.default })
}

async function loadDecoder(format) {
  if (!decoders.has(format)) decoders.set(format, (async () => {
    switch (format) {
      case 'jpeg': {
        const [factory, url] = await Promise.all([
          import('@jsquash/jpeg/codec/dec/mozjpeg_dec.js'),
          import('@jsquash/jpeg/codec/dec/mozjpeg_dec.wasm?url')
        ])
        const module = await emscripten(factory, url)
        return buffer => module.decode(buffer, false)
      }
      case 'png': {
        const [module, url] = await Promise.all([
          import('@jsquash/png/codec/pkg/squoosh_png.js'),
          import('@jsquash/png/codec/pkg/squoosh_png_bg.wasm?url')
        ])
        await module.default(url.default)
        return buffer => module.decode(new Uint8Array(buffer))
      }
      case 'webp': {
        const [factory, url] = await Promise.all([
          import('@jsquash/webp/codec/dec/webp_dec.js'),
          import('@jsquash/webp/codec/dec/webp_dec.wasm?url')
        ])
        const module = await emscripten(factory, url)
        return buffer => module.decode(buffer)
      }
      case 'jxl': {
        const [factory, url] = await Promise.all([
          import('@jsquash/jxl/codec/dec/jxl_dec.js'),
          import('@jsquash/jxl/codec/dec/jxl_dec.wasm?url')
        ])
        const module = await emscripten(factory, url)
        return buffer => module.decode(buffer)
      }
      case 'avif': {
        const [factory, url] = await Promise.all([
          import('@jsquash/avif/codec/dec/avif_dec.js'),
          import('@jsquash/avif/codec/dec/avif_dec.wasm?url')
        ])
        const module = await emscripten(factory, url)
        return buffer => module.decode(buffer, 8)
      }
      default: throw new Error('INVALID_IMAGE')
    }
  })())
  try { return await decoders.get(format) }
  catch (error) { decoders.delete(format); throw error }
}

async function loadEncoder(format) {
  if (!encoders.has(format)) encoders.set(format, (async () => {
    switch (format) {
      case 'jpeg': {
        const [factory, url, meta] = await Promise.all([
          import('@jsquash/jpeg/codec/enc/mozjpeg_enc.js'),
          import('@jsquash/jpeg/codec/enc/mozjpeg_enc.wasm?url'),
          import('@jsquash/jpeg/meta.js')
        ])
        const module = await emscripten(factory, url)
        return (image, options) => module.encode(image.data, image.width, image.height, {
          ...meta.defaultOptions, quality: options.quality,
          progressive: options.strategy === 'progressive', baseline: options.strategy === 'standard'
        }).buffer
      }
      case 'png': {
        const [module, url] = await Promise.all([
          import('@jsquash/png/codec/pkg/squoosh_png.js'),
          import('@jsquash/png/codec/pkg/squoosh_png_bg.wasm?url')
        ])
        await module.default(url.default)
        return image => module.encode(new Uint8Array(image.data.buffer, image.data.byteOffset, image.data.byteLength), image.width, image.height, 8).buffer
      }
      case 'webp': {
        const [factory, url, meta] = await Promise.all([
          import('@jsquash/webp/codec/enc/webp_enc.js'),
          import('@jsquash/webp/codec/enc/webp_enc.wasm?url'),
          import('@jsquash/webp/meta.js')
        ])
        const module = await emscripten(factory, url)
        return (image, options) => module.encode(image.data, image.width, image.height, {
          ...meta.defaultOptions, quality: options.quality,
          lossless: options.strategy === 'lossy' ? 0 : 1,
          near_lossless: options.strategy === 'nearLossless' ? options.quality : 100,
          exact: 1, thread_level: 0, method: options.webpMethod ?? meta.defaultOptions.method
        }).buffer
      }
      case 'jxl': {
        const [factory, url, meta] = await Promise.all([
          import('@jsquash/jxl/codec/enc/jxl_enc.js'),
          import('@jsquash/jxl/codec/enc/jxl_enc.wasm?url'),
          import('@jsquash/jxl/meta.js')
        ])
        const module = await emscripten(factory, url)
        return (image, options) => module.encode(image.data, image.width, image.height, {
          ...meta.defaultOptions, quality: options.strategy === 'lossless' ? 100 : options.quality,
          lossless: options.strategy === 'lossless', effort: options.jxlEffort ?? meta.defaultOptions.effort
        }).buffer
      }
      case 'avif': {
        const [factory, url, meta] = await Promise.all([
          import('@jsquash/avif/codec/enc/avif_enc.js'),
          import('@jsquash/avif/codec/enc/avif_enc.wasm?url'),
          import('@jsquash/avif/meta.js')
        ])
        const module = await emscripten(factory, url)
        return (image, options) => module.encode(image.data, image.width, image.height, {
          ...meta.defaultOptions, quality: options.strategy === 'lossless' ? 100 : options.quality,
          lossless: options.strategy === 'lossless',
          subsample: options.strategy === 'lossless' ? 3 : meta.defaultOptions.subsample,
          bitDepth: 8, speed: options.avifSpeed ?? meta.defaultOptions.speed
        }).buffer
      }
      default: throw new Error('INVALID_IMAGE')
    }
  })())
  try { return await encoders.get(format) }
  catch (error) { encoders.delete(format); throw error }
}

export async function decodeImage(buffer, format) {
  if (format === 'gif') {
    const { createGifFrameReader } = await import('./gif.js')
    const reader = createGifFrameReader(buffer)
    if (reader.frameCount !== 1) throw new Error('UNSUPPORTED_ANIMATION')
    const { data, width, height } = reader.next()
    return new ImageData(data, width, height)
  }
  const decoder = await loadDecoder(format)
  const image = await decoder(buffer)
  if (!image || !image.width || !image.height || image.data.length !== image.width * image.height * 4) throw new Error('INVALID_IMAGE')
  if (image.width * image.height > 64 * 1024 * 1024) throw new Error('INVALID_DIMENSIONS')
  return new ImageData(new Uint8ClampedArray(image.data), image.width, image.height)
}

let optimizer
async function optimizePng(buffer, options) {
  if (!optimizer) optimizer = (async () => {
    const [module, url] = await Promise.all([
      import('@jsquash/oxipng/codec/pkg/squoosh_oxipng.js'),
      import('@jsquash/oxipng/codec/pkg/squoosh_oxipng_bg.wasm?url')
    ])
    await module.default(url.default)
    return module
  })()
  let module
  try { module = await optimizer }
  catch (error) { optimizer = undefined; throw error }
  // This bundled OxiPNG ABI has no Zopfli option; the highest preset uses level 6.
  return module.optimise(new Uint8Array(buffer), options.pngLevel ?? 2, false, false).buffer
}

let resizer
export async function resizeImage(image, width, height) {
  if (image.width === width && image.height === height) return image
  if (!resizer) resizer = (async () => {
    const [module, url] = await Promise.all([
      import('@jsquash/resize/lib/resize/pkg/squoosh_resize.js'),
      import('@jsquash/resize/lib/resize/pkg/squoosh_resize_bg.wasm?url')
    ])
    await module.default(url.default)
    return module
  })()
  let module
  try { module = await resizer }
  catch (error) { resizer = undefined; throw error }
  const output = module.resize(new Uint8Array(image.data.buffer), image.width, image.height, width, height, 3, true, true)
  return new ImageData(new Uint8ClampedArray(output), width, height)
}

export function cropImage(image, crop) {
  if (!crop) return image
  const { x, y, width, height } = crop
  if (![x, y, width, height].every(Number.isInteger) || x < 0 || y < 0 || width < 1 || height < 1
    || x + width > image.width || y + height > image.height) throw new Error('INVALID_DIMENSIONS')
  const data = new Uint8ClampedArray(width * height * 4)
  for (let row = 0; row < height; row++) {
    const start = ((row + y) * image.width + x) * 4
    data.set(image.data.subarray(start, start + width * 4), row * width * 4)
  }
  return new ImageData(data, width, height)
}

export function orientImage(image, orientation = 1) {
  const transformed = transformPixels(image, orientation)
  return transformed === image ? image : new ImageData(transformed.data, transformed.width, transformed.height)
}

export async function createPreview(image) {
  const scale = Math.min(1, 1600 / Math.max(image.width, image.height))
  const preview = await resizeImage(image, Math.max(1, Math.round(image.width * scale)), Math.max(1, Math.round(image.height * scale)))
  return { data: preview.data.slice().buffer, width: preview.width, height: preview.height }
}

// Indexed PNG preserves all palette alpha values rather than flattening transparency.
function pngChunk(type, bytes) {
  const output = new Uint8Array(bytes.length + 12)
  const view = new DataView(output.buffer)
  view.setUint32(0, bytes.length)
  output.set([...type].map(char => char.charCodeAt(0)), 4)
  output.set(bytes, 8)
  let crc = 0xffffffff
  for (let i = 4; i < output.length - 4; i++) {
    crc ^= output[i]
    for (let bit = 0; bit < 8; bit++) crc = (crc >>> 1) ^ ((crc & 1) ? 0xedb88320 : 0)
  }
  view.setUint32(output.length - 4, (crc ^ 0xffffffff) >>> 0)
  return output
}

async function indexedPng(image, options, onStage) {
  onStage('quantize')
  const { quantizeRgba } = await import('./quantize.js')
  const { palette: colors, indices: pixelIndices } = await quantizeRgba(image, options)
  onStage('encode')
  const plte = new Uint8Array(colors.length * 3)
  const alpha = new Uint8Array(colors.length)
  colors.forEach((color, index) => {
    plte.set(color.slice(0, 3), index * 3)
    alpha[index] = color[3]
  })
  const rows = new Uint8Array((image.width + 1) * image.height)
  for (let y = 0; y < image.height; y++) rows.set(pixelIndices.subarray(y * image.width, (y + 1) * image.width), y * (image.width + 1) + 1)
  const compressed = await new Response(new Blob([rows]).stream().pipeThrough(new CompressionStream('deflate'))).arrayBuffer()
  const ihdr = new Uint8Array(13)
  const header = new DataView(ihdr.buffer)
  header.setUint32(0, image.width)
  header.setUint32(4, image.height)
  ihdr[8] = 8
  ihdr[9] = 3
  const chunks = [new Uint8Array([137, 80, 78, 71, 13, 10, 26, 10]), pngChunk('IHDR', ihdr), pngChunk('PLTE', plte), pngChunk('tRNS', alpha), pngChunk('IDAT', new Uint8Array(compressed)), pngChunk('IEND', new Uint8Array())]
  return new Blob(chunks).arrayBuffer()
}

function flattenOnWhite(image) {
  let data
  for (let i = 0; i < image.data.length; i += 4) {
    const alpha = image.data[i + 3] / 255
    if (alpha === 1) continue
    data ??= image.data.slice()
    for (let channel = i; channel < i + 3; channel++) {
      data[channel] = 255 + (data[channel] - 255) * alpha
    }
    data[i + 3] = 255
  }
  return data ? new ImageData(data, image.width, image.height) : image
}

export async function encodeImage(image, format, suppliedOptions = {}, onStage = () => {}) {
  if (format === 'gif') {
    const { encodeStaticGif } = await import('./gif.js')
    return encodeStaticGif(image, suppliedOptions, onStage)
  }
  const options = resolveCompressionOptions(format, suppliedOptions)
  if (!Number.isFinite(options.quality) || options.quality < 1 || options.quality > 100
    || !Number.isInteger(options.colors) || options.colors < 2 || options.colors > 256) throw new Error('INVALID_DIMENSIONS')
  let buffer
  if (format === 'png' && options.strategy === 'palette') buffer = await indexedPng(image, options, onStage)
  else {
    const encode = await loadEncoder(format)
    buffer = await encode(format === 'jpeg' ? flattenOnWhite(image) : image, options)
  }
  if (!buffer?.byteLength) throw new Error('PROCESS_FAILED')
  return format === 'png' ? optimizePng(buffer, options) : buffer
}
