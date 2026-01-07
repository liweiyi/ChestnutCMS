import request from "@/utils/request";

export function getList(params) {
  return request({
    url: "/system/login/config/list",
    method: "get",
    params: params
  });
}

export function getConfigTypeOptions() {
  return request({
    url: "/system/login/config/typeOptions",
    method: "get"
  });
}

export function getConfig(configId) {
  return request({
    url: "/system/login/config/detail",
    method: "get",
    params: { configId: configId }
  });
}

export function addConfig(data) {
  return request({
    url: "/system/login/config/add",
    method: "post",
    data: data
  });
}

export function updateConfig(data) {
  return request({
    url: "/system/login/config/update",
    method: "post",
    data: data
  });
}

export function deleteConfigs(configIds) {
  return request({
    url: "/system/login/config/delete",
    method: 'post',
    data: configIds
  });
}

export function getLoginConfigs() {
  return request({
    url: "/system/login/config/options",
    method: "get"
  });
}