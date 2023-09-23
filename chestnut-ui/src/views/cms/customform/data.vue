<template>
  <!-- 自定义表单管理页 -->
  <div class="app-container">
    <el-row :gutter="10" class="mb12">
      <el-col :span="1.5">
        <el-button
          plain
          type="primary"
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd">{{ $t("Common.Add") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="success"
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleEdit">{{ $t("Common.Edit") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="danger"
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="warning"
          icon="el-icon-close"
          size="mini"
          @click="handleClose">{{ $t("Common.Close") }}</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="loadCustomFormDataList"></right-toolbar>
    </el-row>
    <el-row v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="small" class="el-form-search mb12" :inline="true">
        <el-form-item prop="ip">
          <el-input
            v-model="queryParams.ip"
            clearable
            placeholder="IP"
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

    <el-table 
      v-loading="loading"
      :data="dataList"
      @selection-change="handleSelectionChange">
      <el-table-column 
        type="selection"
        width="55"
        align="center" />
      <el-table-column 
        v-for="field in fields"
        :key="field.code"
        :label="field.name"
        :show-overflow-tooltip="true">
        <template slot-scope="scope">
          {{ scope.row[field.code] }}
        </template>
      </el-table-column>
      <el-table-column  
        :label="$t('Common.Operation')"
        align="center"
        width="320" 
        class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button 
            size="small"
            type="text"
            icon="el-icon-edit"
            @click="handleEdit(scope.row)">{{ $t("Common.Edit") }}</el-button>
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
      @pagination="loadCustomFormDataList" />

    <el-dialog 
      :title="title" 
      :visible.sync="open"
      :close-on-click-modal="false" 
      width="600px" 
      append-to-body>
      <el-form ref="form" :model="form" label-width="130px">
        <div v-for="field in fields" :key="field.code">
          <el-form-item
            v-if="field.editable"
            :label="field.name">
            <el-input v-model="form[field.code]" />
          </el-form-item>
        </div>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleSubmitForm">{{ $t('Common.Submit') }}</el-button>
        <el-button @click="handleCancel">{{ $t('Common.Cancel') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { listModelAllField } from "@/api/meta/model";
import { listCustomFormDatas, getCustomFormData, addCustomFormData, editCustomFormData, deleteCustomFormDatas } from "@/api/customform/customform";

export default {
  name: "CustomFormList",
  data () {
    return {
      loading: true,
      showSearch: true,
      ids: [],
      single: true,
      multiple: true,
      total: 0,
      dataList: [],
      fields: [],
      title: "",
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        formId: this.$route.query.formId
      },
      form: {
      }
    };
  },
  created () {
    this.loadCustomFormFields();
    this.loadCustomFormDataList();
  },
  methods: {
    loadCustomFormFields() {
      listModelAllField(this.queryParams.formId, false).then(response => {
        this.fields = response.data.rows;
        this.loading = false;
      });
    },
    loadCustomFormDataList () {
      this.loading = true;
      listCustomFormDatas(this.queryParams).then(response => {
        this.dataList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.loadCustomFormDataList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange (selection) {
      this.ids = selection.map(item => item.dataId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    reset () {
      this.resetForm("form");
      this.form = {};
    },
    handleAdd () {
      this.reset();
      this.open = true;
      this.title = this.$t('CMS.CustomForm.AddTitle');
    },
    handleEdit(row) {
      this.reset();
      const dataId = row.dataId ? row.dataId : this.ids[0];
      getCustomFormData(this.queryParams.formId, dataId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = this.$t('CMS.CustomForm.EditDataTitle');
      });
    },
    handleCancel () {
      this.open = false;
      this.reset();
    },
    handleSubmitForm () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.dataId) {
            editCustomFormData(this.queryParams.formId, this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.SaveSuccess'));
              this.open = false;
              this.loadCustomFormDataList();
            });
          } else {
            addCustomFormData(this.queryParams.formId, this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.AddSuccess'));
              this.open = false;
              this.loadCustomFormDataList();
            });
          }
        }
      });
    },
    handleDelete (row) {
      const dataIds = row.dataId ? [ row.dataId ] : this.ids;
      const formId = this.queryParams.formId;
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function () {
        return deleteCustomFormDatas(formId, dataIds);
      }).then(() => {
        this.loadCustomFormDataList();
        this.$modal.msgSuccess(this.$t('Common.DeleteSuccess'));
      }).catch(function () { });
    },
    handleClose() {
      const obj = { path: "/operations/customform" };
      this.$tab.closeOpenPage(obj);
    }
  }
};
</script>