export const ACCOUNT_STATUS = Object.freeze({ ENABLED: '0', DISABLED: '1', LOCKED: '2' })

export const ACCOUNT_STATUS_ACTION = Object.freeze({
  DISABLE: 'disable', REMOVE_DISABLEMENT: 'removeDisablement', LOCK: 'lock', UNLOCK: 'unlock'
})

// Match the LocalDateTime format used by the backend without converting time zones.
export function isValidLockEndTime(value) {
  if (typeof value !== 'string') return false
  const match = /^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})$/.exec(value)
  if (!match) return false
  const [year, month, day, hour, minute, second] = match.slice(1).map(Number)
  const leapYear = year % 4 === 0 && (year % 100 !== 0 || year % 400 === 0)
  const daysInMonth = [31, leapYear ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31]
  return month >= 1 && month <= 12 && day >= 1 && day <= daysInMonth[month - 1]
    && hour <= 23 && minute <= 59 && second <= 59
}

export function withoutAccountStatus(data) {
  const { status, lockEndTime, ...payload } = data
  return payload
}
