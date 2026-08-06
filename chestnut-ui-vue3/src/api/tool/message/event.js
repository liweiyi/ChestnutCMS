import request from '@/utils/request'

export function getEventList(query) {
  return request({
    url: '/message/event/list',
    method: 'get',
    params: query
  })
}

export function getEventDetail(id) {
  return request({
    url: `/message/event/${id}`,
    method: 'get',
  })
}

export function saveEvent(data) {
  return request({
    url: '/message/event/save',
    method: 'post',
    data: data
  })
}

export function deleteEvents(ids) {
  return request({
    url: '/message/event/delete',
    method: 'post',
    data: {
      ids: ids
    }
  })
}

export function testEvent(data) {
  return request({
    url: '/message/event/test',
    method: 'post',
    data: data
  })
}