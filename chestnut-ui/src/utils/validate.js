import i18n from '@/i18n'

/**
 * @param {string} path
 * @returns {Boolean}
 */
export function isExternal(path) {
  return /^(https?:|mailto:|tel:)/.test(path)
}

/**
 * @param {string} str
 * @returns {Boolean}
 */
export function validUsername(str) {
  const reg = /^[A-Za-z][A-Za-z0-9_]+$/
  return reg.test(str)
}

export function userNameValidator(rule, value, callback) {
  if (!isBlank(value)) {
    if (!validUsername(value)) {
      callback(new Error(i18n.t('Common.RuleTips.UserName')));
    } else {
      callback();
    }
  } else {
    callback();
  }
}

/**
 * @param {string} url
 * @returns {Boolean}
 */
export function validURL(url) {
  const reg = /^(https?|ftp):\/\/([a-zA-Z0-9.-]+(:[a-zA-Z0-9.&%$-]+)*@)*((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]?)(\.(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])){3}|([a-zA-Z0-9-]+\.)*[a-zA-Z0-9-]+\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(:[0-9]+)*(\/($|[a-zA-Z0-9.,?'\\+&%$#=~_-]+))*$/
  return reg.test(url)
}

export function urlValidator(rule, value, callback) {
  if (!value || value.length == 0) {
    return callback(new Error(i18n.t('Common.RuleTips.NotEmpty')));
  }
  if (!validURL(value) && value.indexOf("iurl://") < 0) {
    return callback(new Error(i18n.t('Common.RuleTips.Url')));
  }
  callback();
}

/**
 * @param {string} str
 * @returns {Boolean}
 */
export function validLowerCase(str) {
  const reg = /^[a-z]+$/
  return reg.test(str)
}

/**
 * @param {string} str
 * @returns {Boolean}
 */
export function validUpperCase(str) {
  const reg = /^[A-Z]+$/
  return reg.test(str)
}

/**
 * @param {string} str
 * @returns {Boolean}
 */
export function validAlphabets(str) {
  const reg = /^[A-Za-z]+$/
  return reg.test(str)
}

/**
 * @param {string} email
 * @returns {Boolean}
 */
export function validEmail(email) {
  const reg = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
  return reg.test(email)
}

export function emailValidator(rule, value, callback) {
  if (!isBlank(value)) {
    if (!validEmail(value)) {
      return callback(new Error(i18n.t('Common.RuleTips.Email')));
    } else {
      callback();
    }
  } else {
    callback();
  }
}

export function validPhoneNumber(phoneNumber) {
  const reg = /^1[3|4|5|6|7|8|9][0-9]\d{8}$/
  return reg.test(phoneNumber)
}

export function phoneNumberValidator(rule, value, callback) {
  if (!isBlank(value)) {
    if (!validPhoneNumber(value)) {
      return callback(new Error(i18n.t('Common.RuleTips.PhoneNumber')));
    } else {
      callback();
    }
  } else {
    callback();
  }
}

export function isBlank(value) {
  return !value || value.trim().length == 0;
}

/**
 * @param {string} str
 * @returns {Boolean}
 */
export function isString(str) {
  if (typeof str === 'string' || str instanceof String) {
    return true
  }
  return false
}

/**
 * @param {Array} arg
 * @returns {Boolean}
 */
export function isArray(arg) {
  if (typeof Array.isArray === 'undefined') {
    return Object.prototype.toString.call(arg) === '[object Array]'
  }
  return Array.isArray(arg)
}

/**
 * 
 * @param {String} code 
 * @returns {Boolean}
 */
export function validCode(code) {
  const reg = /^[A-Za-z0-9_]+$/
  return reg.test(code);
}

export function codeValidator(rule, value, callback) {
  if (!isBlank(value)) {
    if (!validCode(value)) {
      callback(new Error(i18n.t('Common.RuleTips.Code')));
    } else {
      callback();
    }
  } else {
    callback();
  }
}

export function validPath(path) {
  const reg = /^[A-Za-z0-9_\/]+$/
  return reg.test(path)
}

export function pathValidator(rule, value, callback) {
  if (!isBlank(value)) {
    if (!validPath(value)) {
      callback(new Error(i18n.t('Common.RuleTips.Path')));
    } else {
      callback();
    }
  } else {
    callback();
  }
}

export function validFileName(path) {
  const reg = /^[A-Za-z0-9_\.]+$/
  return reg.test(path)
}

export function fileNameValidator(rule, value, callback) {
  if (!isBlank(value)) {
    if (!validPath(value)) {
      callback(new Error(i18n.t('Common.RuleTips.Code')));
    } else {
      callback();
    }
  } else {
    callback();
  }
}