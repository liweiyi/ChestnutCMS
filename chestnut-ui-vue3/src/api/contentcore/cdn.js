import request from '@/utils/request'

export function refreshSite(siteId, refreshAll) {
  return request({
    url: '/cms/cdn/refresh/site',
    method: 'post',
    params: {
      siteId,
      refreshAll
    }
  })
}

export function refreshCatalog(catalogId, refreshAll) {
  return request({
    url: '/cms/cdn/refresh/catalog',
    method: 'post',
    params: {
      catalogId,
      refreshAll
    }
  })
}

export function refreshContent(contentId) {
  return request({
    url: '/cms/cdn/refresh/content',
    method: 'post',
    params: {
      contentId
    }
  })
}