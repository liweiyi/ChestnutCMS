import request from '@/utils/request'

export function getMemberLikeLogs(params) {
  return request({ url: '/member/interaction/likes', method: 'get', params })
}

export function getMemberSignInLogs(params) {
  return request({ url: '/member/interaction/signIns', method: 'get', params })
}
