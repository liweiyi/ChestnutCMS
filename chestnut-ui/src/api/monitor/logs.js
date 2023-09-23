import request from '@/utils/request'

// 查询日志菜单列表
export function getLogMenus() {
  return request({
    url: '/monitor/logs',
    method: 'get'
  })
}