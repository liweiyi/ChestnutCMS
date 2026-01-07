import request from '@/utils/request'

export function getPusherTypes(params) {
  return request({
    url: '/pusher/types',
    method: 'get',
    params: params
  })
}

export function getPusherConfigList(params) {
  return request({
    url: '/pusher/config',
    method: 'get',
    params: params
  })
}

export function getPusherConfig(memberId) {
  return request({
    url: '/pusher/config/' + memberId,
    method: 'get'
  })
}

export function addPusherConfig(data) {
  return request({
    url: '/pusher/config',
    method: 'post',
    data: data
  })
}

export function updatePusherConfig(data) {
  return request({
    url: '/pusher/config',
    method: 'put',
    data: data
  })
}

export function deletePusherConfigs(memberIds) {
  return request({
    url: '/pusher/config/delete',
    method: 'post',
    data: memberIds
  })
}