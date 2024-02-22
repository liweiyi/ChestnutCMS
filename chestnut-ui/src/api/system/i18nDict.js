import request from '@/utils/request'

export function listLangOptions() {
  return request({
    url: '/system/i18n/dict/langOptions',
    method: 'get'
  })
}

export function listI18nDict(query) {
  return request({
    url: '/system/i18n/dict/list',
    method: 'get',
    params: query
  })
}

export function getI18nDict(dictId) {
  return request({
    url: '/system/i18n/dict/' + dictId,
    method: 'get'
  })
}

export function listI18nDictByKey(langKey) {
  return request({
    url: '/system/i18n/dict/langKey/' + langKey,
    method: 'get'
  })
}

export function addI18nDict(data) {
  return request({
    url: '/system/i18n/dict',
    method: 'post',
    data: data
  })
}

export function updateI18nDict(data) {
  return request({
    url: '/system/i18n/dict',
    method: 'put',
    data: data
  })
}

export function batchSaveI18nDicts(data) {
  return request({
    url: '/system/i18n/dict/batch',
    method: 'put',
    data: data
  })
}

export function delI18nDict(dictIds) {
  return request({
    url: '/system/i18n/dict',
    method: 'delete',
    data: dictIds
  })
}

export function refreshCache() {
  return request({
    url: '/system/i18n/dict/refreshCache',
    method: 'delete'
  })
}
