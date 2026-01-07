import request from '@/utils/request'

export function getCaptchaTypeOptions() {
  return request({
    url: '/captcha/options',
    method: 'get',
  })
}

export function getCaptcha(params) {
  return request({
    url: '/captcha/get',
    method: 'get',
    params: params
  })
}

export function checkCaptcha(type, data) {
  return request({
    url: '/captcha/check?type=' + type,
    method: 'post',
    data: data
  })
}