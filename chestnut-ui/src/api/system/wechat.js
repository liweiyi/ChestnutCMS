import request from "@/utils/request";

export function listWeChatConfigs(params) {
  return request({
    url: "/system/wechat",
    method: "get",
    params: params
  });
}

export function getWeChatConfig(configId) {
  return request({
    url: "/system/wechat/" + configId,
    method: "get"
  });
}

export function addWeChatConfig(data) {
  return request({
    url: "/system/wechat",
    method: "post",
    data: data
  });
}

export function saveWeChatConfig(data) {
  return request({
    url: "/system/wechat",
    method: "put",
    data: data
  });
}

export function deleteWeChatConfig(configIds) {
  return request({
    url: "/system/wechat",
    method: "delete",
    data: configIds
  });
}

export function changeConfigStatus(configId) {
  return request({
    url: "/system/wechat/change_status/" + configId,
    method: "put"
  });
}
