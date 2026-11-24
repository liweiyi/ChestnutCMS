import { getImageFormat } from './model.js'

function positiveId(value) {
  if (typeof value === 'number' && !Number.isSafeInteger(value)) return null
  const id = value == null ? '' : String(value).trim()
  return /^[1-9]\d*$/.test(id) ? id : null
}

function internalReference(value) {
  return typeof value === 'string' && value.startsWith('iurl://') ? value : ''
}

function referenceParams(reference) {
  const query = reference.indexOf('?')
  return new URLSearchParams(query < 0 ? '' : reference.slice(query + 1).split('#')[0])
}

function extension(value) {
  return typeof value === 'string' ? value.split(/[?#]/)[0].match(/\.([^./]+)$/)?.[1] || '' : ''
}

export function getOptimizedFilename(filename, outputFormat, copySuffix = '') {
  const original = filename || 'image'
  const dot = original.lastIndexOf('.')
  const baseName = dot > 0 ? original.slice(0, dot) : original
  const originalSuffix = dot > 0 ? original.slice(dot + 1) : ''
  const normalizedSuffix = originalSuffix.toLowerCase()
  const isMatchingFormat = normalizedSuffix === outputFormat || (normalizedSuffix === 'jpg' && outputFormat === 'jpeg')
  const suffix = isMatchingFormat
    ? originalSuffix
    : outputFormat === 'jpeg' ? 'jpg' : outputFormat
  return `${baseName}${copySuffix}.${suffix}`
}

// Business fields store an iurl; the resource API's path is a storage path.
export function createOptimizerResource(value, src, siteId) {
  const resource = value && typeof value === 'object' ? value : { path: value }
  const internalUrl = internalReference(resource.internalUrl) || internalReference(resource.path)
  const params = referenceParams(internalUrl)
  const resourceId = positiveId(params.get('id')) || positiveId(resource.resourceId) || positiveId(resource.id)
  const suffix = resource.suffix || extension(internalUrl) || extension(resource.fileName) || extension(resource.src || src)
  if (!resourceId || !getImageFormat(suffix)) return null
  return {
    ...resource,
    resourceId,
    internalUrl: internalUrl || undefined,
    siteId: positiveId(params.get('sid')) || positiveId(resource.siteId) || positiveId(siteId) || undefined,
    src: resource.src || src || '',
    suffix
  }
}

export function withImageVersion(src, version = Date.now()) {
  if (typeof src !== 'string' || !src) return ''
  const hashAt = src.indexOf('#')
  const hash = hashAt < 0 ? '' : src.slice(hashAt)
  const url = hashAt < 0 ? src : src.slice(0, hashAt)
  const queryAt = url.indexOf('?')
  const path = queryAt < 0 ? url : url.slice(0, queryAt)
  const params = new URLSearchParams(queryAt < 0 ? '' : url.slice(queryAt + 1))
  params.set('_optimizer', String(version))
  return `${path}?${params}${hash}`
}

export function getOriginalImageSource(src) {
  if (typeof src !== 'string' || !src) return ''
  const suffixAt = src.search(/[?#]/)
  const path = suffixAt < 0 ? src : src.slice(0, suffixAt)
  const suffix = suffixAt < 0 ? '' : src.slice(suffixAt)
  return path.replace(/_\d+x\d+(\.[^/.]+)$/i, '$1') + suffix
}

function formatSize(bytes) {
  if (!Number.isFinite(bytes) || bytes < 0) return ''
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(2)} MB`
}

export function normalizeOptimizedResource(resource) {
  const internalUrl = internalReference(resource?.internalUrl) || internalReference(resource?.path)
  if (!internalUrl) return null
  return {
    ...resource,
    path: internalUrl,
    internalUrl,
    src: withImageVersion(resource.src),
    fileSizeName: resource.fileSizeName || formatSize(Number(resource.fileSize))
  }
}
