import request from '@/utils/request'

export function listLicenses(params) {
  return request({
    url: '/license',
    method: 'get',
    params: params
  })
}

export function addLicense(data) {
  return request({
    url: '/license',
    method: 'post',
    data: data
  })
}

export function delLicense(licenseIds) {
  return request({
    url: '/license',
    method: 'delete',
    data: licenseIds
  })
}