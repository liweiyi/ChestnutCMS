import request from '@/utils/request'

export function getCaptcha(type) {
  return request({
    url: '/captcha/get?type=' + type,
    method: 'get',
  })
}

export function checkCaptcha(type, data) {
  return request({
    url: '/captcha/check?type=' + type,
    method: 'post',
    data: data
  })
}