import request from '@/utils/request'

export function getLevelTypes() {
  return request({
    url: '/member/levelConfig/types',
    method: 'get'
  })
}

export function getLevelConfigList(params) {
  return request({
    url: '/member/levelConfig/list',
    method: 'get',
    params: params
  })
}

export function getLevelConfigDetail(configId) {
  return request({
    url: '/member/levelConfig/detail/' + configId,
    method: 'get'
  })
}

export function addLevelConfig(data) {
  return request({
    url: '/member/levelConfig/add',
    method: 'post',
    data: data
  })
}

export function updateLevelConfig(data) {
  return request({
    url: '/member/levelConfig/update',
    method: 'post',
    data: data
  })
}

export function deleteLevelConfigs(levelConfigIds) {
  return request({
    url: '/member/levelConfig/delete',
    method: 'post',
    data: levelConfigIds
  })
}