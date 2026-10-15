<template>
  <div class="app-container">
    <el-form ref="queryFormRef" :model="queryParams" :rules="rules" :inline="true" class="el-form-search" @submit.prevent>
      <el-form-item :label="$t('Member.Follow.UserId')" prop="memberId">
        <el-input v-model.trim="queryParams.memberId" clearable maxlength="19" style="width: 210px"
          :placeholder="$t('Member.Follow.UserId')" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="$t('Member.Follow.Type')" prop="type">
        <el-select v-model="queryParams.type" style="width: 110px" @change="handleTypeChange">
          <el-option :label="$t('Member.Follow.Follows')" value="follow" />
          <el-option :label="$t('Member.Follow.Followers')" value="follower" />
        </el-select>
      </el-form-item>
      <el-form-item prop="targetMemberId">
        <el-input v-model.trim="queryParams.targetMemberId" clearable maxlength="19" style="width: 210px"
          :placeholder="$t('Member.Follow.TargetMemberId')" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item prop="nickName">
        <el-input v-model.trim="queryParams.nickName" clearable maxlength="30" style="width: 160px"
          :placeholder="$t('Member.NickName')" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button-group>
          <el-button type="primary" icon="Search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
          <el-button icon="Refresh" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
        </el-button-group>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="dataList" :empty-text="$t(appliedQuery ? 'Member.Follow.NoData' : 'Member.Follow.EmptyTip')">
      <el-table-column :label="$t('Member.Follow.TargetMemberId')" prop="memberId" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Member.UserName')" prop="userName" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Member.NickName')" prop="nickName" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Common.Operation')" align="center" width="120">
        <template #default="scope">
          <el-button v-hasPermi="['member:follow:cancel']" link type="danger" icon="Close"
            :disabled="cancelPending" @click="handleCancel(scope.row)">{{ $t('Common.Cancel') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="MemberFollow">
import { cancelMemberFollow, getMemberFollowList } from '@/api/member/follow'

const { proxy } = getCurrentInstance()
const queryFormRef = ref()
const loading = ref(false)
const cancelPending = ref(false)
const dataList = ref([])
const total = ref(0)
const appliedQuery = ref()
const queryParams = reactive({
  memberId: '',
  type: 'follow',
  targetMemberId: '',
  nickName: '',
  pageNum: 1,
  pageSize: 10
})
let requestId = 0

function validateId(rule, value, callback) {
  if (!value || (/^[1-9]\d{0,18}$/.test(value) && BigInt(value) <= 9223372036854775807n)) {
    callback()
  } else {
    callback(new Error(proxy.$t('Member.Follow.InvalidId')))
  }
}

const rules = {
  memberId: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
    { validator: validateId, trigger: 'blur' }
  ],
  targetMemberId: [{ validator: validateId, trigger: 'blur' }]
}

// Discard rows when their owner or direction changes, including any in-flight response.
watch(() => [queryParams.memberId, queryParams.type], clearList, { flush: 'sync' })

function clearList() {
  requestId++
  appliedQuery.value = undefined
  dataList.value = []
  total.value = 0
  loading.value = false
  queryParams.pageNum = 1
}

async function getList() {
  if (!appliedQuery.value) return
  const currentRequest = ++requestId
  const params = { ...appliedQuery.value, pageNum: queryParams.pageNum, pageSize: queryParams.pageSize }
  loading.value = true
  try {
    const response = await getMemberFollowList(params)
    if (currentRequest !== requestId) return
    total.value = Number(response.data.total)
    // Removing the last row of a page returns to the last available page.
    const lastPage = Math.max(1, Math.ceil(total.value / queryParams.pageSize))
    if (queryParams.pageNum > lastPage) {
      queryParams.pageNum = lastPage
      return await getList()
    }
    dataList.value = response.data.rows
  } finally {
    if (currentRequest === requestId) loading.value = false
  }
}

async function handleQuery() {
  if (!await queryFormRef.value.validate().catch(() => false)) return
  queryParams.pageNum = 1
  appliedQuery.value = {
    memberId: queryParams.memberId,
    type: queryParams.type,
    targetMemberId: queryParams.targetMemberId || undefined,
    nickName: queryParams.nickName || undefined
  }
  dataList.value = []
  total.value = 0
  await getList()
}

function handleTypeChange() {
  if (queryParams.memberId) return handleQuery()
}

function resetQuery() {
  queryFormRef.value.resetFields()
  clearList()
}

async function handleCancel(row) {
  if (!appliedQuery.value || cancelPending.value) return
  const context = { ...appliedQuery.value }
  cancelPending.value = true
  try {
    const message = context.type === 'follow' ? 'Member.Follow.ConfirmCancelFollow' : 'Member.Follow.ConfirmRemoveFollower'
    await proxy.$modal.confirm(proxy.$t(message, { memberId: context.memberId, targetMemberId: row.memberId }))
    await cancelMemberFollow({ memberId: context.memberId, type: context.type, targetMemberId: row.memberId })
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'))
    await getList()
  } catch {
    // Confirmation dismissal and API errors are handled by the shared modal/request utilities.
  } finally {
    cancelPending.value = false
  }
}
</script>
