import axios from 'axios'
import { Notification, MessageBox, Message, Loading } from 'element-ui'
import store from '@/store'
import { getToken } from '@/utils/auth'
import { tansParams, blobValidate, getResponseCodeErrMsg } from "@/utils/chestnut";
import cache from '@/plugins/cache'
import { saveAs } from 'file-saver'
import i18n from '@/i18n'

let downloadLoadingInstance;
// 是否显示重新登录
export let isRelogin = { show: false };

axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'
// 创建axios实例
const service = axios.create({
  // axios中请求配置有baseURL选项，表示请求URL公共部分
  baseURL: process.env.VUE_APP_BASE_API,
  // 超时
  timeout: 30000
})

// request拦截器
service.interceptors.request.use(config => {
  // 是否需要设置 token
  const nonToken = (config.headers || {}).isToken === false
  // 是否需要防止数据重复提交
  const isRepeatSubmit = (config.headers || {}).repeatSubmit === false
  if (getToken() && !nonToken) {
    config.headers['Authorization'] = 'Bearer ' + getToken() // 让每个请求携带自定义token 请根据实际情况自行修改
    // CMS当前站点
    if (cache.local.get('CurrentSite')) {
      config.headers['CurrentSite'] = cache.local.get('CurrentSite'); 
    }
  }
  // 语言环境
  config.headers['Accept-Language'] = i18n.locale;
  // get请求映射params参数
  if (config.method === 'get' && config.params) {
    let url = config.url + '?' + tansParams(config.params);
    url = url.slice(0, -1);
    config.params = {};
    config.url = url;
  }
  if (!isRepeatSubmit && (config.method === 'post' || config.method === 'put')) {
    const requestObj = {
      url: config.url,
      data: typeof config.data === 'object' ? JSON.stringify(config.data) : config.data,
      time: new Date().getTime()
    }
    const sessionObj = cache.session.getJSON('sessionObj')
    if (sessionObj === undefined || sessionObj === null || sessionObj === '') {
      cache.session.setJSON('sessionObj', requestObj)
    } else {
      const s_url = sessionObj.url;                  // 请求地址
      const s_data = sessionObj.data;                // 请求数据
      const s_time = sessionObj.time;                // 请求时间
      const interval = 1000;                         // 间隔时间(ms)，小于此时间视为重复提交
      if (s_data === requestObj.data && requestObj.time - s_time < interval && s_url === requestObj.url) {
        const message = i18n.t('Common.RepeatSubmit');
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
    const code = res.data.code || 200;
    // 获取错误信息
    const msg = getResponseCodeErrMsg(res.data.code, res.data.msg)
    // 二进制数据则直接返回
    if(res.request.responseType ===  'blob' || res.request.responseType ===  'arraybuffer'){
      return res.data
    }
    if (code === 401) {
      if (!isRelogin.show) {
          isRelogin.show = true;
          MessageBox.confirm(i18n.t('Common.SessionExpired'), i18n.t('Common.SystemTip'), 
              { confirmButtonText: i18n.t('Common.Relogin'), cancelButtonText: i18n.t('Common.Cancel'), type: 'warning' }).then(() => {
            isRelogin.show = false;
            store.dispatch('LogOut').then(() => {
              location.href = process.env.VUE_APP_PATH + 'index';
            })
        }).catch(() => {
          isRelogin.show = false;
        });
      }
      return Promise.reject(i18n.t('Common.InvalidSession'))
    } else if (code === 500) {
      Message({ message: msg, type: 'error' })
      return Promise.reject(new Error(msg))
    } else if (code === 601) {
      Message({ message: msg, type: 'warning' })
      return Promise.reject('error')
    } else if (code !== 200) {
      Notification.error({ title: msg })
      return Promise.reject('error')
    } else {
      return res.data
    }
  }, error => {
    console.log('err' + error)
    let { message } = error;
    if (message == "Network Error") {
      message = i18n.t('Common.ServerConnectFailed');
    } else if (message.includes("timeout")) {
      message = i18n.t('Common.ServerConnectTimeout');
    } else if (message.includes("Request failed with status code")) {
      message = i18n.t('Common.ServerApiError', [ message.substr(message.length - 3) ]);
    }
    Message({ message: message, type: 'error', duration: 5 * 1000 })
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
function download0(url, params, filename, headers, config) {
  downloadLoadingInstance = Loading.service({ text: i18n.t('Common.Downloading'), spinner: "el-icon-loading", background: "rgba(0, 0, 0, 0.7)", })
  return service.post(url, params, {
    transformRequest: [(params) => { return tansParams(params) }],
    headers: headers,
    responseType: 'blob',
    ...config
  }).then(async (data) => {
    const isLogin = await blobValidate(data);
    if (isLogin) {
      const blob = new Blob([data])
      saveAs(blob, filename)
    } else {
      const resText = await data.text();
      const rspObj = JSON.parse(resText);
      const errMsg = getResponseCodeErrMsg(rspObj.code, rspObj.msg)
      Message.error(errMsg);
    }
    downloadLoadingInstance.close();
  }).catch((r) => {
    console.error(r)
    Message.error(i18n.t('Common.DownloadFailed'))
    downloadLoadingInstance.close();
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
