const MAX_LONG = '9223372036854775807'
const MIN_LONG_ABS = '9223372036854775808'
const MAX_INCREASE_SECONDS = '316224000'

// 秒数和会员ID以字符串收发，避免大整数经过浏览器浮点数。
export function normalizeSubscriptionSeconds(value) {
  const negative = value.startsWith('-')
  const digits = value.replace(/^[+-]/, '').replace(/^0+(?=\d)/, '')
  return negative && digits !== '0' ? `-${digits}` : digits
}

function isSignedLong(value) {
  if (typeof value !== 'string' || !/^[+-]?\d+$/.test(value)) return false
  const normalized = normalizeSubscriptionSeconds(value)
  const digits = normalized.replace(/^-/, '')
  const max = normalized.startsWith('-') ? MIN_LONG_ABS : MAX_LONG
  return digits.length < max.length || (digits.length === max.length && digits <= max)
}

export function isSubscriptionSeconds(value) {
  if (!isSignedLong(value)) return false
  const normalized = normalizeSubscriptionSeconds(value)
  return normalized.startsWith('-') || normalized.length < MAX_INCREASE_SECONDS.length
    || (normalized.length === MAX_INCREASE_SECONDS.length && normalized <= MAX_INCREASE_SECONDS)
}

export function isSubscriptionMemberId(value) {
  return typeof value === 'string' && /^\d+$/.test(value) && normalizeSubscriptionSeconds(value) !== '0' && isSignedLong(value)
}

export function isSubscriptionValid(expireTime, now = Date.now()) {
  if (expireTime === null || expireTime === undefined || !/^-?\d+$/.test(String(expireTime))) return false
  // 到期时间为秒，浏览器当前时间为毫秒，统一后比较并保留Long精度。
  return BigInt(expireTime) * 1000n > BigInt(now)
}

export function formatSubscriptionTime(value, unit = 'milliseconds') {
  if (value === null || value === undefined || value === '') return '—'
  const timestamp = /^-?\d+$/.test(String(value)) ? Number(value) * (unit === 'seconds' ? 1000 : 1) : value
  const date = new Date(timestamp)
  return Number.isNaN(date.getTime()) ? String(value) : new Intl.DateTimeFormat(undefined, {
    year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false
  }).format(date)
}

// 只提交新接口声明的字段，旧key、版本及客户端业务来源不会混入写入命令。
export function buildSubscriptionAdjustment(form, requestId) {
  if (!isSubscriptionMemberId(form.memberId) || !form.subscriptionId || !isSubscriptionSeconds(form.deltaSeconds)) {
    throw new TypeError('会员订阅调整参数无效')
  }
  return {
    requestId,
    memberId: normalizeSubscriptionSeconds(form.memberId),
    subscriptionId: String(form.subscriptionId),
    deltaSeconds: normalizeSubscriptionSeconds(form.deltaSeconds),
    reason: form.reason.trim()
  }
}

export function buildSubscriptionRecordQuery(query) {
  return {
    pageNum: query.pageNum,
    pageSize: query.pageSize,
    memberId: query.memberId ? normalizeSubscriptionSeconds(query.memberId) : undefined,
    subscriptionId: query.subscriptionId || undefined,
    valid: ['Y', 'N'].includes(query.valid) ? query.valid : undefined
  }
}

export function buildSubscriptionDefinition(form, isEditing) {
  return {
    ...(isEditing ? { subscriptionId: form.subscriptionId } : {}),
    subscriptionName: form.subscriptionName,
    description: form.description,
    serviceIds: [...form.serviceIds],
    remark: form.remark
  }
}

// 使用随机字节生成稳定请求ID，兼容未启用HTTPS的本地开发界面。
export function createSubscriptionRequestId() {
  return Array.from(crypto.getRandomValues(new Uint8Array(16)), value => value.toString(16).padStart(2, '0')).join('')
}
