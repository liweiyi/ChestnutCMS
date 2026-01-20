import request from '@/utils/request'

// 查询岗位列表
export function listPost(query) {
  return request({
    url: '/system/post/list',
    method: 'get',
    params: query
  })
}

export function getPostOptions() {
  return request({
    url: '/system/post/optionselect',
    method: 'get'
  })
}


// 查询岗位详细
export function getPost(postId) {
  return request({
    url: '/system/post/detail/' + postId,
    method: 'get'
  })
}

// 新增岗位
export function addPost(data) {
  return request({
    url: '/system/post/add',
    method: 'post',
    data: data
  })
}

// 修改岗位
export function updatePost(data) {
  return request({
    url: '/system/post/update',
    method: 'post',
    data: data
  })
}

// 删除岗位
export function delPost(postIds) {
  return request({
    url: '/system/post/delete',
    method: 'post',
    data: postIds
  })
}
