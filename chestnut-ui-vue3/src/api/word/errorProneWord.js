import request from '@/utils/request'

export function getErrorProneWordList(params) {
  return request({
    url: '/word/errorproneword/list',
    method: 'get',
    params: params
  })
}

export function addErrorProneWord(data) {
  return request({
    url: '/word/errorproneword/add',
    method: 'post',
    data: data
  })
}

export function editErrorProneWord(data) {
  return request({
    url: '/word/errorproneword/update',
    method: 'post',
    data: data
  })
}

export function deleteErrorProneWord(data) {
  return request({
    url: '/word/errorproneword/delete',
    method: 'post',
    data: data
  })
}

export function checkFallibleWords(text) {
  return request({
    url: '/word/errorproneword/check',
    method: 'post',
    data: { text: text }
  })
}