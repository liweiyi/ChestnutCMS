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
    url: '/cms/dynamic_page/' + pageId,
    method: 'get'
  })
}

export function addDynamicPage(data) {
  return request({
    url: '/cms/dynamic_page',
    method: 'post',
    data: data
  })
}

export function editDynamicPage(data) {
  return request({
    url: '/cms/dynamic_page',
    method: 'put',
    data: data
  })
}

export function deleteDynamicPages(pageIds) {
  return request({
    url: '/cms/dynamic_page',
    method: 'delete',
    data: pageIds
  })
}
