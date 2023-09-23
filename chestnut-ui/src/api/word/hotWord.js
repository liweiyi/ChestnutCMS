import request from '@/utils/request'

export function getHotWordList(params) {
  return request({
    url: '/word/hotword',
    method: 'get',
    params: params
  })
}

export function addHotWord(data) {
  return request({
    url: '/word/hotword',
    method: 'post',
    data: data
  })
}

export function editHotWord(data) {
  return request({
    url: '/word/hotword',
    method: 'put',
    data: data
  })
}

export function deleteHotWord(data) {
  return request({
    url: '/word/hotword',
    method: 'delete',
    data: data
  })
}

export function getHotWordGroupTreeData() {
  return request({
    url: '/word/hotword/group/treedata',
    method: 'get'
  })
}

export function getHotWordGroupOptions() {
  return request({
    url: '/word/hotword/group/options',
    method: 'get'
  })
}

export function addHotWordGroup(data) {
  return request({
    url: '/word/hotword/group',
    method: 'post',
    data: data
  })
}

export function editHotWordGroup(data) {
  return request({
    url: '/word/hotword/group',
    method: 'put',
    data: data
  })
}

export function deleteHotWordGroup(data) {
  return request({
    url: '/word/hotword/group',
    method: 'delete',
    data: data
  })
}