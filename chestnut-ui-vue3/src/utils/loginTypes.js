export const LOGIN_TYPES = Object.freeze({
  PASSWORD: 'UsernamePassword',
  SMS: 'SMS'
})

export function isSmsLoginEnabled(config) {
  const sms = config?.loginTypes?.[LOGIN_TYPES.SMS]
  return sms?.enabled === true
}

export function resolveLoginMode(mode, config) {
  return mode === 'sms' && isSmsLoginEnabled(config) ? 'sms' : 'password'
}
