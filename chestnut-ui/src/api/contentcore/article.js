import request from '@/utils/request'

export function getUEditorCSS(params) {
  return request({
    url: '/cms/article/ueditor_css',
    method: 'get',
    params: params
  })
}