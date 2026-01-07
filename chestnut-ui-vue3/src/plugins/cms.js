import cache from '@/plugins/cache'
import CONSTANTS from "@/utils/constants";

export default {
  // 当前站点
  setCurrentSite (siteId) {
    cache.local.set(CONSTANTS.CACHE_CURRENT_SITE, siteId)
  },
  getCurrentSite () {
    return cache.local.get(CONSTANTS.CACHE_CURRENT_SITE)
  },
  clearCurrentSite () {
    cache.local.remove(CONSTANTS.CACHE_CURRENT_SITE)
  },
  currentSiteKey () {
    return CONSTANTS.HEADER_CURRENT_SITE;
  },
  currentSiteHeader () {
    return {
      [this.currentSiteKey()]: this.getCurrentSite()
    }
  },
  // 发布任务标识
  getPublishFlag () {
    return cache.local.get(CONSTANTS.CACHE_PUBLISH_FLAG)
  },
  setPublishFlag (flag) {
    cache.local.set(CONSTANTS.CACHE_PUBLISH_FLAG, flag)
  },
  clearPublishFlag () {
    cache.local.remove(CONSTANTS.CACHE_PUBLISH_FLAG)
  },
  // 最近访问栏目
  getLastSelectedCatalog () {
    return cache.local.get(CONSTANTS.CACHE_LAST_SELECTED_CATALOG)
  },
  setLastSelectedCatalog (catalog) {
    cache.local.set(CONSTANTS.CACHE_LAST_SELECTED_CATALOG, catalog)
  },
  clearLastSelectedCatalog () {
    cache.local.remove(CONSTANTS.CACHE_LAST_SELECTED_CATALOG)
  },
  // 最近访问热词分组
  getLastSelectedHotWordGroup () {
    return cache.local.get(CONSTANTS.CACHE_LAST_SELECTED_HOT_WORD_GROUP)
  },
  setLastSelectedHotWordGroup (hotWordGroup) {
    cache.local.set(CONSTANTS.CACHE_LAST_SELECTED_HOT_WORD_GROUP, hotWordGroup)
  },
  clearLastSelectedHotWordGroup () {
    cache.local.remove(CONSTANTS.CACHE_LAST_SELECTED_HOT_WORD_GROUP)
  },
  // 最近访问TAG词分组
  getLastSelectedTagWordGroup () {
    return cache.local.get(CONSTANTS.CACHE_LAST_SELECTED_TAG_WORD_GROUP)
  },
  setLastSelectedTagWordGroup (tagWordGroup) {
    cache.local.set(CONSTANTS.CACHE_LAST_SELECTED_TAG_WORD_GROUP, tagWordGroup)
  },
  clearLastSelectedTagWordGroup () {
    cache.local.remove(CONSTANTS.CACHE_LAST_SELECTED_TAG_WORD_GROUP)
  },
  // 当前语言
  getLanguage () {
    return cache.local.get(CONSTANTS.CACHE_LANGUAGE)
  },
  setLanguage (language) {
    cache.local.set(CONSTANTS.CACHE_LANGUAGE, language)
  },
  clearLanguage () {
    cache.local.remove(CONSTANTS.CACHE_LANGUAGE)
  }
}