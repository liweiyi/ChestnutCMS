import request from '@/utils/request'

export function getLevelTypes() {
  return request({
    url: '/member/levelConfig/types',
    method: 'get'
  })
}

export function getLevelConfigList(params) {
  return request({
    url: '/member/levelConfig',
    method: 'get',
    params: params
  })
}

export function getLevelConfigDetail(configId) {
  return request({
    url: '/member/levelConfig/' + configId,
    method: 'get'
  })
}

export function addLevelConfig(data) {
  return request({
    url: '/member/levelConfig',
    method: 'post',
    data: data
  })
}

export function updateLevelConfig(data) {
  return request({
    url: '/member/levelConfig',
    method: 'put',
    data: data
  })
}

export function deleteLevelConfigs(levelConfigIds) {
  return request({
    url: '/member/levelConfig',
    method: 'delete',
    data: levelConfigIds
  })
}