export const DEFAULT_LANGUAGE = 'zh-CN'
export const SUPPORTED_LANGUAGES = [
  'zh-CN', 'zh-TW', 'en',
]

/** 将浏览器语言及历史保存的区域语言映射到已提供的语言资源。 */
export function normalizeLanguage(language) {
  const tag = String(language || '').replaceAll('_', '-').toLowerCase()
  if (tag === 'zh' || tag.startsWith('zh-')) {
    return /(^|-)hant(-|$)|(^|-)(tw|hk|mo)(-|$)/.test(tag) ? 'zh-TW' : 'zh-CN'
  }
  const primaryLanguage = tag.split('-')[0]
  return SUPPORTED_LANGUAGES.includes(primaryLanguage) ? primaryLanguage : DEFAULT_LANGUAGE
}

/** UEditor 的中文资源使用小写标识，其余语言与系统保持一致。 */
export function getUEditorLanguage(language) {
  const normalized = normalizeLanguage(language)
  return normalized.startsWith('zh-') ? 'zh-cn' : normalized
}