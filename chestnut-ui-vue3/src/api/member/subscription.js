import request from '@/utils/request'

const base = '/member/subscription'

// 订阅定义和业务注册服务项。
export const listMemberSubscriptionDefinitions = params => request({ url: `${base}/definition/list`, method: 'get', params })
export const getMemberSubscriptionDefinition = id => request({ url: `${base}/definition/detail/${encodeURIComponent(id)}`, method: 'get' })
export const listMemberSubscriptionServiceItems = () => request({ url: `${base}/definition/serviceItems`, method: 'get' })
export const listMemberSubscriptionOptions = (params = {}) => request({ url: `${base}/definition/options`, method: 'get', params: { ...params, ids: Array.isArray(params.ids) ? params.ids.join(',') : params.ids } })
export const addMemberSubscriptionDefinition = data => request({ url: `${base}/definition/create`, method: 'post', data })
export const updateMemberSubscriptionDefinition = data => request({ url: `${base}/definition/update`, method: 'post', data })
export const changeMemberSubscriptionDefinitionStatus = data => request({ url: `${base}/definition/changeStatus`, method: 'post', data })
export const delMemberSubscriptionDefinition = id => request({ url: `${base}/definition/delete`, method: 'post', params: { definitionId: id } })

// 会员记录通过正负秒数调整，使用订阅定义ID关联。
export const listMemberSubscriptions = params => request({ url: `${base}/record/list`, method: 'get', params })
export const adjustMemberSubscription = data => request({ url: `${base}/record/adjust`, method: 'post', data })
export const listMemberSubscriptionLogs = params => request({ url: `${base}/log/list`, method: 'get', params })
