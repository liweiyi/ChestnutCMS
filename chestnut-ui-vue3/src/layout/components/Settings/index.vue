<template>
  <el-drawer v-model="showSettings" :withHeader="false" :lock-scroll="false" direction="rtl" size="300px">
    <div class="setting-drawer-title">
      <h3 class="drawer-title">{{ $t('Component.Layout.Settings.ThemeStyle') }}</h3>
    </div>
    <div class="setting-drawer-block-checbox">
      <div class="setting-drawer-block-checbox-item" @click="handleTheme('theme-dark')">
        <img src="@/assets/images/dark.svg" :alt="$t('Component.Layout.Settings.Dark')" />
        <div v-if="sideTheme === 'theme-dark'" class="setting-drawer-block-checbox-selectIcon" style="display: block;">
          <i :aria-label="$t('Common.Selected')" class="anticon anticon-check">
            <svg viewBox="64 64 896 896" data-icon="check" width="1em" height="1em" :fill="theme" aria-hidden="true" focusable="false" class>
              <path d="M912 190h-69.9c-9.8 0-19.1 4.5-25.1 12.2L404.7 724.5 207 474a32 32 0 0 0-25.1-12.2H112c-6.7 0-10.4 7.7-6.3 12.9l273.9 347c12.8 16.2 37.4 16.2 50.3 0l488.4-618.9c4.1-5.1.4-12.8-6.3-12.8z" />
            </svg>
          </i>
        </div>
      </div>
      <div class="setting-drawer-block-checbox-item" @click="handleTheme('theme-light')">
        <img src="@/assets/images/light.svg" :alt="$t('Component.Layout.Settings.Light')" />
        <div v-if="sideTheme === 'theme-light'" class="setting-drawer-block-checbox-selectIcon" style="display: block;">
          <i :aria-label="$t('Common.Selected')" class="anticon anticon-check">
            <svg viewBox="64 64 896 896" data-icon="check" width="1em" height="1em" :fill="theme" aria-hidden="true" focusable="false" class>
              <path d="M912 190h-69.9c-9.8 0-19.1 4.5-25.1 12.2L404.7 724.5 207 474a32 32 0 0 0-25.1-12.2H112c-6.7 0-10.4 7.7-6.3 12.9l273.9 347c12.8 16.2 37.4 16.2 50.3 0l488.4-618.9c4.1-5.1.4-12.8-6.3-12.8z" />
            </svg>
          </i>
        </div>
      </div>
    </div>
    <div class="drawer-item">
      <span>{{ $t('Component.Layout.Settings.ThemeColor') }}</span>
      <span class="comp-style">
        <el-color-picker v-model="theme" :predefine="predefineColors" @change="themeChange"/>
      </span>
    </div>
    <el-divider />

    <h3 class="drawer-title">{{ $t('Component.Layout.Settings.SystemLayoutConfig') }}</h3>

    <div class="drawer-item">
      <span>{{ $t('Component.Layout.Settings.EnableTopNav') }}</span>
      <span class="comp-style">
        <el-switch v-model="settingsStore.topNav" @change="topNavChange" class="drawer-switch" />
      </span>
    </div>

    <div class="drawer-item">
      <span>{{ $t('Component.Layout.Settings.EnableTagsViews') }}</span>
      <span class="comp-style">
        <el-switch v-model="settingsStore.tagsView" class="drawer-switch" />
      </span>
    </div>

    <div class="drawer-item">
      <span>{{ $t('Component.Layout.Settings.ShowTagsIcon') }}</span>
      <span class="comp-style">
        <el-switch v-model="settingsStore.tagsIcon" :disabled="!settingsStore.tagsView" class="drawer-switch" />
      </span>
    </div>

    <div class="drawer-item">
      <span>{{ $t('Component.Layout.Settings.EnableFixedHeader') }}</span>
      <span class="comp-style">
        <el-switch v-model="settingsStore.fixedHeader" class="drawer-switch" />
      </span>
    </div>

    <div class="drawer-item">
      <span>{{ $t('Component.Layout.Settings.ShowLogo') }}</span>
      <span class="comp-style">
        <el-switch v-model="settingsStore.sidebarLogo" class="drawer-switch" />
      </span>
    </div>

    <div class="drawer-item">
      <span>{{ $t('Component.Layout.Settings.EnableDynamicTitle') }}</span>
      <span class="comp-style">
        <el-switch v-model="settingsStore.dynamicTitle" @change="dynamicTitleChange" class="drawer-switch" />
      </span>
    </div>

    <div class="drawer-item">
      <span>{{ $t('Component.Layout.Settings.ShowFooter') }}</span>
      <span class="comp-style">
        <el-switch v-model="settingsStore.footerVisible" class="drawer-switch" />
      </span>
    </div>

    <el-divider />

    <el-button type="primary" plain icon="DocumentAdd" @click="saveSetting">{{ $t('Component.Layout.Settings.Save') }}</el-button>
    <el-button plain icon="Refresh" @click="resetSetting">{{ $t('Component.Layout.Settings.Reset') }}</el-button>
  </el-drawer>

</template>

<script setup>
import useAppStore from '@/store/modules/app'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'
import { handleThemeStyle } from '@/utils/theme'

const { proxy } = getCurrentInstance()
const appStore = useAppStore()
const settingsStore = useSettingsStore()
const permissionStore = usePermissionStore()
const showSettings = ref(false)
const theme = ref(settingsStore.theme)
const sideTheme = ref(settingsStore.sideTheme)
const storeSettings = computed(() => settingsStore)
const predefineColors = ref(["#409EFF", "#ff4500", "#ff8c00", "#ffd700", "#90ee90", "#00ced1", "#1e90ff", "#c71585"])

/** 是否需要topnav */
function topNavChange(val) {
  if (!val) {
    appStore.toggleSideBarHide(false)
    permissionStore.setSidebarRouters(permissionStore.defaultRoutes)
  }
}

/** 是否需要dynamicTitle */
function dynamicTitleChange() {
  useSettingsStore().setTitle(useSettingsStore().title)
}

function themeChange(val) {
  settingsStore.theme = val
  handleThemeStyle(val)
}

function handleTheme(val) {
  settingsStore.sideTheme = val
  sideTheme.value = val
}

function saveSetting() {
  proxy.$modal.loading(proxy.$t('Component.Layout.Settings.Saving'))
  let layoutSetting = {
    "topNav": storeSettings.value.topNav,
    "tagsView": storeSettings.value.tagsView,
    "tagsIcon": storeSettings.value.tagsIcon,
    "fixedHeader": storeSettings.value.fixedHeader,
    "sidebarLogo": storeSettings.value.sidebarLogo,
    "dynamicTitle": storeSettings.value.dynamicTitle,
    "footerVisible": storeSettings.value.footerVisible,
    "sideTheme": storeSettings.value.sideTheme,
    "theme": storeSettings.value.theme
  }
  localStorage.setItem("layout-setting", JSON.stringify(layoutSetting))
  setTimeout(() => proxy.$modal.closeLoading(), 1000)
}

function resetSetting() {
  proxy.$modal.loading(proxy.$t('Component.Layout.Settings.Resetting'))
  localStorage.removeItem("layout-setting")
  setTimeout(() => window.location.reload(), 1000)
}

function openSetting() {
  showSettings.value = true
}

defineExpose({
  openSetting
})
</script>

<style lang='scss' scoped>
.setting-drawer-title {
  margin-bottom: 12px;
  color: var(--el-text-color-primary, rgba(0, 0, 0, 0.85));
  line-height: 22px;
  font-weight: bold;

  .drawer-title {
    font-size: 14px;
  }
}

.setting-drawer-block-checbox {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin-top: 10px;
  margin-bottom: 20px;

  .setting-drawer-block-checbox-item {
    position: relative;
    margin-right: 16px;
    border-radius: 2px;
    cursor: pointer;

    img {
      width: 48px;
      height: 48px;
    }

    .setting-drawer-block-checbox-selectIcon {
      position: absolute;
      top: 0;
      right: 0;
      width: 100%;
      height: 100%;
      padding-top: 15px;
      padding-left: 24px;
      color: #1890ff;
      font-weight: 700;
      font-size: 14px;
    }
  }
}

.drawer-item {
  color: var(--el-text-color-regular, rgba(0, 0, 0, 0.65));
  padding: 12px 0;
  font-size: 14px;

  .comp-style {
    float: right;
    margin: -3px 8px 0px 0px;
  }
}
</style>