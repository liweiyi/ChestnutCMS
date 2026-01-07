import request from '@/utils/request'

export function getSmsProviderOptions(params) {
  return request({
    url: '/sms/config/options',
    method: 'get',
    params: params
  })
}

export function getConfigList(params) {
  return request({
    url: '/sms/config/list',
    method: 'get',
    params: params
  })
}

export function getConfigDetail(id) {
  return request({
    url: '/sms/config/' + id,
    method: 'get'
  })
}

export function addConfig(data) {
  return request({
    url: '/sms/config/add',
    method: 'post',
    data: data
  })
}

export function updateConfig(data) {
  return request({
    url: '/sms/config/update',
    method: 'post',
    data: data
  })
}

export function deleteConfigs(ids) {
  return request({
    url: '/sms/config/delete',
    method: 'post',
    data: ids
  })
}