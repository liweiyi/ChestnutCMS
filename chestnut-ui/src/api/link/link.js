import request from '@/utils/request'

export function getLinkList(params) {
  return request({
    url: '/cms/link',
    method: 'get',
    params: params
  })
}

export function addLink(params) {
  return request({
    url: '/cms/link',
    method: 'post',
    data: params
  })
}

export function editLink(params) {
  return request({
    url: '/cms/link',
    method: 'put',
    data: params
  })
}

export function deleteLink(params) {
  return request({
    url: '/cms/link',
    method: 'delete',
    data: params
  })
}