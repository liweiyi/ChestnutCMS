import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { getCaptcha, checkCaptcha } from '@/api/system/captcha'
import useCaptchaCountdown from './useCaptchaCountdown'

function loadImage(src) {
  return new Promise((resolve, reject) => {
    const image = new Image()
    image.onload = () => resolve({ src, width: image.naturalWidth })
    image.onerror = () => reject(new Error())
    image.src = src
  })
}

// A late response can update its token's cooldown, but cannot restore an old challenge.
export default function useCaptcha({ props, model, emit, type, popup = false, imageSources, t }) {
  const open = ref(false)
  const loading = ref(false)
  const checking = ref(false)
  const preparing = ref(false)
  const ready = ref(false)
  const success = ref(false)
  const captcha = ref(null)
  const images = ref([])
  const errorMessage = ref('')
  const busy = computed(() => loading.value || checking.value || preparing.value)
  const { leftSeconds, isRefreshDisabled, startCountdown, selectToken, pauseCountdown } = useCaptchaCountdown(busy)
  const pendingRequests = new Map()
  let generation = 0
  let disposed = false
  let dialogOpened = false
  let pendingReload = false
  let expiresAt = 0
  let expiryTimer
  let resultTimer

  const active = () => !disposed && (!popup || (open.value && dialogOpened))
  const current = (id, token) => active() && id === generation && token === props.token

  function clearTimers() {
    clearTimeout(expiryTimer)
    clearTimeout(resultTimer)
    expiresAt = 0
  }

  function clearChallenge() {
    generation++
    pendingReload = false
    clearTimers()
    loading.value = checking.value = preparing.value = false
    ready.value = success.value = false
    captcha.value = null
    images.value = []
    errorMessage.value = ''
    model.value = null
  }

  function resetCaptcha() {
    clearChallenge()
    pauseCountdown()
    if (popup) {
      dialogOpened = false
      open.value = false
    }
  }

  function startExpiry() {
    clearTimeout(expiryTimer)
    const seconds = Number(props.expires)
    expiresAt = seconds > 0 ? Date.now() + seconds * 1000 : 0
    if (expiresAt) {
      expiryTimer = setTimeout(() => {
        clearChallenge()
        errorMessage.value = t('Login.CaptchaExpired')
      }, seconds * 1000)
    }
  }

  function isValid() {
    return !busy.value && (!expiresAt || Date.now() < expiresAt) &&
      model.value?.token === props.token && (popup ? success.value : ready.value) &&
      (popup || Boolean(model.value?.data?.trim()))
  }

  async function reloadCaptcha() {
    if (!active() || !props.token || busy.value) return
    clearChallenge()
    const token = props.token
    const id = generation
    loading.value = true
    // GET overwrites and CHECK consumes the server-side challenge. Finish any
    // previous operation for this token before requesting a replacement.
    const previous = pendingRequests.get(token)
    if (previous) await previous.catch(() => {})
    if (!current(id, token)) return
    selectToken(token)
    if (leftSeconds.value > 0) {
      pendingReload = true
      loading.value = false
      return
    }
    const request = getCaptcha({ token }).then(res => {
      startCountdown(res.data.duration, token)
      return res.data.captcha
    })
    pendingRequests.set(token, request)
    try {
      const data = await request
      if (!current(id, token)) return
      const loadedImages = await Promise.all(imageSources(data).map(loadImage))
      if (!current(id, token)) return
      captcha.value = data
      images.value = loadedImages
      ready.value = true
      startExpiry()
    } catch (error) {
      if (current(id, token)) errorMessage.value = error?.message || t('Login.CaptchaLoadFailed')
    } finally {
      if (pendingRequests.get(token) === request) pendingRequests.delete(token)
      if (current(id, token)) loading.value = false
    }
  }

  function handleReloadCaptcha() {
    if (isRefreshDisabled.value) return
    if (popup && !props.token) {
      preparing.value = true
      return onOpened()
    }
    return reloadCaptcha()
  }

  function handleVerify() {
    if (success.value && isValid()) return
    preparing.value = true
    open.value = true
  }

  async function onOpened() {
    dialogOpened = true
    const id = generation
    try {
      const token = props.prepareToken ? await props.prepareToken() : props.token
      await nextTick()
      if (!active() || id !== generation) return
      if (!token || token !== props.token) throw new Error(t('Login.AccountRuleTip'))
      preparing.value = false
      await reloadCaptcha()
    } catch (error) {
      if (active() && id === generation) {
        preparing.value = false
        errorMessage.value = error?.message || t('Login.CaptchaLoadFailed')
      }
    }
  }

  function onClose() {
    generation++
    dialogOpened = false
    pendingReload = false
    clearTimeout(resultTimer)
    loading.value = checking.value = preparing.value = false
    ready.value = false
    captcha.value = null
    images.value = []
    errorMessage.value = ''
    pauseCountdown()
    if (!success.value) {
      clearTimers()
      model.value = null
    }
  }

  async function verify(data, payload) {
    if (!active() || !ready.value || busy.value || success.value) return
    const token = props.token
    const id = generation
    checking.value = true
    const request = checkCaptcha(type, { type, token, data: JSON.stringify(data) })
    pendingRequests.set(token, request)
    try {
      const res = await request
      if (!current(id, token)) return
      ready.value = false
      success.value = res.data.success === true
      if (success.value) {
        model.value = { ...payload, type, token }
        startExpiry()
        resultTimer = setTimeout(() => {
          if (current(id, token)) open.value = false
        }, 1000)
      } else {
        model.value = null
        errorMessage.value = t('Login.CaptchaFail')
        resultTimer = setTimeout(() => {
          if (current(id, token)) reloadCaptcha()
        }, 1000)
      }
    } catch (error) {
      if (current(id, token)) {
        ready.value = false
        model.value = null
        errorMessage.value = error?.message || t('Login.CaptchaFail')
      }
    } finally {
      if (pendingRequests.get(token) === request) pendingRequests.delete(token)
      if (current(id, token)) checking.value = false
    }
  }

  watch(leftSeconds, seconds => {
    if (seconds === 0 && pendingReload && active()) reloadCaptcha()
  })
  watch(() => props.token, (token, previous) => {
    if (popup) {
      // Empty -> prepared token is expected while the dialog awaits prepareToken.
      if (previous) resetCaptcha()
    } else {
      resetCaptcha()
      if (token) reloadCaptcha()
    }
  }, { immediate: true, flush: 'sync' })
  watch(busy, value => emit('loading-change', value), { immediate: true, flush: 'sync' })
  watch(open, value => { if (!value) onClose() }, { flush: 'sync' })
  onBeforeUnmount(() => {
    disposed = true
    resetCaptcha()
  })

  return {
    open, loading: busy, ready, success, captcha, images, errorMessage,
    leftSeconds, isRefreshDisabled, reloadCaptcha, handleReloadCaptcha,
    resetCaptcha, handleVerify, onOpened, verify, isValid
  }
}
