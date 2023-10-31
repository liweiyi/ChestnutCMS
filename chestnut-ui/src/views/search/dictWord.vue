<template>
  <div class="app-container">
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
        <el-button 
          type="success"
          icon="el-icon-magic-stick"
          size="mini"
          plain
          @click="handleWordAnalyzeTest">{{ $t('Search.DictWord.AnalyzeTest') }}</el-button>
      </el-col>
      <el-col :span="12" style="text-align:right">
        <el-form 
          :model="queryParams"
          ref="queryForm"
          :inline="true"
          size="mini"
          class="el-form-search">
          <el-form-item prop="query">
            <el-input v-model="queryParams.query">
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button-group>
              <el-button 
                type="primary"
                icon="el-icon-search"
                @click="handleQuery">{{ $t("Common.Search") }}</el-button>
              <el-button 
                icon="el-icon-refresh"
                @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
            </el-button-group>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
    <el-row>
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
        <template slot-scope="scope">
          <dict-tag :options="dict.type.SearchDictWordType" :value="scope.row.wordType"/>
        </template>
      </el-table-column>
        <el-table-column :label="$t('Search.DictWord.Word')" align="left" prop="word" />
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
      :title="$t('Search.DictWord.AddTitle')"
      :visible.sync="open"
      :close-on-click-modal="false"
      width="500px"
      append-to-body>
      <el-form 
        ref="form"
        :model="form"
        :rules="rules"
        label-width="40px">
        <el-form-item 
          :label="$t('Search.DictWord.Type')"
          prop="wordType">
          <el-select v-model="form.wordType" clearable>
            <el-option
              v-for="dict in dict.type.SearchDictWordType"
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
      <div slot="footer"
           class="dialog-footer">
        <el-button type="primary" @click="submitForm">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
    <!-- 分词测试弹窗 -->
    <el-dialog 
      :title="$t('Search.DictWord.WordAnalyzeTitle')"
      :visible.sync="openAnalyze"
      :close-on-click-modal="false"
      width="500px"
      label-position="top"
      append-to-body>
      <el-form 
        ref="formAnalyze"
        label-position="top"
        v-loading="loadingAnalyze"
        :model="formAnalyze"
        :rules="rulesAnalyze">
        <el-form-item :label="$t('Search.DictWord.WordAnalyzeType')" prop="type">
          <el-select v-model="formAnalyze.type" clearable>
            <el-option
              v-for="dict in dict.type.WordAnalyzeType"
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
      <div slot="footer"
           class="dialog-footer">
        <el-button type="primary" @click="handleWordAnalyze">{{ $t('Search.DictWord.WordAnalyzeTest') }}</el-button>
        <el-button @click="openAnalyze=false">{{ $t("Common.Close") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<style scoped>
</style>
<script>
import { getDictWords, addDictWord, deleteDictWords, wordAnalyze } from "@/api/search/dictword";

export default {
  name: "SearchDictWord",
  dicts: ['SearchDictWordType','WordAnalyzeType'],
  data () {
    return {
      loading: false,
      selectedIds: [],
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
        ]
      },
      loadingAnalyze: false,
      openAnalyze: false,
      formAnalyze: {},
      rulesAnalyze: {
        type: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
        ],
        text: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
        ]
      },
      analyzeResult: ""
    };
  },
  created () {
    this.loadWordList();
  },
  methods: {
    handleWordAnalyzeTest() {
      this.openAnalyze = true;
    },
    handleWordAnalyze() {
      this.$refs["formAnalyze"].validate(valid => {
        if (valid) {
          this.loadingAnalyze = true
          wordAnalyze(this.formAnalyze).then(response => {
            this.loadingAnalyze = false
            this.analyzeResult = response.data;
          });
        }
      });''
    },
    loadWordList () {
      this.loading = true;
      getDictWords(this.queryParams).then(response => {
        this.wordList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
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
          this.form.words = this.form.wordStr.split("/n");
          this.form.wordStr = "";
          addDictWord(this.form).then(response => {
            this.$modal.msgSuccess(response.msg);
            this.open = false;
            this.resetQuery ();
          }); 
        }
      });
    },
    handleDelete (row) {
      const wordIds = row.wordId ? [ row.wordId ] : this.selectedIds;
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function() {
        return deleteDictWords(wordIds);
      }).then((response) => {
        this.$modal.msgSuccess(response.msg);
        this.resetQuery ();
      }).catch(() => {});
    }
  }
};
</script>