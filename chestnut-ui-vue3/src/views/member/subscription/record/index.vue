<template>
  <div class="app-container member-subscription-page">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5" class="permi-wrap"><el-button v-hasPermi="['member:subscription:adjust']" type="primary" plain icon="Plus" @click="handleCreate">{{ $t('MemberSubscription.AddRecord') }}</el-button></el-col>
      <right-toolbar v-model:show-search="showSearch" @query-table="loadList" />
    </el-row>
    <el-form v-show="showSearch" ref="queryRef" :model="query" inline class="el-form-search" @submit.prevent>
      <el-form-item prop="memberId"><el-input v-model.trim="query.memberId" :placeholder="$t('MemberSubscription.MemberId')" maxlength="19" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item prop="subscriptionId" style="width: 260px"><member-subscription-definition-select v-model="query.subscriptionId" /></el-form-item>
      <el-form-item prop="valid"><el-select v-model="query.valid" style="width: 140px" clearable :placeholder="$t('MemberSubscription.Validity')"><el-option :value="'Y'" :label="$t('MemberSubscription.Valid')" /><el-option :value="'N'" :label="$t('MemberSubscription.Expired')" /></el-select></el-form-item>
      <el-form-item><el-button-group><el-button type="primary" icon="Search" @click="handleQuery">{{ $t('Common.Search') }}</el-button><el-button icon="Refresh" @click="handleReset">{{ $t('Common.Reset') }}</el-button></el-button-group></el-form-item>
    </el-form>
    <el-alert v-if="hasFailed" class="mb8" type="error" :closable="false" :title="$t('MemberSubscription.LoadFailed')" />
    <el-alert v-if="hasDefinitionNamesFailed" class="mb8" type="warning" :closable="false" :title="$t('MemberSubscription.NamesLoadFailed')" />
    <div class="member-subscription-page__table">
      <el-table v-loading="isLoading" :data="rows" row-key="recordId" height="100%">
        <el-table-column prop="memberId" :label="$t('MemberSubscription.MemberId')" min-width="170" show-overflow-tooltip />
        <el-table-column :label="$t('MemberSubscription.DefinitionTitle')" min-width="190" show-overflow-tooltip><template #default="{ row }">{{ row.subscriptionName || row.subscriptionId }}</template></el-table-column>
        <el-table-column :label="$t('MemberSubscription.ExpireTime')" min-width="190"><template #default="{ row }">{{ formatSubscriptionTime(row.expireTime, 'seconds') }}</template></el-table-column>
        <el-table-column :label="$t('MemberSubscription.Validity')" width="90"><template #default="{ row }"><el-tag :type="isSubscriptionValid(row.expireTime, currentTime) ? 'success' : 'info'">{{ $t(isSubscriptionValid(row.expireTime, currentTime) ? 'MemberSubscription.Valid' : 'MemberSubscription.Expired') }}</el-tag></template></el-table-column>
        <el-table-column :label="$t('Common.Operation')" width="220" fixed="right"><template #default="{ row }">
          <el-button v-hasPermi="['member:subscription:adjust']" type="primary" link @click="handleAdjust(row)">{{ $t('MemberSubscription.Adjust') }}</el-button>
          <el-button v-hasPermi="['member:subscription:log:view']" type="info" link @click="handleAudit(row)">{{ $t('MemberSubscription.Audit') }}</el-button>
        </template></el-table-column>
      </el-table>
    </div>
    <pagination v-show="total > 0" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @pagination="loadList" />
    <member-subscription-adjust-dialog v-model="isFormOpen" :record="selectedRecord" @success="loadList" />
    <member-subscription-log-drawer v-if="canReadLogs" v-model="isAuditOpen" :record="auditRecord" />
  </div>
</template>

<script setup name="MemberSubscriptionRecordIndex">
import { useTimestamp } from '@vueuse/core'
import {
  listMemberSubscriptions, listMemberSubscriptionOptions
} from '@/api/member/subscription'
import {
  checkPermi
} from '@/utils/permission'
import {
  formatSubscriptionTime, isSubscriptionMemberId, isSubscriptionValid, buildSubscriptionRecordQuery
} from '../formValues'
import MemberSubscriptionDefinitionSelect from '@/views/components/MemberSubscriptionDefinitionSelect/index.vue'
import MemberSubscriptionAdjustDialog from '../components/MemberSubscriptionAdjustDialog.vue'
import MemberSubscriptionLogDrawer from '../components/MemberSubscriptionLogDrawer.vue'

const defaults = () => ({
  pageNum: 1,
  pageSize: 20,
  memberId: '',
  subscriptionId: '',
  valid: undefined
})
const {
  proxy
} = getCurrentInstance()
const queryRef = ref()
const query = reactive(defaults())
const rows = ref([])
const total = ref(0)
const showSearch = ref(true)
const isLoading = ref(false)
const hasFailed = ref(false)
const isFormOpen = ref(false)
const isAuditOpen = ref(false)
const selectedRecord = ref(null)
const auditRecord = ref(null)
const currentTime = useTimestamp({ interval: 1000 })
const hasDefinitionNamesFailed = ref(false)
const canReadLogs = computed(() => checkPermi(['member:subscription:log:view']))
let requestVersion = 0

onMounted(loadList)
onBeforeUnmount(() => {
  requestVersion += 1
})

async function loadList() {
  if (query.memberId && !isSubscriptionMemberId(query.memberId)) {
    proxy.$modal.msgWarning(proxy.$t('MemberSubscription.MemberRule'))
    return
  }
  const version = ++requestVersion
  isLoading.value = true
  hasFailed.value = false
  hasDefinitionNamesFailed.value = false
  try {
    const response = await listMemberSubscriptions(buildSubscriptionRecordQuery(query))
    if (version !== requestVersion) return
    rows.value = (response.data.rows ?? []).map(row => ({ ...row, subscriptionName: '' }))
    total.value = Number(response.data.total ?? 0)
    const ids = [...new Set(rows.value.map(row => row.subscriptionId).filter(Boolean))]
    try {
      const batches = []
      for (let i = 0; i < ids.length; i += 100) batches.push(listMemberSubscriptionOptions({ ids: ids.slice(i, i + 100) }))
      const options = await Promise.all(batches)
      if (version !== requestVersion) return
      const labels = new Map(options.flatMap(result => result.data ?? []).map(option => [option.value, option.label]))
      rows.value = rows.value.map(row => ({ ...row, subscriptionName: labels.get(row.subscriptionId) ?? '' }))
    } catch { if (version === requestVersion) hasDefinitionNamesFailed.value = true }
  } catch (error) {
    if (version === requestVersion) {
      hasFailed.value = true
      rows.value = []
      total.value = 0
    }
  } finally {
    if (version === requestVersion) isLoading.value = false
  }
}

function handleQuery() {
  query.pageNum = 1
  return loadList()
}

function handleReset() {
  queryRef.value?.resetFields()
  Object.assign(query, defaults())
  return loadList()
}

function handleCreate() {
  selectedRecord.value = null
  isFormOpen.value = true
}

function handleAdjust(row) {
  selectedRecord.value = {
    ...row
  }
  isFormOpen.value = true
}

function handleAudit(row) {
  if (!canReadLogs.value) return
  auditRecord.value = {
    ...row
  }
  isAuditOpen.value = true
}
</script>

<style scoped lang="scss">
.member-subscription-page { display: flex; flex-direction: column; height: 100%; min-height: 0; }
.member-subscription-page__table { flex: 1; min-height: 240px; }
</style>
