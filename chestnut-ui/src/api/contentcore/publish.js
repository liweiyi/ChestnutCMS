import request from '@/utils/request'

// 当前发布任务队列长度
export function getPublishTaskCount() {
  return request({
    url: '/cms/publish/taskCount',
    method: 'get'
  })
}

// 清空发布任务队列
export function clearPublishTask() {
  return request({
    url: '/cms/publish/clear',
    method: 'delete'
  })
}
