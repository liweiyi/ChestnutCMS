<template>
  <div class="app-container">
    <el-dialog 
      :title="$t('CMS.Template.SelectorTitle')"
      v-model="visible"
      width="800px"
      :close-on-click-modal="false"
      append-to-body>
      <el-form 
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        class="el-form-search">
        <el-form-item prop="filename">
          <el-input v-model="queryParams.filename" :placeholder="$t('CMS.Template.Name')">
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button-group>
            <el-button 
              type="primary"
              @click="handleQuery">
              <Search />
              {{ $t("Common.Search") }}
            </el-button>
            <el-button 
              @click="resetQuery">
              <Refresh />
              {{ $t("Common.Reset") }}
            </el-button>
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
            {{ tableRowNo(queryParams.pageNum, queryParams.pageSize, scope.$index + 1) }}
          </template>
        </el-table-column>
        <el-table-column 
          :label="$t('CMS.Template.Name')"
          align="left"
          prop="path">
          <template #default="scope">
            <span v-html="scope.row.displayPath"></span>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="total>0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
      <template #footer>
        <el-button type="primary" :disabled="okBtnDisabled" @click="handleOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleCancel">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CmsTemplateSelector">
import { getTemplateList } from "@/api/contentcore/template";

const props = defineProps({
  publishPipeCode: {
    type: String,
    default: "",
    required: true
  },
  open: {
    type: Boolean,
    default: false,
    required: true
  },
  siteId: {
    type: String,
    default: "0",
    required: false,
  }
})

const emit = defineEmits(['ok', 'cancel'])

const { proxy } = getCurrentInstance()

// 遮罩层
const loading = ref(false)
const visible = ref(props.open)
const inited = ref(false)
// 选中数组
const selectedTemplate = ref(undefined)
// 资源表格数据
const templateList = ref([])
const total = ref(0)
// 查询参数
const queryParams = reactive({
  publishPipeCode: props.publishPipeCode,
  filename: undefined,
  siteId: props.siteId,
  pageSize: 10,
  pageNum: 1
})

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
      getList()
    }
  }
})

watch(() => props.publishPipeCode, (newVal) => {
  if (queryParams.publishPipeCode !== newVal) {
    queryParams.publishPipeCode = newVal
    queryParams.pageNum = 1
    if (inited.value) getList()
  }
})

watch(() => props.siteId, (newVal) => {
  if (queryParams.siteId !== newVal) {
    queryParams.siteId = newVal
    queryParams.pageNum = 1
    if (inited.value) getList()
  }
})

const getList = () => {
  if (!inited.value) {
    return
  }
  loading.value = true
  getTemplateList(queryParams).then(response => {
    templateList.value = response.data.rows.map(item => {
      let arr = item.path.split("/")
      if (arr.length > 1) {
        for(let i = 0; i < arr.length - 1; i++) {
          item.displayPath = '<span style="color: #1890ff">' + arr[i] + '</span> / '
        }
        item.displayPath += arr[arr.length - 1]
      } else {
        item.displayPath = arr[0]
      }
      return item
    })
    total.value = parseInt(response.data.total)
    selectedTemplate.value = undefined
    loading.value = false
  })
}

const handleSelectionChange = (selection) => {
  if (selection) {
    selectedTemplate.value = selection.path
  }
}

const handleDbClick = (row) => {
  emit("ok", row.path)
}

const handleOk = () => {
  emit("ok", selectedTemplate.value)
}

const handleCancel = () => {
  emit("cancel")
  queryParams.filename = undefined
}

const handleQuery = () => {
  getList()
}

const resetQuery = () => {
  proxy.resetForm("queryFormRef")
  queryParams.filename = undefined
  handleQuery()
}
</script>