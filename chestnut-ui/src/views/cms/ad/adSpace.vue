<template>
  <div class="app-container adspace-container">
    <el-row :gutter="24"
            class="mb12">
      <el-col :span="12">
        <el-button plain
                   type="primary"
                   icon="el-icon-plus"
                   size="mini"
                   @click="handleAdd">{{ $t("Common.Add") }}</el-button>
        <el-button plain
                   type="danger"
                   icon="el-icon-delete"
                   size="mini"
                   :disabled="selectedRows.length===0"
                   @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
      </el-col>
      <el-col :span="12" style="text-align: right">
        <el-input :placeholder="$t('CMS.Adv.Placeholder.Name')" v-model="queryParams.name" size="mini" class="mr10" style="width: 200px;"></el-input>
        <el-button-group>
          <el-button 
            type="primary"
            icon="el-icon-search"
            size="mini"
            @click="loadAdSpaceList">{{ $t("Common.Search") }}</el-button>
          <el-button 
            icon="el-icon-refresh"
            size="mini"
            @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
        </el-button-group>
      </el-col>
    </el-row>
    <el-row>
      <el-table v-loading="loading"
            :data="dataList"
            @selection-change="handleSelectionChange"
            @row-dblclick="handleEdit">
        <el-table-column type="selection"
                        width="50"
                        align="center" />
        <el-table-column :label="$t('CMS.PageWidget.Name')">
          <template slot-scope="scope">
            <el-link type="primary"
                      @click="handleEdit(scope.row)"
                      class="link-type">
              <span>{{ scope.row.name }}</span>
            </el-link>
          </template>
        </el-table-column>
        <el-table-column :label="$t('CMS.PageWidget.Code')" prop="code" />
        <el-table-column :label="$t('CMS.PageWidget.PublishPipe')" width="100" prop="publishPipeCode" />
        <el-table-column :label="$t('Common.Operation')" align="center" width="300" class-name="small-padding fixed-width">
          <template slot-scope="scope">
            <el-button  
              type="text"
              icon="el-icon-s-promotion"
              size="small"
              @click="handlePublish(scope.row)">{{ $t('CMS.ContentCore.Publish') }}</el-button>
            <el-button
              type="text"
              @click="handlePreview(scope.row)">
              <svg-icon icon-class="eye-open" class="mr2"></svg-icon>{{ $t('CMS.ContentCore.Preview') }}
            </el-button>
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
          </template>
        </el-table-column>
      </el-table>
      <pagination 
        v-show="total>0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="loadAdSpaceList" />
    </el-row>
    <!-- 表单对话框 -->
    <el-dialog 
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      :close-on-click-modal="false"
      width="600px"
      append-to-body>
      <el-form 
        ref="form"
        :model="form"
        :rules="rules"
        label-width="120px">
        <el-form-item :label="$t('CMS.PageWidget.Name')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="$t('CMS.PageWidget.Code')" prop="code">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item :label="$t('CMS.PageWidget.PublishPipe')" prop="publishPipeCode">
          <el-select v-model="form.publishPipeCode">
            <el-option
              v-for="pp in publishPipes"
              :key="pp.pipeCode"
              :label="pp.pipeName"
              :value="pp.pipeCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('CMS.PageWidget.Path')" prop="path">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item :label="$t('CMS.PageWidget.Template')" prop="template">
          <el-input v-model="form.template">
            <el-button 
              slot="append"
              type="primary"
              @click="handleSelectTemplate()">{{ $t("Common.Select") }}</el-button>
          </el-input>
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea" :maxlength="100" />
        </el-form-item>
      </el-form>
      <div slot="footer"
           class="dialog-footer">
        <el-button type="primary" @click="handleDialogOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleDialogClose">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
    <!-- 模板选择组件 -->
    <cms-template-selector 
      :open="openTemplateSelector" 
      :publishPipeCode="form.publishPipeCode" 
      @ok="handleTemplateSelected"
      @cancel="handleTemplateSelectorCancel" />
  </div>
</template>
<script>
import { getPublishPipeSelectData } from "@/api/contentcore/publishpipe";
import { listAdSpaces, addAdSpace, editAdSpace, deleteAdSpace, publishAdSpace } from "@/api/advertisement/advertisement";
import CMSTemplateSelector from '@/views/cms/contentcore/templateSelector';

export default {
  name: "CMSAdSpace",
  components: {
    'cms-template-selector': CMSTemplateSelector
  },
  data () {
    return {
      loading: true,
      dataList: undefined,
      total: 0,
      selectedRows: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: undefined
      },
      dialogTitle: "",
      dialogVisible: false,
      form: {
        path: 'include/ad/',
        publishPipeCode: ''
      },
      rules: {
        name: [
          { required: true, message: this.$t('CMS.PageWidget.RuleTips.Name'), trigger: "blur" }
        ],
        code: [
          { required: true, pattern: "^[A-Za-z0-9_]*$", message: this.$t('CMS.PageWidget.RuleTips.Code'), trigger: "blur" }
        ],
        publishPipeCode: [
          { required: true, message: this.$t('CMS.PageWidget.RuleTips.PublishPipe'), trigger: "blur" }
        ],
        path: [
          { required: true, pattern: "^[A-Za-z0-9_\/]+$", message: this.$t('CMS.PageWidget.RuleTips.Path'), trigger: "blur" }
        ]
      },
      publishPipes: [],
      openTemplateSelector: false
    };
  },
  created () {
    this.loadPublishPipes();
    this.loadAdSpaceList();
  },
  methods: {
    loadPublishPipes() {
      getPublishPipeSelectData().then(response => {
        this.publishPipes = response.data.rows;
      });
    },
    loadAdSpaceList () {
      this.loading = true;
      listAdSpaces(this.queryParams).then(response => {
        this.dataList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    resetQuery () {
      this.queryParams.pageNum = 1;
      this.queryParams.name = undefined;
      this.loadAdSpaceList();
    },
    handleSelectionChange(selection) {
      this.selectedRows = selection.map(item => item);
    },
    handleAdd() {
      this.dialogTitle = this.$t('CMS.Adv.AddSpaceTitle');
      this.form = { path: 'include/ad/' };
      this.dialogVisible = true;
    },
    handleEdit(row) {
      this.$router.push({ path: "/cms/adspace/editor", query: { id: row.pageWidgetId, from: "adspace" } });
    },
    handleDialogClose(reload) {
      this.dialogVisible = false;
      if (reload) {
        this.loadAdSpaceList();
      }
    },
    handleDialogOk () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          addAdSpace(this.form).then(response => {
            this.$modal.msgSuccess(response.msg);
            this.handleDialogClose(true);
          });
        }
      });
    },
    handleDelete(row) {
      const pageWidgetIds = row.pageWidgetId ? [ row.pageWidgetId ] : this.selectedRows.map(item => item.pageWidgetId);
      if (pageWidgetIds.length == 0) {
        return;
      }
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function() {
        return deleteAdSpace(pageWidgetIds);
      }).then(response => {
        this.$modal.msgSuccess(response.msg);
        this.loadAdSpaceList();
      }).catch(() => {});
    },
    handlePublish(row) {
      const pageWidgetIds = [ row.pageWidgetId ];
      publishAdSpace(pageWidgetIds).then(response => {
        this.$modal.msgSuccess(response.msg);
        this.loadAdSpaceList();
      });
    },
    handlePreview(row) {
      let routeData = this.$router.resolve({
        path: "/cms/preview",
        query: { type: "pagewidget", dataId: row.pageWidgetId },
      });
      window.open(routeData.href, '_blank');
    },
    handleSelectTemplate () {
      this.openTemplateSelector = true;
    },
    handleTemplateSelected (template) {
      this.form.template = template;
      this.openTemplateSelector = false;
    },
    handleTemplateSelectorCancel () {
      this.openTemplateSelector = false;
    },
  }
};
</script>
<style scoped>
.adspace-container .el-input, .el-select, .el-textarea {
  width: 300px;
}
</style>