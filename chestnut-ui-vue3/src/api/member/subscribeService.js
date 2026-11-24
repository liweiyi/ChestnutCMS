import request from '@/utils/request'

const base = '/member-subscribe-service'

export const listMemberSubscribeServices = params => request({ url: `${base}/list`, method: 'get', params })
export const getMemberSubscribeService = id => request({ url: `${base}/detail/${encodeURIComponent(id)}`, method: 'get' })
export const listMemberSubscribeServiceOptions = (params = {}) => request({
  url: `${base}/options`,
  method: 'get',
  params: { ...params, ids: Array.isArray(params.ids) ? params.ids.join(',') : params.ids }
})
export const addMemberSubscribeService = data => request({ url: `${base}/add`, method: 'post', data })
export const updateMemberSubscribeService = data => request({ url: `${base}/update`, method: 'post', data })
export const delMemberSubscribeServices = ids => request({ url: `${base}/delete`, method: 'post', data: ids })
