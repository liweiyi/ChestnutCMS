import request from '@/utils/request'

// 查询模板文件数据集合
export function getTemplateList(query) {
  return request({
    url: '/cms/template',
    method: 'get',
    params: query
  })
}

// 查询模板文件内容
export function getTemplateDetail(templateId) {
  return request({
    url: '/cms/template/' + templateId,
    method: 'get'
  })
}

// 模板文件重命名
export function renameTemplate(data) {
  return request({
    url: '/cms/template/rename',
    method: 'post',
    data: data
  })
}

// 新增模板文件
export function addTemplate(data) {
  return request({
    url: '/cms/template',
    method: 'post',
    data: data
  })
}

// 修改模板
export function editTemplate(data) {
  return request({
    url: '/cms/template',
    method: 'put',
    data: data
  })
}

// 删除模板
export function delTemplate(templates) {
  return request({
    url: '/cms/template',
    method: 'delete',
    data: templates
  })
}

// 删除区块模板缓存
export function clearIncludeCache(templates) {
  return request({
    url: '/cms/template/clearIncludeCache',
    method: 'delete',
    data: templates
  })
}
