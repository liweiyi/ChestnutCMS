import request from '@/utils/request'

export function getTagList() {
  return request({
    url: '/cms/staticize/tags',
    method: 'get'
  })
}
export function getFunctionList() {
  return request({
    url: '/cms/staticize/functions',
    method: 'get'
  })
}
