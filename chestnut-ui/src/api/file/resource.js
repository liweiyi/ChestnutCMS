import request from '@/utils/request'

export function getResrouceList(query) {
  return request({
    url: '/ccfile/resource/list',
    method: 'get',
    params: query
  })
}

export function getResourceDetail(resourceId) {
  return request({
    url: '/ccfile/resource/info/' + resourceId,
    method: 'get'
  })
}

export function addResource(data) {
  return request({
    url: '/ccfile/resource',
    method: 'post',
    data: data,
    headers: {
      "Content-type": "multipart/form-data"
    }
  })
}

// 修改资源
export function updateResource(data) {
  return request({
    url: '/ccfile/resource',
    method: 'put',
    data: data
  })
}

// 删除资源
export function delResource(resourceIds) {
  return request({
    url: '/ccfile/resource/' + resourceIds,
    method: 'delete'
  })
}
