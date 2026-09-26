import axios from 'axios'
import { ElNotification , ElMessageBox, ElMessage, ElLoading } from 'element-plus'
import errorCode from '@/utils/errorCode'
import { tansParams, blobValidate } from '@/utils/chestnut'
import { saveAs } from 'file-saver'
import useUserStore from '@/store/modules/user'
import { i18n } from '@/i18n'
import cms from '@/plugins/cms'
import auth from '@/plugins/auth'

let downloadLoadingInstance
// 是否显示重新登录
export let isRelogin = { show: false }

// 防重复提交：以 "method:url" 为 key，存储每个接口最近一次请求的 data 和 time
// 容量上限 100，超出时 FIFO 移除最旧的记录
// data 超过 DATA_COMPARE_LIMIT 时不存储内容，仅凭 method+url 判重
const PENDING_REQUEST_MAX = 100
const DATA_COMPARE_LIMIT = 100 * 1024 // 100KB
const pendingRequestMap = new Map()
const buildRequestKey = (config) => `${config.method}:${config.url}`
const setPendingRequest = (key, value) => {
  if (!pendingRequestMap.has(key) && pendingRequestMap.size >= PENDING_REQUEST_MAX) {
    pendingRequestMap.delete(pendingRequestMap.keys().next().value)
  }
  pendingRequestMap.set(key, value)
}

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
    const dataStr = typeof config.data === 'object' ? JSON.stringify(config.data) : config.data
    const key = buildRequestKey(config)
    const prev = pendingRequestMap.get(key)
    const interval = 1000 // 间隔时间(ms)，小于此时间视为重复提交
    if (prev && Date.now() - prev.time < interval) {
      // prev.data 为 null 表示上次是大数据请求，仅凭 method+url 判重；否则需数据内容也一致
      const dataMatch = prev.data === null || prev.data === dataStr
      if (dataMatch) {
        const message = i18n.global.t('Common.RepeatSubmit')
        console.warn(`[${config.url}]: ` + message)
        return Promise.reject(new Error(message))
      }
    }
    const isLargeData = dataStr && dataStr.length >= DATA_COMPARE_LIMIT
    setPendingRequest(key, { data: isLargeData ? null : dataStr, time: Date.now() })
  }
  return config
}, error => {
    console.log(error)
    Promise.reject(error)
})

// 响应拦截器
service.interceptors.response.use(res => {
    pendingRequestMap.delete(buildRequestKey(res.config))
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
            const redirect = location.pathname.substring(import.meta.env.VITE_APP_PATH.length) + location.search;
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
    } 
    else if (code !== 200) {
      ElNotification.error({ title: msg })
      return Promise.reject('error')
    } else {
      return  Promise.resolve(res.data)
    }
  },
  error => {
    if (error.config) {
      pendingRequestMap.delete(buildRequestKey(error.config))
    }
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