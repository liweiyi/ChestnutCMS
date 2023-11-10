import request from '@/utils/request'

export function getRelaContentList(query) {
  return request({
    url: '/cms/content/rela',
    method: 'get',
    params: query
  })
}

export function addRelaContents(data) {
  return request({
    url: '/cms/content/rela?contentId=' + data.contentId,
    method: 'post',
    data: data.relaContentIds
  })
}

export function delRelaContents(data) {
  return request({
    url: '/cms/content/rela?contentId=' + data.contentId,
    method: 'delete',
    data: data.relaContentIds
  })
}