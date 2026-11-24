export const MIME_TYPES = Object.freeze({
  jpeg: 'image/jpeg', png: 'image/png', webp: 'image/webp',
  jxl: 'image/jxl', avif: 'image/avif', gif: 'image/gif'
})

const STATIC_OUTPUT_FORMATS = Object.freeze(['jpeg', 'png', 'webp', 'gif', 'avif', 'jxl'])

export function getOutputFormats(source) {
  if (!source) return []
  return source.animated ? [source.format] : STATIC_OUTPUT_FORMATS
}

export function getImageFormat(resource) {
  if (resource && typeof resource === 'object') {
    for (const value of [resource.extension, resource.suffix, resource.format, resource.mime,
      resource.mimeType, resource.contentType, resource.fileName, resource.src, resource.internalUrl, resource.name]) {
      const format = getImageFormat(value)
      if (format) return format
    }
    return null
  }
  if (typeof resource !== 'string') return null
  const value = resource.toLowerCase().trim().split(/[?#;]/)[0]
  const mime = Object.entries(MIME_TYPES).find(([, type]) => type === value)
  if (mime) return mime[0]
  const extension = value.match(/(?:^|\.)(jpe?g|png|webp|jxl|avif|gif)$/)?.[1]
  return extension === 'jpg' ? 'jpeg' : extension || null
}

export function getDefaultOptions(format) {
  return {
    strategy: ({ jpeg: 'progressive', png: 'lossless', gif: 'faithful' })[format] || 'lossy',
    quality: format === 'avif' ? 60 : 85,
    colors: 256,
    dither: false
  }
}

export function getOutputFilename(filename, suffix = '优化') {
  const clean = String(filename || 'image').split(/[?#]/)[0].split('/').pop()
  const dot = clean.lastIndexOf('.')
  return dot > 0 ? `${clean.slice(0, dot)}-${suffix}${clean.slice(dot)}` : `${clean}-${suffix}`
}

export function validateDimensions(width, height, sourceWidth, sourceHeight) {
  if (![width, height, sourceWidth, sourceHeight].every(value => Number.isInteger(value) && value > 0)
    || width > sourceWidth || height > sourceHeight || width * height > 64 * 1024 * 1024) {
    throw Object.assign(new Error('INVALID_DIMENSIONS'), { code: 'INVALID_DIMENSIONS' })
  }
  return { width, height }
}

/** EXIF correction is part of decoding; these are the user's effective edits. */
export function hasGeometryChanges(options, sourceWidth, sourceHeight) {
  if ((options.orientation ?? 1) !== 1) return true
  const crop = options.crop
  if (crop && (crop.x !== 0 || crop.y !== 0 || crop.width !== sourceWidth || crop.height !== sourceHeight)) return true
  return (options.width ?? sourceWidth) !== sourceWidth || (options.height ?? sourceHeight) !== sourceHeight
}

function ascii(bytes, start, count) {
  return String.fromCharCode(...bytes.subarray(start, start + count))
}

/** Use the file signature rather than a resource URL (which can point to a thumbnail). */
export function detectImageFormat(buffer) {
  const bytes = new Uint8Array(buffer)
  if (bytes[0] === 0xff && bytes[1] === 0xd8) return 'jpeg'
  if (ascii(bytes, 0, 8) === '\x89PNG\r\n\x1a\n') return 'png'
  if (/^GIF8[79]a$/.test(ascii(bytes, 0, 6))) return 'gif'
  if (ascii(bytes, 0, 4) === 'RIFF' && ascii(bytes, 8, 4) === 'WEBP') return 'webp'
  if ((bytes[0] === 0xff && bytes[1] === 0x0a) || ascii(bytes, 0, 12) === '\0\0\0\x0cJXL \r\n\x87\n') return 'jxl'
  if (bytes.length >= 16 && ascii(bytes, 4, 4) === 'ftyp') {
    const size = new DataView(buffer).getUint32(0)
    for (let pos = 8; pos + 4 <= Math.min(size, bytes.length); pos += 4) {
      if (pos !== 12 && /^(avif|avis)$/.test(ascii(bytes, pos, 4))) return 'avif'
    }
  }
  return null
}

function jxlCodestream(buffer) {
  const bytes = new Uint8Array(buffer)
  if (bytes[0] === 0xff && bytes[1] === 0x0a) return bytes
  const view = new DataView(buffer)
  const chunks = []
  let length = 0
  for (let pos = 12; pos + 8 <= bytes.length;) {
    let size = view.getUint32(pos)
    const type = ascii(bytes, pos + 4, 4)
    let header = 8
    if (size === 1) {
      if (pos + 16 > bytes.length) throw new Error('INVALID_IMAGE')
      size = view.getUint32(pos + 8) * 4294967296 + view.getUint32(pos + 12)
      header = 16
    } else if (!size) size = bytes.length - pos
    if (size < header || pos + size > bytes.length) throw new Error('INVALID_IMAGE')
    if (type === 'jxlc' || type === 'jxlp') {
      const chunk = bytes.subarray(pos + header + (type === 'jxlp' ? 4 : 0), pos + size)
      chunks.push(chunk)
      length += chunk.length
    }
    pos += size
  }
  const stream = new Uint8Array(length)
  let offset = 0
  chunks.forEach(chunk => { stream.set(chunk, offset); offset += chunk.length })
  return stream
}

/** Read only JPEG XL's size/basic metadata header, before pixel decoding can discard animation. */
function isAnimatedJxl(buffer) {
  const bytes = jxlCodestream(buffer)
  let position = 16
  const bits = count => {
    if (position + count > bytes.length * 8) throw new Error('INVALID_IMAGE')
    let result = 0
    for (let i = 0; i < count; i++, position++) result += ((bytes[position >> 3] >> (position & 7)) & 1) * (2 ** i)
    return result
  }
  const u32 = choices => { const [count, offset] = choices[bits(2)]; return bits(count) + offset }
  const size = () => {
    const small = bits(1)
    if (small) bits(5)
    else u32([[9, 1], [13, 1], [18, 1], [30, 1]])
    if (!bits(3)) {
      if (small) bits(5)
      else u32([[9, 1], [13, 1], [18, 1], [30, 1]])
    }
  }
  size()
  if (bits(1)) return false // all_default
  if (!bits(1)) return false // extra_fields
  bits(3) // orientation
  if (bits(1)) size() // intrinsic dimensions
  if (bits(1)) {
    const div8 = bits(1)
    const choices = div8 ? [[0, 16], [0, 32], [5, 1], [9, 33]] : [[6, 1], [8, 65], [10, 321], [12, 1345]]
    u32(choices)
    if (!bits(3)) u32(choices)
  }
  return Boolean(bits(1))
}

export function hasUnsupportedAnimation(buffer, format = detectImageFormat(buffer)) {
  const bytes = new Uint8Array(buffer)
  const view = new DataView(buffer)
  if (format === 'jxl') return isAnimatedJxl(buffer)
  if (format === 'avif') {
    const size = view.getUint32(0)
    for (let pos = 8; pos + 4 <= Math.min(size, bytes.length); pos += 4) {
      if (pos !== 12 && ascii(bytes, pos, 4) === 'avis') return true
    }
  }
  if (format === 'png') {
    for (let pos = 8; pos + 12 <= bytes.length;) {
      const length = view.getUint32(pos)
      if (ascii(bytes, pos + 4, 4) === 'acTL') return true
      if (length > bytes.length - pos - 12) break
      pos += 12 + length
    }
  }
  if (format === 'webp') {
    for (let pos = 12; pos + 8 <= bytes.length;) {
      const length = view.getUint32(pos + 4, true)
      const type = ascii(bytes, pos, 4)
      if (type === 'ANIM' || type === 'ANMF' || (type === 'VP8X' && bytes[pos + 8] & 2)) return true
      if (length > bytes.length - pos - 8) break
      pos += 8 + length + (length & 1)
    }
  }
  return false
}

/** EXIF orientation is applied explicitly; pixel decoders are requested to leave it unchanged. */
export function getExifOrientation(buffer) {
  const view = new DataView(buffer)
  if (view.byteLength < 4 || view.getUint16(0) !== 0xffd8) return 1
  for (let pos = 2; pos + 4 <= view.byteLength;) {
    if (view.getUint8(pos) !== 0xff) break
    const marker = view.getUint8(pos + 1)
    if (marker === 0xda || marker === 0xd9) break
    const length = view.getUint16(pos + 2)
    if (length < 2 || pos + length + 2 > view.byteLength) break
    if (marker === 0xe1 && length >= 16 && view.getUint32(pos + 4) === 0x45786966 && view.getUint16(pos + 8) === 0) {
      const base = pos + 10
      const little = view.getUint16(base) === 0x4949
      if ((!little && view.getUint16(base) !== 0x4d4d) || view.getUint16(base + 2, little) !== 42) return 1
      const directory = base + view.getUint32(base + 4, little)
      const limit = pos + length + 2
      if (directory + 2 > limit) return 1
      const count = view.getUint16(directory, little)
      for (let i = 0; i < count; i++) {
        const entry = directory + 2 + 12 * i
        if (entry + 12 > limit) break
        if (view.getUint16(entry, little) === 0x0112 && view.getUint16(entry + 2, little) === 3 && view.getUint32(entry + 4, little) === 1) {
          const orientation = view.getUint16(entry + 8, little)
          return orientation >= 1 && orientation <= 8 ? orientation : 1
        }
      }
    }
    pos += length + 2
  }
  return 1
}
