import request from '@/utils/request'

export function listPageWidgetTypes() {
  return request({
    url: '/cms/pagewidget/types',
    method: 'get'
  })
}

export function listPageWidgets(params) {
  return request({
    url: '/cms/pagewidget/list',
    method: 'get',
    params: params
  })
}

export function getPageWidget(pageWidgetId) {
  return request({
    url: '/cms/pagewidget/detail/' + pageWidgetId,
    method: 'get'
  })
}

export function addPageWidget(params) {
  return request({
    url: '/cms/pagewidget/add',
    method: 'post',
    data: params
  })
}

export function editPageWidget(params) {
  return request({
    url: '/cms/pagewidget/update',
    method: 'post',
    data: params
  })
}

export function deletePageWidget(params) {
  return request({
    url: '/cms/pagewidget/delete',
    method: 'post',
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

export function offlinePageWidgets(params) {
  return request({
    url: '/cms/pagewidget/offline',
    method: 'post',
    data: params
  })
}