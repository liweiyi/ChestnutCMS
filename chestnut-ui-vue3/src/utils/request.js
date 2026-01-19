import axios from 'axios'
import { ElNotification , ElMessageBox, ElMessage, ElLoading } from 'element-plus'
import errorCode from '@/utils/errorCode'
import { tansParams, blobValidate } from '@/utils/chestnut'
import cache from '@/plugins/cache'
import { saveAs } from 'file-saver'
import useUserStore from '@/store/modules/user'
import { i18n } from '@/i18n'
import cms from '@/plugins/cms'
import auth from '@/plugins/auth'
import router from '../router'

let downloadLoadingInstance
// 是否显示重新登录
export let isRelogin = { show: false }

axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'
// 创建axios实例
const service = axios.create({
  // axios中请求配置有baseURL选项，表示请求URL公共部分
  baseURL: import.meta.env.VITE_APP_BASE_API,
  // 超时
  timeout: 10000
})

// request拦截器
service.interceptors.request.use(config => {
  // 是否需要设置 token
  const isToken = (config.headers || {}).isToken === false
  // 是否忽略数据重复提交校验
  const ignoreRepeatSubmit = (config.headers || {}).ignoreRepeatSubmit === "true"
  if (auth.getHeaderToken() && !isToken) {
    // TOKEN + CMS当前站点
    config.headers = { ...config.headers, ...auth.getTokenHeader(), ...cms.currentSiteHeader() }
  }
  // 语言环境
  config.headers['Accept-Language'] = i18n.global.locale.value;
  // get请求映射params参数
  if (config.method === 'get' && config.params) {
    let url = config.url + '?' + tansParams(config.params)
    url = url.slice(0, -1)
    config.params = {}
    config.url = url
  }
  if (!ignoreRepeatSubmit && (config.method === 'post' || config.method === 'put')) {
    const requestObj = {
      url: config.url,
      data: typeof config.data === 'object' ? JSON.stringify(config.data) : config.data,
      time: new Date().getTime()
    }
    const requestSize = Object.keys(JSON.stringify(requestObj)).length // 请求数据大小
    const limitSize = 5 * 1024 * 1024 // 限制存放数据5M
    if (requestSize >= limitSize) {
      console.warn(`[${config.url}]: ` + '请求数据大小超出允许的5M限制，无法进行防重复提交验证。')
      return config
    }
    const sessionObj = cache.session.getJSON('sessionObj')
    if (sessionObj === undefined || sessionObj === null || sessionObj === '') {
      cache.session.setJSON('sessionObj', requestObj)
    } else {
      const s_url = sessionObj.url                // 请求地址
      const s_data = sessionObj.data              // 请求数据
      const s_time = sessionObj.time              // 请求时间
      const interval = 1000                       // 间隔时间(ms)，小于此时间视为重复提交
      if (s_data === requestObj.data && requestObj.time - s_time < interval && s_url === requestObj.url) {
        const message = i18n.global.t('Common.RepeatSubmit');
        console.warn(`[${s_url}]: ` + message)
        return Promise.reject(new Error(message))
      } else {
        cache.session.setJSON('sessionObj', requestObj)
      }
    }
  }
  return config
}, error => {
    console.log(error)
    Promise.reject(error)
})

// 响应拦截器
service.interceptors.response.use(res => {
    // 未设置状态码则默认成功状态
    const code = res.data.code || 200
    // 获取错误信息
    const msg = errorCode[code] || res.data.msg || errorCode['default']
    // 二进制数据则直接返回
    if (res.request.responseType ===  'blob' || res.request.responseType ===  'arraybuffer') {
      return res.data
    }
    if (code === 401) {
      if (!isRelogin.show) {
        isRelogin.show = true
        ElMessageBox.confirm(
          i18n.global.t('Common.SessionExpired'),
          i18n.global.t('Common.SystemTip'), 
          { 
            confirmButtonText: i18n.global.t('Common.Relogin'), 
            cancelButtonText: i18n.global.t('Common.Cancel'), 
            type: 'warning' 
          }
        ).then(() => {
          isRelogin.show = false
          useUserStore().logOut().then(() => {
            const redirect = location.pathname + location.search;
            window.location.href = `${import.meta.env.VITE_APP_PATH || '/'}login?redirect=${encodeURIComponent(redirect)}`
          })
        }).catch(() => {
          isRelogin.show = false
        })
      }
      return Promise.reject(i18n.global.t('Common.InvalidSession'))
    } else if (code === 500) {
      ElMessage({ message: msg, type: 'error' })
      return Promise.reject(new Error(msg))
    } else if (code === 601) {
      ElMessage({ message: msg, type: 'warning' })
      return Promise.reject(new Error(msg))
    } else if (code !== 200) {
      ElNotification.error({ title: msg })
      return Promise.reject('error')
    } else {
      return  Promise.resolve(res.data)
    }
  },
  error => {
    console.log('err' + error)
    let { message } = error
    if (message == "Network Error") {
      message = i18n.global.t('Common.ServerConnectFailed');
    } else if (message.includes("timeout")) {
      message = i18n.global.t('Common.ServerConnectTimeout');
    } else if (message.includes("Request failed with status code")) {
      message = i18n.global.t('Common.ServerApiError', [ message.substr(message.length - 3) ]);
    }
    ElMessage({ message: message, type: 'error', duration: 5 * 1000 })
    return Promise.reject(error)
  }
)

export function exportExcel(url, params, filename, config) {
  // url += "/export"
  const headers = { 'Content-Type': 'application/x-www-form-urlencoded', 'cc-export': 1 }
  return download0(url, params, filename, headers, config)
}

// 通用下载方法
export function download(url, params, filename, config) {
  const headers = { 'Content-Type': 'application/x-www-form-urlencoded' }
  return download0(url, params, filename, headers, config)
}

// 通用下载方法
export function download0(url, params, filename, headers, config) {
  downloadLoadingInstance = ElLoading.service({ text: i18n.global.t('Common.Downloading'), background: "rgba(0, 0, 0, 0.7)", })
  return service.post(url, params, {
    transformRequest: [(params) => { return tansParams(params) }],
    headers: headers,
    responseType: 'blob',
    ...config
  }).then(async (data) => {
    const isBlob = blobValidate(data)
    if (isBlob) {
      const blob = new Blob([data])
      saveAs(blob, filename)
    } else {
      const resText = await data.text()
      const rspObj = JSON.parse(resText)
      const errMsg = getResponseCodeErrMsg(rspObj.code, rspObj.msg)
      ElMessage.error(errMsg)
    }
    downloadLoadingInstance.close()
  }).catch((r) => {
    console.error(r)
    ElMessage.error(i18n.global.t('Common.DownloadFailed'))
    downloadLoadingInstance.close()
  })
}

export function jsonpRequest(url, callbackName) {

  return new Promise((resolve, reject) => {
    const script = document.createElement('script');
    script.src = `${url}?callback=${callbackName}`;
    window[callbackName] = (data) => {
      resolve(data);
      document.body.removeChild(script);
    };

    script.onerror = () => {
      reject(new Error('JSONP request failed'));
      document.body.removeChild(script);
    };
    document.body.appendChild(script);
  });
}

export default service
