import request from "@/utils/request";

export function listSecurityConfigs(params) {
  return request({
    url: "/system/security/config/list",
    method: "get",
    params: params
  });
}

export function getSecurityConfig(configId) {
  return request({
    url: "/system/security/config/detail/" + configId,
    method: "get"
  });
}

export function currentSecurityConfig(configId) {
  return request({
    url: "/system/security/config/current",
    method: "get"
  });
}

export function addSecurityConfig(data) {
  return request({
    url: "/system/security/config/add",
    method: "post",
    data: data
  });
}

export function saveSecurityConfig(data) {
  return request({
    url: "/system/security/config/update",
    method: "post",
    data: data
  });
}

export function deleteSecurityConfig(configIds) {
  return request({
    url: "/system/security/config/delete",
    method: 'post',
    data: configIds
  });
}

export function changeConfigStatus(configId) {
  return request({
    url: "/system/security/config/changeStatus/" + configId,
    method: "post"
  });
}

export function checkSecurityConfig(configId) {
  return request({
    url: "/system/security/config/check",
    method: "get"
  });
}