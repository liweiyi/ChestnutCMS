 /**
 * v-hasPermi 操作权限处理
 * Copyright (c) 2023 兮玥（190785909@qq.com）
 * 
 * 1、非el-button标签的按钮权限需要给对应控件添加class="btn-permi"
 * 2、表格操作列的多个按钮需要给按钮添加一个<span class="btn-cell-wrap"></span>包裹住用来占位，否则会影响按钮位置
 */
 
import store from '@/store'

function fn (el, binding) {
  const { value } = binding
  const all_permission = "*";
  const permissions = store.getters && store.getters.permissions

  if (!value || !value instanceof Array) {
    return
  }
  const permissionFlag = value.filter(v => v.length > 0)
  if (permissionFlag.length == 0) {
    return
  }

  const hasPermissions = permissions.some(permission => {
    return all_permission === permission || permissionFlag.includes(permission)
  })
  if (!hasPermissions) {
    if (el.classList.contains('el-button') || el.classList.contains("btn-permi")) {
      el.cacheParentElement && el.cacheElement.parentNode == el.cacheParentElement && el.cacheParentElement.removeChild(el.cacheElement)
    } else if (el.classList.contains('el-dropdown-menu__item')) {
      el.cacheElement.style.display = 'none';
    }
  } else {
    if (el.classList.contains('el-button') || el.classList.contains("btn-permi")) {
      el.cacheParentElement && el.cacheElement.parentNode != el.cacheParentElement && el.cacheParentElement.appendChild(el.cacheElement)
    } else if (el.classList.contains('el-dropdown-menu__item')) {
      if (el.cacheElement) {
        el.cacheElement.style.display = '';
      }
    }
    // 有权限则追加之前缓存的 dom 元素
    // el.cacheParentElement && el.cacheParentElement.appendChild(el.cacheElement)
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
