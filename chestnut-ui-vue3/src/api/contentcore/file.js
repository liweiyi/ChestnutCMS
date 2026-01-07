import request from '@/utils/request'

// 站点目录TreeData
export function getDirectoryTreeData() {
  return request({
    url: '/cms/file/directoryTreeData',
    method: 'get'
  })
}

// 获取指定目录下文件
export function getFileList(params) {
  return request({
    url: '/cms/file/list',
    method: 'get',
    params: params
  })
}

// 文件重命名
export function renameFile(data) {
  return request({
    url: '/cms/file/rename',
    method: 'post',
    data: data
  })
}

// 新建文件
export function addFile(data) {
  return request({
    url: '/cms/file/add',
    method: 'post',
    data: data
  })
}

// 获取文件内容
export function readFile(data) {
  return request({
    url: '/cms/file/read',
    method: 'post',
    data: data
  })
}

// 修改文件
export function editFile(data) {
  return request({
    url: '/cms/file/edit',
    method: 'post',
    data: data
  })
}

// 删除文件
export function deleteFile(data) {
  return request({
    url: '/cms/file/delete',
    method: 'post',
    data: data
  })
}
