import request from '@/utils/request'

export function pushToBaidu(contentIds) {
  return request({
    url: '/cms/seo/baidu_push',
    method: 'post',
    data: contentIds
  })
}