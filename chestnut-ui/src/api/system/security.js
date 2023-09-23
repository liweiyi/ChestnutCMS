import request from "@/utils/request";

export function listSecurityConfigs(params) {
  return request({
    url: "/system/security/config",
    method: "get",
    params: params
  });
}

export function getSecurityConfig(configId) {
  return request({
    url: "/system/security/config/" + configId,
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
    url: "/system/security/config",
    method: "post",
    data: data
  });
}

export function saveSecurityConfig(data) {
  return request({
    url: "/system/security/config",
    method: "put",
    data: data
  });
}

export function deleteSecurityConfig(configIds) {
  return request({
    url: "/system/security/config",
    method: "delete",
    data: configIds
  });
}

export function changeConfigStatus(configId) {
  return request({
    url: "/system/security/config/changeStatus/" + configId,
    method: "put"
  });
}
