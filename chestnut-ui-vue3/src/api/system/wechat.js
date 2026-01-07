import request from "@/utils/request";

export function getLoginUrl(configId) {
  return request({
    url: "/sys/wechat/login",
    method: "get",
    params: {
      configId: configId
    }
  });
}