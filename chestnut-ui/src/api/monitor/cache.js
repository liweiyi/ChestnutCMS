import request from '@/utils/request'

// 查询缓存详细
export function getCache() {
  return request({
    url: '/monitor/cache',
    method: 'get'
  })
}

// 查询缓存名称列表
export function listCacheName() {
  return request({
    url: '/monitor/cache/getNames',
    method: 'get'
  })
}

// 查询缓存键名列表
export function listCacheKey(monitoredId) {
  return request({
    url: '/monitor/cache/getKeys/' + monitoredId,
    method: 'get'
  })
}

// 查询缓存内容
export function getCacheValue(monitoredId, cacheKey) {
  return request({
    url: '/monitor/cache/getValue?monitoredId=' + monitoredId + '&cacheKey=' + encodeURIComponent(cacheKey),
    method: 'get'
  })
}

// 清理指定名称缓存
export function clearCacheName(monitoredId) {
  return request({
    url: '/monitor/cache/clearCacheName/' + monitoredId,
    method: 'delete'
  })
}

// 清理指定键名缓存
export function clearCacheKey(monitoredId, cacheKey) {
  return request({
    url: '/monitor/cache/clearCacheKey',
    method: 'delete',
    data: {
      monitoredId: monitoredId,
      cacheKey: cacheKey
    }
  })
}

// 清理全部缓存
export function clearCacheAll() {
  return request({
    url: '/monitor/cache/clearCacheAll',
    method: 'delete'
  })
}
