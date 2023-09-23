import request from '@/utils/request'

export function listAdSpaces() {
  return request({
    url: '/cms/adspace',
    method: 'get'
  })
}

export function getAdSpace(pageWidgetId) {
  return request({
    url: '/cms/adspace/' + pageWidgetId,
    method: 'get'
  })
}

export function addAdSpace(params) {
  return request({
    url: '/cms/adspace',
    method: 'post',
    data: params
  })
}

export function editAdSpace(params) {
  return request({
    url: '/cms/adspace',
    method: 'put',
    data: params
  })
}

export function deleteAdSpace(params) {
  return request({
    url: '/cms/adspace',
    method: 'delete',
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
    url: '/cms/advertisement',
    method: 'get',
    params: params
  })
}

export function getAdvertisement(advertisementId) {
  return request({
    url: '/cms/advertisement/' + advertisementId,
    method: 'get'
  })
}

export function addAdvertisement(params) {
  return request({
    url: '/cms/advertisement',
    method: 'post',
    data: params
  })
}

export function editAdvertisement(params) {
  return request({
    url: '/cms/advertisement',
    method: 'put',
    data: params
  })
}

export function deleteAdvertisement(params) {
  return request({
    url: '/cms/advertisement',
    method: 'delete',
    data: params
  })
}

export function enableAdvertisement(params) {
  return request({
    url: '/cms/advertisement/enable',
    method: 'put',
    data: params
  })
}

export function disableAdvertisement(params) {
  return request({
    url: '/cms/advertisement/disable',
    method: 'put',
    data: params
  })
}