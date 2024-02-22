import Vue from 'vue';
import VueI18n from 'vue-i18n';
import Cache from '@/plugins/cache'

// element-ui内置的语言包
import locale from 'element-ui/lib/locale';
import elementZhCNLocale from 'element-ui/lib/locale/lang/zh-CN'
import elementZhTWLocale from 'element-ui/lib/locale/lang/zh-TW'
import elementEnLocale from 'element-ui/lib/locale/lang/en'

// 自定义语言包
import zhCNLocale from './lang/zh-CN'
import zhTWLocale from './lang/zh-TW'
import enLocale from './lang/en'

Vue.use(VueI18n);

const messages = {
  'zh-CN': {
    ...elementZhCNLocale,
    ...zhCNLocale
  },
  'zh-TW': {
    ...elementZhTWLocale,
    ...zhTWLocale
  },
  'en': {
    ...elementEnLocale,
    ...enLocale
  }
}

export const defaultLang = Cache.local.get('lang') || 'zh-CN';

const i18n = new VueI18n({
    locale: defaultLang, // 通过this.$i18n.locale的值实现语言切换
    fallbackLocale: defaultLang,
    messages,
});

locale.i18n((key, value) => i18n.t(key, value)) //兼容elementui

export default i18n;
