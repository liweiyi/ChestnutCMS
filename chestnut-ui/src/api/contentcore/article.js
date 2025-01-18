import request from '@/utils/request'

export function getUEditorCSS(params) {
  return request({
    url: '/cms/article/ueditor_css',
    method: 'get',
    params: params
  })
}

export function getArticleBodyFormats() {
  return request({
    url: '/cms/article/formats',
    method: 'get'
  })
}