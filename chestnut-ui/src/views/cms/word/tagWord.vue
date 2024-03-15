<template>
  <div class="tag-word-container">
    <el-row :gutter="24">
      <el-col :span="4" :xs="24">
        <cms-tagword-group-tree 
          ref="groupTree"
          :new-btn="true"    
          @node-click="handleTreeNodeClick">
        </cms-tagword-group-tree>
      </el-col>
      <el-col :span="20" :xs="24">
        <el-row :gutter="24" class="mb12">
          <el-col :span="12">
            <el-button 
              type="primary"
              icon="el-icon-plus"
              size="mini"
              plain
              :disabled="selectedGroupId==''"
              @click="handleAdd">{{ $t("Common.Add") }}</el-button>
            <el-button 
              type="primary"
              icon="el-icon-plus"
              size="mini"
              plain
              :disabled="selectedGroupId==''"
              @click="handleBatchAdd">{{ $t("Common.BatchAdd") }}</el-button>
            <el-button 
              type="danger"
              icon="el-icon-delete"
              size="mini"
              plain
              :disabled="selectedGroupId==''||selectedIds.length==0"
              @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
          </el-col>
          <el-col :span="12">
            <el-form 
              :model="queryParams"
              ref="queryForm"
              :inline="true"
              size="mini"
              class="el-form-search">
              <el-form-item prop="query">
                <el-input v-model="queryParams.query" :placeholder="$t('WordMgr.TAG.Placeholder.InputTAG')">
                </el-input>
              </el-form-item>
              <el-form-item>
                <el-button-group>
                  <el-button type="primary" icon="el-icon-search" @click="handleQuery">{{ $t("Common.Search") }}</el-button>
                  <el-button icon="el-icon-refresh" @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
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
            <el-table-column :label="$t('WordMgr.TAG.TAGWordUseCount')" align="center" prop="useCount" width="120"/>
            <el-table-column :label="$t('WordMgr.TAG.TAGWordHitCount')" align="center" prop="hitCount" width="120"/>
            <el-table-column :label="$t('Common.CreateTime')" align="center" width="160">
              <template slot-scope="scope">
                <span>{{ parseTime(scope.row.createTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('Common.CreateBy')" align="center" width="120" prop="createBy" />
            <el-table-column :label="$t('Common.Operation')" align="center" width="180" class-name="small-padding fixed-width">
              <template slot-scope="scope">
                <el-button 
                  size="small"
                  type="text"
                  icon="el-icon-edit"
                  @click="handleEdit(scope.row)">{{ $t('Common.Edit') }}</el-button>
                <el-button 
                  size="small"
                  type="text"
                  icon="el-icon-delete"
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
    <!-- 添加TAG词弹窗 -->
    <el-dialog :title="diagTitle"
               :visible.sync="open"
               :close-on-click-modal="false"
               width="500px"
               append-to-body>
      <el-form ref="form"
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
          <cms-logo-view v-model="form.logo" :src="form.logoSrc" :width="218" :height="150"></cms-logo-view>
        </el-form-item>
      </el-form>
      <div slot="footer"
           class="dialog-footer">
        <el-button type="primary" @click="submitForm">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="closeDialog(false)">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
    <!-- 批量添加TAG词弹窗 -->
    <el-dialog :title="$t('Common.BatchAdd')"
               :visible.sync="batchAddOpen"
               :close-on-click-modal="false"
               width="500px"
               append-to-body>
      <el-form ref="formBatch"
               :model="formBatch"
               :rules="rulesBatch"
               label-width="80px">
        <el-form-item 
          :label="$t('WordMgr.TAG.TAGWord')"
          prop="words">
          <el-input type="textarea" :rows="5" v-model="formBatch.words" :placeholder="$t('WordMgr.TAG.Placeholder.BatchAdd')" />
        </el-form-item>
      </el-form>
      <div slot="footer"
           class="dialog-footer">
        <el-button type="primary" @click="submitBatchAddForm">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleBatchAddClose(false)">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<style scoped>
.el-form-search .el-form-item {
  margin-bottom: 0;
}
</style>
<script>
import { getTagWordList, addTagWord, batchAddTagWords, editTagWord, deleteTagWord } from "@/api/word/tagWord";
import CMSTagWordGroupTree from '@/views/cms/word/tagWordGroupTree';
import CMSLogoView from '@/views/cms/components/LogoView';

export default {
  name: "CMSTagWord",
  components: {
    "cms-logo-view": CMSLogoView,
    'cms-tagword-group-tree': CMSTagWordGroupTree
  },
  data () {
    return {
      loading: false,
      selectedGroupId: '',
      selectedIds: '',
      wordList: [],
      total: 0,
      queryParams: {
        query: undefined,
        groupId: undefined,
        pageSize: 20,
        pageNo: 1
      },
      batchAddOpen: false,
      formBatch: {},
      rulesBatch: {
        words: [
          { required: true, message: this.$t("Common.RuleTips.NotEmpty"), trigger: "blur" }
        ],
      },
      diagTitle: "",
      open: false,
      form: {},
      rules: {
        word: [
          { required: true, message: this.$t("Common.RuleTips.NotEmpty"), trigger: "blur" }
        ],
      }
    };
  },
  created() {

  },
  methods: {
    handleTabClick (tab, event) {
    },
    handleTreeNodeClick(data) {
      this.selectedGroupId = data && data != null ? data.id : '';
      if (this.selectedGroupId !== this.queryParams.groupId) {
        this.queryParams.groupId = this.selectedGroupId;
        if (this.selectedGroupId != '') {
          this.loadWordList();
        }
      }
    },
    loadWordList () {
      this.loading = true;
      getTagWordList(this.queryParams).then(response => {
        if (response.code == 200) {
          this.wordList = response.data.rows;
          this.total = parseInt(response.data.total);
          this.loading = false;
        }
      });
    },
    reset () {
      this.form = {};
    },
    handleQuery () {
      this.queryParams.pageNo = 1;
      this.loadWordList();
    },
    resetQuery () {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange (selection) {
      this.selectedIds = selection.map(item => item.wordId);
    },
    closeDialog(loadList) {
      this.open = false;
      this.reset();
      loadList && this.loadWordList();
    },
    handleBatchAdd () {
      this.formBatch = {}
      this.batchAddOpen = true;
    },
    handleBatchAddClose(loadList) {
      this.batchAddOpen = false;
      this.formBatch = {}
      loadList && this.loadWordList()
    },
    submitBatchAddForm () {
      this.$refs["formBatch"].validate(valid => {
        if (valid) {
          batchAddTagWords({ groupId: this.selectedGroupId, words: this.formBatch.words.split("\n") }).then(response => {
            if (response.code == 200) {
              this.$modal.msgSuccess(response.msg);
              this.handleBatchAddClose(true)
            }
          }); 
        }
      });
    },
    handleAdd () {
      this.reset();
      this.diagTitle = this.$t("WordMgr.TAG.AddTAGTitle");
      this.open = true;
    },
    handleEdit (row) {
      this.reset();
      this.form = row
      this.diagTitle = this.$t("WordMgr.TAG.EditTAGTitle");
      this.open = true;
    },
    submitForm () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.form.groupId = this.selectedGroupId;
          if (this.form.wordId) {
            editTagWord(this.form).then(response => {
              if (response.code == 200) {
                this.$modal.msgSuccess(response.msg);
                this.closeDialog(true);
              }
            }); 
          } else {
            addTagWord(this.form).then(response => {
              if (response.code == 200) {
                this.$modal.msgSuccess(response.msg);
                this.closeDialog(true);
              }
            }); 
          }
        }
      });
    },
    handleDelete (row) {
      const wordIds = row.wordId ? [ row.wordId ] : this.selectedIds;
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function() {
        return deleteTagWord(wordIds);
      }).then((response) => {
        this.$modal.msgSuccess(response.msg);
        this.loadWordList();
      }).catch(() => {});
    }
  }
};
</script>