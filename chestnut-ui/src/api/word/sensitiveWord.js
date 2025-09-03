import request from '@/utils/request'

export function getSensitiveWordList(params) {
  return request({
    url: '/word/sensitiveword/list',
    method: 'get',
    params: params
  })
}

export function addSensitiveWord(data) {
  return request({
    url: '/word/sensitiveword/add',
    method: 'post',
    data: data
  })
}

export function deleteSensitiveWord(data) {
  return request({
    url: '/word/sensitiveword/delete',
    method: 'post',
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