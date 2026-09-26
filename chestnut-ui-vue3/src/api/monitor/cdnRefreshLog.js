import request from '@/utils/request'

export function getConfigOptions() {
  return request({
    url: '/monitor/cdnRefreshLog/configOptions',
    method: 'get'
  })
}

export function listLogs(params) {
  return request({
    url: '/monitor/cdnRefreshLog/list',
    method: 'get',
    params
  })
}
