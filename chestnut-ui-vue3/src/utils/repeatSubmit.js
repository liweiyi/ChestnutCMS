const REPEAT_INTERVAL = 1000
const MAX_RECENT_REQUESTS = 100
const MAX_COMPARABLE_LENGTH = 100 * 1024
const MAX_STORED_FINGERPRINT_LENGTH = 1024 * 1024

function isPlainObject(value) {
  const prototype = Object.getPrototypeOf(value)
  return prototype === Object.prototype || prototype === null
}

function isJsonComparable(value, budget, ancestors = new Set()) {
  if (value === null) {
    budget.remaining -= 4
    return budget.remaining > 0
  }
  if (typeof value === 'string' || typeof value === 'boolean') {
    budget.remaining -= String(value).length
    return budget.remaining > 0
  }
  if (typeof value === 'number') {
    if (!Number.isFinite(value)) return false
    budget.remaining -= String(value).length
    return budget.remaining > 0
  }
  if (typeof value !== 'object') return false
  const isArray = Array.isArray(value)
  if (!isArray && !isPlainObject(value)) return false
  if (ancestors.has(value)) return false

  // 先计算正文长度下界；超限后不再遍历，也不创建大型 JSON 字符串。
  if (isArray) budget.remaining -= value.length
  if (budget.remaining <= 0) return false

  ancestors.add(value)
  let comparable = true
  for (const key in value) {
    if (!Object.hasOwn(value, key)) continue
    if (!isArray) budget.remaining -= key.length
    if (budget.remaining <= 0 || !isJsonComparable(value[key], budget, ancestors)) {
      comparable = false
      break
    }
  }
  ancestors.delete(value)
  return comparable
}

function comparableBody(data) {
  if (data === undefined) return 'undefined'
  if (data === null) return 'null'
  if (typeof data === 'string') {
    return data.length >= MAX_COMPARABLE_LENGTH ? null : `string:${data}`
  }
  if (typeof data === 'boolean') return `boolean:${data}`
  if (typeof data === 'number') return Number.isFinite(data) ? `number:${data}` : null
  if (typeof URLSearchParams !== 'undefined' && data instanceof URLSearchParams) {
    let remaining = MAX_COMPARABLE_LENGTH
    for (const [key, value] of data) {
      remaining -= key.length + value.length
      if (remaining <= 0) return null
    }
    return `params:${data.toString()}`
  }

  try {
    // FormData、Blob、流和含文件的对象无法通过 JSON.stringify 比较真实请求内容。
    if (!isJsonComparable(data, { remaining: MAX_COMPARABLE_LENGTH })) return null
    return `json:${JSON.stringify(data)}`
  } catch {
    return null
  }
}

function requestFingerprint(config, uri) {
  if (uri.length >= MAX_COMPARABLE_LENGTH) return null
  const body = comparableBody(config.data)
  // 大正文及无法可靠序列化的内容交由服务端处理幂等性。
  if (body === null || body.length >= MAX_COMPARABLE_LENGTH) return null
  const fingerprint = JSON.stringify([config.method.toLowerCase(), uri, body])
  return fingerprint.length < MAX_COMPARABLE_LENGTH ? fingerprint : null
}

export function createRepeatSubmitGuard(now = Date.now) {
  const recentRequests = new Map()
  const requestRecords = new WeakMap()
  let storedFingerprintLength = 0

  function removeRecord(fingerprint) {
    const record = recentRequests.get(fingerprint)
    if (!record) return
    recentRequests.delete(fingerprint)
    storedFingerprintLength -= fingerprint.length
    // WeakMap 中仍可能有尚未完成的请求，清掉它对已淘汰大字符串的引用。
    record.fingerprint = null
  }

  return {
    isRepeated(config, uri) {
      const fingerprint = requestFingerprint(config, uri)
      if (fingerprint === null) return false

      const time = now()
      const previous = recentRequests.get(fingerprint)
      if (previous && time - previous.time < REPEAT_INTERVAL) return true

      if (previous) removeRecord(fingerprint)
      // 同时限制记录数和指纹总长度，避免大量接近 100 KB 的表单常驻内存。
      while (recentRequests.size >= MAX_RECENT_REQUESTS ||
             storedFingerprintLength + fingerprint.length > MAX_STORED_FINGERPRINT_LENGTH) {
        removeRecord(recentRequests.keys().next().value)
      }
      const record = { fingerprint, time }
      recentRequests.set(fingerprint, record)
      storedFingerprintLength += fingerprint.length
      requestRecords.set(config, record)
      return false
    },
    clear(config) {
      const record = config && requestRecords.get(config)
      if (!record) return
      // 旧请求完成时不能删除同一指纹下较晚发出的请求。
      if (record.fingerprint && recentRequests.get(record.fingerprint) === record) removeRecord(record.fingerprint)
      requestRecords.delete(config)
    }
  }
}
