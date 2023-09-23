import request from '@/utils/request'

export function getMemberList(params) {
  return request({
    url: '/member',
    method: 'get',
    params: params
  })
}

export function getMemberDetail(memberId) {
  return request({
    url: '/member/' + memberId,
    method: 'get'
  })
}

export function addMember(data) {
  return request({
    url: '/member',
    method: 'post',
    data: data
  })
}

export function updateMember(data) {
  return request({
    url: '/member',
    method: 'put',
    data: data
  })
}

export function deleteMembers(memberIds) {
  return request({
    url: '/member',
    method: 'delete',
    data: memberIds
  })
}

export function resetMemberPassword(memberId, password) {
  const data = {
    "memberId": memberId,
    "password": password
  }
  return request({
    url: '/member/resetPassword',
    method: 'put',
    data: data
  })
}