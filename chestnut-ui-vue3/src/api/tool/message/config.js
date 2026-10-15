import request from '@/utils/request'

export function getTypeOptions() {
  return request({
    url: '/message/config/types',
    method: 'get'
  })
}

export function getSupportTemplateTypeOptions() {
  return getTypeOptions().then(response => ({
    ...response,
    data: response.data.filter(item => item.supportedTemplate === true)
  }))
}

export function getConfigList(query) {
  return request({
    url: '/message/config/list',
    method: 'get',
    params: query
  })
}

export function getConfigDetail(id) {
  return request({
    url: `/message/config/${id}`,
    method: 'get'
  })
}

export function createConfig(data) {
  return request({
    url: '/message/config/add',
    method: 'post',
    data: data
  })
}

export function updateConfig(data) {
  return request({
    url: '/message/config/update',
    method: 'post',
    data: data
  })
}

export function deleteConfigs(ids) {
  return request({
    url: '/message/config/delete',
    method: 'post',
    data: ids
  })
}

export function testConfig(data) {
  return request({
    url: '/message/config/test',
    method: 'post',
    data: data
  })
}
