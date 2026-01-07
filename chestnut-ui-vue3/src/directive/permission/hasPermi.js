 /**
 * v-hasPermi 权限指令
 */
import { checkPermi } from '@/utils/permission'

const fn = (el, binding, vnode) => {
  const { value } = binding
  const permissions = value
  const hasPermissions = checkPermi(permissions)
  // if (!hasPermissions) {
  //   el.parentNode && el.parentNode.removeChild(el)
  // }
  
  if (!hasPermissions) {
    el.classList.add('no-permi');
    // if (el.classList.contains('el-button') || el.classList.contains("btn-permi")) {
    //   el.cacheParentElement && el.cacheElement.parentNode == el.cacheParentElement && el.cacheParentElement.removeChild(el.cacheElement)
    // } else if (el.classList.contains('el-dropdown-menu__item')) {
    //   el.cacheElement.style.display = 'none';
    // }
  } else {
    el.classList.remove('no-permi');
    // if (el.classList.contains('el-button') || el.classList.contains("btn-permi")) {
    //   el.cacheParentElement && el.cacheElement.parentNode != el.cacheParentElement && el.cacheParentElement.appendChild(el.cacheElement)
    // } else if (el.classList.contains('el-dropdown-menu__item')) {
    //   if (el.cacheElement) {
    //     el.cacheElement.style.display = '';
    //   }
    // }
    // 有权限则追加之前缓存的 dom 元素
    // el.cacheParentElement && el.cacheParentElement.appendChild(el.cacheElement)
  }
}

const hasPermi = {
  mounted(el, binding, vnode) {
    // el.cacheElement = el // 缓存本节点
    // el.cacheParentElement = el.parentNode // 缓存父节点
    fn(el, binding, vnode)
  },
  updated(el, binding, vnode) {
    fn(el, binding, vnode)
  }
}

export default hasPermi