<template>
   <div class="app-container">
      <el-row :gutter="20">
         <el-col :span="6" :xs="24">
            <el-card class="box-card">
               <template v-slot:header>
                 <div class="clearfix">
                   <span>{{ $t('AccountCenter.PersonInfo') }}</span>
                 </div>
               </template>
               <div>
                  <div class="text-center">
                     <userAvatar :user="user" />
                  </div>
                  <ul class="list-group list-group-striped">
                     <li class="list-group-item">
                        <svg-icon icon-class="user" />{{ $t('AccountCenter.Account') }}
                        <div class="pull-right">{{ user.userName }}</div>
                     </li>
                     <li class="list-group-item">
                        <svg-icon icon-class="phone" />{{ $t('AccountCenter.PhoneNumber') }}
                        <div class="pull-right">{{ user.phoneNumber }}</div>
                     </li>
                     <li class="list-group-item">
                        <svg-icon icon-class="email" />{{ $t('AccountCenter.Email') }}
                        <div class="pull-right">{{ user.email }}</div>
                     </li>
                     <li class="list-group-item">
                        <svg-icon icon-class="tree" />{{ $t('AccountCenter.Dept') }}
                        <div class="pull-right">{{ user.deptName }}</div>
                     </li>
                     <li class="list-group-item">
                        <svg-icon icon-class="peoples" />{{ $t('AccountCenter.Roles') }}
                        <div class="pull-right">{{ roleGroup }}</div>
                     </li>
                     <li class="list-group-item">
                        <svg-icon icon-class="tree" />{{ $t('AccountCenter.Post') }}
                        <div class="pull-right">{{ postGroup }}</div>
                     </li>
                     <li class="list-group-item">
                        <svg-icon icon-class="date" />{{ $t('AccountCenter.CreateTime') }}
                        <div class="pull-right">{{ user.createTime }}</div>
                     </li>
                  </ul>
               </div>
            </el-card>
         </el-col>
         <el-col :span="18" :xs="24">
            <el-card>
               <template #header>
                 <div class="clearfix">
                   <span>{{ $t('AccountCenter.Basic') }}</span>
                 </div>
               </template>
               <el-tabs v-model="selectedTab">
                  <el-tab-pane :label="$t('AccountCenter.Basic')" name="userinfo">
                     <userInfo :user="user" />
                  </el-tab-pane>
                  <el-tab-pane :label="$t('AccountCenter.ResetPassword')" name="resetPwd">
                     <resetPwd />
                  </el-tab-pane>
               </el-tabs>
            </el-card>
         </el-col>
      </el-row>
   </div>
</template>

<script setup name="Profile">
import userAvatar from "./userAvatar"
import userInfo from "./userInfo"
import resetPwd from "./resetPwd"
import { getUserProfile } from "@/api/system/user"

const route = useRoute()
const selectedTab = ref("userinfo")
const data = reactive({
  user: {},
  roleGroup: {},
  postGroup: {}
})

const { user, roleGroup, postGroup } = toRefs(data)

function getUser() {
  getUserProfile().then(response => {
    user.value = response.data.user
    roleGroup.value = response.data.roleGroup
    postGroup.value = response.data.postGroup
  })
}

onMounted(() => {
  const activeTab = route.params && route.params.activeTab
  if (activeTab) {
    selectedTab.value = activeTab
  }
  getUser()
})
</script>
