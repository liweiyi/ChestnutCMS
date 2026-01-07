<template>
  <div class="error-prone-word-container">
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
      </el-col>
      <el-col :span="12" style="text-align: right;">
        <el-form
          :model="queryParams"
          ref="queryFormRef"
          :inline="true"
          class="el-form-search">
          <el-form-item prop="query">
            <el-input v-model="queryParams.query" :placeholder="$t('WordMgr.ErrorProneWord.Placeholder.InputWord')" />
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
        <el-table-column :label="$t('WordMgr.ErrorProneWord.Word')" align="left" prop="word" />
        <el-table-column :label="$t('WordMgr.ErrorProneWord.ReplaceWord')" align="left" prop="replaceWord" />
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
    </el-row>
    <!-- 添加弹窗 -->
    <el-dialog 
      :title="$t('WordMgr.ErrorProneWord.AddTitle')"
      v-model="open"
      :close-on-click-modal="false"
      width="500px"
      append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="140px">
        <el-form-item :label="$t('WordMgr.ErrorProneWord.Word')" prop="word">
          <el-input v-model="form.word" />
        </el-form-item>
        <el-form-item :label="$t('WordMgr.ErrorProneWord.ReplaceWord')" prop="replaceWord">
          <el-input v-model="form.replaceWord" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CMSErrorProneWord">
import { getErrorProneWordList, addErrorProneWord, deleteErrorProneWord } from "@/api/word/errorProneWord";

const { proxy } = getCurrentInstance();

const loading = ref(true);
const selectedIds = ref([]);
const wordList = ref([]);
const total = ref(0);
const open = ref(false);
const data = reactive({
  queryParams: {
    query: undefined,
    pageSize: 20,
    pageNum: 1
  },
  form: {},
  rules: {
    word: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
    ]
  }
});
const { queryParams, form, rules } = toRefs(data);

onMounted(() => {
  loadWordList();
});
function loadWordList() {
  loading.value = true;
  getErrorProneWordList(queryParams.value).then(response => {
    wordList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
}
function cancel() {
  open.value = false;
  reset();
}
function reset() {
  proxy.resetForm("formRef");
  form.value = {};
}
function handleQuery() {
  queryParams.value.pageNum = 1;
  loadWordList();
}
function resetQuery() {
  proxy.resetForm("queryFormRef");
  handleQuery();
}
function handleSelectionChange(selection) {
  selectedIds.value = selection.map(item => item.wordId);
}
function handleAdd() {
  reset();
  open.value = true;
}
function submitForm() {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      addErrorProneWord(form.value).then(response => {
        proxy.$modal.msgSuccess(proxy.$t('Common.Success'));
        open.value = false;
        loadWordList();
      });
    }
  });
}
function handleDelete(row) {
  const wordIds = row.wordId ? [ row.wordId ] : selectedIds.value;
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function() {
    return deleteErrorProneWord(wordIds);
  }).then(() => {
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
    loadWordList();
  }).catch(() => {});
}
</script>