import request from '@/utils/request'

export function getMemberConfig() {
  return request({
    url: '/member/config/detail',
    method: 'get'
  })
}

export function getMemberConfigDefinitions() {
  return request({
    url: '/member/config/definitions',
    method: 'get'
  })
}

export function updateMemberConfig(data) {
  return request({
    url: '/member/config/update',
    method: 'post',
    data
  })
}
