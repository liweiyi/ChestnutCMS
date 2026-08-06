import request from '@/utils/request'

export function getTemplateList(query) {
  return request({
    url: '/message/template/list',
    method: 'get',
    params: query
  })
}

export function getTemplateDetail(id) {
  return request({
    url: `/message/template/${id}`,
    method: 'get',
  })
}

export function createTemplate(data) {
  return request({
    url: '/message/template/add',
    method: 'post',
    data: data
  })
}

export function updateTemplate(data) {
  return request({
    url: '/message/template/update',
    method: 'post',
    data: data
  })
}

export function deleteTemplates(ids) {
  return request({
    url: '/message/template/delete',
    method: 'post',
    data: ids
  })
}