<template>
  <div class="app-container">
    <el-row :gutter="24" class="mb12">
      <el-col :span="12">
        <el-row :gutter="10">
          <el-col :span="1.5">
            <el-button 
              type="primary"
              icon="el-icon-plus"
              size="mini"
              plain
              @click="handleAdd">{{ $t("Common.Add") }}</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button 
              type="success"
              icon="el-icon-edit"
              size="mini"
              plain
              :disabled="single"
              @click="handleEdit">{{ $t('Common.Edit') }}</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button 
              type="danger"
              icon="el-icon-delete"
              size="mini"
              plain
              :disabled="multiple"
              @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button 
              type="danger"
              icon="el-icon-remove-outline"
              size="mini"
              plain
              :disabled="multiple"
              @click="handleClearIncludeCache">{{ $t("CMS.Template.ClearIncludeCache") }}</el-button>
          </el-col>
        </el-row>
      </el-col>
      <el-col :span="12" style="text-align:right">
        <el-form 
          :model="queryParams"
          ref="queryForm"
          :inline="true"
          size="mini"
          class="el-form-search">
          <el-form-item prop="publishPipeCode">
            <el-select v-model="queryParams.publishPipeCode" :placeholder="$t('CMS.ContentCore.PublishPipe')" style="width:120px;">
              <el-option
                v-for="pp in publishPipes"
                :key="pp.pipeCode"
                :label="pp.pipeName"
                :value="pp.pipeCode"
              />
            </el-select>
          </el-form-item>
          <el-form-item prop="filename">
            <el-input v-model="queryParams.filename" :placeholder="$t('CMS.Template.Name')"></el-input>
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
      <el-col>
        <el-table 
          v-loading="loading"
          :data="templateList"
          @selection-change="handleSelectionChange"
          @row-dblclick="handleEdit">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column type="index" :label="$t('Common.RowNo')" align="center" width="80" />
          <el-table-column 
            :label="$t('CMS.ContentCore.PublishPipe')"
            align="center"
            width="160"
            prop="publishPipeCode" />
          <el-table-column 
            :label="$t('CMS.Template.Name')"
            align="left"
            :show-overflow-tooltip="true"
            prop="path">
          </el-table-column>
          <el-table-column 
            :label="$t('Common.Remark')"
            align="left"
            :show-overflow-tooltip="true"
            width="120"
            prop="remark">
          </el-table-column>
          <el-table-column 
            :label="$t('CMS.Template.FileSize')"
            align="right"
            width="160"
            prop="filesizeName" />
          <el-table-column 
            :label="$t('CMS.Template.ModifyTime')"
            align="center"
            width="160"
            prop="modifyTime" />
          <el-table-column 
            :label="$t('Common.Operation')"
            align="center"
            width="320" 
            class-name="small-padding fixed-width">
            <template slot-scope="scope">
              <el-button 
                type="text"
                icon="el-icon-edit"
                size="small"
                @click="handleRename(scope.row)">{{ $t('CMS.Template.Rename') }}</el-button>
              <el-button
                type="text"
                icon="el-icon-edit"
                size="small"
                @click="handleEdit(scope.row)">{{ $t("Common.Edit") }}</el-button>
              <el-button
                type="text"
                icon="el-icon-delete"
                size="small"
                @click="handleDelete(scope.row)">{{ $t("Common.Delete") }}</el-button>
              <el-button
                type="text"
                size="small"
                icon="el-icon-remove-outline"
                @click="handleClearIncludeCache(scope.row)">{{ $t("CMS.Template.ClearIncludeCache") }}</el-button>
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
      </el-col> 
    </el-row>
    <!-- 添加或修改模板文件对话框 -->
    <el-dialog 
      :title="title"
      :visible.sync="open"
      width="500px"
      append-to-body>
      <el-form 
        ref="form"
        :model="form"
        :rules="rules"
        label-width="100px">
        <el-form-item v-if="!form.templateId||form.templateId==0" :label="$t('CMS.ContentCore.PublishPipe')" prop="publishPipeCode">
          <el-select v-model="form.publishPipeCode" >
            <el-option
              v-for="pp in publishPipes"
              :key="pp.pipeCode"
              :label="pp.pipeName"
              :value="pp.pipeCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('CMS.Template.Name')" prop="path">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="form.remark" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getPublishPipeSelectData } from "@/api/contentcore/publishpipe";
import { getConfigKey } from "@/api/system/config"
import { getTemplateList, getTemplateDetail, renameTemplate, addTemplate, delTemplate, clearIncludeCache } from "@/api/contentcore/template";

export default {
  name: "CmsContentcoreTemplate",
  data () {
    const validatePath = (rule, value, callback) => {
        if (!value || value.length == 0 || !value.endsWith(this.templateSuffix)) {
          callback(new Error(this.$t('CMS.Template.RuleTips.Name', [ this.templateSuffix ])));
          return;
        }
        const hasMatchFail = value.substring(0, value.indexOf('.template.html')).split('\/').some(item => !(/^[A-Za-z0-9_\.\/]+$/.test(item)));
        if (hasMatchFail) {
            callback(new Error(this.$t('CMS.Template.RuleTips.Name', [ this.templateSuffix ])));
            return;
        }
        callback();
      };
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      selectedIds: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 资源表格数据
      templateList: [],
      total: 0,
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      publishPipes: [],
      // 查询参数
      queryParams: {
        publishPipeCode: undefined,
        filename: undefined
      },
      templateSuffix: ".template.html",
      isRename: false,
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        path: [
          { trigger: "blur", validator: validatePath }
        ]
      }
    };
  },
  created () {
    getPublishPipeSelectData().then(response => {
        this.publishPipes = response.data.rows;
      });
    getConfigKey("CMSTemplateSuffix").then(response => {
      this.templateSuffix = response.data;
    })
    this.getList();
  },
  methods: {
    /** 查询资源列表 */
    getList () {
      this.loading = true;
      getTemplateList(this.queryParams).then(response => {
        this.templateList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    // 取消按钮
    cancel () {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset () {
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery () {
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery () {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange (selection) {
      this.selectedIds = selection.map(item => item.templateId);
      this.single = selection.length != 1;
      this.multiple = !selection.length;
    },
    /** 新增按钮操作 */
    handleAdd () {
      this.form = {};
      this.open = true;
      this.title = this.$t('CMS.Template.AddTitle');
    },
    /** 修改按钮操作 */
    handleRename (row) {
      this.form = {};
      getTemplateDetail(row.templateId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = this.$t('CMS.Template.EditTitle');
      });
    },
    handleEdit (row) {
      this.$router.push({ 
        path: "/cms/template/editor", 
        query: { 
          id: row.templateId
        } 
      });
    },
    /** 提交按钮 */
    submitForm: function () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.templateId) {
            renameTemplate(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.SaveSuccess'));
              this.open = false;
              this.getList();
            }); 
          } else {
            addTemplate(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.SaveSuccess'));
              this.open = false;
              this.getList();
            }); 
          }
          
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete (row) {
      const templateIds = row.templateId ? [ row.templateId ] : this.selectedIds
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function () {
        return delTemplate(templateIds);
      }).then(() => {
        this.$modal.msgSuccess(this.$t('Common.SaveSuccess'));
        this.getList();
      }).catch(function () { });
    },
    handleClearIncludeCache(row) {
      const templateIds = row.templateId ? [ row.templateId ] : this.selectedIds
      clearIncludeCache(templateIds).then(response => {
        this.$modal.msgSuccess(this.$t('Common.SaveSuccess'));
      });
    }
  }
};
</script>
<style scoped>
.time {
  font-size: 13px;
  color: #999;
}
.el-card {
  margin-bottom: 10px;
  padding: 10px;
}
.r-image {
  width: 130px;
}
.el-form-search {
  width: 100%;
}
</style>