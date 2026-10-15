import request from '@/utils/request'

export function getMemberFollowList(params) {
  return request({
    url: '/member/follow/list',
    method: 'get',
    params
  })
}

export function cancelMemberFollow(data) {
  return request({
    url: '/member/follow/cancel',
    method: 'post',
    data
  })
}
