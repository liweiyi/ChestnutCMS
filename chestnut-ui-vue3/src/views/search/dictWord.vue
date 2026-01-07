<template>
  <div class="app-container">
    <el-row :gutter="24" class="mb8">
      <el-col :span="12">
        <el-button 
          type="primary"
          icon="Plus"
          plain
          @click="handleAdd">{{ $t("Common.Add") }}</el-button>
        <el-button 
          type="danger"
          icon="Delete"
          plain
          :disabled="selectedIds.length==0"
          @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
        <el-button 
          type="success"
          icon="MagicStick"
          plain
          @click="handleWordAnalyzeTest">{{ $t('Search.DictWord.AnalyzeTest') }}</el-button>
      </el-col>
      <el-col :span="12" style="text-align:right">
        <el-form 
          :model="queryParams"
          ref="queryFormRef"
          :inline="true"
          class="el-form-search">
          <el-form-item prop="query">
            <el-input v-model="queryParams.query">
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button-group>
              <el-button 
                type="primary"
                icon="Search"
                @click="handleQuery">{{ $t("Common.Search") }}</el-button>
              <el-button 
                icon="Refresh"
                @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
            </el-button-group>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
    <el-table 
      v-loading="loading"
      :data="wordList"
      @selection-change="handleSelectionChange">
      <el-table-column 
        type="selection"
        width="50"
        align="center" />
      <el-table-column 
        type="index"
        :label="$t('Common.RowNo')"
        align="center"
        width="100" />
    <el-table-column :label="$t('Search.DictWord.Type')" align="center" prop="wordType" width="100">
      <template #default="scope">
        <dict-tag :options="SearchDictWordType" :value="scope.row.wordType"/>
      </template>
    </el-table-column>
      <el-table-column :label="$t('Search.DictWord.Word')" align="left" prop="word" />
      <el-table-column :label="$t('Common.CreateTime')" align="center" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.CreateBy')" align="center" width="160" prop="createBy" />
      <el-table-column :label="$t('Common.Operation')" align="center" width="180">
        <template #default="scope">
          <el-button 
            type="text"
            icon="Delete"
            @click="handleDelete(scope.row)">{{ $t("Common.Delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="loadWordList"
    />
    <!-- 添加弹窗 -->
    <el-dialog 
      :title="$t('Search.DictWord.AddTitle')"
      v-model="open"
      :close-on-click-modal="false"
      width="500px"
      append-to-body>
      <el-form 
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="40px">
        <el-form-item 
          :label="$t('Search.DictWord.Type')"
          prop="wordType">
          <el-select v-model="form.wordType" clearable>
            <el-option
              v-for="dict in SearchDictWordType"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item 
          :label="$t('Search.DictWord.Word')"
          prop="wordStr">
          <el-input type="textarea" :rows="5" v-model="form.wordStr" :placeholder="$t('Search.DictWord.WordPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
    <!-- 分词测试弹窗 -->
    <el-dialog 
      :title="$t('Search.DictWord.WordAnalyzeTitle')"
      v-model="openAnalyze"
      :close-on-click-modal="false"
      width="500px"
      label-position="top"
      append-to-body>
      <el-form 
        ref="formAnalyzeRef"
        label-position="top"
        v-loading="loadingAnalyze"
        :model="formAnalyze"
        :rules="rulesAnalyze">
        <el-form-item :label="$t('Search.DictWord.WordAnalyzeType')" prop="type">
          <el-select v-model="formAnalyze.type" clearable>
            <el-option
              v-for="dict in WordAnalyzeType"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('Search.DictWord.WordAnalyzeText')" prop="text">
          <el-input type="textarea" :rows="3" v-model="formAnalyze.text" :placeholder="$t('Search.DictWord.WordAnalyzePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('Search.DictWord.WordAnalyzeResult')" prop="result">
          <el-input type="textarea" :rows="10" v-model="analyzeResult" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleWordAnalyze">{{ $t('Search.DictWord.WordAnalyzeTest') }}</el-button>
        <el-button @click="openAnalyze=false">{{ $t("Common.Close") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="SearchDictWord">
import { getDictWords, addDictWord, deleteDictWords, wordAnalyze } from "@/api/search/dictword"

const { proxy } = getCurrentInstance()
const { SearchDictWordType, WordAnalyzeType } = proxy.useDict('SearchDictWordType', 'WordAnalyzeType')

const loading = ref(false)
const selectedIds = ref([])
const wordList = ref([])
const total = ref(0)
const open = ref(false)
const queryParams = reactive({
  query: undefined,
  pageSize: 20,
  pageNum: 1
})
const form = reactive({})
const rules = reactive({
  word: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ]
})
const loadingAnalyze = ref(false)
const openAnalyze = ref(false)
const formAnalyze = reactive({})
const rulesAnalyze = reactive({
  type: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  text: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ]
})
const analyzeResult = ref("")

onMounted(() => {
  loadWordList()
})

const handleWordAnalyzeTest = () => {
  openAnalyze.value = true
}

const handleWordAnalyze = () => {
  proxy.$refs["formAnalyzeRef"].validate(valid => {
    if (valid) {
      loadingAnalyze.value = true
      wordAnalyze(formAnalyze).then(response => {
        loadingAnalyze.value = false
        analyzeResult.value = response.data
      })
    }
  })
}

const loadWordList = () => {
  loading.value = true
  getDictWords(queryParams).then(response => {
    wordList.value = response.data.rows
    total.value = parseInt(response.data.total)
    loading.value = false
  })
}

const cancel = () => {
  open.value = false
  reset()
}

const reset = () => {
  Object.assign(form, {})
}

const handleQuery = () => {
  queryParams.pageNum = 1
  loadWordList()
}

const resetQuery = () => {
  proxy.resetForm("queryFormRef")
  handleQuery()
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.wordId)
}

const handleAdd = () => {
  reset()
  open.value = true
}

const submitForm = () => {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      form.words = form.wordStr.split("/n")
      form.wordStr = ""
      addDictWord(form).then(response => {
        proxy.$modal.msgSuccess(response.msg)
        open.value = false
        resetQuery()
      })
    }
  })
}

const handleDelete = (row) => {
  const wordIds = row.wordId ? [ row.wordId ] : selectedIds.value
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function() {
    return deleteDictWords(wordIds)
  }).then((response) => {
    proxy.$modal.msgSuccess(response.msg)
    resetQuery()
  }).catch(() => {})
}
</script>