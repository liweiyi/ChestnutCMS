import request from '@/utils/request'

export function executeGroovyScript(data) {
  return request({
    url: '/groovy/exec',
    method: 'post',
    data: data
  })
}

export function getGroovyScript(scriptId) {
  return request({
    url: '/groovy/' + scriptId,
    method: 'get'
  })
}

export function getGroovyScriptList() {
  return request({
    url: '/groovy/list',
    method: 'get'
  })
}

export function saveGroovyScript(data) {
  return request({
    url: '/groovy/save',
    method: 'post',
    data: data
  })
}

export function deleteGroovyScript(scriptIds) {
  return request({
    url: '/groovy/delete',
    method: 'delete',
    data: scriptIds
  })
}