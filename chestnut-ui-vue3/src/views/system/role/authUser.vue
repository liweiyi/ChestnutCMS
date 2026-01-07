
<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" v-show="showSearch" :inline="true" class="el-form-search">
         <el-form-item prop="userName">
            <el-input
               v-model="queryParams.userName"
               :placeholder="$t('System.User.UserName')"
               clearable
               style="width: 160px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item prop="phoneNumber">
            <el-input
               v-model="queryParams.phoneNumber"
               :placeholder="$t('System.User.PhoneNumber')"
               clearable
               style="width: 160px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
            <el-button icon="Refresh" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
         </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
         <el-col :span="1.5" class="permi-wrap">
            <el-button
               type="primary"
               plain
               icon="Plus"
               @click="openSelectUser"
               v-hasPermi="['system:role:edit']"
            >{{ $t('System.UserRole.AddUser') }}</el-button>
         </el-col>
         <el-col :span="1.5" class="permi-wrap">
            <el-button
               type="danger"
               plain
               icon="CircleClose"
               :disabled="multiple"
               @click="cancelAuthUser"
               v-hasPermi="['system:role:edit']"
            >{{ $t('System.UserRole.RemoveUser') }}</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="55" align="center" />
         <el-table-column :label="$t('System.User.UserName')" prop="userName" :show-overflow-tooltip="true" />
         <el-table-column :label="$t('System.User.NickName')" prop="nickName" :show-overflow-tooltip="true" />
         <el-table-column :label="$t('System.User.Email')" prop="email" :show-overflow-tooltip="true" />
         <el-table-column :label="$t('System.User.PhoneNumber')" prop="phoneNumber" :show-overflow-tooltip="true" />
         <el-table-column :label="$t('System.User.Status')" align="center" prop="status">
            <template #default="scope">
               <dict-tag :options="SysUserStatus" :value="scope.row.status" />
            </template>
         </el-table-column>
         <el-table-column :label="$t('Common.CreateTime')" align="center" prop="createTime" width="180">
            <template #default="scope">
               <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
         </el-table-column>
         <el-table-column :label="$t('Common.Operation')" width="160">
            <template #default="scope">
               <el-button link type="danger" icon="CircleClose" @click="cancelAuthUser(scope.row)" v-hasPermi="['system:role:edit']">{{ $t('Common.Remove') }}</el-button>
            </template>
         </el-table-column>
      </el-table>

      <pagination
         v-show="total > 0"
         :total="total"
         v-model:page="queryParams.pageNum"
         v-model:limit="queryParams.pageSize"
         @pagination="getList"
      />
      <select-user ref="selectRef" :roleId="queryParams.roleId" @ok="handleQuery" />
   </div>
</template>

<script setup name="AuthUser">
import selectUser from "./selectUser"
import { allocatedUserList, authUserCancel } from "@/api/system/role"

const props = defineProps({
  roleId: {
    type: String,
    required: true
  }
})

const { proxy } = getCurrentInstance()
const { SysUserStatus } = proxy.useDict("SysUserStatus")

const userList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const multiple = ref(true)
const total = ref(0)
const userIds = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  roleId: props.roleId,
  userName: undefined,
  phoneNumber: undefined,
})

watch(() => props.roleId, (newVal) => {
  queryParams.roleId = newVal;
  getList();
})

/** 查询授权用户列表 */
function getList() {
  loading.value = true
  allocatedUserList(queryParams).then(response => {
    userList.value = response.data.rows
    total.value = parseInt(response.data.total)
    loading.value = false
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  userIds.value = selection.map(item => item.userId)
  multiple.value = !selection.length
}

/** 打开授权用户表弹窗 */
function openSelectUser() {
  proxy.$refs["selectRef"].show()
}

/** 取消授权按钮操作 */
function cancelAuthUser(row) {
   const _userIds = row.userId ? [ row.userId ] : userIds.value;
   if (!_userIds || _userIds.length == 0) {
      return;
   }
  proxy.$modal.confirm(proxy.$t('System.Role.ConfirmRemoveUser')).then(function () {
    return authUserCancel({ roleId: queryParams.roleId, userIds: _userIds })
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'))
  }).catch(() => {})
}

getList()
</script>
