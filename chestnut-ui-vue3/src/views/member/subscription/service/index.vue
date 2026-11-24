<template>
  <div class="app-container member-subscribe-service-page">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5" class="permi-wrap">
        <el-button v-hasPermi="['member:subscription:definition:add']" type="primary" plain icon="Plus" @click="handleAdd">{{ $t('MemberSubscribeService.Add') }}</el-button>
      </el-col>
      <right-toolbar v-model:show-search="showSearch" @query-table="loadList" />
    </el-row>
    <el-form v-show="showSearch" ref="queryRef" :model="query" inline class="el-form-search" @submit.prevent>
      <el-form-item prop="keyword">
        <el-input v-model.trim="query.keyword" :placeholder="$t('MemberSubscribeService.Keyword')" maxlength="100" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button-group>
          <el-button type="primary" icon="Search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
          <el-button icon="Refresh" @click="handleReset">{{ $t('Common.Reset') }}</el-button>
        </el-button-group>
      </el-form-item>
    </el-form>
    <el-alert v-if="hasFailed" class="mb8" type="error" :closable="false" :title="$t('MemberSubscription.LoadFailed')" />
    <div class="member-subscribe-service-page__table">
      <el-table v-loading="isLoading" :data="rows" row-key="serviceId" height="100%">
        <el-table-column prop="serviceId" :label="$t('MemberSubscribeService.Id')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="name" :label="$t('MemberSubscribeService.Name')" min-width="160" show-overflow-tooltip />
        <el-table-column prop="remark" :label="$t('Common.Remark')" min-width="220" show-overflow-tooltip />
        <el-table-column prop="createTime" :label="$t('Common.CreateTime')" width="180" />
        <el-table-column :label="$t('Common.Operation')" width="170" fixed="right">
          <template #default="{ row }">
            <el-button v-hasPermi="['member:subscription:definition:edit']" link type="success" icon="Edit" @click="handleEdit(row)">{{ $t('Common.Edit') }}</el-button>
            <el-button v-hasPermi="['member:subscription:definition:remove']" link type="danger" icon="Delete" :disabled="Boolean(deletingId)" @click="handleDelete(row)">{{ $t('Common.Delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <pagination v-show="total > 0" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @pagination="loadList" />
    <member-subscribe-service-dialog v-model="isFormOpen" :service-id="editingId" @success="loadList" />
  </div>
</template>

<script setup name="MemberSubscribeServiceIndex">
import { listMemberSubscribeServices, delMemberSubscribeServices } from '@/api/member/subscribeService'
import { checkPermi } from '@/utils/permission'
import MemberSubscribeServiceDialog from '../components/MemberSubscribeServiceDialog.vue'

const { proxy } = getCurrentInstance()
const createQuery = () => ({ pageNum: 1, pageSize: 20, keyword: '' })
const queryRef = ref()
const query = reactive(createQuery())
const rows = ref([])
const total = ref(0)
const showSearch = ref(true)
const isLoading = ref(false)
const hasFailed = ref(false)
const isFormOpen = ref(false)
const editingId = ref('')
const deletingId = ref('')
let requestVersion = 0

onMounted(loadList)
onBeforeUnmount(() => { requestVersion += 1 })

defineExpose({ refresh: loadList })

async function loadList() {
  const version = ++requestVersion
  isLoading.value = true
  hasFailed.value = false
  try {
    const response = await listMemberSubscribeServices({ ...query })
    if (version !== requestVersion) return
    rows.value = response.data.rows ?? []
    total.value = Number(response.data.total ?? 0)
  } catch {
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
  editingId.value = row.serviceId
  isFormOpen.value = true
}

async function handleDelete(row) {
  if (deletingId.value || !checkPermi(['member:subscription:definition:remove'])) return
  try {
    await proxy.$modal.confirm(proxy.$t('MemberSubscribeService.ConfirmDelete', [row.name]))
  } catch {
    return
  }
  deletingId.value = row.serviceId
  try {
    await delMemberSubscribeServices([row.serviceId])
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'))
    if (rows.value.length === 1 && query.pageNum > 1) query.pageNum -= 1
    await loadList()
  } finally {
    deletingId.value = ''
  }
}
</script>

<style scoped lang="scss">
.member-subscribe-service-page { display: flex; flex-direction: column; height: 100%; min-height: 0; }
.member-subscribe-service-page__table { flex: 1; min-height: 240px; }
</style>
