<template>
  <el-drawer :model-value="modelValue" :title="$t('MemberSubscription.Audit')" size="min(1200px, 96vw)" append-to-body @update:model-value="emit('update:modelValue', $event)">
    <el-descriptions v-if="record" :column="2" class="mb8"><el-descriptions-item :label="$t('MemberSubscription.MemberId')">{{ record.memberId }}</el-descriptions-item><el-descriptions-item :label="$t('MemberSubscription.DefinitionTitle')">{{ record.subscriptionName || record.subscriptionId }}</el-descriptions-item></el-descriptions>
    <el-form inline class="el-form-search" @submit.prevent>
      <el-form-item><el-button-group><el-button type="primary" icon="Search" @click="handleQuery">{{ $t('Common.Search') }}</el-button><el-button icon="Refresh" @click="handleReset">{{ $t('Common.Reset') }}</el-button></el-button-group></el-form-item>
    </el-form>
    <el-alert v-if="hasFailed" class="mb8" type="error" :closable="false" :title="$t('MemberSubscription.LoadFailed')" />
    <el-table v-loading="isLoading" :data="rows" row-key="logId" max-height="600">
      <el-table-column :label="$t('MemberSubscription.OperationTime')" min-width="190"><template #default="{ row }">{{ formatSubscriptionTime(row.operationTime) }}</template></el-table-column>
      <el-table-column :label="$t('MemberSubscription.DeltaSeconds')" prop="deltaSeconds" min-width="140" />
      <el-table-column :label="$t('MemberSubscription.Before')" min-width="190"><template #default="{ row }">{{ (row.beforeExpireTime === null || row.beforeExpireTime === undefined || String(row.beforeExpireTime) === '0') ? $t('MemberSubscription.Initial') : formatSubscriptionTime(row.beforeExpireTime, 'seconds') }}</template></el-table-column>
      <el-table-column :label="$t('MemberSubscription.After')" min-width="190"><template #default="{ row }">{{ formatSubscriptionTime(row.afterExpireTime, 'seconds') }}</template></el-table-column>
      <el-table-column :label="$t('MemberSubscription.OperatorId')" prop="operatorId" min-width="130" show-overflow-tooltip />
      <el-table-column :label="$t('MemberSubscription.Source')" min-width="150"><template #default="{ row }">{{ row.sourceType === 'manual' ? $t('MemberSubscription.Manual') : row.sourceType }} {{ row.sourceId }}</template></el-table-column>
      <el-table-column :label="$t('MemberSubscription.Reason')" prop="reason" min-width="180" show-overflow-tooltip />
      <el-table-column :label="$t('MemberSubscription.OperatorType')" prop="operatorType" min-width="140" show-overflow-tooltip />
    </el-table>
    <pagination v-show="total > 0" v-model:page="page.pageNum" v-model:limit="page.pageSize" :total="total" @pagination="loadList" />
  </el-drawer>
</template>

<script setup name="MemberSubscriptionLogDrawer">
import {
  listMemberSubscriptionLogs
} from '@/api/member/subscription'
import {
  checkPermi
} from '@/utils/permission'
import {
  formatSubscriptionTime
} from '../formValues'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  record: {
    type: Object,
    default: null
  }
})
const emit = defineEmits(['update:modelValue'])
const page = reactive({
  pageNum: 1,
  pageSize: 20
})
const rows = ref([])
const total = ref(0)
const isLoading = ref(false)
const hasFailed = ref(false)
let requestVersion = 0

watch([() => props.modelValue, () => props.record], ([open]) => {
  requestVersion += 1
  if (!open) {
    rows.value = []
    total.value = 0
    isLoading.value = false
    return
  }
  page.pageNum = 1
  return loadList()
}, {
  immediate: true
})
onBeforeUnmount(() => {
  requestVersion += 1
})

async function loadList() {
  if (!props.modelValue || !props.record || !checkPermi(['member:subscription:log:view'])) return
  const version = ++requestVersion
  isLoading.value = true
  hasFailed.value = false
  try {
    const response = await listMemberSubscriptionLogs({
      ...page,
      memberId: String(props.record.memberId),
      subscriptionId: props.record.subscriptionId
    })
    if (version !== requestVersion || !props.modelValue) return
    rows.value = response.data.rows ?? []
    total.value = Number(response.data.total ?? 0)
  } catch (error) {
    if (version === requestVersion) {
      rows.value = []
      total.value = 0
      hasFailed.value = true
    }
  } finally {
    if (version === requestVersion) isLoading.value = false
  }
}

function handleQuery() {
  page.pageNum = 1
  return loadList()
}

function handleReset() {
  return handleQuery()
}
</script>
