import request from '@/utils/request'

export function getCommentList(params) {
  return request({
    url: '/comment/list',
    method: 'get',
    params: params
  })
}

export function getCommentReplyList(commentId, params) {
  return request({
    url: '/comment/reply/' + commentId,
    method: 'get',
    params: params
  })
}

export function getCommentLikeList(commentId, params) {
  return request({
    url: '/comment/like/' + commentId,
    method: 'get',
    params: params
  })
}

export function deleteComments(commentIds) {
  return request({
    url: '/comment/delete',
    method: 'post',
    data: commentIds
  })
}

export function recoverComments(commentIds) {
  return request({
    url: '/comment/recover',
    method: 'post',
    data: commentIds
  })
}

export function auditComment(data) {
  return request({
    url: '/comment/audit',
    method: 'post',
    data: data
  })
}