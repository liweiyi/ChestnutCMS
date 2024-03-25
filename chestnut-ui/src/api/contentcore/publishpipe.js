import request from '@/utils/request'

// 查询发布通道列表
export function getPublishPipeList(query) {
  return request({
    url: '/cms/publishpipe/list',
    method: 'get',
    params: query
  })
}

// 查询发布通道列表
export function getPublishPipeSelectData(query) {
  return request({
    url: '/cms/publishpipe/selectData',
    method: 'get',
    params: query
  })
}

// 查询发布通道详情
export function getPublishPipeData(siteId) {
  return request({
    url: '/cms/publishpipe/' + siteId,
    method: 'get'
  })
}

// 新增发布通道
export function addPublishPipe(data) {
  return request({
    url: '/cms/publishpipe',
    method: 'post',
    data: data
  })
}

// 修改发布通道
export function updatePublishPipe(data) {
  return request({
    url: '/cms/publishpipe',
    method: 'put',
    data: data
  })
}

// 删除发布通道
export function delPublishPipe(publishPipeIds) {
  return request({
    url: '/cms/publishpipe',
    method: 'delete',
    data: publishPipeIds
  })
}


export function getPublishPipePropValue(params) {
  return request({
    url: '/cms/publishpipe/prop_value',
    method: 'get',
    params: params
  })
}
