import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getConfigKey } from '@/api/system/config'
import { getCurrentSite, setCurrentSite } from '@/api/contentcore/site'
import cms from '@/plugins/cms'

const useSiteStore = defineStore('site', () => {
  const siteId = ref(null)
  const siteName = ref('')
  const enabled = ref(null)
  const status = ref('idle')
  const ready = computed(() => status.value === 'ready')

  let initialization = null
  let contextVersion = 0

  function clearSite() {
    cms.clearCurrentSite()
    siteId.value = null
    siteName.value = ''
  }

  function applySite(site) {
    // 先更新请求头读取的缓存，再允许页面挂载并发起站点请求。
    cms.setCurrentSite(site.siteId, site.siteName)
    siteId.value = site.siteId
    siteName.value = site.siteName
    status.value = 'ready'
  }

  async function initialize() {
    if (initialization) return initialization
    if (status.value === 'ready' || status.value === 'disabled') return

    const version = contextVersion
    status.value = 'loading'
    const task = (async () => {
      try {
        const config = await getConfigKey('CMSModuleEnable')
        if (version !== contextVersion) return
        enabled.value = config.data === 'true'
        if (!enabled.value) {
          clearSite()
          status.value = 'disabled'
          return
        }

        // 本地站点只作为偏好，由后端校验权限并返回可访问的当前站点。
        const response = await getCurrentSite()
        if (version !== contextVersion) return
        if (response.data?.siteId) {
          applySite(response.data)
        } else {
          clearSite()
          status.value = 'empty'
        }
      } catch {
        if (version !== contextVersion) return
        clearSite()
        // CMS 初始化失败不影响登录态，页面可独立展示错误并重试。
        status.value = 'error'
      }
    })()

    initialization = task
    try {
      await task
    } finally {
      if (initialization === task) initialization = null
    }
  }

  async function changeSite(id) {
    const version = contextVersion
    const response = await setCurrentSite(id)
    if (version !== contextVersion) return false
    if (!response.data?.siteId) throw new Error('Current site is unavailable')
    // 手动选择成功后，忽略尚未完成的初始化结果，避免旧响应覆盖新站点。
    contextVersion++
    initialization = null
    applySite(response.data)
    return true
  }

  function reset() {
    // 忽略退出登录前尚未完成的站点请求，避免覆盖下一个会话。
    contextVersion++
    initialization = null
    // 保留本地站点偏好，下次登录仍须经过后端权限校验后才能使用。
    siteId.value = null
    siteName.value = ''
    enabled.value = null
    status.value = 'idle'
  }

  return { siteId, siteName, enabled, status, ready, initialize, changeSite, reset }
})

export default useSiteStore
