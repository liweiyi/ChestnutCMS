<template>
  <div class="tag-word-container">
    <el-row :gutter="24">
      <el-col :span="4" :xs="24">
        <cms-tag-word-group-tree 
          ref="groupTreeRef"
          :new-btn="true"    
          @node-click="handleTreeNodeClick">
        </cms-tag-word-group-tree>
      </el-col>
      <el-col :span="20" :xs="24">
        <el-row :gutter="24">
          <el-col :span="12">
            <el-button 
              type="primary"
              icon="Plus"
              plain
              :disabled="selectedGroupId==''"
              @click="handleAdd">{{ $t("Common.Add") }}</el-button>
            <el-button 
              type="primary"
              icon="Plus"
              plain
              :disabled="selectedGroupId==''"
              @click="handleBatchAdd">{{ $t("Common.BatchAdd") }}</el-button>
            <el-button 
              type="danger"
              icon="Delete"
              plain
              :disabled="selectedGroupId==''||selectedIds.length==0"
              @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
          </el-col>
          <el-col :span="12" style="text-align: right;">
            <el-form 
              :model="queryParams"
              ref="queryFormRef"
              :inline="true"
              class="el-form-search">
              <el-form-item prop="query">
                <el-input v-model="queryParams.query" :placeholder="$t('WordMgr.TAG.Placeholder.InputTAG')">
                </el-input>
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
          <el-table 
            v-loading="loading"
            :data="wordList"
            @selection-change="handleSelectionChange"
            @row-dblclick="handleEdit">
            <el-table-column type="selection" width="50" align="center" />
            <el-table-column type="index" :label="$t('Common.RowNo')" align="center" width="50" />
            <el-table-column :label="$t('WordMgr.TAG.TAGWord')" align="left" prop="word" />
            <el-table-column :label="$t('Common.Sort')" align="center" prop="sortFlag" width="120"/>
            <!-- <el-table-column :label="$t('WordMgr.TAG.TAGWordUseCount')" align="center" prop="useCount" width="120"/> -->
            <el-table-column :label="$t('WordMgr.TAG.TAGWordHitCount')" align="center" prop="hitCount" width="120"/>
            <el-table-column :label="$t('Common.CreateTime')" align="center" width="160">
              <template #default="scope">
                <span>{{ parseTime(scope.row.createTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('Common.CreateBy')" align="center" width="120" prop="createBy" />
            <el-table-column :label="$t('Common.Operation')" align="center" width="180">
              <template #default="scope">
                <el-button 
                  type="text"
                  icon="Edit"
                  @click="handleEdit(scope.row)">{{ $t('Common.Edit') }}</el-button>
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
      </el-col>
    </el-row>
    <!-- 添加TAG词弹窗 -->
    <el-dialog 
      :title="diagTitle"
      v-model="open"
      :close-on-click-modal="false"
      width="500px"
      append-to-body>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px">
        <el-form-item :label="$t('WordMgr.TAG.TAGWord')" prop="word">
          <el-input v-model="form.word" />
        </el-form-item>
        <el-form-item :label="$t('Common.Sort')" prop="sortFlag">
          <el-input-number v-model="form.sortFlag" />
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="form.remark" />
        </el-form-item>
        <el-form-item :label="$t('WordMgr.TAG.TAGWordLogo')" prop="logo">
          <cms-logo-view v-model="form.logo" :width="218" :height="150"></cms-logo-view>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="closeDialog(false)">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
    <!-- 批量添加TAG词弹窗 -->
    <el-dialog :title="$t('Common.BatchAdd')"
      v-model="batchAddOpen"
      :close-on-click-modal="false"
      width="500px"
      append-to-body>
      <el-form
        ref="formBatchRef"
        :model="formBatch"
        :rules="rulesBatch"
        label-width="80px">
        <el-form-item :label="$t('WordMgr.TAG.TAGWord')" prop="words">
          <el-input type="textarea" :rows="5" v-model="formBatch.words" :placeholder="$t('WordMgr.TAG.Placeholder.BatchAdd')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitBatchAddForm">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleBatchAddClose(false)">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CmsTagWord">
import { getTagWordList, addTagWord, batchAddTagWords, editTagWord, deleteTagWord } from "@/api/word/tagWord";
import CmsTagWordGroupTree from '@/views/cms/word/tagWordGroupTree';
import CmsLogoView from '@/views/cms/components/LogoView';

const { proxy } = getCurrentInstance()

const loading = ref(false);
const selectedGroupId = ref('');
const selectedIds = ref('');
const wordList = ref([]);
const total = ref(0);
const batchAddOpen = ref(false);
const diagTitle = ref("");
const open = ref(false);
const rulesBatch = {
  words: [
    { required: true, message: proxy.$t("Common.RuleTips.NotEmpty"), trigger: "blur" },
  ],
};
const rules = {
  word: [
    { required: true, message: proxy.$t("Common.RuleTips.NotEmpty"), trigger: "blur" },
    { max: 255, message: proxy.$t('Common.RuleTips.MaxLength', [ 255 ]), trigger: [ "blur", "change" ] },
  ],
  sortFlag: [
    { required: true, message: proxy.$t("Common.RuleTips.NotEmpty"), trigger: "blur" },
  ],
}

const objects = reactive({
  queryParams: {
    query: undefined,
    groupId: undefined,
    pageSize: 20,
    pageNum: 1
  },
  formBatch: {},
  form: {},
});

const { queryParams, formBatch, form } = toRefs(objects);

function handleTreeNodeClick(data) {
  selectedGroupId.value = data && data != null ? data.id : '';
  if (selectedGroupId.value !== queryParams.value.groupId) {
    queryParams.value.groupId = selectedGroupId.value;
    if (selectedGroupId.value != '') {
      loadWordList();
    }
  }
}
function loadWordList () {
  loading.value = true;
  getTagWordList(queryParams.value).then(response => {
    wordList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
}
function reset () {
  form.value = {};
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
function closeDialog(loadList) {
  open.value = false;
  reset();
  loadList && loadWordList();
}
function handleBatchAdd () {
  formBatch.value = {};
  batchAddOpen.value = true;
}
function handleBatchAddClose(loadList) {
  batchAddOpen.value = false;
  formBatch.value = {};
  loadList && loadWordList();
}
function submitBatchAddForm () {
  proxy.$refs["formBatchRef"].validate(valid => {
    if (valid) {
      batchAddTagWords({ groupId: selectedGroupId.value, words: formBatch.value.words.split("\n") }).then(response => {
        if (response.code == 200) {
          proxy.$modal.msgSuccess(response.msg);
          handleBatchAddClose(true);
        }
      });
    }
  });
}
function handleAdd () {
  reset();
  diagTitle.value = proxy.$t("WordMgr.TAG.AddTAGTitle");
  open.value = true;
}
function handleEdit (row) {
  reset();
  form.value = row;
  diagTitle.value = proxy.$t("WordMgr.TAG.EditTAGTitle");
  open.value = true;
}
function submitForm () {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      form.value.groupId = selectedGroupId.value;
      if (form.value.wordId) {
        editTagWord(form.value).then(response => {
          if (response.code == 200) {
            proxy.$modal.msgSuccess(response.msg);
            closeDialog(true);
          }
        }); 
      } else {
        addTagWord(form.value).then(response => {
          if (response.code == 200) {
            proxy.$modal.msgSuccess(response.msg);
            closeDialog(true);
          }
        }); 
      }
    }
  });
}
function handleDelete (row) {
  const wordIds = row.wordId ? [ row.wordId ] : selectedIds.value;
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function() {
    return deleteTagWord(wordIds);
  }).then((response) => {
    proxy.$modal.msgSuccess(response.msg);
    loadWordList();
  }).catch(() => {});
}
</script>