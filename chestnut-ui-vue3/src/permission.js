import router from './router'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import auth from '@/plugins/auth'
import { isHttp, isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'
import useUserStore from '@/store/modules/user'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'
import useSiteStore from '@/store/modules/site'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/register', '/loginRedirect']

const isWhiteList = (path) => {
  return whiteList.some(pattern => isPathMatch(pattern, path))
}

router.beforeEach(async (to, from, next) => {
  NProgress.start()
  if (auth.getToken()) {
    to.meta.title && useSettingsStore().setTitle(to.meta.title)
    /* has token*/
    if (to.path === '/login') {
      next({ path: '/' })
      NProgress.done()
    } else if (isWhiteList(to.path)) {
      next()
    } else {
      if (useUserStore().roles.length === 0) {
        isRelogin.show = true
        // 判断当前用户是否已拉取完user_info信息
        try {
          await useUserStore().getInfo()
        } catch (err) {
          isRelogin.show = false
          await useUserStore().logOut()
          ElMessage.error(err)
          next({ path: '/index' })
          return
        }
        isRelogin.show = false
        // 站点校正完成后再挂载业务页面，避免首次请求携带无权限的旧站点。
        // 初始化的 empty/error 状态由 CMS 页面处理，不走身份失败的登出分支。
        await useSiteStore().initialize()
        try {
          const accessRoutes = await usePermissionStore().generateRoutes()
          // 根据roles权限生成可访问的路由表
          accessRoutes.forEach(route => {
            if (!isHttp(route.path)) {
              router.addRoute(route) // 动态添加可访问路由表
            }
          })
          next({ ...to, replace: true }) // hack方法 确保addRoutes已完成
        } catch (err) {
          ElMessage.error(err)
          next(false)
          NProgress.done()
        }
      } else {
        if (['idle', 'loading'].includes(useSiteStore().status)) {
          await useSiteStore().initialize()
        }
        next()
      }
    }
  } else {
    // 没有token
    if (isWhiteList(to.path)) {
      // 在免登录白名单，直接进入
      next()
    } else {
      next(`/login?redirect=${to.fullPath}`) // 否则全部重定向到登录页
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})
