import request from '@/utils/request'

export function getLinkGroupList(params) {
  return request({
    url: '/cms/link_group',
    method: 'get',
    params: params
  })
}

export function addLinkGroup(params) {
  return request({
    url: '/cms/link_group',
    method: 'post',
    data: params
  })
}

export function editLinkGroup(params) {
  return request({
    url: '/cms/link_group',
    method: 'put',
    data: params
  })
}

export function deleteLinkGroup(params) {
  return request({
    url: '/cms/link_group',
    method: 'delete',
    data: params
  })
}