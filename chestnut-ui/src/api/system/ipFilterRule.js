import request from '@/utils/request'

export function listRule(query) {
  return request({
    url: '/sys/iprule',
    method: 'get',
    params: query
  })
}

export function getRule(ruleId) {
  return request({
    url: '/sys/iprule/' + ruleId,
    method: 'get'
  })
}

export function addRule(data) {
  return request({
    url: '/sys/iprule',
    method: 'post',
    data: data
  })
}

export function editRule(data) {
  return request({
    url: '/sys/iprule',
    method: 'put',
    data: data
  })
}

export function delRule(ids) {
  return request({
    url: '/sys/iprule',
    method: 'delete',
    data: ids
  })
}