import request from '@/utils/request'

export function getTaskTypes() {
  return request({
    url: '/monitor/task/typeOptions',
    method: 'get'
  })
}

export function listTask(query) {
  return request({
    url: '/monitor/task',
    method: 'get',
    params: query
  })
}

export function getTask(taskId) {
  return request({
    url: '/monitor/task/' + taskId,
    method: 'get'
  })
}

export function addTask(data) {
  return request({
    url: '/monitor/task',
    method: 'post',
    data: data
  })
}

export function updateTask(data) {
  return request({
    url: '/monitor/task',
    method: 'put',
    data: data
  })
}

export function delTask(taskIds) {
  return request({
    url: '/monitor/task',
    method: 'delete',
    data: taskIds
  })
}

export function enableTask(taskId) {
  return request({
    url: '/monitor/task/enable/' + taskId,
    method: 'put'
  })
}

export function disableTask(taskId) {
  return request({
    url: '/monitor/task/disable/' + taskId,
    method: 'put'
  })
}

export function executeTask(taskId) {
  return request({
    url: '/monitor/task/exec/' + taskId,
    method: 'post'
  })
}

export function getTaskLogs(params) {
  return request({
    url: '/monitor/task/logs',
    method: 'get',
    params: params
  })
}

export function delTaskLogs(logIds) {
  return request({
    url: '/monitor/task/logs',
    method: 'delete',
    data: logIds
  })
}
