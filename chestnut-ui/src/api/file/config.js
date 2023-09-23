import request from "@/utils/request";

export function getFileConfig() {
  return request({
    url: "/ccfile/config",
    method: "get"
  });
}

export function saveFileConfig(data) {
  return request({
    url: "/ccfile/config",
    method: "put",
    data: data
  });
}

export function getFileStorageTypes() {
  return request({
    url: "/ccfile/config/storageTypes",
    method: "get"
  });
}
