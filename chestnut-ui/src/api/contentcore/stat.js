import request from '@/utils/request'

export function getSiteStatData() {
  return request({
    url: '/cms/dashboard/stat',
    method: 'get'
  })
}
