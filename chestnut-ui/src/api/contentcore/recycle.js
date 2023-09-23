import request from '@/utils/request'

export function getRecycleContentList(query) {
  return request({
    url: '/cms/content/recycle',
    method: 'get',
    params: query
  })
}

export function recoverRecycleContent(backupIds) {
  return request({
    url: '/cms/content/recycle/recover',
    method: 'post',
    data: backupIds
  })
}

export function deleteRecycleContents(backupIds) {
  return request({
    url: '/cms/content/recycle',
    method: 'delete',
    data: backupIds
  })
}