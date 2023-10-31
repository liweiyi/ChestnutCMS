import request from '@/utils/request'

// 生成站点地图
export function genSitemap(params) {
  return request({
    url: '/cms/seo/sitemap',
    method: 'post',
    params: params
  })
}