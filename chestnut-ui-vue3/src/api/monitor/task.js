import request from '@/utils/request'

export function getTaskTypes() {
  return request({
    url: '/monitor/task/typeOptions',
    method: 'get'
  })
}

export function listTask(query) {
  return request({
    url: '/monitor/task/list',
    method: 'get',
    params: query
  })
}

export function getTask(taskId) {
  return request({
    url: '/monitor/task/detail/' + taskId,
    method: 'get'
  })
}

export function addTask(data) {
  return request({
    url: '/monitor/task/add',
    method: 'post',
    data: data
  })
}

export function updateTask(data) {
  return request({
    url: '/monitor/task/update',
    method: 'post',
    data: data
  })
}

export function delTask(taskIds) {
  return request({
    url: '/monitor/task/delete',
    method: 'post',
    data: taskIds
  })
}

export function enableTask(taskId) {
  return request({
    url: '/monitor/task/enable/' + taskId,
    method: 'post'
  })
}

export function disableTask(taskId) {
  return request({
    url: '/monitor/task/disable/' + taskId,
    method: 'post'
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
    url: '/monitor/task/logs/delete',
    method: 'post',
    data: logIds
  })
}
