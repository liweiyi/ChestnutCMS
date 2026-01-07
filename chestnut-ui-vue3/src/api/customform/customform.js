import request from '@/utils/request'

export function getLimitRules() {
  return request({
    url: '/cms/customform/limit_rules',
    method: 'get',
  })
}

export function listCustomForms(params) {
  return request({
    url: '/cms/customform/list',
    method: 'get',
    params: params
  })
}

export function getCustomForm(formId) {
  return request({
    url: '/cms/customform/detail/' + formId,
    method: 'get'
  })
}

export function addCustomForm(data) {
  return request({
    url: '/cms/customform/add',
    method: 'post',
    data: data
  })
}

export function editCustomForm(data) {
  return request({
    url: '/cms/customform/update',
    method: 'post',
    data: data
  })
}

export function deleteCustomForms(data) {
  return request({
    url: '/cms/customform/delete',
    method: 'post',
    data: data
  })
}

export function publishCustomForm(formIds) {
  return request({
    url: '/cms/customform/publish',
    method: 'post',
    data: formIds
  })
}

export function offlineCustomForm(formIds) {
  return request({
    url: '/cms/customform/offline',
    method: 'post',
    data: formIds
  })
}

export function listCustomFormDatas(params) {
  return request({
    url: '/cms/customform/data/list',
    method: 'get',
    params: params
  })
}

export function getCustomFormData(formId, dataId) {
  return request({
    url: '/cms/customform/data/detail',
    method: 'get',
    params: {
      formId: formId,
      dataId: dataId
    }
  })
}

export function addCustomFormData(formId, data) {
  return request({
    url: '/cms/customform/data/add?formId=' + formId,
    method: 'post',
    data: data
  })
}

export function editCustomFormData(formId, data) {
  return request({
    url: '/cms/customform/data/update?formId=' + formId,
    method: 'post',
    data: data
  })
}

export function deleteCustomFormDatas(formId, dataIds) {
  return request({
    url: '/cms/customform/data/delete?formId=' + formId,
    method: 'post',
    data: dataIds
  })
}