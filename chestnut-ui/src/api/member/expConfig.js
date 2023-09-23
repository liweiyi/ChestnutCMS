import request from '@/utils/request'

export function getExpOperations(params) {
  return request({
    url: '/member/expConfig/types',
    method: 'get',
    params: params
  })
}

export function getExpConfigList(params) {
  return request({
    url: '/member/expConfig',
    method: 'get',
    params: params
  })
}

export function getExpConfigDetail(expOperationId) {
  return request({
    url: '/member/expConfig/' + expOperationId,
    method: 'get'
  })
}

export function addExpConfig(data) {
  return request({
    url: '/member/expConfig',
    method: 'post',
    data: data
  })
}

export function updateExpConfig(data) {
  return request({
    url: '/member/expConfig',
    method: 'put',
    data: data
  })
}

export function deleteExpConfigs(expOperationIds) {
  return request({
    url: '/member/expConfig',
    method: 'delete',
    data: expOperationIds
  })
}