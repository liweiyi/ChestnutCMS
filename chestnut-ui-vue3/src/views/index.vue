<template>
  <div class="app-container">
    <el-row :gutter="10">
      <el-col :span="24">
        <user-info></user-info>
      </el-col>
    </el-row>
    <el-row v-if="siteStore.ready" :gutter="10">
      <el-col :span="24">
        <cms-site-panel></cms-site-panel>
      </el-col>
    </el-row>
    <el-row v-else-if="siteStore.status !== 'disabled'" :gutter="10">
      <el-col :span="24">
        <el-card class="site-status" shadow="never" v-loading="siteInitializing">
          <el-empty :description="siteStatusDescription" :image-size="80">
            <el-button v-if="!siteInitializing" type="primary" @click="retrySiteInitialization">
              {{ $t('Common.Refresh') }}
            </el-button>
          </el-empty>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="10">
      <el-col :span="24">
        <shortcut></shortcut>
      </el-col>
    </el-row>
    <el-row v-if="siteStore.ready" :gutter="10">
      <el-col :span="24">
        <cms-site-visit-stat v-if="appStore.isFree"></cms-site-visit-stat>
      </el-col>
    </el-row>
    <el-row :gutter="10">
      <el-col v-if="siteStore.ready" :span="12">
        <cms-site-data-stat></cms-site-data-stat>
      </el-col>
      <el-col :span="siteStore.ready ? 12 : 24">
        <server-info></server-info>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import useAppStore from '@/store/modules/app'
import useSiteStore from '@/store/modules/site'
import UserInfo from '@/views/system/dashboard/userInfo'
import Shortcut from '@/views/system/dashboard/shortcut'
import ServerInfo from '@/views/system/dashboard/serverInfo'
import CmsSiteVisitStat from '@/views/cms/dashboard/siteVisitStat'
import CmsSiteDataStat from '@/views/cms/dashboard/siteDataStat'
import CmsSitePanel from '@/views/cms/dashboard/sitePanel'

const appStore = useAppStore()
const siteStore = useSiteStore()
const { proxy } = getCurrentInstance()
const showTodoTask = computed(() => proxy.$auth.hasPermi('flw:task:view'))
const siteInitializing = computed(() => siteStore.status === 'idle' || siteStore.status === 'loading')
const siteStatusDescription = computed(() => {
  if (siteStore.status === 'empty') {
    return proxy.$t('CMS.Site.NoAccessibleSite')
  }
  if (siteStore.status === 'error') {
    return proxy.$t('CMS.Site.InitializationFailed')
  }
  return proxy.$t('Common.Loading')
})

const retrySiteInitialization = () => {
  siteStore.initialize().catch(() => {
    // 初始化状态和错误提示分别由站点状态管理和请求层处理。
  })
}
</script>
<style lang="scss" scoped>
.app-container {
  padding: 10px;
  position: relative;

  .site-status {
    margin-bottom: 10px;
  }

  .chart-wrapper {
    padding: 16px 16px 0;
    margin-bottom: 32px;
  }
}

@media (max-width:1024px) {
  .chart-wrapper {
    padding: 8px;
  }
}
</style>