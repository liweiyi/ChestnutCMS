import request from '@/utils/request'

export function listPageWidgetTypes() {
  return request({
    url: '/cms/pagewidget/types',
    method: 'get'
  })
}

export function listPageWidgets(params) {
  return request({
    url: '/cms/pagewidget',
    method: 'get',
    params: params
  })
}

export function getPageWidget(pageWidgetId) {
  return request({
    url: '/cms/pagewidget/' + pageWidgetId,
    method: 'get'
  })
}

export function addPageWidget(params) {
  return request({
    url: '/cms/pagewidget?type=' + params.type,
    method: 'post',
    data: params
  })
}

export function editPageWidget(params) {
  return request({
    url: '/cms/pagewidget?type=' + params.type,
    method: 'put',
    data: params
  })
}

export function deletePageWidget(params) {
  return request({
    url: '/cms/pagewidget',
    method: 'delete',
    data: params
  })
}

export function publishPageWidgets(params) {
  return request({
    url: '/cms/pagewidget/publish',
    method: 'post',
    data: params
  })
}