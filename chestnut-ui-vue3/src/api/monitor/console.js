import request from '@/utils/request'

export function getConsoleLogs(sinceIndex) {
  return request({
    url: '/monitor/console?sinceIndex=' + sinceIndex,
    method: 'get'
  })
}