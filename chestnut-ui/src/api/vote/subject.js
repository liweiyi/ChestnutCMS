import request from '@/utils/request'

export function getVoteSubjectList(voteId) {
  return request({
    url: '/vote/subject',
    method: 'get',
    params: { voteId: voteId }
  })
}

export function getVoteSubjectDetail(subjectId) {
  return request({
    url: '/vote/subject/' + subjectId,
    method: 'get'
  })
}

export function addVoteSubject(data) {
  return request({
    url: '/vote/subject',
    method: 'post',
    data: data
  })
}

export function updateVoteSubject(data) {
  return request({
    url: '/vote/subject',
    method: 'put',
    data: data
  })
}

export function deleteVoteSubjects(voteIds) {
  return request({
    url: '/vote/subject',
    method: 'delete',
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