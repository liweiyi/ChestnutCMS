import request from '@/utils/request'

export function getContentDynamicData(params) {
  return request({
    url: '/cms/stat/contentDynamicData',
    method: 'get',
    params: params
  })
}

export function getContentStatByCatalog(params) {
  return request({
    url: '/cms/stat/contentStatByCatalog',
    method: 'get',
    params: params
  })
}

export function getContentStatByUser(params) {
  return request({
    url: '/cms/stat/contentStatByUser',
    method: 'get',
    params: params
  })
}