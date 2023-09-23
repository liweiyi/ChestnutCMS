import request from '@/utils/request'

export function executeGroovySrcity(data) {
  return request({
    url: '/groovy/exec',
    method: 'post',
    data: data
  })
}