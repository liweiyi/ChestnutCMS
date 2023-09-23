import request from '@/utils/request'

export function getTaskList(params) {
  return request({
    url: '/async/task',
    method: 'get',
    params: params
  })
}

export function getTaskInfo(taskId) {
  return request({
    url: '/async/task/' + taskId,
    method: 'get'
  })
}

export function stopTask(taskIds) {
  return request({
    url: '/async/task/stop',
    method: 'put',
    data: taskIds
  })
}

export function removeTask(taskIds) {
  return request({
    url: '/async/task/remove',
    method: 'delete',
    data: taskIds
  })
}