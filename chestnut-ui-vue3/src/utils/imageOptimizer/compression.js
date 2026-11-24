import { getDefaultOptions } from './model.js'

export const QUALITY_PRESETS = Object.freeze([
  { value: 'veryLow', label: 'QualityVeryLow' },
  { value: 'low', label: 'QualityLow' },
  { value: 'medium', label: 'QualityMedium' },
  { value: 'high', label: 'QualityHigh' },
  { value: 'veryHigh', label: 'QualityVeryHigh' },
  { value: 'lossless', label: 'QualityLossless' }
])

export const EFFORT_PRESETS = Object.freeze([
  { value: 'faster', label: 'EffortFaster' },
  { value: 'fast', label: 'EffortFast' },
  { value: 'balanced', label: 'EffortBalanced' },
  { value: 'strong', label: 'EffortStrong' },
  { value: 'extreme', label: 'EffortExtreme' }
])

const QUALITY_VALUES = { veryLow: 20, low: 50, medium: 82, high: 90, veryHigh: 100 }
const PALETTE_COLORS = { veryLow: 32, low: 64, medium: 128, high: 192, veryHigh: 256 }
const EFFORT_VALUES = {
  png: [0, 1, 2, 4, 6],
  webp: [0, 2, 4, 5, 6],
  jxl: [1, 3, 7, 8, 9],
  avif: [10, 8, 6, 3, 0]
}

export function getDefaultCompression() {
  return { qualityPreset: 'medium', effort: 'balanced' }
}

export function supportsEffort(format) {
  return Object.hasOwn(EFFORT_VALUES, format)
}

function invalidOptions() {
  return Object.assign(new RangeError('INVALID_COMPRESSION_OPTIONS'), { code: 'INVALID_COMPRESSION_OPTIONS' })
}

/** Quality controls visual loss; effort controls encoder work independently. */
export function resolveCompressionOptions(format, supplied = {}) {
  const options = { ...getDefaultOptions(format), ...supplied }
  // Keep the original numeric/raw strategy API available to callers and fixtures.
  if (supplied.qualityPreset === undefined) return options
  const preset = supplied.qualityPreset
  const lossless = preset === 'lossless'
  if (!Object.hasOwn(QUALITY_VALUES, preset) && !lossless) throw invalidOptions()
  if (!['jpeg', 'png', 'webp', 'jxl', 'avif', 'gif'].includes(format)
    || (lossless && !['png', 'webp', 'jxl', 'avif'].includes(format))) throw invalidOptions()
  const effort = supplied.effort ?? 'balanced'
  const index = EFFORT_PRESETS.findIndex(item => item.value === effort)
  if (index < 0) throw invalidOptions()
  options.quality = lossless ? 100 : QUALITY_VALUES[preset]
  if (format === 'png' || format === 'gif') {
    options.strategy = lossless ? 'lossless' : 'palette'
    if (!lossless) {
      options.colors = PALETTE_COLORS[preset]
      options.dither = false
    }
  } else options.strategy = format === 'jpeg' ? 'progressive' : lossless ? 'lossless' : 'lossy'
  if (format === 'png') {
    // jSquash's OxiPNG ABI exposes optimization level, but no Zopfli switch.
    options.pngLevel = EFFORT_VALUES.png[index]
  }
  if (format === 'webp') options.webpMethod = EFFORT_VALUES.webp[index]
  if (format === 'jxl') options.jxlEffort = EFFORT_VALUES.jxl[index]
  if (format === 'avif') options.avifSpeed = EFFORT_VALUES.avif[index]
  return options
}
