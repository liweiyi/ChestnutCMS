import request from '@/utils/request'

// 查询内容索引列表
export function getContentIndexList(query) {
  return request({
    url: '/cms/search/contents',
    method: 'get',
    params: query
  })
}

// 删除内容索引
export function deleteContentIndex(contentIds) {
  return request({
    url: '/cms/search/contents',
    method: 'delete',
    data: contentIds
  })
}

// 内容索引详情
export function getContentIndexDetail() {
  return request({
    url: '/cms/search/content/' + contentId,
    method: 'get'
  })
}

export function rebuildIndex() {
  return request({
    url: '/cms/search/rebuild',
    method: 'post'
  })
}