<template>
  <div class="app-container member-subscription-page">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5" class="permi-wrap">
        <el-button v-hasPermi="['member:subscription:definition:add']" type="primary" plain icon="Plus" @click="handleAdd">{{ $t('MemberSubscription.AddDefinition') }}</el-button>
      </el-col>
      <right-toolbar v-model:show-search="showSearch" @query-table="loadList" />
    </el-row>
    <el-form v-show="showSearch" ref="queryRef" :model="query" inline class="el-form-search" @submit.prevent>
      <el-form-item prop="subscriptionName"><el-input v-model.trim="query.subscriptionName" :placeholder="$t('MemberSubscription.Name')" maxlength="128" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item><el-button-group>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
        <el-button icon="Refresh" @click="handleReset">{{ $t('Common.Reset') }}</el-button>
      </el-button-group></el-form-item>
    </el-form>
    <el-alert v-if="hasFailed" class="mb8" type="error" :closable="false" :title="$t('MemberSubscription.LoadFailed')" />
    <div class="member-subscription-page__table">
      <el-table v-loading="isLoading" :data="rows" row-key="definitionId" height="100%">
        <el-table-column prop="definitionId" :label="$t('MemberSubscription.DefinitionId')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="subscriptionName" :label="$t('MemberSubscription.Name')" min-width="160" show-overflow-tooltip />
        <el-table-column prop="description" :label="$t('MemberSubscription.Description')" min-width="200" show-overflow-tooltip />
        <el-table-column :label="$t('MemberSubscription.Status')" width="110"><template #default="{ row }"><dict-tag :options="EnableOrDisable" :value="row.status" /></template></el-table-column>
        <el-table-column :label="$t('MemberSubscription.Items')" min-width="200">
          <template #default="{ row }"><el-tag v-for="id in row.serviceIds" :key="id" class="mr5" size="small">{{ itemLabels.get(id) || id }}</el-tag></template>
        </el-table-column>
        <el-table-column :label="$t('Common.Operation')" width="260" fixed="right">
          <template #default="{ row }">
            <el-button v-hasPermi="['member:subscription:definition:edit']" link type="success" icon="Edit" @click="handleEdit(row)">{{ $t('Common.Edit') }}</el-button>
            <el-button v-hasPermi="['member:subscription:definition:edit']" link :type="row.status === '0' ? 'warning' : 'success'" :loading="changingStatusId === row.definitionId" :disabled="Boolean(deletingId || changingStatusId)" @click="handleStatus(row)">{{ $t(row.status === '0' ? 'MemberSubscription.Disable' : 'MemberSubscription.Enable') }}</el-button>
            <el-button v-hasPermi="['member:subscription:definition:remove']" link type="danger" icon="Delete" :disabled="Boolean(deletingId || changingStatusId)" @click="handleDelete(row)">{{ $t('Common.Delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <pagination v-show="total > 0" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @pagination="loadList" />
    <member-subscription-definition-dialog v-model="isFormOpen" :subscription-id="editingId" @success="loadList" />
  </div>
</template>

<script setup name="MemberSubscriptionDefinitionIndex">
import {
  listMemberSubscriptionDefinitions,
  delMemberSubscriptionDefinition, changeMemberSubscriptionDefinitionStatus
} from '@/api/member/subscription'
import { listMemberSubscribeServiceOptions } from '@/api/member/subscribeService'
import { checkPermi } from '@/utils/permission'
import MemberSubscriptionDefinitionDialog from '../components/MemberSubscriptionDefinitionDialog.vue'

const createQuery = () => ({
  pageNum: 1,
  pageSize: 20,
  subscriptionName: ''
})
const {
  proxy
} = getCurrentInstance()
const { EnableOrDisable } = proxy.useDict('EnableOrDisable')
const changingStatusId = ref('')
const queryRef = ref()
const query = reactive(createQuery())
const rows = ref([])
const total = ref(0)
const isLoading = ref(false)
const hasFailed = ref(false)
const showSearch = ref(true)
const isFormOpen = ref(false)
const editingId = ref('')
const deletingId = ref('')
const items = ref([])
const itemLabels = computed(() => new Map(items.value.map(item => [item.value, item.label])))
let requestVersion = 0

onMounted(loadList)
onBeforeUnmount(() => {
  requestVersion += 1
})

defineExpose({ refresh: loadList })

async function loadItems(records, version) {
  const ids = [...new Set(records.flatMap(row => row.serviceIds ?? []))]
  const requests = []
  for (let i = 0; i < ids.length; i += 100) requests.push(listMemberSubscribeServiceOptions({ ids: ids.slice(i, i + 100) }))
  try {
    const responses = await Promise.all(requests)
    if (version === requestVersion) items.value = responses.flatMap(response => response.data ?? [])
  } catch {
    if (version === requestVersion) hasFailed.value = true
  }
}

async function loadList() {
  const version = ++requestVersion
  isLoading.value = true
  hasFailed.value = false
  try {
    const response = await listMemberSubscriptionDefinitions({
      ...query
    })
    if (version !== requestVersion) return
    rows.value = response.data.rows ?? []
    total.value = Number(response.data.total ?? 0)
    await loadItems(rows.value, version)
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
  Object.assign(query, createQuery())
  return loadList()
}

function handleAdd() {
  editingId.value = ''
  isFormOpen.value = true
}

function handleEdit(row) {
  editingId.value = row.definitionId
  isFormOpen.value = true
}
async function handleStatus(row) {
  if (changingStatusId.value || deletingId.value || !checkPermi(['member:subscription:definition:edit'])) return
  const status = row.status === '0' ? '1' : '0'
  try { await proxy.$modal.confirm(proxy.$t('MemberSubscription.ConfirmStatus', [row.subscriptionName, proxy.$t(status === '0' ? 'MemberSubscription.Enable' : 'MemberSubscription.Disable')])) }
  catch { return }
  changingStatusId.value = row.definitionId
  try {
    await changeMemberSubscriptionDefinitionStatus({ subscriptionId: row.definitionId, status })
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'))
    await loadList()
  } finally { changingStatusId.value = '' }
}
async function handleDelete(row) {
  if (deletingId.value || changingStatusId.value || !checkPermi(['member:subscription:definition:remove'])) return
  try {
    await proxy.$modal.confirm(proxy.$t('MemberSubscription.ConfirmDelete', [row.subscriptionName]))
  } catch {
    return
  }
  deletingId.value = row.definitionId
  try {
    await delMemberSubscriptionDefinition(row.definitionId)
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'))
    await loadList()
  } finally {
    deletingId.value = ''
  }
}
</script>

<style scoped lang="scss">
.member-subscription-page { display: flex; flex-direction: column; height: 100%; min-height: 0; }
.member-subscription-page__table { flex: 1; min-height: 240px; }
</style>
