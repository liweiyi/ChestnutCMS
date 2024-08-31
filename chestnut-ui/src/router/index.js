import Vue from 'vue'
import Router from 'vue-router'
import i18n from '@/i18n'

Vue.use(Router)

/* Layout */
import Layout from '@/layout'

/**
 * Note: 路由配置项
 *
 * hidden: true                     // 当设置 true 的时候该路由不会再侧边栏出现 如401，login等页面，或者如一些编辑页面/edit/1
 * alwaysShow: true                 // 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
 *                                  // 只有一个时，会将那个子路由当做根路由显示在侧边栏--如引导页面
 *                                  // 若你想不管路由下面的 children 声明的个数都显示你的根路由
 *                                  // 你可以设置 alwaysShow: true，这样它就会忽略之前定义的规则，一直显示根路由
 * redirect: noRedirect             // 当设置 noRedirect 的时候该路由在面包屑导航中不可被点击
 * name:'router-name'               // 设定路由的名字，一定要填写不然使用<keep-alive>时会出现各种问题
 * query: '{"id": 1, "name": "ry"}' // 访问路由的默认传递参数
 * roles: ['admin', 'common']       // 访问路由的角色权限
 * permissions: ['a:a:a', 'b:b:b']  // 访问路由的菜单权限
 * meta : {
    noCache: true                   // 如果设置为true，则不会被 <keep-alive> 缓存(默认 false)
    title: 'title'                  // 设置该路由在侧边栏和面包屑中展示的名字
    icon: 'svg-name'                // 设置该路由的图标，对应路径src/assets/icons/svg
    breadcrumb: false               // 如果设置为false，则不会在breadcrumb面包屑中显示
    activeMenu: '/system/user'      // 当路由设置了该属性，则会高亮相对应的侧边栏。
  }
 */

// 公共路由
export const constantRoutes = [
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect')
      }
    ]
  },
  {
    path: '/login',
    component: () => import('@/views/login'),
    hidden: true
  },
  {
    path: '/register',
    component: () => import('@/views/register'),
    hidden: true
  },
  {
    path: '/404',
    component: () => import('@/views/error/404'),
    hidden: true
  },
  {
    path: '/401',
    component: () => import('@/views/error/401'),
    hidden: true
  },
  {
    path: '',
    component: Layout,
    redirect: 'index',
    children: [
      {
        path: 'index',
        component: () => import('@/views/index'),
        name: 'Index',
        meta: { title: i18n.t('Router.Home'), icon: 'dashboard', affix: true }
      }
    ]
  },
  {
    path: '/user',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'profile',
        component: () => import('@/views/system/user/profile/index'),
        name: 'Profile',
        meta: { title: i18n.t('Router.AccountCenter'), icon: 'user' }
      },
      {
        path: 'preference',
        component: () => import('@/views/system/user/userPreference'),
        name: 'UserPreference',
        meta: { title: i18n.t('Router.UserPreference'), icon: 'user' }
      }
    ]
  },
  {
    path: '/cms/preview',
    component: () => import('@/views/cms/contentcore/preview'),
    hidden: true,
    name: 'ContentCorePreview'
  },
  {
    path: '/cms/content/editorW',
    component: () => import('@/views/cms/contentcore/contentEditor'),
    hidden: true,
    name: 'CMSContentEditorW',
    meta: { title: i18n.t('CMS.ContentCore.Route.EditContent') }
  },
  {
    path: '/cms',
    component: Layout,
    hidden: true,
    redirect: '/cms/site',
    children: [
      {
        path: 'site/tabs',
        component: () => import('@/views/cms/contentcore/siteTab'),
        name: 'CMSSiteTab',
        meta: { noCache: true, title: i18n.t('CMS.ContentCore.Route.EditSite'), activeMenu: '/configs/site'}
      },
      {
        path: 'template/editor',
        component: () => import('@/views/cms/contentcore/templateEditor'),
        name: 'CMSTemplateEditor',
        meta: { noCache: true, title: i18n.t('CMS.ContentCore.Route.EditTemplate'), activeMenu: '/configs/template'}
      },
      {
        path: 'file/editor',
        component: () => import('@/views/cms/contentcore/fileEditor'),
        name: 'CMSFileEditor',
        meta: { noCache: true, title: i18n.t('CMS.ContentCore.Route.EditFile'), activeMenu: '/configs/file'}
      },
      {
        path: 'content/editor',
        component: () => import('@/views/cms/contentcore/contentEditor'),
        name: 'CMSContentEditor',
        meta: { noCache: true, title: i18n.t('CMS.ContentCore.Route.EditContent'), activeMenu: '/configs/content'}
      },
      {
        path: 'exmodel/fields',
        component: () => import('@/views/meta/fieldList'),
        name: 'CMSEXModelFields',
        meta: { noCache: true, title: i18n.t('CMS.ExModel.RouteExModelField'), activeMenu: '/configs/exmodel'}
      },
      {
        path: 'block/manual/editor',
        component: () => import('@/views/cms/block/manualBlockEditor'),
        name: 'CMSBlockManualEditor',
        meta: { noCache: true, title: i18n.t('CMS.ContentCore.Route.EditManualBlock'), activeMenu: '/configs/content'}
      },
      {
        path: 'adspace/editor',
        component: () => import('@/views/cms/ad/adSpaceEditor'),
        name: 'CMSAdSpaceEditor',
        meta: { noCache: true, title: i18n.t('CMS.ContentCore.Route.EditAdvSpace'), activeMenu: '/operations/advertisement'}
      },
      {
        path: 'ad/editor',
        component: () => import('@/views/cms/ad/advertisementEditor'),
        name: 'CMSAdvertisementEditor',
        meta: { noCache: true, title: i18n.t('CMS.ContentCore.Route.EditAdv'), activeMenu: '/operations/advertisement'}
      }
    ]
  },
  {
    path: '/operations',
    component: Layout,
    hidden: true,
    redirect: '/operations/link',
    children: [
      {
        path: 'link/list',
        component: () => import('@/views/cms/link/link'),
        name: 'CmsLink',
        meta: { noCache: true, title: i18n.t('CMS.FriendLink.RouteLinkList'), activeMenu: '/operations/link'}
      },
      {
        path: 'customform/fields',
        component: () => import('@/views/meta/fieldList'),
        name: 'CmsCustomFields',
        meta: { noCache: true, title: i18n.t('CMS.CustomForm.RouteFieldList'), activeMenu: '/operations/customform'}
      },
      {
        path: 'customform/data',
        component: () => import('@/views/cms/customform/data'),
        name: 'CmsCustomData',
        meta: { noCache: true, title: i18n.t('CMS.CustomForm.RouteData'), activeMenu: '/operations/customform'}
      }
    ]
  }
]

// 动态路由，基于用户权限动态去加载
export const dynamicRoutes = [
  {
    path: '/system/role-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:role:edit'],
    children: [
      {
        path: 'user/:roleId(\\d+)',
        component: () => import('@/views/system/role/authUser'),
        name: 'AuthUser',
        meta: { title: i18n.t('System.Role.UserSetting'), activeMenu: '/system/role' }
      }
    ]
  },
  {
    path: '/system/dict-data',
    component: Layout,
    hidden: true,
    permissions: ['system:dict:view'],
    children: [
      {
        path: 'index/:dictId(\\d+)',
        component: () => import('@/views/system/dict/data'),
        name: 'Data',
        meta: { title: i18n.t('System.Dict.DataList'), activeMenu: '/system/dict' }
      }
    ]
  },
  {
    path: '/tool/gen-edit',
    component: Layout,
    hidden: true,
    permissions: ['tool:gen:edit'],
    children: [
      {
        path: 'index/:tableId(\\d+)',
        component: () => import('@/views/tool/gen/editTable'),
        name: 'GenEdit',
        meta: { title: i18n.t('System.GenCode.EditGenConfig'), activeMenu: '/tool/gen' }
      }
    ]
  }
]

// 防止连续点击多次路由报错
let routerPush = Router.prototype.push;
Router.prototype.push = function push(location) {
  return routerPush.call(this, location).catch(err => err)
}

export default new Router({
  base: process.env.VUE_APP_PATH,
  mode: 'history', // 去掉url中的#
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
})