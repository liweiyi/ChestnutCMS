import request from '@/utils/request'

export function listCustomForms(params) {
  return request({
    url: '/cms/customform',
    method: 'get',
    params: params
  })
}

export function getCustomForm(formId) {
  return request({
    url: '/cms/customform/' + formId,
    method: 'get'
  })
}

export function addCustomForm(data) {
  return request({
    url: '/cms/customform',
    method: 'post',
    data: data
  })
}

export function editCustomForm(data) {
  return request({
    url: '/cms/customform',
    method: 'put',
    data: data
  })
}

export function deleteCustomForms(data) {
  return request({
    url: '/cms/customform',
    method: 'delete',
    data: data
  })
}

export function publishCustomForm(formIds) {
  return request({
    url: '/cms/customform/publish',
    method: 'put',
    data: formIds
  })
}

export function offlineCustomForm(formIds) {
  return request({
    url: '/cms/customform/offline',
    method: 'put',
    data: formIds
  })
}

export function listCustomFormDatas(params) {
  return request({
    url: '/cms/customform/data',
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
    url: '/cms/customform/data?formId=' + formId,
    method: 'post',
    data: data
  })
}

export function editCustomFormData(formId, data) {
  return request({
    url: '/cms/customform/data?formId=' + formId,
    method: 'put',
    data: data
  })
}

export function deleteCustomFormDatas(formId, dataIds) {
  return request({
    url: '/cms/customform/data?formId=' + formId,
    method: 'delete',
    data: dataIds
  })
}