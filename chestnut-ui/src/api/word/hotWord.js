import request from '@/utils/request'

export function getHotWordList(params) {
  return request({
    url: '/word/hotword/list',
    method: 'get',
    params: params
  })
}

export function addHotWord(data) {
  return request({
    url: '/word/hotword/add',
    method: 'post',
    data: data
  })
}

export function editHotWord(data) {
  return request({
    url: '/word/hotword/update',
    method: 'put',
    data: data
  })
}

export function deleteHotWord(data) {
  return request({
    url: '/word/hotword/delete',
    method: 'post',
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
    url: '/word/hotword/group/add',
    method: 'post',
    data: data
  })
}

export function editHotWordGroup(data) {
  return request({
    url: '/word/hotword/group/update',
    method: 'put',
    data: data
  })
}

export function deleteHotWordGroup(data) {
  return request({
    url: '/word/hotword/group/delete',
    method: 'post',
    data: data
  })
}