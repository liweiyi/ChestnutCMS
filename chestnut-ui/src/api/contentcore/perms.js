import request from '@/utils/request'

export function getSitePermissions(params) {
  return request({
    url: '/cms/perms/site',
    method: 'get',
    params: params
  })
}

export function saveSitePermissions(data) {
  return request({
    url: '/cms/perms/site',
    method: 'put',
    data: data
  })
}

export function getSiteOptions(params) {
  return request({
    url: '/cms/perms/site/options',
    method: 'get',
    params: params
  })
}

export function getCatalogPermissions(params) {
  return request({
    url: '/cms/perms/catalog',
    method: 'get',
    params: params
  })
}

export function saveCatalogPermissions(data) {
  return request({
    url: '/cms/perms/catalog',
    method: 'put',
    data: data
  })
}

export function getPageWidgetPermissions(params) {
  return request({
    url: '/cms/perms/pageWidget',
    method: 'get',
    params: params
  })
}

export function savePageWidgetPermissions(data) {
  return request({
    url: '/cms/perms/pageWidget',
    method: 'put',
    data: data
  })
}