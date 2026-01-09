import request from "@/utils/request";

export function getTypeOptions() {
  return request({
    url: "/system/cloud/config/typeOptions",
    method: "get",
  });
}

export function getList(params) {
  return request({
    url: "/system/cloud/config/list",
    method: "get",
    params: params
  });
}


export function getOptions() {
  return request({
    url: "/system/cloud/config/options",
    method: "get",
  });
}

export function getDetail(configId) {
  return request({
    url: "/system/cloud/config/detail/" + configId,
    method: "get"
  });
}

export function addConfig(data) {
  return request({
    url: "/system/cloud/config/add",
    method: "post",
    data: data
  });
}

export function updateConfig(data) {
  return request({
    url: "/system/cloud/config/update",
    method: "post",
    data: data
  });
}

export function deleteConfigs(configIds) {
  return request({
    url: "/system/cloud/config/delete",
    method: 'post',
    data: configIds
  });
}
