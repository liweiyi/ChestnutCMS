<template>
  <div class="notify-template-selector-container">
    <el-dialog 
      :title="$t('Message.Template.SelectorTitle')"
      v-model="visible"
      width="800px"
      :close-on-click-modal="false"
      append-to-body>
      <el-form 
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        class="el-form-search">
        <el-form-item prop="name">
          <el-input v-model="queryParams.name" :placeholder="$t('Message.Template.Name')" />
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
        :data="templateList"
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
        <el-table-column :label="$t('Message.Template.Type')" align="left" prop="type" />
        <el-table-column :label="$t('Message.Template.Name')" align="left" prop="name" />
      </el-table>
      <pagination
        v-show="total>0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="loadTemplateList"
      />
      <template #footer>
        <el-button type="primary" :disabled="okBtnDisabled" @click="handleOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleCancel">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="MessageTemplateSelector">
import * as messageTemplateApi from "@/api/tool/message/template";

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
const visible = ref(false)
const inited = ref(false)
const selectedTemplate = ref(undefined)
const templateList = ref([])
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

const okBtnDisabled = computed(() => {
  return selectedTemplate.value == undefined
})

watch(() => props.open, (newVal) => {
  visible.value = newVal
})

watch(visible, (newVal) => {
  if (!newVal) {
    handleCancel()
  } else {
    if (!inited.value) {
      inited.value = true
      loadTemplateList()
    }
  }
})

watch(() => props.type, (newVal) => {
  if (queryParams.value.type !== newVal) {
    queryParams.value.type = newVal
    queryParams.value.pageNum = 1
    if (inited.value) loadTemplateList()
  }
})

function loadTemplateList() {
  if (!inited.value) {
    return
  }
  loading.value = true
  messageTemplateApi.getTemplateList(queryParams.value).then(response => {
    templateList.value = response.data.rows
    total.value = response.data.total
    selectedTemplate.value = undefined
    loading.value = false
  })
}

function handleSelectionChange(selection) {
  if (selection) {
    selectedTemplate.value = selection.templateId
  } else {
    selectedTemplate.value = undefined
  }
}

function handleDbClick(row) {
  emit("ok", row)
}

function handleOk() {
  emit("ok", selectedTemplate.value)
}

function handleCancel() {
  emit("update:open", false)
  emit("cancel")
  queryParams.value.name = undefined
}

function handleQuery() {
  loadTemplateList()
}

function resetQuery() {
  proxy.resetForm("queryFormRef")
  queryParams.value.name = undefined
  handleQuery()
}
</script>