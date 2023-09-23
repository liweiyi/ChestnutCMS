import request from '@/utils/request'

// 查询站点列表
export function listSite(query) {
  return request({
    url: '/cms/site/list',
    method: 'get',
    params: query
  })
}

export function getSiteOptions() {
  return request({
    url: '/cms/site/options',
    method: 'get'
  })
}

// 获取当前站点
export function getCurrentSite() {
  return request({
    url: '/cms/site/getCurrentSite',
    method: 'get'
  })
}

// 设置当前站点
export function setCurrentSite(siteId) {
  return request({
    url: '/cms/site/setCurrentSite/' + siteId,
    method: 'post'
  })
}

// 查询站点详细
export function getSite(siteId) {
  return request({
    url: '/cms/site/' + siteId,
    method: 'get'
  })
}

// 新增站点配置
export function addSite(data) {
  return request({
    url: '/cms/site',
    method: 'post',
    data: data
  })
}

// 修改站点配置
export function updateSite(data) {
  return request({
    url: '/cms/site',
    method: 'put',
    data: data
  })
}

// 删除站点配置
export function delSite(siteId) {
  return request({
    url: '/cms/site/' + siteId,
    method: 'delete'
  })
}

// 发布站点
export function publishSite(data) {
  return request({
    url: '/cms/site/publish',
    method: 'post',
    data: data // { siteId:long, publishIndex:boolean, contentStatus:int }
  })
}

// 导出站点主题
export function exportSiteTheme(data) {
  return request({
    url: '/cms/site/exportTheme',
    method: 'post',
    data: data
  })
}

// 重建全站索引
export function rebuildIndex() {
  return request({
    url: '/cms/site/rebuild_indexes',
    method: 'post'
  })
}

// 获取站点扩展属性
export function getSiteExtends(params) {
  return request({
    url: '/cms/site/extends',
    method: 'get',
    params: params
  })
}

// 保存站点扩展属性
export function saveSiteExtends(siteId, data) {
  return request({
    url: '/cms/site/extends/' + siteId,
    method: 'post',
    data: data
  })
}

// 站点自定义属性列表
export function getSitePropertyList(params) {
  return request({
    url: '/cms/site/prop/list',
    method: 'get',
    params: params
  })
}

// 新增站点自定义属性
export function addSiteProperty(data) {
  return request({
    url: '/cms/site/prop',
    method: 'post',
    data: data
  })
}

// 修改站点自定义属性
export function saveSiteProperty(data) {
  return request({
    url: '/cms/site/prop',
    method: 'put',
    data: data
  })
}

// 删除站点自定义属性
export function deleteSiteProperty(data) {
  return request({
    url: '/cms/site/prop',
    method: 'delete',
    data: data
  })
}

// 获取站点默认模板配置数据
export function getDefaultTemplates(params) {
  return request({
    url: '/cms/site/default_template',
    method: 'get',
    params: params
  })
}

// 保存站点默认模板配置数据
export function saveDefaultTemplates(data) {
  return request({
    url: '/cms/site/default_template',
    method: 'post',
    data: data
  })
}

// 站点默认模板配置应用到指定栏目
export function applyDefaultTemplate(data) {
  return request({
    url: '/cms/site/apply_default_template',
    method: 'post',
    data: data
  })
}

// 上传水印图片
export function uploadWatermarker(siteId, data) {
  return request({
    url: '/cms/site/upload_watermarker/' + siteId,
    method: 'post',
    data: data
  })
}

// 动态模板类型
export function getDynamicPageTypes() {
  return request({
    url: '/cms/dynamicPageTypes',
    method: 'get'
  })
}