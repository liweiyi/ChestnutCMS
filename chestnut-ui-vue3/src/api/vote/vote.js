import request from '@/utils/request'

export function getVoteUserTypes() {
  return request({
    url: '/vote/userTypes',
    method: 'get'
  })
}

export function getVoteItemTypes() {
  return request({
    url: '/vote/item/types',
    method: 'get'
  })
}

export function getVoteList(params) {
  return request({
    url: '/vote/list',
    method: 'get',
    params: params
  })
}

export function getVoteDetail(voteId) {
  return request({
    url: '/vote/detail/' + voteId,
    method: 'get'
  })
}

export function addVote(data) {
  return request({
    url: '/vote/add',
    method: 'post',
    data: data
  })
}

export function updateVote(data) {
  return request({
    url: '/vote/update',
    method: 'post',
    data: data
  })
}

export function deleteVotes(voteIds) {
  return request({
    url: '/vote/delete',
    method: 'post',
    data: voteIds
  })
}

export function getVoteSubjects(voteId) {
  return request({
    url: '/vote/subject/' + voteId,
    method: 'get'
  })
}

export function saveVoteSubjects(data) {
  return request({
    url: '/vote/subject',
    method: 'post',
    data: data
  })
}