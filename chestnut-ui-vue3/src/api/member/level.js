import request from '@/utils/request'

export function getLevelMembers(params) {
  return request({ url: '/member/level/members', method: 'get', params })
}

export function getMemberLevels(memberId) {
  return request({ url: '/member/level/list', method: 'get', params: { memberId } })
}

export function getMemberLevelLogs(params) {
  return request({ url: '/member/level/logs', method: 'get', params })
}

export function getMemberLevelOperations(params) {
  return request({ url: '/member/level/operations', method: 'get', params })
}
