<template>
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
      <right-toolbar :showSearch.sync="showSearch" @queryTable="loadDynamicPageList"></right-toolbar>
    </el-row>
    <el-row v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="small" class="el-form-search mb12" :inline="true">
        <el-form-item prop="query">
          <el-input
            v-model="queryParams.query"
            clearable
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
      <el-table-column :label="$t('CMS.Staticize.CustomDynamicPage.Code')" align="left" prop="code"></el-table-column>
      <el-table-column :label="$t('CMS.Staticize.CustomDynamicPage.Name')" align="left" prop="name"></el-table-column>
      <el-table-column :label="$t('CMS.Staticize.CustomDynamicPage.Path')" align="left" prop="path"></el-table-column>
      <el-table-column :label="$t('Common.CreateBy')" align="left" prop="createBy"></el-table-column>
      <el-table-column 
        :label="$t('Common.CreateTime')"
        align="center"
        prop="createTime"
        width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column  
        :label="$t('Common.Operation')"
        align="right"
        width="350" 
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
      @pagination="loadDynamicPageList" />

    <el-dialog 
      :title="title" 
      :visible.sync="open"
      :close-on-click-modal="false" 
      width="600px" 
      append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="130px">
        <el-form-item :label="$t('CMS.Staticize.CustomDynamicPage.Name')" prop="name">
          <el-input v-model="form.name" :maxlength="30" />
        </el-form-item>
        <el-form-item :label="$t('CMS.Staticize.CustomDynamicPage.Code')" prop="code">
          <el-input v-model="form.code" :maxlength="30" />
        </el-form-item>
        <el-form-item :label="$t('CMS.Staticize.CustomDynamicPage.Path')" prop="path">
          <el-input v-model="form.path" :disabled="form.pageId && form.pageId > 0"/>
        </el-form-item>
        <el-form-item :label="$t('CMS.Staticize.CustomDynamicPage.InitDataType')">
          <el-checkbox-group v-model="form.initDataTypes">
            <el-checkbox v-for="item in initDataTypes" :key="item.type" :label="item.type">{{ item.name }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item 
          v-for="item in publishPipes" 
          :label="item.pipeName" 
          :key="item.pipeCode">
          <el-input v-model="item.template">
            <el-button slot="append" icon="el-icon-folder-opened" @click="handleSelectTemplate(item.pipeCode)"></el-button>
          </el-input>
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')">
          <el-input v-model="form.remark" type="textarea"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleSubmitForm">{{ $t('Common.Submit') }}</el-button>
        <el-button @click="handleCancel">{{ $t('Common.Cancel') }}</el-button>
      </div>
    </el-dialog>
    <!-- 模板选择组件 -->
    <cms-template-selector 
      :open="openTemplateSelector" 
      :publishPipeCode="publishPipe" 
      @ok="handleTemplateSelected"
      @cancel="handleTemplateSelectorCancel" />
  </div>
</template>
<script>
import { getDynamicPageInitDataTypes, getDynamicPageList, getDynamicPageDetail, addDynamicPage, editDynamicPage, deleteDynamicPages } from "@/api/contentcore/dynamic";
import { getPublishPipeSelectData } from "@/api/contentcore/publishpipe"

import CMSTemplateSelector from '@/views/cms/contentcore/templateSelector';

export default {
  name: "CustomDynamicPageList",
  dicts: [  ],
  components: {
    'cms-template-selector': CMSTemplateSelector,
  },
  data () {
    return {
      loading: true,
      showSearch: true,
      openTemplateSelector: false,
      publishPipe: "",
      ids: [],
      single: true,
      multiple: true,
      total: 0,
      dataList: [],
      title: "",
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
      },
      form: {
        initDataTypes: [],
        templates: {}
      },
      rules: {
        name: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
        ],
        code: [
          { required: true, pattern: "^[A-Za-z0-9_]+$", message: this.$t('Common.RuleTips.Code'), trigger: "blur" }
        ],
        path: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
        ]
      },
      initDataTypes: [],
      publishPipes: []
    };
  },
  created () {
    this.loadPublishPipes();
    this.loadDynamicPageInitDataTypes();
    this.loadDynamicPageList();
  },
  methods: {
    loadPublishPipes() {
      getPublishPipeSelectData().then(response => {
        this.publishPipes = response.data.rows;
        this.publishPipes.forEach(item => {
          item.template = ""
        });
      })
    },
    loadDynamicPageInitDataTypes() {
      getDynamicPageInitDataTypes().then(response => {
        this.initDataTypes = response.data;
      });
    },
    loadDynamicPageList () {
      this.loading = true;
      getDynamicPageList(this.queryParams).then(response => {
        this.dataList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.loadDynamicPageList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange (selection) {
      this.ids = selection.map(item => item.pageId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    reset () {
      this.resetForm("form");
      this.form = {
        initDataTypes: [],
        templates: {}
      };
      this.publishPipes.forEach(item => item.template = "")
    },
    handleSelectTemplate(publishPipeCode) {
      this.publishPipe = publishPipeCode;
      this.$nextTick(() => {
        this.openTemplateSelector = true;
      })
    },
    handleTemplateSelected (template) {
      this.publishPipes.some(item => {
        if (item.pipeCode == this.publishPipe) {
            item.template = template;
            return true;
        }
        return false;
      });
      this.form.templates[this.publishPipe] = template;
      this.openTemplateSelector = false;
    },
    handleTemplateSelectorCancel () {
      this.openTemplateSelector = false;
    },
    handleAdd () {
      this.reset();
      this.open = true;
      this.title = this.$t('CMS.Staticize.CustomDynamicPage.AddTitle');
    },
    handleEdit (row) {
      this.reset();
      const pageId = row.pageId ? row.pageId : this.ids[0];
      getDynamicPageDetail(pageId).then(response => {
        this.form = response.data;
        this.publishPipes.forEach(item => {
          item.template = this.form.templates[item.pipeCode] || ""
        })
        this.open = true;
        this.title = this.$t('CMS.Staticize.CustomDynamicPage.EditTitle');
      });
    },
    handleCancel () {
      this.open = false;
      this.reset();
    },
    handleSubmitForm () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.pageId != undefined) {
            editDynamicPage(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.SaveSuccess'));
              this.open = false;
              this.loadDynamicPageList();
            });
          } else {
            addDynamicPage(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.AddSuccess'));
              this.open = false;
              this.loadDynamicPageList();
            });
          }
        }
      });
    },
    handleDelete (row) {
      const pageIds = row.pageId ? [ row.pageId ] : this.ids;
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function () {
        return deleteDynamicPages(pageIds);
      }).then(() => {
        this.loadDynamicPageList();
        this.$modal.msgSuccess(this.$t('Common.DeleteSuccess'));
      }).catch(function () { });
    }
  }
};
</script>