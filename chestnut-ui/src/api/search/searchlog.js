import request from '@/utils/request'

// 查询搜索日志列表
export function getSearchLogs(params) {
  return request({
    url: '/search/log',
    method: 'get',
    params: params
  })
}

// 删除搜索日志
export function deleteSearchLogs(searchLogIds) {
  return request({
    url: '/search/log',
    method: 'delete',
    data: searchLogIds
  })
}

export function getSiteSearchLogs(params) {
  return request({
    url: '/cms/search/log',
    method: 'get',
    params: params
  })
}