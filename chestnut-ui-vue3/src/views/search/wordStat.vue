<template>
  <div class="">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button 
          type="primary"
          icon="Plus"
          plain
          @click="handleAdd">{{ $t("Common.Add") }}</el-button>
      </el-col>
          <el-col :span="1.5">
            <el-button 
              plain
              type="success"
              icon="Edit"
              :disabled="single"
              @click="handleEdit">{{ $t("Common.Edit") }}</el-button>
          </el-col>
      <el-col :span="1.5">
        <el-button 
          type="danger"
          icon="Delete"
          plain
          :disabled="multiple"
          @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="loadWordStatList"></right-toolbar>
    </el-row>
    <el-row v-show="showSearch">
      <el-form :model="queryParams" ref="queryFormRef" class="el-form-search" :inline="true">
        <el-form-item prop="query">
          <el-input
            v-model="queryParams.query"
            :placeholder="$t('Search.WordStat.Placeholder.Word')"
            clearable
            style="width: 240px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button-group>
            <el-button type="primary" icon="Search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
            <el-button icon="Refresh" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
          </el-button-group>
        </el-form-item>
      </el-form>
    </el-row>

    <el-table ref="tableWordListRef" :height="tableHeight" :max-height="tableMaxHeight" v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="$t('Search.WordStat.Word')" align="left" prop="word" />
      <el-table-column :label="$t('Search.WordStat.Total')" align="center" prop="searchTotal" width="200" />
      <el-table-column :label="$t('Search.WordStat.Topping')" align="center" width="100">
          <template #default="scope">
            <svg-icon icon-class="top" style="color: #11A983;" v-if="scope.row.topFlag > 0" />
          </template>
      </el-table-column>
      <el-table-column :label="$t('Search.WordStat.TopEndTime')" align="center" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.topDate) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" align="left" width="320">
        <template #default="scope">
          <el-button type="text" icon="Edit" @click="handleEdit(scope.row)">{{ $t('Common.Edit') }}</el-button>
          <el-button type="text" icon="Top" @click="handleSetTop(scope.row)" v-if="scope.row.topFlag==0">{{ $t('Search.WordStat.Topping') }}</el-button>
          <el-button type="text" icon="Bottom" @click="handleCancelTop(scope.row)" v-if="scope.row.topFlag>0">{{ $t('Search.WordStat.CancelTop') }}</el-button>
          <el-button type="text" icon="DataLine" @click="handleCharts(scope.row)">{{ $t('Search.WordStat.Trend') }}</el-button>
          <span class="ml5">
            <el-dropdown @command="(command) => handleCommand(command, scope.row)">
              <el-button type="text" icon="More"></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="handleAddExtWord" icon="Plus">{{ $t('Search.WordStat.AddExtWord') }}</el-dropdown-item>
                  <el-dropdown-item command="handleAddStopWord" icon="Plus">{{ $t('Search.WordStat.AddStopWord') }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </span>
        </template>
      </el-table-column>
    </el-table>
    <pagination 
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="loadWordStatList" />

    <!-- 搜索词表单弹窗 -->
    <el-dialog 
      :title="title"
      v-model="open"
      :close-on-click-modal="false"
      width="500px"
      append-to-body>
      <el-form ref="formRef" :model="form" label-width="120px" :rules="rules">
        <el-form-item :label="$t('Search.WordStat.Word')" prop="word">
          <el-input v-model="form.word" />
        </el-form-item>
        <el-form-item :label="$t('Search.WordStat.Total')" prop="searchTotal">
          <el-input-number v-model="form.searchTotal" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>

    <!-- 置顶时间设置弹窗 -->  
    <el-dialog 
      :title="$t('Search.WordStat.Topping')"
      width="400px"
      v-model="topDialogVisible"
      :close-on-click-modal="false"
      append-to-body>
      <el-form ref="topFormRef" label-width="140px" :model="topForm">
        <el-form-item :label="$t('Search.WordStat.TopEndTime')" prop="topEndTime">
          <el-date-picker 
            v-model="topForm.topEndTime" 
            :disabled-date="topEndTimePickerOptions.disabledDate"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="datetime">
          </el-date-picker>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleTopDialogOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="topDialogVisible=false">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>

    <!-- 搜索趋势图表 -->
    <el-dialog 
      :title="$t('Search.WordStat.TrendDialogTitle')"
      v-model="openTrend"
      :close-on-click-modal="false"
      width="1000px"
      append-to-body>
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-date-picker
            v-model="chartDateRange"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="datetimerange"
            range-separator="-"
            :clearable="false"
            :start-placeholder="$t('Common.BeginDate')"
            :end-placeholder="$t('Common.EndDate')"
            style="width:335px"
          ></el-date-picker>
        </el-col>
        <el-col :span="1.5">
          <el-button 
            type="primary"
            icon="Search"
            @click="loadLineChartDatas">{{ $t("Common.Search") }}</el-button>
        </el-col>
      </el-row>
      <line-chart :chart-data="lineChartData" />
      <template #footer>
        <el-button @click="handleChartsClose">{{ $t("Common.Close") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="SearchWordStat">
import { getSiteSearchWordList, addSiteSearchWord, editSearchWord, deleteSearchWords, setTop, cancelTop, getTrendDatas } from "@/api/search/wordstat"
import { addDictWord } from "@/api/search/dictword"
import LineChart from '@/views/dashboard/LineChart'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const showSearch = ref(true)
const tableHeight = ref(600)
const tableMaxHeight = ref(600)
const total = ref(0)
const dataList = ref([])
const selectedRows = ref([])
const single = ref(true)
const multiple = ref(true)
const title = ref("")
const open = ref(false)
const form = reactive({})
const rules = reactive({
  word: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  searchTotal: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
})
const topDialogVisible = ref(false)
const openTrend = ref(false)
const queryParams = reactive({
  pageNum: 1,
  pageSize: 50,
  beginTime: undefined,
  endTime: undefined
})
const topForm = reactive({
  topEndTime: undefined
})
const topEndTimePickerOptions = reactive({
  disabledDate(time) {
    return time.getTime() < Date.now() - 8.64e7 //如果没有后面的-8.64e7就是不可以选择今天的 
  }
})
const dateRange = ref([])
const chartDateRange = ref([])
const chartWordId = ref(undefined)
const lineChartData = reactive({
  xAxisDatas:[],
  datas: []
})

onMounted(() => {
  changeTableHeight()
  loadWordStatList()
})

const changeTableHeight = () => {
  let height = document.body.offsetHeight // 网页可视区域高度
  tableHeight.value = height - 330
  tableMaxHeight.value = tableHeight.value
}

const loadWordStatList = () => {
  loading.value = true
  queryParams.beginTime = dateRange.value[0]
  queryParams.endTime = dateRange.value[1]
  getSiteSearchWordList(queryParams).then(response => {
    dataList.value = response.data.rows
    total.value = parseInt(response.data.total)
    loading.value = false
  })
}

const handleQuery = () => {
  queryParams.pageNum = 1
  loadWordStatList()
}

const resetQuery = () => {
  proxy.resetForm("queryFormRef")
  queryParams.pageNum = 1
  dateRange.value = []
  handleQuery()
}

const handleCharts = (row) => {
  var endDate = proxy.parseTime(new Date())
  var startDate = proxy.parseTime(new Date(new Date().getTime() - 3600 * 24 * 7 * 1000))
  chartDateRange.value = [ startDate, endDate ]
  chartWordId.value = row.wordId
  openTrend.value = true
  loadLineChartDatas()
}

const loadLineChartDatas = () => {
  const params = { wordId: chartWordId.value, beginTime: chartDateRange.value[0], endTime: chartDateRange.value[1] }
  getTrendDatas(params).then(response => {
    lineChartData.xAxisDatas = response.data.xAxisDatas
    lineChartData.datas = response.data.lineDatas
  })
}

const handleChartsClose = () => {
  openTrend.value = false
  resetQuery()
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection.map(item => item)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

const handleAdd = () => {
  reset()
  title.value = proxy.$t('Search.WordStat.AddTitle')
  open.value = true
}

const handleEdit = (row) => {
  const data = row.wordId ? row : selectedRows.value[0]
  reset()
  title.value = proxy.$t('Search.WordStat.EditTitle')
  Object.assign(form, data)
  open.value = true
}

const submitForm = () => {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.wordId) {
        editSearchWord(form).then(response => {
          proxy.$modal.msgSuccess(response.msg)
          open.value = false
          loadWordStatList()
        })
      } else {
        addSiteSearchWord(form).then(response => {
          proxy.$modal.msgSuccess(response.msg)
          open.value = false
          loadWordStatList()
        })
      }
    }
  })
}

const reset = () => {
  Object.assign(form, {})
}

const cancel = () => {
  open.value = false
  reset()
}

const handleDelete = (row) => {
  const wordIds = row.wordId ? [ row.wordId ] : selectedRows.value.map(item => item.wordId)
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function() {
    return deleteSearchWords(wordIds)
  }).then((response) => {
    proxy.$modal.msgSuccess(response.msg)
    loadWordStatList()
  }).catch(() => {})
}

const toggleAllCheckedRows = () => {
  selectedRows.value.forEach(row => {
    proxy.$refs.tableWordListRef.toggleRowSelection(row, false)
  })
  selectedRows.value = []
}

const handleSetTop = (row) => {
  if (row.wordId) {
    toggleAllCheckedRows()
    selectedRows.value.push(row)
  }
  topDialogVisible.value = true
}

const handleTopDialogOk = () => {
  const wordIds = selectedRows.value.map(item => item.wordId)
  if (wordIds.length == 0) {
    proxy.$modal.msgWarning(proxy.$t('Common.SelectFirst'))
    return
  }
  proxy.$refs["topFormRef"].validate(valid => {
    if (valid) {
      setTop({ wordIds: wordIds, topEndTime: topForm.topEndTime }).then(response => {
        proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'))
        topDialogVisible.value = false
        topForm.topEndTime = undefined
        loadWordStatList()
      })
    }
  })
}

const handleCancelTop = (row) => {
  const wordIds = row.wordId ? [ row.wordId ] : selectedRows.value.map(item => item.wordId)
  cancelTop(wordIds).then(response => {
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'))
    loadWordStatList()
  })
}

const handleCommand = (command, row) => {
  if (command === "handleAddExtWord") {
    handleAddExtWord(row)
  } else if (command === "handleAddStopWord") {
    handleAddStopWord(row)
  }
}

const handleAddExtWord = (row) => {
  const form = { wordType: "WORD", words: [ row.word ] }
  proxy.$modal.confirm(proxy.$t('Search.WordStat.ConfirmAddToDict')).then(function() {
    return addDictWord(form)
  }).then((response) => {
    proxy.$modal.msgSuccess(response.msg)
  }).catch(() => {})
}

const handleAddStopWord = (row) => {
  const form = { wordType: "STOP", words: [ row.word ] }
  proxy.$modal.confirm(proxy.$t('Search.WordStat.ConfirmAddToDict')).then(function() {
    return addDictWord(form)
  }).then((response) => {
    proxy.$modal.msgSuccess(response.msg)
  }).catch(() => {})
}
</script>