import store from '@/store'

/**
 * 字符权限校验
 * @param {Array} value 校验值
 * @returns {Boolean}
 */
export function checkPermi(value) {
  if (value && value instanceof Array && value.length > 0) {
    const permissions = store.getters && store.getters.permissions
    const permissionDatas = value
    const all_permission = "*";

    const hasPermission = permissions.some(permission => {
      return all_permission === permission || permissionDatas.includes(permission)
    })

    if (!hasPermission) {
      return false
    }
    return true
  } else {
    console.error(`need roles! Like checkPermi="['system:user:add','system:user:edit']"`)
    return false
  }
}

/**
 * 角色权限校验
 * @param {Array} value 校验值
 * @returns {Boolean}
 */
export function checkRole(value) {
  if (value && value instanceof Array && value.length > 0) {
    const roles = store.getters && store.getters.roles
    const permissionRoles = value
    const super_admin = "admin";

    const hasRole = roles.some(role => {
      return super_admin === role || permissionRoles.includes(role)
    })

    if (!hasRole) {
      return false
    }
    return true
  } else {
    console.error(`need roles! Like checkRole="['admin','editor']"`)
    return false
  }
}

/**
 * 权限字符串解析，处理占位符。
 *
 * 支持：
 * parsePermi('Site:Edit:{siteId}', { 'siteId': 1234 })
 * parsePermi('Site:Edit:{0}', [ 1234 ])
 *
 * @param {String} value 权限字符串
 * @param {Array|Object} replacements 占位符替换值
 * @returns {Boolean}
 */
export function parsePermi(value, replacements) {
  if (replacements) {
    if (replacements instanceof Array && replacements.length > 0) {
      for(let i = 0; i < replacements.length; i++) {
        let replacement = replacements[i]
        if (!replacement || replacement.length == 0) {
          return ""
        }
        value = value.replace(new RegExp("\\{" + i + "\\}", "g"), replacement)
      }
    } else if (replacements instanceof Object && Object.keys(replacements).length > 0) {
      Object.keys(replacements).forEach(key => {
        let replacement = replacements[key]
        if (!replacement || replacement.length == 0) {
          return ""
        }
        value = value.replace(new RegExp("\\{" + key + "\\}", "g"), replacement)
      })
    }
  }
  return value;
}
