<template>
  <div class="catalog-info-container">
    <el-row :gutter="10" class="mb12 btn-row">
      <el-col :span="1.5">
        <el-button 
          plain
          type="success"
          icon="el-icon-edit"
          size="mini"
          :disabled="!this.catalogId"
          v-hasPermi="[ $p('Catalog:Edit:{0}', [ catalogId ]) ]"
          @click="handleUpdate">{{ $t("Common.Save") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="primary"
          size="mini"
          :disabled="!this.catalogId"
          @click="handlePreview"><svg-icon icon-class="eye-open" class="mr5"></svg-icon>{{ $t('CMS.ContentCore.Preview') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-dropdown class="btn-permi" v-hasPermi="[ $p('Catalog:Publish:{0}', [ catalogId ]) ]">
          <el-button 
            plain
            size="mini" 
            type="primary"
            icon="el-icon-s-promotion"
            :disabled="!this.catalogId"
            @click="handlePublish(-1)">
            {{ $t('CMS.ContentCore.Publish') }}<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown" :key="'btn-publish2-' + catalogId">
            <el-dropdown-item 
              v-for="dict in dict.type.CMSContentStatus" 
              :key="dict.value" 
              icon="el-icon-s-promotion" 
              @click.native="handlePublish(dict.value)"
            >{{dict.label}}{{ $t('CMS.Catalog.Content') }}</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="warning"
          :icon="catalogVisible?'el-icon-circle-close':'el-icon-circle-check'"
          size="mini"
          :disabled="!this.catalogId"
          v-hasPermi="[ $p('Catalog:ShowHide:{0}', [ catalogId ]) ]"
          @click="handleChangeVisible">{{ catalogVisible ? $t("Common.Hide") : $t("Common.Show") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          type="primary"
          icon="el-icon-rank"
          size="mini"
          plain
          :disabled="!this.catalogId"
          v-hasPermi="[ $p('Catalog:Move:{0}', [ catalogId ]) ]"
          @click="handleMoveCatalog">{{ $t('Common.Move') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          type="primary"
          icon="el-icon-document-copy"
          size="mini"
          plain
          :disabled="!this.catalogId"
          v-hasPermi="[ $p('Catalog:Edit:{0}', [ catalogId ]) ]"
          @click="handleMergeCatalogs">{{ $t('CMS.Catalog.Merge') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-popover
          width="226"
          :disabled="!this.catalogId"
          class="btn-permi"
          v-hasPermi="[ $p('Catalog:Sort:{0}', [ catalogId ]) ]"
          v-model="showSortPop">
          <el-input-number v-model="sortValue" size="small" style="width:200px;" />
          <div style="color: #909399;font-size:12px;line-height: 30px;">
            <i class="el-icon-info mr5"></i>{{ $t('CMS.Catalog.SortTip') }}
          </div>
          <div style="text-align: right; margin-top: 5px;">
            <el-button size="mini" type="text" @click="handleSortCatalogCancel">{{ $t('Common.Cancel') }}</el-button>
            <el-button type="primary" size="mini" @click="handleSortCatalog">{{ $t('Common.Confirm') }}</el-button>
          </div>
          <el-button 
            slot="reference" 
            size="mini"
            plain
            type="primary"
            icon="el-icon-sort"
            :disabled="!this.catalogId"
          >{{ $t('Common.Sort') }}</el-button>
        </el-popover>
      </el-col>
      <el-col :span="1.5">
        <el-popconfirm :title="$t('CMS.Catalog.DeleteTip')" @confirm="handleDelete" class="btn-permi" v-hasPermi="[ $p('Catalog:Delete:{0}', [ catalogId ]) ]">
          <el-button 
            type="danger" 
            icon="el-icon-delete"
            size="mini"
            plain
            :disabled="!this.catalogId"
            slot="reference"
          >{{ $t("Common.Delete") }}</el-button>
        </el-popconfirm>
      </el-col>
      <el-col :span="1.5">
        <el-popconfirm :title="$t('CMS.Catalog.ClearTip')" @confirm="handleClearCatalog" class="btn-permi" v-hasPermi="[ $p('Catalog:Edit:{0}', [ catalogId ]) ]">
          <el-button 
            type="danger"
            icon="el-icon-delete-solid"
            size="mini"
            plain
            :disabled="!this.catalogId"
            slot="reference"
          >{{ $t('CMS.Catalog.Clear') }}</el-button>
        </el-popconfirm>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          type="primary"
          icon="el-icon-rank"
          size="mini"
          plain
          @click="handleExportCatalogTree">{{ $t('CMS.Catalog.ExportCatalogTree') }}</el-button>
      </el-col>
    </el-row>
    <el-form 
      ref="form_info"
      v-loading="loading"
      :model="form_info"
      :rules="rules"
      :disabled="!this.catalogId"
      label-width="165px">
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>{{ $t('CMS.Catalog.Basic') }}</span>
        </div>
        <el-form-item :label="$t('CMS.Catalog.CatalogId')" prop="catalogId">
          <span class="span_catalogid" v-if="form_info.catalogId!=undefined">{{form_info.catalogId}}</span>
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.Name')" prop="name">
          <el-input v-model="form_info.name" />
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.Alias')" prop="alias">
          <el-input v-model="form_info.alias" />
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.Path')" prop="path">
          <el-input v-model="form_info.path" />
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.CatalogType')" prop="catalogType">
          <el-select v-model="form_info.catalogType" :placeholder="$t('CMS.Catalog.CatalogType')">
            <el-option 
              v-for="ct in catalogTypeOptions"
              :key="ct.id"
              :label="ct.name"
              :value="ct.id" />
          </el-select>
        </el-form-item>
        <el-form-item
          :label="$t('CMS.Catalog.RedirectUrl')"
          v-if="form_info.catalogType==='link'"
          prop="redirectUrl">
          <el-input v-model="form_info.redirectUrl" placeholder="http(s)://">
            <el-dropdown slot="append" @command="handleLinkTo">
              <el-button>
                {{ $t('CMS.ContentCore.InternalUrl') }}<i class="el-icon-arrow-down el-icon--right"></i>
              </el-button>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="content">{{ $t('CMS.ContentCore.SelectContent') }}</el-dropdown-item>
                <el-dropdown-item command="catalog">{{ $t('CMS.ContentCore.SelectCatalog') }}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </el-input>
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.Desc')" prop="description">
          <el-input v-model="form_info.description" type="textarea" maxlength="100" />
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.ContentPathRule')" prop="detailNameRule">
          <el-select v-model="form_info.detailNameRule" :placeholder="$t('CMS.Catalog.ContentPathRule')">
            <el-option 
              v-for="ct in contentPathRuleOptions"
              :key="ct.id"
              :label="ct.name"
              :value="ct.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.StaticFlag')" prop="staticFlag">
          <el-switch
            v-model="form_info.staticFlag"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.TagIgnore')" prop="tagIgnore">
          <el-switch
            v-model="form_info.tagIgnore"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.Logo')" prop="logo">
          <cms-logo-view v-model="form_info.logo" :src="form_info.logoSrc" :height="150"></cms-logo-view>
        </el-form-item>
      </el-card>
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>{{ $t('CMS.Catalog.PublishPipeConf') }}</span>
        </div>
        <el-tabs v-model="publishPipeActiveName">
          <el-tab-pane 
            v-for="pp in this.form_info.publishPipeDatas"
            :key="pp.pipeCode"
            :command="pp"
            :name="pp.pipeCode"
            :label="pp.pipeName">
            <el-divider content-position="left">{{ $t('CMS.Catalog.TemplateConfig') }}</el-divider>
            <el-form-item :label="$t('CMS.Catalog.IndexTemplate')" prop="indexTemplate">
              <el-input v-model="pp.props.indexTemplate">
                <el-button 
                  slot="append"
                  type="primary"
                  @click="handleSelectTemplate('indexTemplate')"
                >{{ $t("Common.Select") }}</el-button>
              </el-input>
            </el-form-item>
            <el-form-item :label="$t('CMS.Catalog.ListTemplate')" prop="listTemplate">
              <el-input class="mr5" v-model="pp.props.listTemplate">
                <el-button 
                  slot="append"
                  type="primary"
                  @click="handleSelectTemplate('listTemplate')"
                >{{ $t("Common.Select") }}</el-button>
              </el-input>
              <el-button 
                plain 
                icon="el-icon-bottom-right" 
                type="primary" 
                @click="handleApplyToChildren('listTemplate')">{{ $t('CMS.Catalog.ApplyToChildren') }}</el-button>
            </el-form-item>
            <el-form-item 
              v-for="ct of contentTypes" 
              :key="ct.id" 
              :command="ct"
              :label="ct.name + $t('CMS.Catalog.DetailTemplate')"
              :prop="`detailTemplate_${ct.id}`">
              <el-input class="mr5" v-model="pp.props[`detailTemplate_${ct.id}`]">
                <el-button 
                  slot="append"
                  type="primary"
                  @click="handleSelectTemplate(`detailTemplate_${ct.id}`)"
                >{{ $t("Common.Select") }}</el-button>
              </el-input>
              <el-button 
                icon="el-icon-bottom-right" 
                type="primary" 
                plain 
                @click="handleApplyToChildren(`detailTemplate_${ct.id}`)"
              >{{ $t('CMS.Catalog.ApplyToChildren') }}</el-button>
            </el-form-item>
            <el-form-item :label="$t('CMS.Catalog.ContentExTemplate')" prop="contentExTemplate">
              <el-input class="mr5" v-model="pp.props.contentExTemplate">
                <el-button 
                  slot="append"
                  type="primary"
                  @click="handleSelectTemplate('contentExTemplate')"
                >{{ $t("Common.Select") }}</el-button>
              </el-input>
              <el-button 
                plain 
                icon="el-icon-bottom-right" 
                type="primary" 
                @click="handleApplyToChildren('contentExTemplate')">{{ $t('CMS.Catalog.ApplyToChildren') }}</el-button>
            </el-form-item>
            <el-divider content-position="left">{{ $t('CMS.Catalog.OtherConfig') }}</el-divider>
            <el-form-item :label="$t('CMS.Site.UEditorCss')">
              <el-input v-model="pp.props.ueditorCss">
                <el-button 
                  slot="append"
                  type="primary"
                  @click="handleSelectFile()"
                >{{ $t("Common.Select") }}</el-button>
              </el-input>
            </el-form-item>
          </el-tab-pane>
        </el-tabs>
      </el-card>
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>{{ $t('CMS.ContentCore.SEOConfig') }}</span>
        </div>
        <el-form-item :label="$t('CMS.ContentCore.SEOTitle')" prop="seoTitle">
          <el-input v-model="form_info.seoTitle" />
        </el-form-item>
        <el-form-item :label="$t('CMS.ContentCore.SEOKeyword')" prop="seoKeywords">
          <el-input v-model="form_info.seoKeywords" />
        </el-form-item>
        <el-form-item :label="$t('CMS.ContentCore.SEODescription')" prop="seoDescription">
          <el-input v-model="form_info.seoDescription" type="textarea" :maxlength="100" />
        </el-form-item>
      </el-card>
      <el-card v-if="showEXModel" shadow="hover">
        <div slot="header" class="clearfix">
          <span>{{ $t('CMS.Catalog.ExModelProps') }}</span>
        </div>
        
        <cms-exmodel-editor 
          ref="EXModelEditor"
          :xmodel="form_info.configProps.CatalogExtendModel" 
          type="catalog"
          :id="form_info.catalogId">
        </cms-exmodel-editor>
      </el-card>
    </el-form>
    <el-dialog 
      :title="$t('CMS.Catalog.PublishDialogTitle')"
      :visible.sync="publishDialogVisible"
      width="500px"
      class="publish-dialog">
      <div>
        <p>{{ $t('Common.Tips') }}</p>
        <p>{{ $t('CMS.Catalog.PublishTips') }}</p>
        <el-checkbox v-model="publishChild">{{ $t('CMS.Catalog.ContainsChildren') }}</el-checkbox>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="publishDialogVisible = false">{{ $t("Common.Cancel") }}</el-button>
        <el-button type="primary" @click="handleDoPublish">{{ $t("Common.Confirm") }}</el-button>
      </span>
    </el-dialog>
    <!-- 导出栏目树 -->
    <el-dialog 
      :title="$t('CMS.Catalog.ExportCatalogTree')"
      :visible.sync="catalogTreeDialogVisible"
      width="500px"
      class="catalog-tree-dialog">
      <div>
        <el-input type="textarea" :rows="10" v-model="catalogTree" readonly style="width:100%;"></el-input>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" v-clipboard:copy="catalogTree" v-clipboard:success="clipboardSuccess">{{ $t("Common.Copy") }}</el-button>
        <el-button @click="catalogTreeDialogVisible = false">{{ $t("Common.Close") }}</el-button>
      </span>
    </el-dialog>
    <!-- 模板选择组件 -->
    <cms-template-selector 
      :open="openTemplateSelector" 
      :publishPipeCode="publishPipeActiveName"
      @ok="handleTemplateSelected"
      @cancel="handleTemplateSelectorCancel" />
    <!-- 栏目选择组件 -->
    <cms-catalog-selector
      :open="openCatalogSelector"
      :showRootNode="showCatalogSelectorRootNode"
      :disableLink="disableLinkCatalog"
      :multiple="multipleCatalogSeletor"
      @ok="handleCatalogSelectorOk"
      @close="handleCatalogSelectorClose"></cms-catalog-selector>
    <!-- 内容选择组件 -->
    <cms-content-selector
      :open="openContentSelector"
      @ok="handleContentSelectorOk"
      @close="handleContentSelectorClose"></cms-content-selector>
    <!-- 站点文件选择组件 -->
    <cms-file-selector :open.sync="openFileSelector" suffix="css" @ok="handleSetUEditorStyle" @close="openFileSelector=false"></cms-file-selector>
    <!-- 进度条 -->
    <cms-progress :title="progressTitle" :open.sync="openProgress" :taskId="taskId" @close="handleCloseProgress"></cms-progress>
  </div>
</template>
<script>
import * as catalogApi from "@/api/contentcore/catalog";
import CMSCatalogSelector from "@/views/cms/contentcore/catalogSelector";
import CMSContentSelector from "@/views/cms/contentcore/contentSelector";
import CMSTemplateSelector from '@/views/cms/contentcore/templateSelector';
import CMSProgress from '@/views/components/Progress';
import CMSLogoView from '@/views/cms/components/LogoView';
import CMSEXModelEditor from '@/views/cms/components/EXModelEditor';
import CMSFileSelector from '@/views/cms/components/FileSelector';

export default {
  name: "CMSCatalogInfo",
  components: {
    "cms-exmodel-editor": CMSEXModelEditor,
    'cms-template-selector': CMSTemplateSelector,
    'cms-catalog-selector': CMSCatalogSelector,
    'cms-content-selector': CMSContentSelector,
    'cms-progress': CMSProgress,
    "cms-logo-view": CMSLogoView,
    "cms-file-selector": CMSFileSelector
  },
  dicts: ['CMSStaticSuffix', 'CMSContentStatus'],
  props: {
    cid: {
      type: String, 
      default: undefined,
      required: false,
    },
  },
  computed: {
    showEXModel() {
      return this.form_info.configProps && this.form_info.configProps.CatalogExtendModel != null && this.form_info.configProps.CatalogExtendModel.length > 0;
    },
    catalogVisible() {
      return this.form_info.visibleFlag == "Y";
    }
  },
  data () {
    return {
      // 遮罩层
      loading: false,
      activeName: 'basicInfo',
      openCatalogSelector: false,
      catalogSelectorFor: undefined,
      showCatalogSelectorRootNode: false,
      multipleCatalogSeletor: false,
      disableLinkCatalog: false,
      openContentSelector: false,
      openTemplateSelector: false, // 是否显示模板选择弹窗
      propKey: "", // 选择模板时记录变更的模板对应属性Key
      openProgress: false, // 是否显示任务进度条
      progressTitle: "",
      progressType: "",
      taskId: "", // 任务ID
      // 发布选项弹窗
      publishDialogVisible: false,
      publishChild: false,
      publishStatus: -1,
      publishPipeActiveName: "", // 当前选中的发布通道Tab
      catalogId: parseInt(this.cid),
      showSortPop: false,
      sortValue: 0,
      // 栏目信息表单
      form_info: {
        siteId: ""
      },
      contentPathRuleOptions: [],
      catalogTypeOptions: [],
      publishPipes: [], // 栏目发布通道数据
      // 表单校验
      rules: {
        name: [
          { required: true, message: this.$t('CMS.Catalog.RuleTips.Name'), trigger: "blur" }
        ],
        alias: [
          { required: true, pattern: "^[A-Za-z0-9_]+$", message: this.$t('CMS.Catalog.RuleTips.Alias'), trigger: "blur" }
        ],
        path: [
          { required: true, pattern: "^[A-Za-z0-9_\/]+$", message: this.$t('CMS.Catalog.RuleTips.Path'), trigger: "blur" }
        ],
        catalogType: [
          { required: true, message: this.$t('CMS.Catalog.RuleTips.CatalogType'), trigger: "blur" }
        ]
      },
      openFileSelector: false,
      catalogTreeDialogVisible: false,
      catalogTree: ""
    };
  },
  created() {
    this.loadCatalogTypes();
    this.loadContentTypes();
    this.loadCatalogInfo();
    this.loadContentPathRules();
  },
  watch: {
    cid(newVal) {
      this.catalogId = newVal;
    },
    catalogId(newVal) {
      if (newVal && newVal > 0) {
        this.loadCatalogInfo();
      } else {
        this.form_info = { siteId: "" };
      }
    }
  },
  methods: {
    loadContentPathRules() {
      catalogApi.getContentPathRules().then(response => {
        this.contentPathRuleOptions = response.data;
      });
    },
    loadContentTypes() {
      catalogApi.getContentTypes().then(response => {
        this.contentTypes = response.data;
      });
    },
    loadCatalogTypes () {
      catalogApi.getCatalogTypes().then(response => {
        this.catalogTypeOptions = response.data;
      });
    },
    loadCatalogInfo () {
      if (!this.catalogId) {
        // this.$modal.msgError(this.$t('CMS.Catalog.SelectCatalogFirst'));
        return;
      }
      this.loading = true;
      catalogApi.getCatalogData(this.catalogId).then(response => {
        this.form_info = response.data;
        if (this.form_info.publishPipeDatas.length > 0) {
          this.publishPipeActiveName = this.form_info.publishPipeDatas[0].pipeCode;
        }
        this.loading = false;
      });
    },
    handleUpdate () {
      this.$refs["form_info"].validate(valid => {
        if (valid) {
          if (this.showEXModel) {
            this.form_info.params = this.$refs.EXModelEditor.getDatas();
          }
          catalogApi.updateCatalog(this.form_info).then(response => {
            this.$modal.msgSuccess(this.$t('Common.SaveSuccess'));
            this.$emit("reload");
          });
        }
      });
    },
    handleChangeVisible () {
        const visible = this.form_info.visibleFlag == "Y" ? "N" : "Y";
        catalogApi.changeVisible(this.form_info.catalogId, visible).then(response => {
          this.$modal.msgSuccess(response.msg);
          this.form_info.visibleFlag = visible;
        });
    },
    handlePreview () {
      let routeData = this.$router.resolve({
        path: "/cms/preview",
        query: { type: "catalog", dataId: this.form_info.catalogId },
      });
      window.open(routeData.href, '_blank');
    },
    handlePublish (publishStatus) {
      this.publishStatus = publishStatus;
      this.publishDialogVisible = true;
    },
    handleDoPublish() {
      const data = {
        catalogId: this.form_info.catalogId,
        publishDetail: this.publishStatus != -1,
        publishStatus: this.publishStatus,
        publishChild: this.publishChild
      };
      this.publishDialogVisible = false;
      this.publishChild = false;
      catalogApi.publishCatalog(data).then(response => {
        this.taskId = response.data;
        this.progressTitle = this.$t('CMS.Catalog.PublishProgressTitle');
        this.progressType = "Publish";
        this.openProgress = true;
        this.$cache.local.set('publish_flag', "true")
      }); 
    },
    handleDelete () {
      if (!this.catalogId) {
        this.$modal.msgWarning(this.$t('CMS.Catalog.SelectCatalogFirst'));
        retrun;
      }
      catalogApi.delCatalog(this.catalogId).then(response => {
        if (response.data && response.data != "") {
          this.taskId = response.data;
          this.progressTitle = this.$t('CMS.Catalog.DeleteProgressTitle');
          this.progressType = "Delete";
          this.openProgress = true;
          
          console.log("parentId", this.form_info.parentId)
          this.$cache.local.set("LastSelectedCatalogId", this.form_info.parentId);
        }
      });
    },
    handleMoveCatalog() {
      this.catalogSelectorFor = "MoveCatalog";
      this.openCatalogSelector = true;
      this.showCatalogSelectorRootNode = true;
      this.disableLinkCatalog = false;
      this.multipleCatalogSeletor = false;
    },
    handleCloseProgress() {
      if (this.progressType == 'Delete' || this.progressType == 'Move' || this.progressType == 'Merge') {
          this.resetForm("form_info");
          this.$emit("reload"); 
      }
    },
    handleSelectTemplate (propKey) {
      this.propKey = propKey;
      this.openTemplateSelector = true;
    },
    handleTemplateSelected (template) {
      this.form_info.publishPipeDatas.map(item => {
        if (item.pipeCode == this.publishPipeActiveName) {
          item.props[this.propKey] = template;
        }
      });
      this.openTemplateSelector = false;
    },
    handleTemplateSelectorCancel () {
      this.openTemplateSelector = false;
    },
    handleApplyToChildren (propKey) {
      const data = { 
        catalogId: this.catalogId,
        publishPipeCode: this.publishPipeActiveName,
        publishPipePropKeys: [ propKey ]
       }
       catalogApi.applyPublishPipeToChildren(data).then(res => {
         this.$modal.msgSuccess(res.msg);
       });
    },
    handleLinkTo(type) {
      if (type === 'content') {
        this.openContentSelector = true;
      } else if (type === 'catalog') {
        this.openCatalogSelector = true;
        this.showCatalogSelectorRootNode = false;
        this.disableLinkCatalog = true;
        this.multipleCatalogSeletor = false;
        this.catalogSelectorFor = "";
      }
    },
    handleCatalogSelectorOk(catalogs) {
      if (this.catalogSelectorFor == 'MoveCatalog') {
        let toCatalog = "0";
        if (catalogs && catalogs.length > 0) {
          toCatalog = catalogs[0].id;
        }
        catalogApi.moveCatalog(this.catalogId, toCatalog).then(response => {
          if (response.data && response.data != "") {
            this.taskId = response.data;
            this.progressTitle = this.$t('CMS.Catalog.MoveProgressTitle');;
            this.progressType = "Move";
            this.openProgress = true;
          }
        })
      } if (this.catalogSelectorFor == 'MergeCatalog') {
        this.doMergeCatalogs(catalogs)
      } else {
        if (catalogs && catalogs.length > 0) {
          this.form_info.redirectUrl = catalogs[0].props.internalUrl;
        }
      }
      this.openCatalogSelector = false;
      this.catalogSelectorFor = undefined;
    },
    handleCatalogSelectorClose() {
      this.openCatalogSelector = false;
    },
    handleContentSelectorOk(contents) {
      if (contents && contents.length > 0) {
        this.form_info.redirectUrl = contents[0].internalUrl;
      }
      this.openContentSelector = false;
    },
    handleContentSelectorClose() {
      this.openContentSelector = false;
    },
    handleSortCatalog() {
      if (this.sortValue == 0) {
        this.$modal.msgWarning("排序值不能为0");
        return;
      }
      let data = { catalogId: this.catalogId, sort: this.sortValue }
      catalogApi.sortCatalog(data).then(response => {
          this.$modal.msgSuccess(response.msg);
          this.showSortPop = false;
          this.sortValue = 0;
          this.$emit("reload"); 
      });
    },
    handleSortCatalogCancel() {
      this.showSortPop = false;
      this.sortValue = 0;
    },
    handleSelectFile() {
      this.openFileSelector = true;
    },
    handleSetUEditorStyle(files) {
      if (files.length > 0) {
        this.form_info.publishPipeDatas.map(item => {
          if (item.pipeCode == this.publishPipeActiveName) {
            item.props.ueditorCss = files[0].filePath;
          }
        });
      }
      this.openFileSelector = false;
    },
    handleMergeCatalogs() {
      this.catalogSelectorFor = "MergeCatalog";
      this.openCatalogSelector = true;
      this.showCatalogSelectorRootNode = false;
      this.disableLinkCatalog = true;
      this.multipleCatalogSeletor = true;
    },
    doMergeCatalogs(catalogs) {
      if (!catalogs || catalogs.length == 0) {
        this.$modal.msgWarning(this.$t('CMS.Catalog.SelectCatalogFirst'));
        return;
      }
      const mergeCatalogIds = catalogs.map(c => c.id);
      const data = { catalogId: this.catalogId, mergeCatalogIds: mergeCatalogIds }
      catalogApi.mergeCatalogs(data).then(response => {
        if (response.data && response.data != "") {
          this.taskId = response.data;
          this.progressTitle = this.$t('CMS.Catalog.MergeProgressTitle');;
          this.progressType = "Merge";
          this.openProgress = true;
        }
      })
    },
    handleClearCatalog() {
      const data = { catalogId: this.catalogId }
      catalogApi.clearCatalog(data).then(response => {
        if (response.data && response.data != "") {
          this.taskId = response.data;
          this.progressTitle = this.$t('CMS.Catalog.ClearProgressTitle');;
          this.progressType = "Clear";
          this.openProgress = true;
        }
      });
    },
    handleExportCatalogTree() {
      this.catalogTreeDialogVisible = true;
      const params = { catalogId: this.catalogId || 0 }
      catalogApi.getCatalogTree(params).then(response => {
        this.catalogTree = response.data;
      });
    },
    clipboardSuccess() {
      this.$modal.msgSuccess(this.$t('Common.CopySuccess'));
    }
  }
};
</script>
<style scoped>
.el-form-item {
  margin-bottom: 18px;
  width: 700px;
}
.el-input, .el-select, .el-textarea {
  width: 330px;
}
.el-card {
  margin-bottom: 10px;
}
</style>