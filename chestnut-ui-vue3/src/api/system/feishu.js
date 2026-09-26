import request from '@/utils/request'

export function getLoginUrl(configId) {
  return request({
    url: '/sys/feishu/login?configId=' + configId,
    method: 'get',
    headers: { isToken: false },
  })
}
