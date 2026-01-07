import request from '@/utils/request'

export function listAdSpaces() {
  return request({
    url: '/cms/adspace/list',
    method: 'get'
  })
}

export function getAdSpace(pageWidgetId) {
  return request({
    url: '/cms/adspace/detail/' + pageWidgetId,
    method: 'get'
  })
}

export function addAdSpace(params) {
  return request({
    url: '/cms/adspace/add',
    method: 'post',
    data: params
  })
}

export function editAdSpace(params) {
  return request({
    url: '/cms/adspace/update',
    method: 'post',
    data: params
  })
}

export function deleteAdSpace(params) {
  return request({
    url: '/cms/adspace/delete',
    method: 'post',
    data: params
  })
}

export function publishAdSpace(params) {
  return request({
    url: '/cms/adspace/publish',
    method: 'post',
    data: params
  })
}

export function listAdvertisementTypes() {
  return request({
    url: '/cms/advertisement/types',
    method: 'get'
  })
}

export function listAdvertisements(params) {
  return request({
    url: '/cms/advertisement/list',
    method: 'get',
    params: params
  })
}

export function getAdvertisement(advertisementId) {
  return request({
    url: '/cms/advertisement/detail/' + advertisementId,
    method: 'get'
  })
}

export function addAdvertisement(params) {
  return request({
    url: '/cms/advertisement/add',
    method: 'post',
    data: params
  })
}

export function editAdvertisement(params) {
  return request({
    url: '/cms/advertisement/update',
    method: 'post',
    data: params
  })
}

export function deleteAdvertisement(params) {
  return request({
    url: '/cms/advertisement/delete',
    method: 'post',
    data: params
  })
}

export function enableAdvertisement(params) {
  return request({
    url: '/cms/advertisement/enable',
    method: 'post',
    data: params
  })
}

export function disableAdvertisement(params) {
  return request({
    url: '/cms/advertisement/disable',
    method: 'post',
    data: params
  })
}