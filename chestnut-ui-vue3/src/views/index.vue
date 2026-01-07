<template>
  <div class="app-container">
    <el-row :gutter="10">
      <el-col :span="24">
        <user-info></user-info>
      </el-col>
    </el-row>
    <el-row :gutter="10">
      <el-col :span="24">
        <cms-site-panel></cms-site-panel>
      </el-col>
    </el-row>
    <el-row :gutter="10">
      <el-col :span="24">
        <shortcut></shortcut>
      </el-col>
    </el-row>
    <el-row :gutter="10">
      <el-col :span="24">
        <cms-site-visit-stat v-if="userStore==='Free'"></cms-site-visit-stat>
        <cms-site-visit-trend-panel v-else></cms-site-visit-trend-panel>
      </el-col>
    </el-row>
    <el-row :gutter="10">
      <el-col :span="12">
        <cms-site-data-stat></cms-site-data-stat>
      </el-col>
      <el-col :span="12">
        <server-info></server-info>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import useUserStore from '@/store/modules/user'
import UserInfo from '@/views/system/dashboard/userInfo'
import Shortcut from '@/views/system/dashboard/shortcut'
import ServerInfo from '@/views/system/dashboard/serverInfo'
import CmsSiteVisitStat from '@/views/cms/dashboard/siteVisitStat'
import CmsSiteDataStat from '@/views/cms/dashboard/siteDataStat'
import CmsSitePanel from '@/views/cms/dashboard/sitePanel'

const userStore = useUserStore()
</script>
<style lang="scss" scoped>
.app-container {
  padding: 10px;
  position: relative;

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