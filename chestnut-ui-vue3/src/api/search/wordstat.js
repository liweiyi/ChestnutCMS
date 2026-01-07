import request from '@/utils/request'

export function getSearchWordList(params) {
  return request({
    url: '/search/word/list',
    method: 'get',
    params: params
  })
}

export function addSearchWord(data) {
  return request({
    url: '/search/word/add',
    method: 'post',
    data: data
  })
}

export function editSearchWord(data) {
  return request({
    url: '/search/word/update',
    method: 'post',
    data: data
  })
}

export function deleteSearchWords(wordIds) {
  return request({
    url: '/search/word/delete',
    method: 'post',
    data: wordIds
  })
}

export function setTop(data) {
  return request({
    url: '/search/word/set_top',
    method: 'post',
    data: data
  })
}

export function cancelTop(data) {
  return request({
    url: '/search/word/cancel_top',
    method: 'post',
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
    url: '/cms/search/word/list',
    method: 'get',
    params: params
  })
}

export function addSiteSearchWord(data) {
  return request({
    url: '/cms/search/word/add',
    method: 'post',
    data: data
  })
}