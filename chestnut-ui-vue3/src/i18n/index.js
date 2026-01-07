import { createI18n } from "vue-i18n";
import messages from "./lang";
import elementPlusZhCn from 'element-plus/dist/locale/zh-cn.min.mjs'
import elementPlusEn from 'element-plus/dist/locale/en.min.mjs'

const DEFAULT_LANG = "zh-CN";

let localLanguage = DEFAULT_LANG;
try {
  const currentLang = localStorage.getItem('cc.language')
  if (currentLang)  {
    localLanguage = currentLang;
  } else {
    // 获取浏览器的语言设置
    const navLang = navigator.language;
    localLanguage = navLang;
  }
} catch (e) {
  console.error("Load current language fail: ", e);
}
console.log("currentLang", localLanguage)
const i18n = createI18n({
  legacy: false, // 启用VUE3组合式API模式（必须设置）
  locale: localLanguage,
  fallbackLocale: DEFAULT_LANG,
  globalInjection: true, // 全局注册 $t 方法以便在模板中使用
  allowComposition: true, // 允许组合式 API 的使用
  messages
});
const elementPlusLocales = {
  'zh-CN': elementPlusZhCn,
  'en': elementPlusEn
};
export { i18n, elementPlusLocales };