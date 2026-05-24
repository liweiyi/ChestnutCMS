import request from '@/utils/request'

export function listDataSource(query) {
  return request({
    url: '/collector/datasource/list',
    method: 'get',
    params: query
  })
}

export function allDataSource() {
  return request({
    url: '/collector/datasource/all',
    method: 'get'
  })
}

export function getDataSource(id) {
  return request({
    url: '/collector/datasource/detail/' + id,
    method: 'get'
  })
}

export function addDataSource(data) {
  return request({
    url: '/collector/datasource/add',
    method: 'post',
    data: data
  })
}

export function updateDataSource(data) {
  return request({
    url: '/collector/datasource/update',
    method: 'post',
    data: data
  })
}

export function deleteDataSource(id) {
  return request({
    url: '/collector/datasource/delete/' + id,
    method: 'post'
  })
}

export function testConnection(data) {
  return request({
    url: '/collector/datasource/testConnection',
    method: 'post',
    data: data
  })
}
