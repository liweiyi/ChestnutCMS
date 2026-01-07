import request from '@/utils/request'

export function getStatMenuTreeData() {
  return request({
    url: '/stat/menu/tree',
    method: 'get'
  })
}

export function getStatMenuTreeSelectorData() {
  return request({
    url: '/stat/menu/options',
    method: 'get'
  })
}