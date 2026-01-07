import request from '@/utils/request'

// 保存权限配置{ownerType: 授权对象类型, owner: 授权对象唯一标识, permissions: []}
export function savePermissions(data) {
  return request({
    url: '/system/permission',
    method: 'post',
    data: data
  })
}

// 菜单权限数据
export function getMenuPerms(params) {
  return request({
    url: '/system/permission/menu',
    method: 'get',
    params: params
  })
}