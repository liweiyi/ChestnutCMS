<template>
  <div class="navbar">
    <hamburger id="hamburger-container" :is-active="sidebar.opened" class="hamburger-container" @toggleClick="toggleSideBar" />

    <breadcrumb id="breadcrumb-container" class="breadcrumb-container" v-if="!topNav"/>
    <top-nav id="topmenu-container" class="topmenu-container" v-if="topNav"/>

    <div class="right-menu">
      <template v-if="device!=='mobile'">
        <publish-task class="right-menu-item hover-effect"></publish-task>
        <search id="header-search" class="right-menu-item" />

        <el-tooltip :content="$t('Component.Layout.Navbar.FullScreen')" effect="dark" placement="bottom">
          <screenfull id="screenfull" class="right-menu-item hover-effect" />
        </el-tooltip>

        <el-tooltip :content="$t('Component.Layout.Navbar.LayoutSize')" effect="dark" placement="bottom">
          <size-select id="size-select" class="right-menu-item hover-effect" />
        </el-tooltip>

        <el-tooltip :content="$t('Component.Layout.Navbar.Language')" effect="dark" placement="bottom">
          <language-select id="language-select" class="right-menu-item hover-effect" />
        </el-tooltip>

        <current-site id="current-site" class="right-menu-item" />
      </template>

      <el-dropdown class="avatar-container right-menu-item hover-effect" trigger="click">
        <div class="avatar-wrapper">
          <el-image :src="avatar" class="user-avatar">
            <div slot="error" class="image-slot">
              <img src="@/assets/images/default_banner_avatar.png" class="user-avatar" />
            </div>
          </el-image>
          <i class="el-icon-caret-bottom" />
        </div>
        <el-dropdown-menu slot="dropdown">
          <router-link to="/user/profile">
            <el-dropdown-item>{{ $t('Router.AccountCenter') }}</el-dropdown-item>
          </router-link>
          <router-link to="/user/preference">
            <el-dropdown-item>{{ $t('Router.UserPreference') }}</el-dropdown-item>
          </router-link>
          <el-dropdown-item @click.native="setting = true">
            <span>{{ $t('Router.LayoutSetting') }}</span>
          </el-dropdown-item>
          <el-dropdown-item divided @click.native="logout">
            <span>{{ $t('Router.Logout') }}</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
  </div>
</template>

<script>
import { checkSecurityConfig } from "@/api/system/security";

import { mapGetters } from 'vuex'
import Breadcrumb from '@/components/Breadcrumb'
import TopNav from '@/components/TopNav'
import Hamburger from '@/components/Hamburger'
import Screenfull from '@/components/Screenfull'
import SizeSelect from '@/components/SizeSelect'
import Search from '@/components/HeaderSearch'
import ChestnutCMSGit from '@/components/ChestnutCMS/Git'
import ChestnutCMSDoc from '@/components/ChestnutCMS/Doc'
import LanguageSelect from '@/components/LanguageSelect'
import CurrentSite from '@/components/CurrentSite'
import PublishTask from '@/components/PublishTask'

export default {
  components: {
    Breadcrumb,
    TopNav,
    Hamburger,
    Screenfull,
    SizeSelect,
    Search,
    ChestnutCMSGit,
    ChestnutCMSDoc,
    LanguageSelect,
    CurrentSite,
    PublishTask,
  },
  data() {
    return {
      securityConfig: {}
    }
  },
  computed: {
    ...mapGetters([
      'sidebar',
      'avatar',
      'device'
    ]),
    setting: {
      get() {
        return this.$store.state.settings.showSettings
      },
      set(val) {
        this.$store.dispatch('settings/changeSetting', {
          key: 'showSettings',
          value: val
        })
      }
    },
    topNav: {
      get() {
        return this.$store.state.settings.topNav
      }
    }
  },
  created() {
    this.loadSecurityConfig();
  },
  methods: {
    loadSecurityConfig() {
      checkSecurityConfig().then(res => {
        if (res.data) {
          this.securityConfig = res.data;
          if (this.securityConfig.forceModifyPassword) {
            checkResetPassword(this.$t('System.Security.ForceModifyPwd'))
          } else if (this.securityConfig.isPasswordExpired) {
            checkResetPassword(this.$t('System.Security.PwdExpired'))
          }
        }
      });
    },
    checkResetPassword(msg) {
      this.$alert(msg, {
          dangerouslyUseHTMLString: true,
          showClose: false,
          closeOnClickModal: false,
          closeOnPressEscape: false,
          closeOnHashChange: false,
          confirmButtonText: this.$t('Common.Confirm'),
          callback: action => {
            this.$router.push({ path: "/user/profile", query: { tab: "resetPwd" } });
          }
        });
    },
    toggleSideBar() {
      this.$store.dispatch('app/toggleSideBar')
    },
    async logout() {
      this.$confirm(this.$t('Component.Layout.Navbar.ConfirmLogoutTip'), this.$t('Common.SystemTip'), {
        confirmButtonText: this.$t('Common.Yes'),
        cancelButtonText: this.$t('Common.No'),
        type: 'warning'
      }).then(() => {
        this.$store.dispatch('LogOut').then(() => {
          location.href = process.env.VUE_APP_PATH + 'index';
          // location.reload();
        })
      }).catch(() => {});
    }
  }
}
</script>

<style lang="scss" scoped>
.navbar {
  height: 50px;
  overflow: hidden;
  position: relative;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);

  .hamburger-container {
    line-height: 46px;
    height: 100%;
    float: left;
    cursor: pointer;
    transition: background .3s;
    -webkit-tap-highlight-color:transparent;

    &:hover {
      background: rgba(0, 0, 0, .025)
    }
  }

  .breadcrumb-container {
    float: left;
  }

  .topmenu-container {
    position: absolute;
    left: 50px;
  }

  .errLog-container {
    display: inline-block;
    vertical-align: top;
  }

  .right-menu {
    float: right;
    height: 100%;
    line-height: 50px;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-block;
      padding: 0 8px;
      height: 100%;
      font-size: 18px;
      color: #5a5e66;
      vertical-align: text-bottom;

      &.hover-effect {
        cursor: pointer;
        transition: background .3s;

        &:hover {
          background: rgba(0, 0, 0, .025)
        }
      }
    }

    .avatar-container {
      margin-right: 30px;

      .avatar-wrapper {
        margin-top: 5px;
        position: relative;

        .user-avatar {
          cursor: pointer;
          width: 40px;
          height: 40px;
          border-radius: 10px;
        }

        .el-icon-caret-bottom {
          cursor: pointer;
          position: absolute;
          right: -20px;
          top: 25px;
          font-size: 12px;
        }
      }
    }
  }
}
</style>