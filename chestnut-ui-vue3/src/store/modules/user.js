import router from '@/router'
import { i18n } from '@/i18n'
import { ElMessageBox, } from 'element-plus'
import { login, logout, getInfo } from '@/api/login'
import auth from '@/plugins/auth'
import { isHttp, isEmpty } from "@/utils/validate"
import defAva from '@/assets/images/profile.jpg'

const useUserStore = defineStore(
  'user',
  {
    state: () => ({
      token: auth.getToken(),
      id: '',
      name: '',
      nickName: '',
      avatar: '',
      roles: [],
      permissions: [],
      productEdition: 'Enterprise'
    }),
    actions: {
      // 登录
      login(userInfo) {
        userInfo.username = userInfo.username.trim()
        return new Promise((resolve, reject) => {
          login(userInfo).then(res => {
            auth.setToken(res.data)
            this.token = res.data
            resolve()
          }).catch(error => {
            reject(error)
          })
        })
      },
      tokenLogin(token) {
        auth.setToken(token)
        this.token = token
      },
      // 获取用户信息
      getInfo() {
        return new Promise((resolve, reject) => {
          getInfo().then(res => {
            const user = res.data.user
            let avatar = user.avatarSrc || ""
            if (!isHttp(avatar)) {
              avatar = (isEmpty(avatar)) ? defAva : import.meta.env.VITE_APP_BASE_API + avatar
            }
            if (res.data.roles && res.data.roles.length > 0) { // 验证返回的roles是否是一个非空数组
              this.roles = res.data.roles
            } else {
              this.roles = ['ROLE_DEFAULT']
            }
            if (res.data.permissions && res.data.permissions.length > 0) {
              this.permissions = res.data.permissions
            }
            this.id = user.userId
            this.name = user.userName
            this.nickName = user.nickName
            this.avatar = avatar
            /* 初始密码提示 */
            if(user.forceModifyPassword === 'Y') {
              ElMessageBox.confirm(
                i18n.global.t('System.Security.ForceModifyPwd'), 
                i18n.global.t('System.Security.SecurityTips'), 
                {  
                  confirmButtonText: i18n.global.t('Common.Confirm'),  
                  cancelButtonText: i18n.global.t('Common.Cancel'),  
                  type: 'warning' 
                }
              ).then(() => {
                router.push({ name: 'Profile', params: { activeTab: 'resetPwd' } })
              }).catch(() => {})
            }
            /* 过期密码提示 */
            if (user.passwordExpireSeconds > 0) {
              const lastModifyTime = user.passwordModifyTime || user.createTime
              const expireTime = new Date(lastModifyTime).getTime() + user.passwordExpireSeconds * 24 * 60 * 60 * 1000
              const now = new Date().getTime()
              if (now > expireTime) {
                ElMessageBox.confirm(
                  i18n.global.t('System.Security.PwdExpired'), 
                  i18n.global.t('System.Security.SecurityTips'), 
                  {  
                    confirmButtonText: i18n.global.t('Common.Confirm'),  
                    cancelButtonText: i18n.global.t('Common.Cancel'),  
                    type: 'warning' 
                  }
                ).then(() => {
                  router.push({ name: 'Profile', params: { activeTab: 'resetPwd' } })
                }).catch(() => {})
              }
            }
            resolve(res)
          }).catch(error => {
            reject(error)
          })
        })
      },
      // 退出系统
      logOut() {
        return new Promise((resolve, reject) => {
          logout(this.token).then(() => {
            this.token = ''
            this.roles = []
            this.permissions = []
            auth.removeToken()
            resolve()
          }).catch(error => {
            console.log('user.logOut', error)
            reject(error)
          })
        })
      }
    }
  })

export default useUserStore
