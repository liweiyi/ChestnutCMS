import { computed, onBeforeUnmount, ref } from 'vue'

export default function useCaptchaCountdown(loading) {
  const leftSeconds = ref(0)
  let countdownTimer
  const deadlines = new Map()
  let currentToken = ''

  const isRefreshDisabled = computed(() => loading.value || leftSeconds.value > 0)

  const stopCountdown = () => {
    if (countdownTimer) {
      clearInterval(countdownTimer)
      countdownTimer = undefined
    }
  }

  const pauseCountdown = () => {
    stopCountdown()
    currentToken = ''
    leftSeconds.value = 0
  }

  const updateCountdown = () => {
    leftSeconds.value = Math.max(0, Math.ceil(((deadlines.get(currentToken) || 0) - Date.now()) / 1000))
    if (!leftSeconds.value) {
      deadlines.delete(currentToken)
      stopCountdown()
    }
  }

  const selectToken = (token) => {
    pauseCountdown()
    currentToken = token
    updateCountdown()
    if (leftSeconds.value > 0) countdownTimer = setInterval(updateCountdown, 250)
  }

  const startCountdown = (seconds, token = currentToken) => {
    const parsedSeconds = Number(seconds)
    if (Number.isFinite(parsedSeconds) && parsedSeconds > 0) {
      deadlines.set(token, Date.now() + Math.ceil(parsedSeconds) * 1000)
    } else {
      deadlines.delete(token)
    }
    if (currentToken === token) selectToken(token)
  }

  onBeforeUnmount(stopCountdown)

  return {
    leftSeconds,
    isRefreshDisabled,
    startCountdown,
    selectToken,
    pauseCountdown
  }
}
