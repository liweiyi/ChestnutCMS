import request from '@/utils/request'

// 获取服务信息
export function getServer() {
  return request({
    url: '/monitor/server',
    method: 'get'
  })
}

export function getDashboardServerInfo() {
  return request({
    url: '/monitor/server/dashboard',
    method: 'get'
  })
}