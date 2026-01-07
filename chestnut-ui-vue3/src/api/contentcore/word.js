import request from '@/utils/request'

export function getHotWordGroupTreeData() {
  return request({
    url: '/cms/hotword/group/treedata',
    method: 'get'
  })
}

export function getHotWordGroupOptions() {
  return request({
    url: '/cms/hotword/group/options',
    method: 'get'
  })
}

export function addHotWordGroup(data) {
  return request({
    url: '/cms/hotword/group',
    method: 'post',
    data: data
  })
}

export function getTagWordGroupTreeData() {
  return request({
    url: '/cms/tagword/group/treedata',
    method: 'get'
  })
}

export function addTagWordGroup(data) {
  return request({
    url: '/cms/tagword/group',
    method: 'post',
    data: data
  })
}