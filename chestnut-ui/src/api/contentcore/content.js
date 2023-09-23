import request from '@/utils/request'

// 查询内容列表
export function getContentList(query) {
  return request({
    url: '/cms/content/list',
    method: 'get',
    params: query
  })
}

// 初始化内容编辑数据
export function getInitContentEditorData(catalogId, contentType, contentId) {
  return request({
    url: '/cms/content/init/' + catalogId + "/" + contentType + "/" + contentId,
    method: 'get'
  })
}

// 新增内容
export function addContent(data) {
  return request({
    url: '/cms/content?contentType=' + data.contentType,
    method: 'post',
    data: data
  })
}

// 修改内容
export function saveContent(data) {
  return request({
    url: '/cms/content?contentType=' + data.contentType,
    method: 'put',
    data: data
  })
}

// 删除内容
export function delContent(data) {
  return request({
    url: '/cms/content',
    method: 'delete',
    data: data
  })
}

// 发布内容
export function publishContent(contentIds) {
  return request({
    url: '/cms/content/publish',
    method: 'post',
    data: { contentIds: contentIds }
  })
}

// 锁定内容
export function lockContent(contentId) {
  return request({
    url: '/cms/content/lock/' + contentId,
    method: 'post'
  })
}

// 解锁内容
export function unLockContent(contentId) {
  return request({
    url: '/cms/content/unlock/' + contentId,
    method: 'post'
  })
}

export function createIndexes(contentId) {
  return request({
    url: '/cms/search/build/' + contentId,
    method: 'post'
  })
}

export function copyContent(data) {
  return request({
    url: '/cms/content/copy',
    method: 'post',
    data: data
  })
}

export function moveContent(data) {
  return request({
    url: '/cms/content/move',
    method: 'post',
    data: data
  })
}

export function setTopContent(data) {
  return request({
    url: '/cms/content/set_top',
    method: 'post',
    data: data
  })
}

export function cancelTopContent(data) {
  return request({
    url: '/cms/content/cancel_top',
    method: 'post',
    data: data
  })
}

export function sortContent(data) {
  return request({
    url: '/cms/content/sort',
    method: 'post',
    data: data
  })
}

export function offlineContent(data) {
  return request({
    url: '/cms/content/offline',
    method: 'post',
    data: data
  })
}

export function toPublishContent(contentIds) {
  return request({
    url: '/cms/content/to_publish',
    method: 'post',
    data: contentIds
  })
}

export function archiveContent(data) {
  return request({
    url: '/cms/content/archive',
    method: 'post',
    data: data
  })
}

export function addContentAttribute(data) {
  return request({
    url: '/cms/content/attr',
    method: 'post',
    data: data
  })
}

export function removeContentAttribute(data) {
  return request({
    url: '/cms/content/attr',
    method: 'delete',
    data: data
  })
}
