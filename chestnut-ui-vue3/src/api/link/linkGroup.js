import request from '@/utils/request'

export function getLinkGroupList(params) {
  return request({
    url: '/cms/link_group/list',
    method: 'get',
    params: params
  })
}

export function addLinkGroup(params) {
  return request({
    url: '/cms/link_group/add',
    method: 'post',
    data: params
  })
}

export function editLinkGroup(params) {
  return request({
    url: '/cms/link_group/update',
    method: 'post',
    data: params
  })
}

export function deleteLinkGroup(params) {
  return request({
    url: '/cms/link_group/delete',
    method: 'post',
    data: params
  })
}