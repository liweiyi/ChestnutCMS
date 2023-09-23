import request from '@/utils/request'

export function getAdStatList(params) {
  return request({
    url: '/cms/ad/stat',
    method: 'get',
    params: params
  })
}

export function getLineChartStatDatas(params) {
  return request({
    url: '/cms/ad/stat/chart',
    method: 'get',
    params: params
  })
}

export function getAdClickLogList(params) {
  return request({
    url: '/cms/ad/stat/click',
    method: 'get',
    params: params
  })
}

export function getAdViewLogList(params) {
  return request({
    url: '/cms/ad/stat/view',
    method: 'get',
    params: params
  })
}