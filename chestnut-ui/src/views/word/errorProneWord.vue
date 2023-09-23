<template>
  <div class="error-prone-word-container">
    <el-row :gutter="24" class="mb12">
      <el-col :span="12">
        <el-button 
          type="primary"
          icon="el-icon-plus"
          size="mini"
          plain
          @click="handleAdd">{{ $t("Common.Add") }}</el-button>
        <el-button
          type="danger"
          icon="el-icon-delete"
          size="mini"
          plain
          :disabled="selectedIds.length==0"
          @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
      </el-col>
      <el-col :span="12" style="text-align:right">
        <el-form
         :model="queryParams"
          ref="queryForm"
          size="mini"
          :inline="true"
          class="el-form-search">
          <el-form-item prop="query">
            <el-input v-model="queryParams.query" :placeholder="$t('WordMgr.ErrorProneWord.Placeholder.InputWord')">
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
      <el-table v-loading="loading" :data="wordList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column type="index" :label="$t('Common.RowNo')" align="center" width="100" />
        <el-table-column :label="$t('WordMgr.ErrorProneWord.Word')" align="left" prop="word" />
        <el-table-column :label="$t('WordMgr.ErrorProneWord.ReplaceWord')" align="left" prop="replaceWord" />
        <el-table-column :label="$t('Common.CreateTime')" align="center" width="160">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Common.CreateBy')" align="center" width="160" prop="createBy" />
        <el-table-column :label="$t('Common.Operation')" align="center" width="180" class-name="small-padding fixed-width">
          <template slot-scope="scope">
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
    <!-- 添加弹窗 -->
    <el-dialog 
      :title="$t('WordMgr.ErrorProneWord.AddTitle')"
      :visible.sync="open"
      :close-on-click-modal="false"
      width="500px"
      append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item :label="$t('WordMgr.ErrorProneWord.Word')" prop="word">
          <el-input v-model="form.word" />
        </el-form-item>
        <el-form-item :label="$t('WordMgr.ErrorProneWord.ReplaceWord')" prop="replaceWord">
          <el-input v-model="form.replaceWord" />
        </el-form-item>
      </el-form>
      <div slot="footer"
           class="dialog-footer">
        <el-button type="primary" @click="submitForm">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<style scoped>
</style>
<script>
import { getErrorProneWordList, addErrorProneWord, deleteErrorProneWord } from "@/api/word/errorProneWord";

export default {
  name: "CMSErrorProneWord",
  data () {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      selectedIds: [],
      // 资源表格数据
      wordList: [],
      total: 0,
      open: false,
      queryParams: {
        query: undefined,
        pageSize: 20,
        pageNo: 1
      },
      form: {},
      rules: {
        word: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
        ],
        replaceWord: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
        ]
      }
    };
  },
  created () {
    this.loadWordList();
  },
  methods: {
    loadWordList () {
      this.loading = true;
      getErrorProneWordList(this.queryParams).then(response => {
        if (response.code == 200) {
          this.wordList = response.data.rows;
          this.total = parseInt(response.data.total);
          this.loading = false;
        }
      });
    },
    cancel () {
      this.open = false;
      this.reset();
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
    handleAdd () {
      this.reset();
      this.open = true;
    },
    submitForm () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          addErrorProneWord(this.form).then(response => {
            if (response.code == 200) {
              this.$modal.msgSuccess(response.msg);
              this.open = false;
              this.loadWordList();
            }
          }); 
        }
      });
    },
    handleDelete (row) {
      const wordIds = row.wordId ? [ row.wordId ] : this.selectedIds;
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function() {
        return deleteErrorProneWord(wordIds);
      }).then((response) => {
        this.$modal.msgSuccess(response.msg);
        this.loadWordList();
      }).catch(() => {});
    }
  }
};
</script>