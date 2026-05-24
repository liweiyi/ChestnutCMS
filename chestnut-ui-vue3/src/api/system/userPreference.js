import request from '@/utils/request'

export function getUserPreference() {
  return request({
    url: '/system/userPreference',
    method: 'get'
  })
}

export function saveConfig(data) {
  return request({
    url: '/system/userPreference',
    method: 'post',
    data: data
  })
}

export function saveSingleConfig(data) {
  return request({
    url: '/system/userPreference/single',
    method: 'post',
    data: data
  })
}