import request from '@/utils/request'
import { getResponseCodeErrMsg } from '@/utils/chestnut'
import auth from '@/plugins/auth'
import cms from '@/plugins/cms'

function resourceHeaders(siteId) {
  const targetSiteId = siteId == null ? '' : String(siteId).trim()
  if (!/^[1-9]\d*$/.test(targetSiteId)) return {}
  // 显式站点只用于本次资源请求，保留认证并跳过全局站点头的自动注入。
  return {
    isToken: false,
    ...auth.getTokenHeader(),
    [cms.currentSiteKey()]: targetSiteId
  }
}

export function getResourceTypes() {
  return request({
    url: '/cms/resource/types',
    method: 'get'
  })
}
export function getResourceSelectList(query) {
  return request({
    url: '/cms/resource/selectList',
    method: 'get',
    params: query
  })
}

// 查询资源列表
export function getResourceList(query) {
  return request({
    url: '/cms/resource',
    method: 'get',
    params: query
  })
}

// 获取资源详情
export function getResourceDetail(resourceId, { siteId, signal } = {}) {
  return request({
    url: '/cms/resource/' + resourceId,
    method: 'get',
    headers: resourceHeaders(siteId),
    signal
  })
}

// Read the original bytes rather than the thumbnail returned in the resource list.
export async function downloadResourceFile(resourceId, { siteId, signal } = {}) {
  const blob = await request({
    url: '/cms/resource/download/' + encodeURIComponent(resourceId),
    method: 'get',
    responseType: 'blob',
    timeout: 0,
    headers: resourceHeaders(siteId),
    signal
  })
  // The download endpoint can return the normal JSON error envelope as a Blob.
  // Avoid converting a large image to text just to check for an error response.
  if (blob.type.includes('json') || blob.size <= 65536) {
    let response
    try {
      response = JSON.parse(await blob.text())
    } catch {
      return blob
    }
    if (response && typeof response === 'object' && response.code != null) {
      throw new Error(getResponseCodeErrMsg(response.code, response.msg))
    }
  }
  return blob
}

// 新增资源
export function addResource(data, { siteId, signal } = {}) {
  return request({
    url: '/cms/resource',
    method: 'post',
    data: data,
    signal,
    headers: {
      ...resourceHeaders(siteId),
      "Content-type": "multipart/form-data"
    }
  })
}

// 修改资源
export function updateResource(data) {
  return request({
    url: '/cms/resource',
    method: 'post',
    data: data
  })
}

// 删除资源
export function delResource(resourceIds) {
  return request({
    url: '/cms/resource/delete',
    method: 'post',
    data: resourceIds
  })
}
