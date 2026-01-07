<template>
  <div class="sensitive-word-container">
    <el-row :gutter="24" class="mb8">
      <el-col :span="12">
        <el-button 
          plain
          type="primary"
          icon="Plus"
          @click="handleAdd">{{ $t("Common.Add") }}</el-button>
        <el-button 
          plain
          type="danger"
          icon="Delete"
          :disabled="selectedIds.length==0"
          @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
      </el-col>
      <el-col :span="12" style="text-align: right;">
        <el-form 
          :model="queryParams"
          ref="queryFormRef"
          :inline="true"
          class="el-form-search">
          <el-form-item prop="query">
            <el-input v-model="queryParams.query" :placeholder="$t('WordMgr.SensitiveWord.Placeholder.InputWord')" />
          </el-form-item>
          <el-form-item>
            <el-button-group>
              <el-button type="primary" icon="Search" @click="handleQuery">{{ $t("Common.Search") }}</el-button>
              <el-button icon="Refresh" @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
            </el-button-group>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
    
    <el-row>
      <el-table v-loading="loading" :data="wordList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column type="index" :label="$t('Common.RowNo')" align="center" width="100" />
        <el-table-column :label="$t('WordMgr.SensitiveWord.Word')" align="left" prop="word" />
        <el-table-column :label="$t('WordMgr.SensitiveWord.Type')" align="center" width="100" prop="type">
          <template #default="scope">
            <el-tag :type="scope.row.type==='WHITE'?'success':'warning'">{{ formatType(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
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
              plain
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
    </el-row>
    <!-- 添加弹窗 -->
    <el-dialog 
      :title="$t('WordMgr.SensitiveWord.AddHotWordTitle')"
      v-model="open"
      :close-on-click-modal="false"
      width="500px"
      append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="130px">
        <el-form-item :label="$t('WordMgr.SensitiveWord.Word')" prop="word">
          <el-input v-model="form.word" />
        </el-form-item>
        <el-form-item :label="$t('WordMgr.SensitiveWord.Type')" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio v-for="item in typeOptions" :key="item.id" :label="item.id">{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CMSSensitiveWord">
import { getSensitiveWordList, addSensitiveWord, deleteSensitiveWord } from "@/api/word/sensitiveWord";

const { proxy } = getCurrentInstance();

// 遮罩层
const loading = ref(true);
// 选中数组
const selectedIds = ref([])
// 资源表格数据
const wordList = ref([])
const total = ref(0)
// 类型选项
const typeOptions = ref([
  { id: "BLACK", label: proxy.$t('WordMgr.SensitiveWord.TypeSensitive') },
  { id: "WHITE", label: proxy.$t('WordMgr.SensitiveWord.TypeWhiteList') }
]);
// 弹窗是否打开
const open = ref(false)

const objects = reactive({
  queryParams: {
    query: undefined,
    pageSize: 20,
    pageNum: 1
  },
  form: {
    type: 'BLACK',
  },
});
const { queryParams, form } = toRefs(objects);
const rules = {
  word: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  type: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ]
}
onMounted(() => {
  loadWordList();
});
function loadWordList () {
  loading.value = true;
  getSensitiveWordList(queryParams.value).then(response => {
    wordList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
}
function formatType(type) {
  for(let i = 0; i < typeOptions.value.length; i++) {
    if (typeOptions.value[i].id === type) {
      return typeOptions.value[i].label;
    }
  }
  return "unknow";
}
function cancel () {
  open.value = false;
  reset();
}
function reset () {
  form.value = {
    type: 'BLACK',
  };
}
function handleQuery () {
  queryParams.value.pageNum = 1;
  loadWordList();
}
function resetQuery () {
  proxy.resetForm("queryFormRef");
  handleQuery();
}
function handleSelectionChange (selection) {
      selectedIds.value = selection.map(item => item.wordId);
}
function handleAdd () {
  reset();
  open.value = true;
}
function submitForm () {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      addSensitiveWord(form.value).then(response => {
        proxy.$modal.msgSuccess(response.msg);
        open.value = false;
        loadWordList();
      }); 
    }
  });
}
function handleDelete (row) {
  const wordIds = row.wordId ? [ row.wordId ] : selectedIds.value;
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function() {
    return deleteSensitiveWord(wordIds);
  }).then((response) => {
    proxy.$modal.msgSuccess(response.msg);
    loadWordList();
  }).catch(() => {});
}
</script>