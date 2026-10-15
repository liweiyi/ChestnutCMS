<template>
  <div class="app-container">
    <cc-spliter storage-key="member-level-manage">
      <cc-spliter-column width="37.5%">
        <el-form ref="memberFormRef" :model="memberQuery" :rules="memberRules" :inline="true" class="el-form-search" @submit.prevent>
          <el-form-item prop="memberId">
            <el-input v-model.trim="memberQuery.memberId" clearable maxlength="19" style="width: 170px"
              :placeholder="$t('Member.UID')" @keyup.enter="searchMembers" />
          </el-form-item>
          <el-form-item prop="keyword">
            <el-input v-model.trim="memberQuery.keyword" clearable maxlength="30" style="width: 170px"
              :placeholder="$t('Member.LevelManage.MemberKeyword')" @keyup.enter="searchMembers" />
          </el-form-item>
          <el-form-item>
            <el-button-group>
              <el-button type="primary" icon="Search" @click="searchMembers">{{ $t('Common.Search') }}</el-button>
              <el-button icon="Refresh" @click="resetMembers">{{ $t('Common.Reset') }}</el-button>
            </el-button-group>
          </el-form-item>
        </el-form>
        <el-table v-loading="memberLoading" :data="members" row-key="memberId" highlight-current-row
          :current-row-key="selectedMember?.memberId" @row-click="selectMember" class="member-table">
          <el-table-column :label="$t('Member.UserName')" prop="userName" min-width="110" show-overflow-tooltip />
          <el-table-column :label="$t('Member.NickName')" prop="nickName" min-width="110" show-overflow-tooltip />
          <el-table-column :label="$t('Member.Status')" prop="status" width="80" align="center">
            <template #default="scope"><dict-tag :options="MemberStatus" :value="scope.row.status" /></template>
          </el-table-column>
        </el-table>
        <pagination v-show="memberTotal > 0" :total="memberTotal" v-model:page="memberQuery.pageNum"
          v-model:limit="memberQuery.pageSize" layout="total, prev, pager, next" :pager-count="5"
          :auto-scroll="false" @pagination="loadMembers" />
      </cc-spliter-column>
      <cc-spliter-column>
        <div class="level-header">
          {{ selectedMember ? $t('Member.LevelManage.MemberLevels', { name: selectedMember.nickName || selectedMember.userName || selectedMember.memberId }) : $t('Member.LevelManage.Levels') }}
          <el-text v-if="selectedMember" type="info" class="selected-member-id">ID: {{ selectedMember.memberId }}</el-text>
        </div>
        <el-table v-loading="levelLoading" :data="levels" row-key="levelType"
          :empty-text="$t(selectedMember ? 'Member.LevelManage.NoLevels' : 'Member.LevelManage.SelectMember')">
          <el-table-column :label="$t('Member.LevelManage.LevelType')" prop="levelTypeName" min-width="130" show-overflow-tooltip />
          <el-table-column :label="$t('Member.Level.Level')" prop="level" width="80" align="center">
            <template #default="scope">Lv{{ scope.row.level }}</template>
          </el-table-column>
          <el-table-column :label="$t('Member.Level.Name')" min-width="120" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.name || '-' }}</template>
          </el-table-column>
          <el-table-column :label="$t('Member.LevelManage.Progress')" min-width="230">
            <template #default="scope">
              <template v-if="scope.row.progress != null">
                <el-progress :percentage="scope.row.progress" :status="scope.row.maxLevel ? 'success' : undefined" />
                <el-text size="small" type="info">{{ scope.row.maxLevel ? $t('Member.LevelManage.MaxLevel') : `${scope.row.exp} / ${scope.row.nextNeedExp}` }}</el-text>
              </template>
              <el-text v-else type="info">{{ $t('Member.LevelManage.NotConfigured') }}</el-text>
            </template>
          </el-table-column>
          <el-table-column :label="$t('Common.Operation')" width="90" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="Document" @click="openLogs(scope.row)">{{ $t('Member.LevelManage.Logs') }}</el-button>
            </template>
          </el-table-column>
        </el-table>
      </cc-spliter-column>
    </cc-spliter>

    <el-drawer v-model="logOpen" direction="rtl" size="min(960px, 95vw)" append-to-body
      :title="logTitle" @close="closeLogs">
      <el-form :model="logQuery" :inline="true" class="el-form-search" @submit.prevent>
        <el-form-item :label="$t('Member.ExpOpType')">
          <el-select v-model="logQuery.opType" clearable filterable style="width: 180px" :placeholder="$t('Member.ExpOpType')">
            <el-option v-for="operation in operations" :key="operation.id" :label="operation.name" :value="operation.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('Member.LevelManage.ChangeDate')">
          <el-date-picker v-model="logQuery.dateRange" type="daterange" unlink-panels value-format="YYYY-MM-DD"
            :start-placeholder="$t('Common.BeginDate')" :end-placeholder="$t('Common.EndDate')" style="width: 270px" />
        </el-form-item>
        <el-form-item>
          <el-button-group>
            <el-button type="primary" icon="Search" @click="searchLogs">{{ $t('Common.Search') }}</el-button>
            <el-button icon="Refresh" @click="resetLogs">{{ $t('Common.Reset') }}</el-button>
          </el-button-group>
        </el-form-item>
      </el-form>
      <el-table v-loading="logLoading" :data="logs" row-key="logId">
        <el-table-column :label="$t('Member.LevelManage.ChangeTime')" min-width="170">
          <template #default="scope">{{ parseTime(scope.row.logTime) }}</template>
        </el-table-column>
        <el-table-column :label="$t('Member.ExpOpType')" prop="opTypeName" min-width="150" show-overflow-tooltip />
        <el-table-column :label="$t('Member.LevelManage.ChangeExp')" width="120" align="center">
          <template #default="scope">
            <el-text :type="scope.row.changeExp >= 0 ? 'success' : 'danger'">{{ scope.row.changeExp > 0 ? '+' : '' }}{{ scope.row.changeExp }}</el-text>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Member.LevelManage.LevelAfter')" prop="level" width="130" align="center" />
        <el-table-column :label="$t('Member.LevelManage.ExpAfter')" prop="exp" min-width="160" align="right" />
      </el-table>
      <pagination v-show="logTotal > 0" :total="logTotal" v-model:page="logQuery.pageNum"
        v-model:limit="logQuery.pageSize" :auto-scroll="false" @pagination="loadLogs" />
    </el-drawer>
  </div>
</template>

<script setup name="MemberLevelManage">
import { getLevelMembers, getMemberLevels, getMemberLevelLogs, getMemberLevelOperations } from '@/api/member/level'
import useMemberLevels from '@/composables/useMemberLevels'

const { proxy } = getCurrentInstance()
const { MemberStatus } = proxy.useDict('MemberStatus')
const memberFormRef = ref()
const {
  members, memberTotal, memberLoading, selectedMember, levels, levelLoading, memberQuery,
  logOpen, logContext, logs, logTotal, logLoading, operations, logQuery,
  loadMembers, selectMember, openLogs, closeLogs, loadLogs, searchLogs, resetLogs
} = useMemberLevels({
  getMembers: getLevelMembers, getLevels: getMemberLevels, getLogs: getMemberLevelLogs, getOperations: getMemberLevelOperations
})
const logTitle = computed(() => logContext.value
  ? proxy.$t('Member.LevelManage.LogTitle', { name: logContext.value.memberName, type: logContext.value.levelTypeName })
  : proxy.$t('Member.LevelManage.Logs'))

const memberRules = {
  memberId: [{ validator(rule, value, callback) {
    if (!value || (/^[1-9]\d{0,18}$/.test(value) && BigInt(value) <= 9223372036854775807n)) callback()
    else callback(new Error(proxy.$t('Member.Follow.InvalidId')))
  }, trigger: 'blur' }]
}

async function searchMembers() {
  if (!await memberFormRef.value.validate().catch(() => false)) return
  memberQuery.pageNum = 1
  await loadMembers()
}

function resetMembers() {
  memberFormRef.value.resetFields()
  return searchMembers()
}

onMounted(loadMembers)
</script>

<style scoped>
.level-header { margin-bottom: 16px; }
.member-table :deep(.el-table__row) { cursor: pointer; }
.selected-member-id { margin-left: 12px; }
</style>
