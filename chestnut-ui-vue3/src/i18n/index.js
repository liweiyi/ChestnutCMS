import { createI18n } from 'vue-i18n'
import messages from './lang'
import elementPlusZhCn from 'element-plus/dist/locale/zh-cn.min.mjs'
import elementPlusZhTw from 'element-plus/dist/locale/zh-tw.min.mjs'
import elementPlusEn from 'element-plus/dist/locale/en.min.mjs'
import { DEFAULT_LANGUAGE, normalizeLanguage } from './locales'

let localLanguage = normalizeLanguage(navigator.language)
try {
  const storedLanguage = localStorage.getItem('cc.language')
  if (storedLanguage) localLanguage = normalizeLanguage(storedLanguage)
} catch (error) {
  console.error('Load current language fail:', error)
}

const i18n = createI18n({
  legacy: false,
  locale: localLanguage,
  fallbackLocale: DEFAULT_LANGUAGE,
  globalInjection: true,
  allowComposition: true,
  messages
})

const elementPlusLocales = {
  'zh-CN': elementPlusZhCn,
  'zh-TW': elementPlusZhTw,
  en: elementPlusEn,
}

export { i18n, elementPlusLocales }