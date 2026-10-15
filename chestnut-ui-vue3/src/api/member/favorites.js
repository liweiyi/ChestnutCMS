import request from '@/utils/request'

export function getFavoriteMembers(params) {
  return request({ url: '/member/favorites/members', method: 'get', params })
}

export function getMemberFavorites(params) {
  return request({ url: '/member/favorites/list', method: 'get', params })
}
