import request from '@/utils/request'

export function getGuaXiang() {
  return request({
    url: '/xyz/bagua/qigua',
    method: 'get'
  })
}