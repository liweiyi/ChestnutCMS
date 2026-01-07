import request from '@/utils/request'

// 登录方法
export function login(data) {
  return request({
    url: '/login',
    headers: {
      isToken: false
    },
    method: 'post',
    data: data
  })
}

// 注册方法
export function register(data) {
  return request({
    url: '/register',
    headers: {
      isToken: false
    },
    method: 'post',
    data: data
  })
}

// 获取用户详细信息
export function getInfo() {
  return request({
    url: '/getInfo',
    method: 'get'
  })
}

// 退出方法
export function logout() {
  return request({
    url: '/logout',
    method: 'post'
  })
}

// 获取验证码配置
export function getLoginCaptchaConfig() {
  return request({
    url: '/captcha/config',
    headers: {
      isToken: false
    },
    method: 'get',
    timeout: 20000
  })
}

export function checkUsername(username) {
  return request({
    url: '/checkUsername',
    headers: {
      isToken: false
    },
    method: 'get',
    params: { username: username }
  })
}

// 获取登录配置
export function getLoginConfig() {
  return request({
    url: '/login/config',
    headers: {
      isToken: false
    },
    method: 'get',
    timeout: 20000,
  })
}

