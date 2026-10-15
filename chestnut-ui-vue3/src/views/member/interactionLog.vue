<template>
  <div class="app-container">
    <el-tabs :model-value="activeTab" @tab-change="handleTabChange">
      <el-tab-pane :label="$t('Member.InteractionLog.LikeLogs')" name="likes" />
      <el-tab-pane :label="$t('Member.InteractionLog.SignInLogs')" name="signIns" />
    </el-tabs>
    <el-form ref="queryFormRef" :model="filters" :rules="rules" :inline="true" class="el-form-search" @submit.prevent>
      <el-form-item prop="memberId">
        <el-input v-model.trim="filters.memberId" clearable maxlength="19" style="width: 210px"
          :placeholder="$t('Member.UID')" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="$t('Member.InteractionLog.RecordTime')" prop="timeRange">
        <el-date-picker v-model="filters.timeRange" type="datetimerange" value-format="YYYY-MM-DD HH:mm:ss"
          :default-time="defaultTimes" :start-placeholder="$t('Common.BeginTime')"
          :end-placeholder="$t('Common.EndTime')" style="width: 380px" />
      </el-form-item>
      <el-form-item>
        <el-button-group>
          <el-button type="primary" icon="Search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
          <el-button icon="Refresh" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
        </el-button-group>
      </el-form-item>
    </el-form>

    <el-table v-if="activeTab === 'likes'" key="likes" v-loading="loading" :data="rows" row-key="logId">
      <el-table-column :label="$t('Common.RowNo')" type="index" width="80" align="center"
        :index="index => tableRowNo(pagination.pageNum, pagination.pageSize, index + 1)" />
      <el-table-column :label="$t('Member.UID')" prop="memberId" min-width="180" show-overflow-tooltip />
      <el-table-column :label="$t('Member.UserName')" prop="userName" min-width="140" show-overflow-tooltip :formatter="row => row.userName || '-'" />
      <el-table-column :label="$t('Member.NickName')" prop="nickName" min-width="140" show-overflow-tooltip :formatter="row => row.nickName || '-'" />
      <el-table-column :label="$t('Member.FavoritesManage.DataType')" prop="dataType" min-width="150" show-overflow-tooltip />
      <el-table-column :label="$t('Member.InteractionLog.DataId')" prop="dataId" min-width="180" show-overflow-tooltip />
      <el-table-column :label="$t('Member.InteractionLog.DataName')" min-width="250" show-overflow-tooltip>
        <template #default="scope">
          <span v-if="scope.row.name">{{ scope.row.name }}</span>
          <el-text v-else type="info">{{ $t('Member.FavoritesManage.Unavailable', { id: scope.row.dataId }) }}</el-text>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Member.InteractionLog.LikeTime')" width="180">
        <template #default="scope">{{ parseTime(scope.row.createTime) }}</template>
      </el-table-column>
    </el-table>
    <el-table v-else key="signIns" v-loading="loading" :data="rows" row-key="logId">
      <el-table-column :label="$t('Common.RowNo')" type="index" width="80" align="center"
        :index="index => tableRowNo(pagination.pageNum, pagination.pageSize, index + 1)" />
      <el-table-column :label="$t('Member.UID')" prop="memberId" min-width="180" show-overflow-tooltip />
      <el-table-column :label="$t('Member.UserName')" prop="userName" min-width="140" show-overflow-tooltip :formatter="row => row.userName || '-'" />
      <el-table-column :label="$t('Member.NickName')" prop="nickName" min-width="140" show-overflow-tooltip :formatter="row => row.nickName || '-'" />
      <el-table-column :label="$t('Member.InteractionLog.SignInDate')" width="180">
        <template #default="scope">{{ formatSignInDate(scope.row.signInKey) }}</template>
      </el-table-column>
      <el-table-column :label="$t('Member.InteractionLog.RecordTime')" width="180">
        <template #default="scope">{{ parseTime(scope.row.logTime) }}</template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="pagination.pageNum"
      v-model:limit="pagination.pageSize" @pagination="loadLogs" />
  </div>
</template>

<script setup name="MemberInteractionLog">
import { getMemberLikeLogs, getMemberSignInLogs } from '@/api/member/interactionLog'
import useMemberInteractionLogs from '@/composables/useMemberInteractionLogs'

const { proxy } = getCurrentInstance()
const queryFormRef = ref()
const defaultTimes = [new Date(2000, 0, 1, 0, 0, 0), new Date(2000, 0, 1, 23, 59, 59)]
const { activeTab, filters, pagination, rows, total, loading, selectTab, loadLogs, search, reset } = useMemberInteractionLogs({
  getLikeLogs: getMemberLikeLogs, getSignInLogs: getMemberSignInLogs
})
const rules = {
  memberId: [{ validator(rule, value, callback) {
    if (!value || (/^[1-9]\d{0,18}$/.test(value) && BigInt(value) <= 9223372036854775807n)) callback()
    else callback(new Error(proxy.$t('Member.Follow.InvalidId')))
  }, trigger: 'blur' }]
}

async function handleQuery() {
  if (!await queryFormRef.value.validate().catch(() => false)) return
  await search()
}

async function handleTabChange(name) {
  if (selectTab(name)) await handleQuery()
}

function resetQuery() {
  queryFormRef.value.clearValidate()
  return reset()
}

function formatSignInDate(value) {
  const key = String(value ?? '')
  return /^\d{8}$/.test(key) ? `${key.slice(0, 4)}-${key.slice(4, 6)}-${key.slice(6, 8)}` : '-'
}

onMounted(loadLogs)
</script>
