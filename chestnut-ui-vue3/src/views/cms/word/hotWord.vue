<template>
  <div class="tag-word-container">
    <el-row :gutter="24" v-loading="loading">
      <el-col :span="4" :xs="24">
        <cms-hot-word-group-tree 
          ref="groupTreeRef"
          :new-btn="true"    
          @node-click="handleTreeNodeClick">
        </cms-hot-word-group-tree>
      </el-col>
      <el-col :span="20" :xs="24">
        <el-row :gutter="24" class="mb12">
          <el-col :span="12">
            <el-button 
              type="primary"
              icon="Plus"
              plain
              :disabled="selectedGroupId==''"
              @click="handleAdd">{{ $t("Common.Add") }}</el-button>
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
                <el-input v-model="queryParams.query" :placeholder="$t('WordMgr.HotWord.Placeholder.InputHotWord')">
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
            <el-table-column :label="$t('WordMgr.HotWord.HotWord')" align="left" prop="word" />
            <el-table-column :label="$t('WordMgr.HotWord.HotWordLink')" align="left">
              <template #default="scope">
                <el-link :href="scope.row.url" target="_blank">{{ scope.row.url }}</el-link>
              </template>
            </el-table-column>
            <el-table-column :label="$t('WordMgr.HotWord.HotWordLinkTarget')" align="center" prop="urlTarget" width="120" />
            <!-- <el-table-column :label="$t('WordMgr.HotWord.HotWordUseCount')" align="center" prop="useCount" width="120"/> -->
            <el-table-column :label="$t('WordMgr.HotWord.HotWordHitCount')" align="center" prop="hitCount" width="120"/>
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
            :page.sync="queryParams.pageNum"
            :limit.sync="queryParams.pageSize"
            @pagination="loadWordList"
          />
        </el-row>
      </el-col>
    </el-row>
    <!-- 添加热词弹窗 -->
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
        label-width="100px">
        <el-form-item :label="$t('WordMgr.HotWord.HotWord')" prop="word">
          <el-input v-model="form.word" />
        </el-form-item>
        <el-form-item :label="$t('WordMgr.HotWord.HotWordLink')" prop="url">
          <el-input v-model="form.url" />
        </el-form-item>
        <el-form-item :label="$t('WordMgr.HotWord.HotWordLinkTarget')" prop="urlTarget">
          <el-radio-group v-model="form.urlTarget">
            <el-radio label="_self">{{ $t('WordMgr.HotWord.LinkTargetSelf') }}</el-radio>
            <el-radio label="_blank">{{ $t('WordMgr.HotWord.LinkTargetBlank') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="form.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CmsHotWord">
import { validURL } from '@/utils/validate'
import { getHotWordList, addHotWord, editHotWord, deleteHotWord } from "@/api/word/hotWord";
import CmsHotWordGroupTree from '@/views/cms/word/hotWordGroupTree';

const { proxy } = getCurrentInstance();

const loading = ref(false);
const selectedGroupId = ref('');
const selectedIds = ref('');
const wordList = ref([]);
const total = ref(0);
const open = ref(false);
const diagTitle = ref("");
const objects = reactive({
  queryParams: {
    query: undefined,
    groupId: undefined,
    pageSize: 20,
    pageNum: 1
  },
  form: {},
});
const { queryParams, form } = toRefs(objects);
const rules = reactive({
  word: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    { max: 255, message: proxy.$t('Common.RuleTips.MaxLength', [ 255 ]), trigger: [ "blur", "change" ] },
  ],
  url: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    { max: 255, message: proxy.$t('Common.RuleTips.MaxLength', [ 255 ]), trigger: [ "blur", "change" ] },
    { 
      trigger: "blur", 
      validator: (rule, value, callback) => {
        if (!validURL(value)) {
          callback(new Error(proxy.$t('Common.RuleTips.Url')));
        } else {
          callback();
        }
      } 
    }
  ],
  urlTarget: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
});

function handleTabClick (tab, event) {
}
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
  getHotWordList(queryParams.value).then(response => {
    wordList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
}
function cancel () {
  open.value = false;
  reset();
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
function handleAdd () {
  reset();
  diagTitle.value = proxy.$t("WordMgr.HotWord.AddHotWordTitle")
  open.value = true;
}
function handleEdit (row) {
  reset();
  form.value = row;
  diagTitle.value = proxy.$t("WordMgr.HotWord.EditHotWordTitle")
  open.value = true;
}
function submitForm () {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      form.value.groupId = selectedGroupId.value;
      if (form.value.wordId) {
        editHotWord(form.value).then(response => {
          if (response.code == 200) {
            proxy.$modal.msgSuccess(response.msg);
            open.value = false;
            loadWordList();
          }
        }); 
      } else {
        addHotWord(form.value).then(response => {
          if (response.code == 200) {
            proxy.$modal.msgSuccess(response.msg);
            open.value = false;
            loadWordList();
          }
        }); 
      }
    }
  });
}
function handleDelete (row) {
  const wordIds = row.wordId ? [ row.wordId ] : selectedIds.value;
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function() {
    return deleteHotWord(wordIds);
  }).then((response) => {
    proxy.$modal.msgSuccess(response.msg);
    loadWordList();
  }).catch(() => {});
}
</script>