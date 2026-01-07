import request from '@/utils/request'

export function getMemberList(params) {
  return request({
    url: '/member/list',
    method: 'get',
    params: params
  })
}

export function getMemberDetail(memberId) {
  return request({
    url: '/member/detail/' + memberId,
    method: 'get'
  })
}

export function addMember(data) {
  return request({
    url: '/member/add',
    method: 'post',
    data: data
  })
}

export function updateMember(data) {
  return request({
    url: '/member/update',
    method: 'post',
    data: data
  })
}

export function deleteMembers(memberIds) {
  return request({
    url: '/member/delete',
    method: 'post',
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
    method: 'post',
    data: data
  })
}