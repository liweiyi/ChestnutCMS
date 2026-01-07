import request from '@/utils/request'

export function getCmsConfiguration() {
  return request({
    url: '/cms/dashboard/config',
    method: 'get'
  })
}