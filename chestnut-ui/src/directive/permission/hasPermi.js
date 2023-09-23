 /**
 * v-hasPermi 操作权限处理
 * Copyright (c) 2023 兮玥（190785909@qq.com）
 */
 
import store from '@/store'

function fn (el, binding) {
  const { value } = binding
  const all_permission = "*";
  const permissions = store.getters && store.getters.permissions

  if (value && value instanceof Array && value.length > 0) {
    const permissionFlag = value

    const hasPermissions = permissions.some(permission => {
      return all_permission === permission || permissionFlag.includes(permission)
    })

    if (!hasPermissions) {
      if (el.cacheParentElement && el.cacheParentElement.className.split(' ').indexOf('el-dropdown') > -1) {
          el.cacheParentElement.parentNode.removeChild(el.cacheParentElement)
          el.cacheParentElement.parentNode.style.display = 'none'; 
      } else if (el.className.indexOf('el-button') > -1) {
        el.parentNode && el.parentNode.removeChild(el)
        if (el.cacheParentElement != null) {
          el.cacheParentElement.style.display = 'none'; 
        }
      } else if (el.className.indexOf('el-dropdown-menu__item') > -1) {
        el.cacheElement.style.display = 'none';
      }
    } else {
      if (el.cacheParentElement && el.cacheParentElement.className.split(' ').indexOf('el-dropdown') > -1) {
          el.cacheParentElement.parentNode.appendChild(el.cacheElement.parentNode)
          el.cacheParentElement.parentNode.style.display = ''; 
      } else if (el.className.indexOf('el-button') > -1) {
        if (el.cacheParentElement) {
          el.cacheParentElement.appendChild(el.cacheElement)
          el.cacheParentElement.style.display = ''; 
        }
      } else if (el.className.indexOf('el-dropdown-menu__item') > -1) {
        if (el.cacheElement) {
          el.cacheElement.style.display = '';
        }
      }
      // 有权限则追加之前缓存的 dom 元素
      // el.cacheParentElement && el.cacheParentElement.appendChild(el.cacheElement)
    }
  } else {
    throw new Error(`请设置操作权限标签值`)
  }
}

const hasPermi = {
  inserted: function (el, binding, vnode) {
    el.cacheElement = el // 缓存本节点
    el.cacheParentElement = el.parentNode // 缓存父节点
    fn(el, binding)
  },
  update: function (el, binding) {
    fn(el, binding)
  }
}

export default hasPermi
