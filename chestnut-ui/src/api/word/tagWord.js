import request from '@/utils/request'

export function getTagWordList(params) {
  return request({
    url: '/word/tagword',
    method: 'get',
    params: params
  })
}

export function addTagWord(data) {
  return request({
    url: '/word/tagword',
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
    url: '/word/tagword',
    method: 'put',
    data: data
  })
}

export function deleteTagWord(data) {
  return request({
    url: '/word/tagword',
    method: 'delete',
    data: data
  })
}

export function getTagWordGroupTreeData() {
  return request({
    url: '/word/tagword/group/treedata',
    method: 'get'
  })
}

export function addTagWordGroup(data) {
  return request({
    url: '/word/tagword/group',
    method: 'post',
    data: data
  })
}

export function editTagWordGroup(data) {
  return request({
    url: '/word/tagword/group',
    method: 'put',
    data: data
  })
}

export function deleteTagWordGroup(data) {
  return request({
    url: '/word/tagword/group',
    method: 'delete',
    data: data
  })
}