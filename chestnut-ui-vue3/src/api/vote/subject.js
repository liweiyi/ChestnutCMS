import request from '@/utils/request'

export function getVoteSubjectList(voteId) {
  return request({
    url: '/vote/subject/list',
    method: 'get',
    params: { voteId: voteId }
  })
}

export function getVoteSubjectDetail(subjectId) {
  return request({
    url: '/vote/subject/detail/' + subjectId,
    method: 'get'
  })
}

export function addVoteSubject(data) {
  return request({
    url: '/vote/subject/add',
    method: 'post',
    data: data
  })
}

export function updateVoteSubject(data) {
  return request({
    url: '/vote/subject/update',
    method: 'post',
    data: data
  })
}

export function deleteVoteSubjects(voteIds) {
  return request({
    url: '/vote/subject/delete',
    method: 'post',
    data: voteIds
  })
}

export function getSubjectItems(subjectId) {
  return request({
    url: '/vote/subject/items/' + subjectId,
    method: 'get'
  })
}

export function saveSubjectItems(data) {
  return request({
    url: '/vote/subject/items',
    method: 'post',
    data: data
  })
}