import { MIME_TYPES, detectImageFormat, getExifOrientation, hasGeometryChanges, hasUnsupportedAnimation, validateDimensions } from './model.js'
import { createPreview, cropImage, decodeImage, encodeImage, orientImage, resizeImage } from './codecs.js'

async function decodeOriginal(buffer, format) {
  if (hasUnsupportedAnimation(buffer, format)) throw Object.assign(new Error('UNSUPPORTED_ANIMATION'), { code: 'UNSUPPORTED_ANIMATION' })
  const decoded = await decodeImage(buffer, format)
  return format === 'jpeg' ? orientImage(decoded, getExifOrientation(buffer)) : decoded
}

function sendResult(message) {
  const transfers = [message.buffer, message.preview?.data, message.reference?.data].filter(value => value instanceof ArrayBuffer)
  self.postMessage(message, [...new Set(transfers)])
}

self.onmessage = async ({ data: request }) => {
  const { id, type, buffer, options = {} } = request
  const stage = (value, progress) => self.postMessage({ id, type: 'stage', stage: value, progress })
  const start = performance.now()
  try {
    if (!(buffer instanceof ArrayBuffer) || !buffer.byteLength || !['inspect', 'process'].includes(type)) throw new Error('INVALID_IMAGE')
    const sourceFormat = detectImageFormat(buffer)
    if (!sourceFormat) throw new Error('INVALID_IMAGE')
    const format = type === 'inspect' ? sourceFormat : request.format ?? sourceFormat
    if (!Object.hasOwn(MIME_TYPES, format)) throw new Error('INVALID_IMAGE')
    stage('decode')
    if (sourceFormat === 'gif' && format === 'gif') {
      const gif = await import('./gif.js')
      if (type === 'inspect') {
        const result = await gif.inspectGif(buffer)
        sendResult({ id, type: 'inspected', ...result, format, mime: MIME_TYPES[format] })
      } else {
        const { geometryChanged, ...result } = await gif.processGif(buffer, options, stage)
        const usedOriginal = !geometryChanged && result.buffer.byteLength >= buffer.byteLength
        if (usedOriginal) {
          result.buffer = buffer
          result.preview = result.reference
        }
        sendResult({ id, type: 'processed', ...result, usedOriginal, format, mime: MIME_TYPES[format], elapsed: performance.now() - start })
      }
      return
    }
    const original = await decodeOriginal(buffer, sourceFormat)
    if (type === 'inspect') {
      stage('preview')
      const preview = await createPreview(original)
      sendResult({ id, type: 'inspected', format, mime: MIME_TYPES[format], width: original.width, height: original.height, animated: false, preview })
      return
    }
    stage('crop')
    let image = cropImage(orientImage(original, options.orientation ?? 1), options.crop)
    const width = options.width ?? image.width
    const height = options.height ?? image.height
    validateDimensions(width, height, image.width, image.height)
    stage('resize')
    image = await resizeImage(image, width, height)
    // Compare at the identical crop and output dimensions, before encoding losses.
    const reference = await createPreview(image)
    stage('encode')
    let result = await encodeImage(image, format, options, stage)
    const usedOriginal = format === sourceFormat && !hasGeometryChanges(options, original.width, original.height)
      && result.byteLength >= buffer.byteLength
    if (usedOriginal) result = buffer
    stage('preview')
    const preview = usedOriginal ? reference : await createPreview(await decodeImage(result, format))
    sendResult({ id, type: 'processed', buffer: result, usedOriginal, width, height, format, mime: MIME_TYPES[format], preview, reference, elapsed: performance.now() - start })
  } catch (error) {
    const known = ['UNSUPPORTED_ANIMATION', 'INVALID_IMAGE', 'INVALID_DIMENSIONS']
    const candidate = error.code || error.message
    self.postMessage({ id, type: 'error', code: known.includes(candidate) ? candidate : 'PROCESS_FAILED', message: error.message || 'PROCESS_FAILED' })
  }
}
