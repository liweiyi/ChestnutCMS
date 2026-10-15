import { normalizePhoneNumber, normalizePhoneParts, validatePhoneNumber } from '../../../utils/phoneNumber.js'

export function validSmsPhoneNumber(value, phoneCallingCode) {
  return arguments.length < 2 ? validatePhoneNumber(value) : validatePhoneNumber(value, phoneCallingCode)
}

export function validSmsCode(value) {
  return /^\d{6}$/.test(value)
}

// Requests remain associated with their original phone, even if the user edits it.
export function createSmsCodeRequests({ sendCode, onSent, onPendingChange }) {
  const pending = new Map()
  let selectedPhone = normalizePhoneParts('')
  let identity = ''
  let version = 0
  let disposed = false

  const notify = () => { if (!disposed) onPendingChange?.() }

  function selectPhone(value, phoneCallingCode) {
    selectedPhone = arguments.length < 2 ? normalizePhoneParts(value) : normalizePhoneParts(value, phoneCallingCode)
    const normalized = normalizePhoneNumber(selectedPhone.phoneNumber, selectedPhone.phoneCallingCode)
    if (normalized !== identity) {
      identity = normalized
      version++
    }
    notify()
  }

  function isPending() {
    return pending.has(identity)
  }

  async function send() {
    if (disposed || !validSmsPhoneNumber(selectedPhone.phoneNumber, selectedPhone.phoneCallingCode) || isPending()) return null
    const request = { ...selectedPhone, identity, version }
    pending.set(request.identity, request)
    notify()
    try {
      const response = await sendCode({ phoneCallingCode: request.phoneCallingCode, phoneNumber: request.phoneNumber })
      if (disposed) return null
      // Preserve the old phone's cooldown without restoring its form state.
      onSent?.(response.data, request.identity)
      return {
        data: response.data,
        current: request.version === version && request.identity === identity
      }
    } finally {
      if (pending.get(request.identity) === request) pending.delete(request.identity)
      notify()
    }
  }

  function dispose() {
    disposed = true
    version++
    pending.clear()
  }

  return { selectPhone, isPending, send, dispose }
}
