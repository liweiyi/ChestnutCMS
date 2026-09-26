<template>
  <div class="app-container">
    <el-form v-show="showSearch" :model="queryParams" class="el-form-search" :inline="true" @submit.prevent>
      <el-form-item prop="configId">
        <el-select v-model="queryParams.configId" :placeholder="$t('Monitor.Logs.CdnRefresh.ConfigName')" style="width: 220px" @change="handleQuery">
          <el-option v-for="item in configs" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item prop="type">
        <el-select v-model="queryParams.type" style="width: 160px" @change="handleQuery">
          <el-option v-for="type in queryTypes" :key="type" :label="$t(`Monitor.Logs.CdnRefresh.QueryTypes.${type}`)" :value="type" />
        </el-select>
      </el-form-item>
      <el-form-item prop="url">
        <el-input v-model="queryParams.url" clearable :placeholder="$t('Monitor.Logs.CdnRefresh.UrlPlaceholder')" style="width: 280px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-date-picker
          v-model="dateRange"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="datetimerange"
          range-separator="-"
          :start-placeholder="$t('Common.BeginTime')"
          :end-placeholder="$t('Common.EndTime')"
          style="width: 380px"
        />
      </el-form-item>
      <el-form-item>
        <el-button-group>
          <el-button type="primary" icon="Search" :disabled="!queryParams.configId" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
          <el-button icon="Refresh" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
        </el-button-group>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="info" plain icon="Close" @click="handleClose">{{ $t('Common.Close') }}</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>
    <el-alert class="mb8" :title="$t('Monitor.Logs.CdnRefresh.QueryTip')" type="info" :closable="false" show-icon />
    <el-alert v-if="configsLoaded && configs.length === 0" class="mb8" :title="$t('Monitor.Logs.CdnRefresh.NoConfig')" type="warning" :closable="false" show-icon />

    <el-table v-loading="loading" :data="dataList">
      <el-table-column :label="$t('Common.RowNo')" type="index" align="center" width="60" />
      <el-table-column :label="$t('Monitor.Logs.CdnRefresh.Url')" prop="url" min-width="360" show-overflow-tooltip />
      <el-table-column :label="$t('Monitor.Logs.CdnRefresh.TaskType')" prop="taskType" width="130" align="center">
        <template #default="scope">{{ $t(`Monitor.Logs.CdnRefresh.TaskTypes.${scope.row.taskType}`) }}</template>
      </el-table-column>
      <el-table-column :label="$t('Monitor.Logs.CdnRefresh.Type')" prop="type" width="130" align="center">
        <template #default="scope">{{ $t(`Monitor.Logs.CdnRefresh.Types.${scope.row.type}`) }}</template>
      </el-table-column>
      <el-table-column :label="$t('Monitor.Logs.CdnRefresh.Time')" prop="time" width="200" align="center" />
      <el-table-column :label="$t('Monitor.Logs.CdnRefresh.Status')" prop="status" width="140" align="center">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ $t(`Monitor.Logs.CdnRefresh.Statuses.${scope.row.status}`) }}</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script setup name="MonitorLogsCdnRefresh">
import { listLogs, getConfigOptions } from '@/api/monitor/cdnRefreshLog'

const { proxy } = getCurrentInstance()
const loading = ref(false)
const showSearch = ref(true)
const dataList = ref([])
const total = ref(0)
const dateRange = ref([])
const configs = ref([])
const configsLoaded = ref(false)
const queryTypes = ['url', 'directory', 'preheat']
const queryParams = ref({ pageNum: 1, pageSize: 10, configId: undefined, type: 'url', url: undefined })
let requestId = 0

async function getList() {
  const currentRequest = ++requestId
  dataList.value = []
  total.value = 0
  if (!queryParams.value.configId) {
    loading.value = false
    return
  }
  loading.value = true
  try {
    const response = await listLogs({
      ...queryParams.value,
      beginTime: dateRange.value?.[0],
      endTime: dateRange.value?.[1]
    })
    if (currentRequest === requestId) {
      dataList.value = response.data.rows
      total.value = Number(response.data.total)
    }
  } catch {
    // 请求错误由全局拦截器提示。
  } finally {
    if (currentRequest === requestId) loading.value = false
  }
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  dateRange.value = []
  queryParams.value.type = 'url'
  queryParams.value.url = undefined
  handleQuery()
}

function statusTagType(status) {
  if (status === 'SUCCESS') return 'success'
  if (['FAILED', 'TIMEOUT', 'INVALID'].includes(status)) return 'danger'
  if (status === 'PROCESSING') return 'warning'
  return 'info'
}

function handleClose() {
  proxy.$tab.closeOpenPage({ path: '/monitor/logs' })
}

onMounted(async () => {
  try {
    const response = await getConfigOptions()
    configs.value = response.data
    configsLoaded.value = true
    queryParams.value.configId = configs.value[0]?.value
    getList()
  } catch {
    // 请求错误由全局拦截器提示。
  }
})
</script>
