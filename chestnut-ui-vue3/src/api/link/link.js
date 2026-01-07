import request from '@/utils/request'

export function getLinkList(params) {
  return request({
    url: '/cms/link/list',
    method: 'get',
    params: params
  })
}

export function addLink(params) {
  return request({
    url: '/cms/link/add',
    method: 'post',
    data: params
  })
}

export function editLink(params) {
  return request({
    url: '/cms/link/update',
    method: 'post',
    data: params
  })
}

export function deleteLink(params) {
  return request({
    url: '/cms/link/delete',
    method: 'post',
    data: params
  })
}