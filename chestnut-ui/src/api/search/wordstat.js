import request from '@/utils/request'

export function getSearchWordList(params) {
  return request({
    url: '/search/word',
    method: 'get',
    params: params
  })
}

export function addSearchWord(data) {
  return request({
    url: '/search/word',
    method: 'post',
    data: data
  })
}

export function editSearchWord(data) {
  return request({
    url: '/search/word',
    method: 'put',
    data: data
  })
}

export function deleteSearchWords(wordIds) {
  return request({
    url: '/search/word',
    method: 'delete',
    data: wordIds
  })
}

export function setTop(data) {
  return request({
    url: '/search/word/set_top',
    method: 'put',
    data: data
  })
}

export function cancelTop(data) {
  return request({
    url: '/search/word/cancel_top',
    method: 'put',
    data: data
  })
}

export function getTrendDatas(params) {
  return request({
    url: '/search/word/trend',
    method: 'get',
    params: params
  })
}

export function getSiteSearchWordList(params) {
  return request({
    url: '/cms/search/word',
    method: 'get',
    params: params
  })
}

export function addSiteSearchWord(data) {
  return request({
    url: '/cms/search/word',
    method: 'post',
    data: data
  })
}