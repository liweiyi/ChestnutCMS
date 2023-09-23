<template>
  <div class="sensitive-word-container">
    <el-row :gutter="24" class="mb12">
      <el-col :span="12">
        <el-button 
          plain
          type="primary"
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd">{{ $t("Common.Add") }}</el-button>
        <el-button 
          plain
          type="danger"
          icon="el-icon-delete"
          size="mini"
          :disabled="selectedIds.length==0"
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
            <el-input v-model="queryParams.query" :placeholder="$t('WordMgr.SensitiveWord.Placeholder.InputWord')">
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
        <el-table-column :label="$t('WordMgr.SensitiveWord.Word')" align="left" prop="word" />
        <el-table-column :label="$t('WordMgr.SensitiveWord.Type')" align="center" width="100" prop="type">
          <template slot-scope="scope">
            <el-tag :type="scope.row.type==='WHITE'?'success':''">{{ formatType(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
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
      :title="$t('WordMgr.SensitiveWord.AddHotWordTitle')"
      :visible.sync="open"
      :close-on-click-modal="false"
      width="500px"
      append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item :label="$t('WordMgr.SensitiveWord.Word')" prop="word">
          <el-input v-model="form.word" />
        </el-form-item>
        <el-form-item :label="$t('WordMgr.SensitiveWord.Type')" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio v-for="item in typeOptions" :key="item.id" :label="item.id">{{ item.label }}</el-radio>
          </el-radio-group>
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
import { getSensitiveWordList, addSensitiveWord, deleteSensitiveWord } from "@/api/word/sensitiveWord";

export default {
  name: "CMSSensitiveWord",
  data () {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      selectedIds: [],
      // 资源表格数据
      wordList: [],
      total: 0,
      typeOptions: [
        { id: "BLACK", label: this.$t('WordMgr.SensitiveWord.TypeSensitive') },
        { id: "WHITE", label: this.$t('WordMgr.SensitiveWord.TypeWhiteList') }
      ],
      open: false,
      diagTitle: "",
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
        type: [
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
      getSensitiveWordList(this.queryParams).then(response => {
        if (response.code == 200) {
          this.wordList = response.data.rows;
          this.total = parseInt(response.data.total);
          this.loading = false;
        }
      });
    },
    formatType(type) {
      for(let i = 0; i < this.typeOptions.length; i++) {
        if (this.typeOptions[i].id === type) {
          return this.typeOptions[i].label;
        }
      }
      return "unknow";
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
          addSensitiveWord(this.form).then(response => {
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
        return deleteSensitiveWord(wordIds);
      }).then((response) => {
        this.$modal.msgSuccess(response.msg);
        this.loadWordList();
      }).catch(() => {});
    }
  }
};
</script>