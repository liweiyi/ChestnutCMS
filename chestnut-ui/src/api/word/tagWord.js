import request from '@/utils/request'

export function getTagWordList(params) {
  return request({
    url: '/word/tagword/list',
    method: 'get',
    params: params
  })
}

export function addTagWord(data) {
  return request({
    url: '/word/tagword/add',
    method: 'post',
    data: data
  })
}

export function batchAddTagWords(data) {
  return request({
    url: '/word/tagword/batchAdd',
    method: 'post',
    data: data
  })
}

export function editTagWord(data) {
  return request({
    url: '/word/tagword/update',
    method: 'put',
    data: data
  })
}

export function deleteTagWord(data) {
  return request({
    url: '/word/tagword/delete',
    method: 'post',
    data: data
  })
}

export function getTagWordGroupTreeData() {
  return request({
    url: '/word/tagword/group/treedata',
    method: 'get'
  })
}

export function getTagWordGroupData(groupId) {
  return request({
    url: '/word/tagword/group/data/' + groupId,
    method: 'get'
  })
}

export function addTagWordGroup(data) {
  return request({
    url: '/word/tagword/group/add',
    method: 'post',
    data: data
  })
}

export function editTagWordGroup(data) {
  return request({
    url: '/word/tagword/group/update',
    method: 'put',
    data: data
  })
}

export function deleteTagWordGroup(data) {
  return request({
    url: '/word/tagword/group/delete',
    method: 'post',
    data: data
  })
}