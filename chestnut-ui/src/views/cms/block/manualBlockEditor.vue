<template>
  <div class="app-container block-manual-container">
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
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>{{ $t('CMS.Block.Basic') }}</span>
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
          <el-form-item :label="$t('Common.Remark')"
                        prop="remark">
            <el-input v-model="form.remark" />
          </el-form-item>
        </div>
      </el-card>
    </el-form>
    <el-card class="mt10">
      <div slot="header" class="clearfix">
        <span>{{ $t('CMS.Block.ManualList') }}</span>
      </div>
      <el-table :data="this.form.content" style="width: 100%">
        <el-table-column type="index" :label="$t('Common.RowNo')" width="50">
        </el-table-column>
        <el-table-column :label="$t('CMS.Block.Title')" prop="title">
          <template slot-scope="scope">
            <span class="row-insert">
              <el-button 
                icon="el-icon-plus" 
                circle 
                size="mini"
                @click="handleAddItem(scope.$index)">
              </el-button>
            </span>
            <span class="row">
              <el-tag
                class="item-data"
                effect="plain"
                type="info"
                :key="item.title"
                v-for="(item, index) in scope.row.items">
                <el-link :underline="false" @click="handleEditItem(scope.$index, index)">{{item.title}}</el-link>
                <span class="item-op">
                  <el-link 
                    class="item-op-add"
                    :underline="false" 
                    icon="el-icon-circle-plus-outline" 
                    @click="handleAddItem(scope.$index, index + 1)">
                  </el-link>
                  <el-link 
                    class="item-op-del"
                    :underline="false" 
                    icon="el-icon-circle-close" 
                    @click="handleDeleteItem(scope.$index, index)">
                  </el-link>
                </span>
              </el-tag>
            </span>
          </template>
        </el-table-column>
        <el-table-column width="220" align="right">
          <template slot="header">
            <el-button
              plain
              type="primary"
              icon="el-icon-plus"
              size="mini"
              @click="handleAddRow(form.content.length)">{{ $t('CMS.Block.AddRow') }}</el-button>
            <el-button
              plain
              type="danger"
              icon="el-icon-delete"
              size="mini"
              @click="handleClear">{{ $t('CMS.Block.Clean') }}</el-button>
          </template>
          <template slot-scope="scope">
            <el-button
              plain
              size="mini"
              icon="el-icon-plus"
              @click="handleAddRow(scope.$index)">{{ $t('CMS.Block.InsertRow') }}</el-button>
            <el-button
              plain
              size="mini"
              icon="el-icon-delete"
              type="danger"
              @click="handleDeleteRow(scope.$index)">{{ $t("Common.Delete") }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <!-- 链接编辑弹窗 -->
    <el-dialog 
      :title="title"
      :visible.sync="dialogVisible"
      :close-on-click-modal="false"
      width="600px"
      append-to-body>
      <el-form 
        ref="form_item"
        :model="form_item"
        label-width="80px"
        class="form_item">
        <div class="form-row">
          <el-form-item :label="$t('CMS.Block.Title')" prop="title">
            <el-input v-model="form_item.title">
              <el-dropdown slot="append" @command="handleLinkTo">
                <el-button size="mini" type="primary">
                  {{ $t('Common.Select') }}<i class="el-icon-arrow-down el-icon--right"></i>
                </el-button>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="content">{{ $t('CMS.ContentCore.SelectContent') }}</el-dropdown-item>
                  <el-dropdown-item command="catalog">{{ $t('CMS.ContentCore.SelectCatalog') }}</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </el-input>
          </el-form-item>
          <el-form-item :label="$t('CMS.Block.Link')" prop="url">
            <el-input v-model="form_item.url" />
          </el-form-item>
          <el-form-item :label="$t('CMS.Block.Summary')" prop="summary">
            <el-input type="textarea" v-model="form_item.summary" />
          </el-form-item>
          <el-form-item :label="$t('CMS.Block.Date')" prop="publishDate">
            <el-date-picker
              v-model="form_item.publishDate"
              type="datetime"
              value-format="yyyy-MM-dd HH:mm:ss">
            </el-date-picker>
          </el-form-item>
          <el-form-item label="LOGO" prop="logo">
            <cms-logo-view v-model="form_item.logo" :src="form_item.logoSrc" :width="218" :height="150"></cms-logo-view>
          </el-form-item>
        </div>
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
    <!-- 栏目选择组件 -->
    <cms-catalog-selector
      :open="openCatalogSelector"
      @ok="handleCatalogSelectorOk"
      @close="handleCatalogSelectorClose"></cms-catalog-selector>
    <!-- 内容选择组件 -->
    <cms-content-selector
      :open="openContentSelector"
      @ok="handleContentSelectorOk"
      @close="handleContentSelectorClose"></cms-content-selector>
  </div>
</template>
<script>
import { getPublishPipeSelectData } from "@/api/contentcore/publishpipe";
import { getPageWidget, addPageWidget, editPageWidget, publishPageWidgets } from "@/api/contentcore/pagewidget";
import CMSLogoView from '@/views/cms/components/LogoView';
import CMSTemplateSelector from '@/views/cms/contentcore/templateSelector';
import CMSCatalogSelector from "@/views/cms/contentcore/catalogSelector";
import CMSContentSelector from "@/views/cms/contentcore/contentSelector";

export default {
  name: "CMSBlockManualEditor",
  components: {
    'cms-template-selector': CMSTemplateSelector,
    'cms-catalog-selector': CMSCatalogSelector,
    'cms-content-selector': CMSContentSelector,
    'cms-logo-view': CMSLogoView
  },
  data () {
    return {
      loading: false,
      pageWidgetId: this.$route.query.id,
      publishPipes: [],
      form: {
        publishPipeCode: '',
        content: []
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
      title: "",
      dialogVisible: false,
      form_item: {},
      addItem: false,
      current: undefined,
      openCatalogSelector: false,
      openContentSelector: false,
      publishAfterSave: false,
    };
  },
  computed: {
    templateDisabled() {
      return !this.form.publishPipeCode || this.form.publishPipeCode == null || this.form.publishPipeCode === '';
    }
  },
  created() {
    if (this.pageWidgetId) {
      this.loadPublishPipes();
      this.loadPageWidgetInfo();
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
          this.form.contentStr = JSON.stringify(this.form.content);
          if (this.pageWidgetId) {
            editPageWidget(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.SaveSuccess'));
              this.initDataStr = JSON.stringify(this.form);
              if (this.publishAfterSave) {
                this.publishAfterSave = false
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
    handleAddRow(index) {
      this.form.content.splice(index, 0, {items:[]});
    },
    handleDeleteRow(index) {
      this.form.content.splice(index, 1);
    },
    handleAddItem(rowIndex, itemIndex) {
      this.title = this.$t('CMS.Block.AddItem');
      this.form_item = {};
      this.current = { row: rowIndex, col: itemIndex || 0 };
      this.addItem = true;
      this.dialogVisible = true;
    },
    handleDialogOk() {
      if (this.addItem) {
        this.form.content[this.current.row].items.splice(this.current.col, 0, this.form_item);
      } else {
        this.$set(this.form.content[this.current.row].items, this.current.col, this.form_item);
      }
      this.dialogVisible = false;
    },
    handleDialogClose() {
      this.dialogVisible = false;
    },
    handleEditItem(rowIndex, itemIndex) {
      this.current = { row: rowIndex, col: itemIndex };
      this.title = this.$t('CMS.Block.EditItem');
      this.addItem = false;
      this.form_item = this.form.content[rowIndex].items[itemIndex];
      this.dialogVisible = true;
    },
    handleDeleteItem(rowIndex, itemIndex) {
      this.form.content[rowIndex].items.splice(itemIndex, 1);
    },
    handleClear() {
      this.form.content = [];
    },
    handleLinkTo(type) {
      if (type === 'content') {
        this.openContentSelector = true;
      } else if (type === 'catalog') {
        this.openCatalogSelector = true;
      }
    },
    handleContentSelectorOk(contents) {
      if (contents && contents.length > 0) {
        this.form_item = {
          title: contents[0].title,
          logo: contents[0].logo || '',
          logoSrc: contents[0].logoSrc || '',
          publishDate: contents[0].publishDate || '',
          url: contents[0].internalUrl || '',
          summary: contents[0].summary || ''
        }
        this.openContentSelector = false;
      } else {
        this.$modal.msgWarning(this.$t('Common.SelectFirst'));
      }
      console.log(this.form_item)
    },
    handleContentSelectorClose() {
      this.openContentSelector = false;
    },
    handleCatalogSelectorOk(catalogs) {
      if (catalogs && catalogs.length > 0) {
        this.form_item = {
          title: catalogs[0].name,
          logo: catalogs[0].props.logo || '',
          logoSrc: catalogs[0].props.logoSrc || '',
          publishDate: '',
          url: catalogs[0].props.internalUrl || '',
          summary: catalogs[0].props.description || ''
        }
      }
      console.log(this.form_item)
      this.openCatalogSelector = false;
    },
    handleCatalogSelectorClose() {
      this.openCatalogSelector = false;
    },
    handleGoBack() {
      const obj = { name: "Content", params: { tab: "pageWdiget" } };
      this.$tab.closeOpenPage(obj);
    },
  }
};
</script>
<style scoped>
.block-manual-container .item-data {
  margin-left: 5px ;
  border-radius: 15px;
}
.block-manual-container .row {
  line-height: 36px;
}
.block-manual-container .row .el-link {
  font-size: 12px;
}
.block-manual-container .item-data .item-op .el-link {
  margin-left: 2px;
  font-size: 16px;
}
.block-manual-container .item-op-add {
  color: #1890ff;
}
.block-manual-container .item-op-del {
  color: #ff4949;
}
.block-manual-container .el-input, .el-select, .el-textarea {
  width: 400px;
}
.block-manual-container .form-row {
  width: 100%;
  display: inline-block;
}
.block-manual-container .el-form-item {
  width: 500px;
  float: left;
}
</style>