import request from '@/utils/request'

export function getSensitiveWordList(params) {
  return request({
    url: '/word/sensitiveword',
    method: 'get',
    params: params
  })
}

export function addSensitiveWord(data) {
  return request({
    url: '/word/sensitiveword',
    method: 'post',
    data: data
  })
}

export function editSensitiveWord(data) {
  return request({
    url: '/word/sensitiveword',
    method: 'put',
    data: data
  })
}

export function deleteSensitiveWord(data) {
  return request({
    url: '/word/sensitiveword',
    method: 'delete',
    data: data
  })
}

export function checkSensitiveWords(text) {
  return request({
    url: '/word/sensitiveword/check',
    method: 'post',
    data: { text: text }
  })
}