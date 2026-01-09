<template>
  <div class="app-container">
    <el-tabs v-model="activeName" @tab-click="handleTabClick">
      <el-tab-pane :label="$t('System.Security.Tab.SecurityConfig')" name="securityConfig">
        <security-config v-if="activeName=='securityConfig'"></security-config>
      </el-tab-pane>
      <el-tab-pane :label="$t('System.Security.Tab.LoginConfig')" name="loginConfig">
        <login-config v-if="activeName=='loginConfig'"></login-config>
      </el-tab-pane>
      <el-tab-pane :label="$t('System.Security.Tab.CloudConfig')" name="cloudConfig">
        <cloud-config v-if="activeName=='cloudConfig'"></cloud-config>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<script setup name="SystemSecurityIndex">
import SecurityConfig from '@/views/system/security/securityConfig';
import LoginConfig from '@/views/system/security/loginConfig';
import CloudConfig from '@/views/system/security/cloudConfig';
const activeName = ref('securityConfig')

const handleTabClick = (tab, event) => {
}
</script>