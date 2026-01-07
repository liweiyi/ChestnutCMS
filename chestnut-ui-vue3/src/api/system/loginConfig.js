import request from "@/utils/request";

export function getList(params) {
  return request({
    url: "/system/loginconfig/list",
    method: "get",
    params: params
  });
}

export function getDetail(configId) {
  return request({
    url: "/system/loginconfig/detail/" + configId,
    method: "get"
  });
}

export function addConfig(data) {
  return request({
    url: "/system/loginconfig/add",
    method: "post",
    data: data
  });
}

export function updateConfig(data) {
  return request({
    url: "/system/loginconfig/update",
    method: "post",
    data: data
  });
}

export function deleteConfigs(configIds) {
  return request({
    url: "/system/loginconfig/delete",
    method: 'post',
    data: configIds
  });
}
