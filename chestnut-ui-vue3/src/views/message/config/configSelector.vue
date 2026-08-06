<template>
  <div class="notify-config-selector-container">
    <el-dialog 
      :title="$t('Message.Config.SelectorTitle')"
      v-model="visible"
      width="800px"
      :close-on-click-modal="false"
      append-to-body>
      <el-form 
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        class="el-form-search">
        <el-form-item prop="type">
          <el-select v-model="queryParams.type" :placeholder="$t('Message.Config.Type')" style="width: 120px;" :disabled="!!props.type" clearable>
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item prop="name">
          <el-input v-model="queryParams.name" :placeholder="$t('Message.Config.Name')" />
        </el-form-item>
        <el-form-item>
          <el-button-group>
            <el-button type="primary" icon="Search" @click="handleQuery">{{ $t("Common.Search") }}</el-button>
            <el-button icon="Refresh" @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
          </el-button-group>
        </el-form-item>
      </el-form>
      <el-table 
        v-loading="loading"
        :height="435"
        :data="configList"
        highlight-current-row
        @cell-dblclick="handleDbClick"
        @current-change="handleSelectionChange">
        <el-table-column 
          type="index"
          :label="$t('Common.RowNo')"
          align="center"
          width="50">
          <template #default="scope">
            {{ $tools.tableRowNo(queryParams.pageNum, queryParams.pageSize, scope.$index + 1) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('Message.Config.Type')" align="left" prop="type" />
        <el-table-column :label="$t('Message.Config.Name')" align="left" prop="name" />
      </el-table>
      <pagination
        v-show="total>0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="loadConfigList"
      />
      <template #footer>
        <el-button type="primary" :disabled="okBtnDisabled" @click="handleOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleCancel">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="MessageConfigSelector">
import * as messageConfigApi from "@/api/tool/message/config";

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  type: {
    type: String,
    required: false,
  }
})

const emit = defineEmits(['update:open', 'ok', 'cancel'])

const { proxy } = getCurrentInstance()

const loading = ref(false)
const visible = ref(props.open)
const inited = ref(false)
const selectedConfig = ref(undefined)
const configList = ref([])
const total = ref(0)
const objects = reactive({
  queryParams: {
    type: props.type,
    name: undefined,
    pageSize: 10,
    pageNum: 1
  }
})
const { queryParams } = toRefs(objects)


const typeOptions = ref([])

const okBtnDisabled = computed(() => {
  return selectedConfig.value === undefined
})

watch(() => props.open, (newVal) => {
  visible.value = newVal
})

watch(() => props.type, (newVal) => {
  if (queryParams.value.type !== newVal) {
    queryParams.value.type = newVal
    queryParams.value.pageNum = 1
    if (inited.value) loadConfigList()
  }
})

watch(visible, (newVal) => {
  if (!newVal) {
    handleCancel()
  } else {
    if (!inited.value) {
      inited.value = true
      loadTypeOptions();
      loadConfigList()
    }
  }
})

function loadTypeOptions() {
  messageConfigApi.getTypeOptions().then(response => {
    typeOptions.value = response.data;
  });
}

function loadConfigList() {
  if (!inited.value) {
    return
  }
  loading.value = true
  messageConfigApi.getConfigList(queryParams.value).then(response => {
    configList.value = response.data.rows
    total.value = response.data.total
    selectedConfig.value = undefined
    loading.value = false
  })
}

function handleSelectionChange(selection) {
  if (selection) {
    selectedConfig.value = selection
  } else {
    selectedConfig.value = undefined
  }
}

function handleDbClick(row) {
  emit("ok", row)
}

function handleOk() {
  emit("ok", selectedConfig.value)
}

function handleCancel() {
  emit("update:open", false)
  emit("cancel")
  queryParams.value.name = undefined
  queryParams.value.type = props.type
}

function handleQuery() {
  loadConfigList()
}

function resetQuery() {
  proxy.resetForm("queryFormRef")
  queryParams.value.name = undefined
  queryParams.value.type = props.type
  handleQuery()
}
</script>
