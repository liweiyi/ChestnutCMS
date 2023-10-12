<template>
  <div class="app-container adspace-editor-container">
    <el-row :gutter="10" class="mb12">
      <el-col :span="1.5">
        <el-button 
          plain
          type="success"
          icon="el-icon-edit"
          size="mini"
          v-hasPermi="[ $p('PageWidget:Edit:{0}', [ pageWidgetId ]) ]"
          @click="handleSave">{{ $t("Common.Save") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="primary"
          icon="el-icon-s-promotion"
          size="mini"
          v-hasPermi="[ $p('PageWidget:Publish:{0}', [ pageWidgetId ]) ]"
          @click="handlePublish">{{ $t('CMS.ContentCore.Publish') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="primary"
          icon="el-icon-view"
          size="mini"
          @click="handlePreview">{{ $t('CMS.ContentCore.Preview') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="info"
          icon="el-icon-back"
          size="mini"
          @click="handleGoBack">{{ $t('Common.GoBack') }}</el-button>
      </el-col>
    </el-row>
    <el-form 
      ref="form"
      :model="form"
      :rules="rules"
      label-width="110px">
      <el-card shadow="never">
        <div slot="header" class="clearfix">
          <span>{{ $t('CMS.Adv.Basic') }}</span>
        </div>
        <div class="form-col">
          <el-form-item :label="$t('CMS.PageWidget.Name')" prop="name">
            <el-input v-model="form.name" />
          </el-form-item>
          <el-form-item :label="$t('CMS.PageWidget.Code')" prop="code">
            <el-input v-model="form.code" />
          </el-form-item>
          <el-form-item :label="$t('CMS.PageWidget.Path')" prop="path">
            <el-input v-model="form.path" />
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
          <el-form-item :label="$t('CMS.PageWidget.Template')" prop="template">
            <el-input v-model="form.template" :disabled="templateDisabled" >
              <el-button 
                slot="append"
                type="primary"
                :disabled="templateDisabled"
                @click="handleSelectTemplate()">{{ $t("Common.Select") }}</el-button>
            </el-input>
          </el-form-item>
          <el-form-item :label="$t('Common.Remark')" prop="remark">
            <el-input v-model="form.remark" />
          </el-form-item>
        </div>
      </el-card>
    </el-form>
    <el-card class="mt10" shadow="never">
      <div slot="header" class="clearfix">
        <span>{{ $t('CMS.Adv.AdList') }}</span>
      </div>
      <el-row :gutter="24" class="mb12">
        <el-col :span="12">
          <el-button 
            plain
            type="primary"
            icon="el-icon-plus"
            size="mini"
            @click="handleAddAdvertisement">{{ $t("Common.Add") }}</el-button>
          <el-button 
            plain
            type="success"
            icon="el-icon-edit"
            size="mini"
            :disabled="selectedRows.length!==1"
            @click="handleEditAdvertisement">{{ $t('Common.Edit') }}</el-button>
          <el-button 
            plain
            type="danger"
            icon="el-icon-plus"
            size="mini"
            :disabled="selectedRows.length===0"
            @click="handleDeleteAdvertisements">{{ $t("Common.Delete") }}</el-button>
        </el-col>
        <el-col :span="12" style="text-align: right">
          <el-input :placeholder="$t('CMS.Adv.Placeholder.Name')" v-model="queryParams.name" size="mini" style="width: 200px;" class="mr10"></el-input>
          <el-button 
            type="primary"
            icon="el-icon-search"
            size="mini"
            @click="handleQuery">{{ $t("Common.Search") }}</el-button>
          <el-button 
            icon="el-icon-refresh"
            size="mini"
            @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
        </el-col>
      </el-row>
      <el-row>
        <el-table 
          v-loading="loading"
          :data="dataList"
          @selection-change="handleSelectionChange"
          @row-dblclick="handleEditAdvertisement">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column :label="$t('CMS.Adv.AdName')" prop="name">
          </el-table-column>
          <el-table-column :label="$t('CMS.Adv.Type')" width="100" align="center" prop="typeName">
          </el-table-column>
          <el-table-column :label="$t('CMS.Adv.Weight')" width="100" align="center" prop="weight" />
          <el-table-column :label="$t('CMS.Adv.Status')" width="100" align="center" prop="state">
            <template slot-scope="scope">
              <dict-tag :options="dict.type.EnableOrDisable" :value="scope.row.state"/>
            </template>
          </el-table-column>
          <el-table-column :label="$t('CMS.Adv.OnlineDate')" align="center" prop="onlineDate" width="160">
            <template slot-scope="scope">
              <span>{{ scope.row.onlineDate }}</span>
            </template>
          </el-table-column>
          <el-table-column :label="$t('CMS.Adv.OfflineDate')" align="center" prop="offlineDate" width="160">
            <template slot-scope="scope">
              <span>{{ scope.row.offlineDate }}</span>
            </template>
          </el-table-column>
          <el-table-column :label="$t('Common.Operation')" align="center" width="200" class-name="small-padding fixed-width">
            <template slot-scope="scope">
              <el-button 
                v-if="scope.row.state==='1'"
                size="small"
                type="text"
                icon="el-icon-switch-button"
                @click="handleEnableAdvertisements(scope.row)">{{ $t('Common.Enable') }}</el-button>
              <el-button 
                v-if="scope.row.state==='0'"
                size="small"
                type="text"
                icon="el-icon-switch-button"
                @click="handleDisableAdvertisements(scope.row)">{{ $t('Common.Disable') }}</el-button>
              <el-button 
                size="small"
                type="text"
                icon="el-icon-edit"
                @click="handleEditAdvertisement(scope.row)">{{ $t("Common.Edit") }}</el-button>
              <el-button 
                size="small"
                type="text"
                icon="el-icon-delete"
                @click="handleDeleteAdvertisements(scope.row)">{{ $t("Common.Delete") }}</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination 
          v-show="total>0"
          :total="total"
          :page.sync="queryParams.pageNum"
          :limit.sync="queryParams.pageSize"
          @pagination="loadAdvertisementList" />
      </el-row>
    </el-card>
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
import { getPageWidget, addPageWidget, editPageWidget, publishPageWidgets } from "@/api/contentcore/pagewidget";
import { listAdvertisements, deleteAdvertisement, enableAdvertisement, disableAdvertisement } from "@/api/advertisement/advertisement";
import CMSTemplateSelector from '@/views/cms/contentcore/templateSelector';

export default {
  name: "CMSAdSpaceEditor",
  components: {
    'cms-template-selector': CMSTemplateSelector,
  },
  dicts: [ 'EnableOrDisable' ],
  data () {
    return {
      loading: true,
      pageWidgetId: this.$route.query.id,
      publishPipes: [],
      pageWidgetTypes: [],
      form: {
        publishPipeCode: '',
        content: {}
      },
      rules: {
        name: [
          { required: true, message: this.$t('CMS.PageWidget.RuleTips.Name'), trigger: "blur" }
        ],
        code: [
          { required: true, pattern: "^[A-Za-z0-9_]+$", message: this.$t('CMS.PageWidget.RuleTips.Code'), trigger: "blur" }
        ],
        publishPipeCode: [
          { required: true, message: this.$t('CMS.PageWidget.RuleTips.PublishPipe'), trigger: "blur" }
        ],
        path: [
          { required: true, pattern: "^[A-Za-z0-9_\/]+$", message: this.$t('CMS.PageWidget.RuleTips.Path'), trigger: "blur" }
        ]
      },
      openTemplateSelector: false,
      dataList: undefined,
      total: 0,
      selectedRows: [],
      adSpaceId: this.$route.query.id,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        adSpaceId: this.$route.query.id,
        name: undefined
      },
      initDataStr: "",
      publishAfterSave: false,
    };
  },
  computed: {
    templateDisabled() {
      return !this.form.publishPipeCode || this.form.publishPipeCode == null || this.form.publishPipeCode === '';
    }
  },
  created () {
    if (this.pageWidgetId) {
      this.loadPublishPipes();
      this.loadPageWidgetInfo();
      this.loadAdvertisementList();
    } else {
      this.$modal.msgError(this.$t('CMS.PageWidget.InvalidPageWidgetId', [ this.pageWidgetId ]));
    }
  },
  methods: {
    loadPublishPipes() {
      getPublishPipeSelectData().then(response => {
        this.publishPipes = response.data.rows;
      });
    },
    loadPageWidgetInfo() {
      getPageWidget(this.pageWidgetId).then(response => {
        this.form = response.data;
        this.dataList = this.form.content ? JSON.parse(this.form.content) : [];
        this.initDataStr = JSON.stringify(this.form);
      });
    },
    isFormChanged() {
      return JSON.stringify(this.form) != this.initDataStr;
    },
    handleSave () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.form.catalogId = this.catalogId;
          this.form.content = JSON.stringify(this.dataList);
          if (this.pageWidgetId) {
            editPageWidget(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.SaveSuccess'));
              this.initDataStr = JSON.stringify(this.form);
              if (this.publishAfterSave) {
                this.publishAfterSave = false;
                this.handlePublish();
              }
            });
          } else {
            addPageWidget(this.form).then(response => {
              this.$modal.msgSuccess(response.msg);
              this.$router.push({ path: form.route, query: { id: response.data } });
            });
          }
        }
      });
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
    handlePublish() {
      if (this.isFormChanged()) {
        this.publishAfterSave = true;
        this.handleSave(this.handlePublish);
        return;
      }
      const pageWidgetIds = [ this.pageWidgetId ];
      publishPageWidgets(pageWidgetIds).then(response => {
        this.$modal.msgSuccess(this.$t('CMS.ContentCore.PublishSuccess'));
      });
    },
    handlePreview() {
      let routeData = this.$router.resolve({
        path: "/cms/preview",
        query: { type: "pagewidget", dataId: this.pageWidgetId },
      });
      window.open(routeData.href, '_blank');
    },
    loadAdvertisementList () {
      this.loading = true;
      listAdvertisements(this.queryParams).then(response => {
        if (response.code == 200) {
          this.dataList = response.data.rows;
          this.total = parseInt(response.data.total);
          this.loading = false;
        }
      });
    },
    handleQuery () {
      this.queryParams.pageNum = 1;
      this.loadAdvertisementList();
    },
    resetQuery () {
      this.queryParams.name = undefined;
      this.queryParams.pageNum = 1;
      this.handleQuery();
    },
    handleSelectionChange(selection) {
      this.selectedRows = selection.map(item => item);
    },
    handleAddAdvertisement() {
      this.$router.push({ path: "/cms/ad/editor", query: { adSpaceId: this.adSpaceId } });
    },
    handleEditAdvertisement(row) {
      const advertisementId = row.advertisementId || this.selectedRows[0].advertisementId;
      this.$router.push({ path: "/cms/ad/editor", query: { adSpaceId: this.adSpaceId, id: advertisementId } });
    },
    handleDeleteAdvertisements(row) {
      const advertisementIds = row.advertisementId ? [ row.advertisementId ] : this.selectedRows.map(item => item.advertisementId);
      if (advertisementIds.length == 0) {
        return;
      }
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function() {
        return deleteAdvertisement(advertisementIds);
      }).then(response => {
        if (response.code === 200) {
          this.$modal.msgSuccess(response.msg);
          this.loadAdvertisementList();
        }
      }).catch(() => {});
    },
    handleEnableAdvertisements(row) {
      const advertisementIds = row.advertisementId ? [ row.advertisementId ] : this.selectedRows.map(item => item.advertisementId);
      enableAdvertisement(advertisementIds).then(response => {
        this.$modal.msgSuccess(response.msg);
        this.loadAdvertisementList();
      });
    },
    handleDisableAdvertisements(row) {
      const advertisementIds = row.advertisementId ? [ row.advertisementId ] : this.selectedRows.map(item => item.advertisementId);
      disableAdvertisement(advertisementIds).then(response => {
        this.$modal.msgSuccess(response.msg);
        this.loadAdvertisementList();
      });
    },
    handleGoBack() {
      if (this.$route.query.from == 'pagewidget') {
        const obj = { name: "Content", params: { tab: "pageWdiget" } };
        this.$tab.closeOpenPage(obj);
      } else {
        const obj = { name: "Advertisement" };
        this.$tab.closeOpenPage(obj);
      }
    },
  }
};
</script>
<style scoped>
.adspace-editor-container .el-input, .el-select, .el-textarea {
  width: 400px;
}
.adspace-editor-container .form-row {
  width: 100%;
  display: inline-block;
}
.adspace-editor-container .el-form-item {
  width: 500px;
  float: left;
}
</style>