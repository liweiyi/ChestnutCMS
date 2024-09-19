<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="small"
          @click="handleAdd"
          v-hasPermi="['system:i18ndict:add']"
        >{{ $t('Common.Add') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="small"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:i18ndict:edit']"
        >{{ $t('Common.Edit') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="small"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:i18ndict:remove']"
        >{{ $t('Common.Delete') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="small"
          @click="handleExport"
          v-hasPermi="['system:i18ndict:export']"
        >{{ $t('Common.Export') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-refresh"
          size="small"
          @click="handleRefreshCache"
          v-hasPermi="['system:i18ndict:remove']"
        >{{ $t('Common.RefreshCache') }}</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-row v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="small" class="el-form-search mb12" :inline="true">
        <el-form-item prop="langTag">
          <el-select
            v-model="queryParams.langTag"
            clearable
            :placeholder="$t('System.I18n.LangTag')"
            style="width: 135px"
          >
            <el-option
              v-for="dict in dict.type.I18nDictType"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item prop="langKey">
          <el-input
            v-model="queryParams.langKey"
            clearable
            style="width: 160px"
            :placeholder="$t('System.I18n.LangKey')"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item prop="langValue">
          <el-input
            v-model="queryParams.langValue"
            clearable
            :placeholder="$t('System.I18n.LangValue')"
            style="width: 160px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button-group>
            <el-button type="primary" icon="el-icon-search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
            <el-button icon="el-icon-refresh" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
          </el-button-group>
        </el-form-item>
      </el-form>
    </el-row>

    <el-table v-loading="loading" :data="dictList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="$t('System.I18n.LangTag')" align="center" width="180" prop="langTag" />
      <el-table-column :label="$t('System.I18n.LangKey')" align="left" prop="langKey" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.I18n.LangValue')" align="left" prop="langValue" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Common.Operation')" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <span class="btn-cell-wrap">
            <el-button
              size="small"
              type="text"
              icon="el-icon-edit"
              @click="handleUpdate(scope.row)"
              v-hasPermi="['system:i18ndict:edit']"
            >{{ $t('Common.Edit') }}</el-button>
          </span>
          <span class="btn-cell-wrap">
            <el-button
              size="small"
              type="text"
              icon="el-icon-delete"
              @click="handleDelete(scope.row)"
              v-hasPermi="['system:i18ndict:remove']"
            >{{ $t('Common.Delete') }}</el-button>
          </span>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="130px">
        <el-form-item :label="$t('System.I18n.LangTag')" prop="langTag">
          <el-select v-model="form.langTag">
            <el-option
              v-for="dict in dict.type.I18nDictType"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('System.I18n.LangKey')" prop="langKey">
          <el-input v-model="form.langKey" />
        </el-form-item>
        <el-form-item :label="$t('System.I18n.LangValue')" prop="langValue">
          <el-input v-model="form.langValue" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">{{ $t('Common.Save') }}</el-button>
        <el-button @click="cancel">{{ $t('Common.Cancel') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listI18nDict, getI18nDict, delI18nDict, addI18nDict, updateI18nDict, refreshCache } from "@/api/system/i18nDict";

export default {
  name: "SystemI18nIndex",
  dicts: [ 'I18nDictType' ],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      dictList: [],
      title: "",
      open: false,
      dateRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        langTag: undefined,
        langKey: undefined,
        langValue: undefined
      },
      form: {},
      rules: {
        langTag: [
          { required: true, message: this.$t('System.I18n.RuleTips.LangTag'), trigger: "blur" }
        ],
        langKey: [
          { required: true, message: this.$t('System.I18n.RuleTips.LangKey'), trigger: "blur" }
        ],
        langValue: [
          { required: true, message: this.$t('System.I18n.RuleTips.LangValue'), trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listI18nDict(this.queryParams).then(response => {
          this.dictList = response.data.rows;
          this.total = parseInt(response.data.total);
          this.loading = false;
        }
      );
    },
    cancel() {
      this.open = false;
      this.reset();
    },
    reset() {
      this.resetForm("form");
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = this.$t("System.I18n.Dialog.Add");
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.dictId)
      this.single = selection.length!=1
      this.multiple = !selection.length
    },
    handleUpdate(row) {
      this.reset();
      const dictId = row.dictId || this.ids
      getI18nDict(dictId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = this.$t("System.I18n.Dialog.Edit");
      });
    },
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.dictId != undefined) {
            updateI18nDict(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.OpSuccess'));
              this.open = false;
              this.getList();
            });
          } else {
            addI18nDict(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.OpSuccess'));
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDelete(row) {
      const dictIds = row.dictId || this.ids;
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function() {
          return delI18nDict(dictIds);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess(this.$t('Common.OpSuccess'));
        }).catch(() => {});
    },
    handleExport() {
      this.exportExcel('system/i18n/dict/list', {
        ...this.queryParams
      }, `i18n_dict_${this.parseTime(new Date(),'{y}{m}{d}{h}{i}{s}')}.xlsx`)
    },
    handleRefreshCache() {
      refreshCache().then(() => {
        this.$modal.msgSuccess(this.$t('Common.OpSuccess'));
      });
    }
  }
};
</script>
