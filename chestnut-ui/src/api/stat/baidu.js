import request from '@/utils/request'

export function getSiteList() {
  return request({
    url: '/cms/stat/baidu/sites',
    method: 'get'
  })
}

export function refreshBdTongjiToken() {
  return request({
    url: '/cms/stat/baidu/refreshToken',
    method: 'put'
  })
}

// 站点趋势概览数据
export function getSiteTrendOverviewDatas(params) {
  return request({
    url: '/cms/stat/baidu/trendOverview',
    method: 'get',
    params: params
  })
}

// 站点区域分布概览数据
export function getSiteDistrictOverviewDatas(params) {
  return request({
    url: '/cms/stat/baidu/districtOverview',
    method: 'get',
    params: params
  })
}

// 站点其他概览数据
export function getSiteOtherOverviewDatas(params) {
  return request({
    url: '/cms/stat/baidu/otherOverview',
    method: 'get',
    params: params
  })
}

// 站点趋势分析数据
export function getSiteTimeTrendDatas(params) {
  return request({
    url: '/cms/stat/baidu/timeTrend',
    method: 'get',
    params: params
  })
}

// 站点访问来源数据
export function getSiteVisitSource(params) {
  return request({
    url: '/cms/stat/baidu/sourceAll',
    method: 'get',
    params: params
  })
}

// 搜索引擎来源数据
export function getSiteEngineSource(params) {
  return request({
    url: '/cms/stat/baidu/sourceEngine',
    method: 'get',
    params: params
  })
}

// 搜索词来源数据
export function getSiteSearchWordSource(params) {
  return request({
    url: '/cms/stat/baidu/sourceSearchWord',
    method: 'get',
    params: params
  })
}