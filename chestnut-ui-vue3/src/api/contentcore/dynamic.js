import request from '@/utils/request'

export function getDynamicPageInitDataTypes(query) {
  return request({
    url: '/cms/dynamic_page/init_data_types',
    method: 'get',
    params: query
  })
}

export function getDynamicPageList(query) {
  return request({
    url: '/cms/dynamic_page/list',
    method: 'get',
    params: query
  })
}

export function getDynamicPageDetail(pageId) {
  return request({
    url: '/cms/dynamic_page/detail/' + pageId,
    method: 'get'
  })
}

export function addDynamicPage(data) {
  return request({
    url: '/cms/dynamic_page/add',
    method: 'post',
    data: data
  })
}

export function editDynamicPage(data) {
  return request({
    url: '/cms/dynamic_page/update',
    method: 'post',
    data: data
  })
}

export function deleteDynamicPages(pageIds) {
  return request({
    url: '/cms/dynamic_page/delete',
    method: 'post',
    data: pageIds
  })
}
