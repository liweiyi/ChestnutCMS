<template>
  <div class="app-container content-editor-container">
    <el-row>
      <el-col :span="24">
        <div class="grid-btn-bar bg-purple-white">
          <el-row :gutter="10">
            <el-col :span="1.5">
              <el-button plain type="success" size="mini" icon="el-icon-edit" v-if="contentId=='0'" v-hasPermi="[ $p('Catalog:AddContent:{0}', [ catalogId ]) ]" @click="handleSave">{{ $t("Common.Save") }}</el-button>
              <el-button plain type="success" size="mini" icon="el-icon-edit" v-else v-hasPermi="[ $p('Catalog:EditContent:{0}', [ catalogId ]) ]" @click="handleSave">{{ $t("Common.Save") }}</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button plain type="primary" size="mini" icon="el-icon-timer" v-hasPermi="[ $p('Catalog:Publish:{0}', [ catalogId ]) ]" @click="handleToPublish">{{ $t('CMS.ContentCore.ToPublish') }}</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button plain type="primary" size="mini" icon="el-icon-s-promotion" v-hasPermi="[ $p('Catalog:Publish:{0}', [ catalogId ]) ]" @click="handlePublish">{{ $t('CMS.ContentCore.Publish') }}</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button plain type="primary" size="mini" @click="handlePreview"><svg-icon icon-class="eye-open" class="mr5"></svg-icon>{{ $t('CMS.ContentCore.Preview') }}</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button plain type="warning" v-if="isLock" size="mini" icon="el-icon-unlock" v-hasPermi="[ $p('Catalog:EditContent:{0}', [ catalogId ]) ]" @click="handleChangeLockState">{{ $t('CMS.Content.Unlock') }}</el-button>
              <el-button plain type="primary" v-else size="mini" icon="el-icon-lock" v-hasPermi="[ $p('Catalog:EditContent:{0}', [ catalogId ]) ]" @click="handleChangeLockState">{{ $t('CMS.Content.Lock') }}</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button plain type="primary" size="mini" icon="el-icon-share" @click="handleRelaContent">{{ $t('CMS.Content.RelaContent') }}</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button plain type="primary" size="mini" icon="el-icon-files" v-hasPermi="[ $p('Catalog:EditContent:{0}', [ catalogId ]) ]" @click="handlePushToBaidu">{{ $t('CMS.Content.PushToBaidu') }}</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button plain type="primary" size="mini" icon="el-icon-takeaway-box" @click="handleOpLogs">{{ $t('CMS.Content.OpLog') }}</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-popover v-model="addPopoverVisible" class="btn-permi" placement="bottom-start" :width="420" trigger="click" v-hasPermi="[ $p('Catalog:AddContent:{0}', [ catalogId ]) ]">
                <el-row style="margin-bottom:20px;">
                  <el-divider content-position="left">{{ $t('CMS.Content.ContentType') }}</el-divider>
                  <el-radio-group v-model="addContentType">
                    <el-radio-button 
                      v-for="ct in contentTypeOptions"
                      :key="ct.id"
                      :label="ct.id"
                    >{{ ct.name }}</el-radio-button>
                  </el-radio-group>
                  <el-divider v-if="addContentType=='article'" content-position="left">{{ $t('CMS.Article.Format') }}</el-divider>
                  <el-radio-group v-if="addContentType=='article'" v-model="addArticleBodyFormat">
                    <el-radio-button 
                      v-for="item in articleBodyFormatOptions"
                      :key="item.id"
                      :label="item.id"
                    >{{ item.name }}</el-radio-button>
                  </el-radio-group>
                </el-row>
                <el-row style="text-align:right;">
                  <el-button 
                    plain
                    type="primary"
                    size="small"
                    @click="handleAdd">{{ $t('Common.Confirm') }}</el-button>
                </el-row>
                <el-button 
                  type="primary"
                  slot="reference"
                  icon="el-icon-plus"
                  size="mini"
                  plain>{{ $t("Common.Add") }}<i class="el-icon-arrow-down el-icon--right"></i>
                </el-button>
              </el-popover>
            </el-col>
            <el-col :span="1.5">
              <el-button plain type="warning" size="mini" icon="el-icon-close" @click="handleClose">{{ $t('Common.Close') }}</el-button>
            </el-col>
          </el-row>
        </div>
      </el-col>
    </el-row>
    <el-row class="art-editor-container" :gutter="10" v-loading="loading">
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-col :span="16">
          <el-row>
            <el-col class="pr10">
              <el-card shadow="always" class="card-title">
                <div class="art-title bg-purple-white">
                  <el-form-item :label="$t('CMS.Content.Title')" prop="title" style="margin-bottom: 18px;">
                    <el-input
                      v-model="form.title"
                      maxlength="360"
                      show-word-limit>
                      <el-button
                        slot="append"
                        icon="el-icon-arrow-down"
                        @click="toggleOtherTitle"
                      ></el-button>
                    </el-input>
                  </el-form-item>
                  <el-form-item
                    :label="shortTitleLabel"
                    v-if="showOtherTitle"
                    prop="shortTitle">
                    <el-input v-model="form.shortTitle" maxlength="120" show-word-limit />
                  </el-form-item>
                  <el-form-item
                    :label="subTitleLabel"
                    v-if="showOtherTitle"
                    prop="subTitle">
                    <el-input v-model="form.subTitle" maxlength="120" show-word-limit />
                  </el-form-item>
                  <el-form-item
                    :label="$t('CMS.Content.LinkFlag')"
                    prop="linkFlag">
                    <el-checkbox v-model="form.linkFlag" true-label="Y" false-label="N"></el-checkbox>
                  </el-form-item>
                  <el-form-item
                    :label="$t('CMS.Content.RedirectUrl')"
                    v-if="form.linkFlag==='Y'"
                    prop="redirectUrl">
                    <el-input v-model="form.redirectUrl" placeholder="http(s)://" class="mb12" />
                    <el-dropdown @command="handleLinkTo">
                      <el-button
                        type="primary">
                        {{ $t('CMS.ContentCore.InternalUrl') }}<i class="el-icon-arrow-down el-icon--right"></i>
                      </el-button>
                      <el-dropdown-menu slot="dropdown">
                        <el-dropdown-item command="content">{{ $t('CMS.ContentCore.SelectContent') }}</el-dropdown-item>
                        <el-dropdown-item command="catalog">{{ $t('CMS.ContentCore.SelectCatalog') }}</el-dropdown-item>
                      </el-dropdown-menu>
                    </el-dropdown>
                  </el-form-item>
                </div>
              </el-card>
            </el-col>
          </el-row>
          <el-row v-if="xmodelVisible" class="mb10">
            <el-col class="pr10">
              <el-card shadow="always" class="card-exmodel">
                <cms-exmodel-editor 
                  ref="EXModelEditor"
                  :xmodel="form.catalogConfigProps.ContentExtendModel" 
                  type="content"
                  :id="form.contentId">
                </cms-exmodel-editor>
              </el-card>
            </el-col>
          </el-row>
          <el-row v-if="this.form.linkFlag !== 'Y' && this.contentType === 'article'">
            <el-col class="pr10">
              <el-card v-if="articleFormat=='RichText'" shadow="always" class="card-editor">
                <el-row>
                  <el-col :span="12">
                    <el-form-item :label="$t('CMS.Content.DownloadImage')" prop="downloadRemoteImage" style="margin-bottom:0;">
                      <el-switch
                        v-model="form.downloadRemoteImage"
                        active-value="Y"
                        inactive-value="N" />
                        <span style="color: #909399;font-size:12px;"><i class="el-icon-info mr5 ml10"></i>{{ $t('CMS.Content.DownloadImageTip') }}</span>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item style="text-align:right;">
                      <el-tooltip class="item" effect="dark" :content="$t('CMS.Content.ImportCSSTip')" placement="top">
                        <i class="el-icon-info" style="color:#909399;margin-right:16px;" />
                      </el-tooltip>
                      <el-select v-model="ueditorImportCss" :placeholder="$t('CMS.Content.Placeholder.ImportCSS')" clearable  @change="handleChangeUEditorCSS()">
                        <el-option 
                          v-for="pp in publishPipeProps"
                          :key="pp.pipeCode"
                          :label="pp.pipeName"
                          :value="pp.pipeCode" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                <div class="content bg-purple-white">
                  <ueditor editorId="ue_article" v-model="form.contentHtml" :height="800"></ueditor>
                </div>
              </el-card>
            </el-col>
          </el-row>
          <el-row v-if="this.form.linkFlag !== 'Y' && this.contentType === 'image'">
            <el-col class="pr10">
              <cms-image-editor v-model="form.imageList" @choose="handleSetLogo"></cms-image-editor>
            </el-col>
          </el-row>
          <el-row v-if="this.form.linkFlag !== 'Y' && this.contentType === 'audio'">
            <el-col class="pr10">
              <cms-audio-editor v-model="form.audioList" :logo="form.logoSrc" @choose="handleSetLogo"></cms-audio-editor>
            </el-col>
          </el-row>
          <el-row v-if="this.form.linkFlag !== 'Y' && this.contentType === 'video'">
            <el-col class="pr10">
              <cms-video-editor v-model="form.videoList" @choose="handleSetLogo"></cms-video-editor>
            </el-col>
          </el-row>
        </el-col>
        <el-col :span="8">
          <div class="bg-purple-white">
            <el-card shadow="always">
              <el-tabs v-model="activeName" @tab-click="handleTabClick">
                <el-tab-pane :label="$t('CMS.Content.Basic')" name="basic">
                  <el-form-item :label="$t('CMS.Content.Catalog')" prop="catalogId">
                    <el-button-group>
                      <el-button plain type="primary" style="width:152px;" disabled>{{ form.catalogName }}</el-button>
                      <el-button type="primary" icon="el-icon-edit" @click="handleCatalogChange"></el-button>
                    </el-button-group>
                  </el-form-item>
                  <el-form-item :label="$t('CMS.Content.Logo')" prop="logo">
                    <cms-image-group v-model="form.images" :src="form.imagesSrc" :limit="6"></cms-image-group>
                  </el-form-item>
                  <el-form-item :label="$t('CMS.Content.Author')" prop="author">
                    <el-input v-model="form.author" />
                  </el-form-item>
                  <el-form-item :label="$t('CMS.Content.Editor')" prop="editor">
                    <el-input v-model="form.editor" />
                  </el-form-item>
                  <el-form-item :label="$t('CMS.Content.Original')" prop="original">
                    <el-switch
                      v-model="form.original"
                      active-value="Y"
                      inactive-value="N"
                    ></el-switch>
                  </el-form-item>
                  <el-form-item :label="$t('CMS.Content.Attribute')" prop="attributes">
                    <el-checkbox-group v-model="form.attributes">
                      <el-checkbox v-for="dict in dict.type.CMSContentAttribute" :label="dict.value" :key="dict.value">{{ dict.label }}</el-checkbox>
                    </el-checkbox-group>
                  </el-form-item>
                  <el-form-item :label="$t('CMS.Content.Summary')" prop="summary">
                    <el-input type="textarea" v-model="form.summary" :autosize="summaryInputSize" maxlength="500" show-word-limit />
                  </el-form-item>
                  <el-form-item :label="$t('CMS.Content.Tags')" prop="tags">
                    <cms-tag-editor v-model="form.tags" :select="true"></cms-tag-editor>
                  </el-form-item>
                  <el-form-item :label="$t('CMS.Content.Keywords')" prop="keywords">
                    <cms-tag-editor v-model="form.keywords"></cms-tag-editor>
                  </el-form-item>
                  <el-form-item :label="$t('CMS.Content.Source')" prop="source">
                    <el-input v-model="form.source" />
                  </el-form-item>
                  <el-form-item :label="$t('CMS.Content.SourceUrl')" prop="sourceUrl">
                    <el-input v-model="form.sourceUrl" placeholder="http(s)://" />
                  </el-form-item>
                  <el-divider></el-divider>
                  <el-form-item :label="$t('CMS.Content.PublishDate')" prop="publishDate">
                    <el-date-picker v-model="form.publishDate" value-format="yyyy-MM-dd HH:mm:ss" type="datetime" style="width:195px;" />
                  </el-form-item>
                  <el-form-item :label="$t('CMS.Content.OfflineDate')" prop="offlineDate">
                    <el-date-picker v-model="form.offlineDate" value-format="yyyy-MM-dd HH:mm:ss" type="datetime" style="width:195px;" />
                  </el-form-item>
                  <el-form-item :label="$t('CMS.Content.StaticPath')">
                    <el-input v-model="form.staticPath" />
                  </el-form-item>
                  <el-form-item :label="$t('CMS.Content.Template')">
                    <el-switch v-model="showTemplate" @change="handleShowTemplateChange" />
                  </el-form-item>
                  <el-form-item v-show="showTemplate" 
                                v-for="pp in publishPipeProps" 
                                :label="pp.pipeName" 
                                :key="pp.pipeCode" 
                                :prop="'template_' + pp.value">
                    <el-input v-model="pp.props.template">
                      <el-button slot="append" icon="el-icon-folder-opened" @click="handleSelectTemplate(pp)"></el-button>
                    </el-input>
                  </el-form-item>
                </el-tab-pane>
                <el-tab-pane :label="$t('CMS.Content.ExtendConfig')" name="extend">
                  <el-divider content-position="left">{{ $t('CMS.Content.SEOConfig') }}</el-divider>
                  <el-form-item :label="$t('CMS.ContentCore.SEOTitle')" prop="seoTitle">
                    <el-input v-model="form.seoTitle" />
                  </el-form-item>
                  <el-form-item :label="$t('CMS.ContentCore.SEOKeyword')" prop="seoKeywords">
                    <el-input v-model="form.seoKeywords" />
                  </el-form-item>
                  <el-form-item :label="$t('CMS.ContentCore.SEODescription')" prop="seoDescription">
                    <el-input v-model="form.seoDescription" />
                  </el-form-item>
                  <el-divider content-position="left">{{ $t('CMS.Content.ExtendTemplate') }}</el-divider>
                  <el-form-item v-for="pp in publishPipeProps" 
                                :label="pp.pipeName" 
                                :key="pp.pipeCode + '_ex'" 
                                :prop="'contentExTemplate_' + pp.value">
                    <el-input v-model="pp.props.contentExTemplate">
                      <el-button slot="append" icon="el-icon-folder-opened" @click="handleSelectTemplate(pp, true)"></el-button>
                    </el-input>
                  </el-form-item>
                </el-tab-pane>
              </el-tabs>
            </el-card>
          </div>
        </el-col>
      </el-form>
    </el-row>
    <!-- 模板选择组件 -->
    <cms-template-selector :open="openTemplateSelector" 
                       :publishPipeCode="publishPipeActiveName"
                       @ok="handleTemplateSelected"
                       @cancel="handleTemplateSelectorCancel" />
    <!-- 栏目选择组件 -->
    <cms-catalog-selector
      :open="openCatalogSelector"
      :disableLink="disableLinkCatalog"
      @ok="handleCatalogSelectorOk"
      @close="handleCatalogSelectorClose"></cms-catalog-selector>
    <!-- 内容选择组件 -->
    <cms-content-selector
      :open="openContentSelector"
      @ok="handleContentSelectorOk"
      @close="handleContentSelectorClose"></cms-content-selector>
    <cms-content-rela-dialog :cid="contentId" :open="openRelaContentDialog" @close="handleRelaContentClose"></cms-content-rela-dialog>
    <cms-content-oplog-dialog :cid="contentId" :open="openContentOpLogDialog" @close="handleOpLogsClose"></cms-content-oplog-dialog>
    <!-- 进度条 -->
    <cms-progress :title="progressTitle" :open.sync="openProgress" :taskId="taskId" message-class="message-top-right" @close="handleProgressClose"></cms-progress>
  </div>
</template>
<script>
import { getContentTypes } from "@/api/contentcore/catalog";
import { getInitContentEditorData, addContent, saveContent, toPublishContent, publishContent, lockContent, unLockContent, moveContent } from "@/api/contentcore/content";
import { getUEditorCSS, getArticleBodyFormats } from "@/api/contentcore/article"
import { pushToBaidu } from "@/api/seo/baidupush";
import Sticky from '@/components/Sticky'
// import CKEditor5 from '@/components/CKEditor5';
import UEditor from '@/views/cms/components/UEditorPlus'
import CMSProgress from '@/views/components/Progress';
import CMSImageEditor from '@/views/cms/imageAlbum/editor';
import CMSAudioEditor from '@/views/cms/media/audioEditor';
import CMSVideoEditor from '@/views/cms/media/videoEditor';
import CMSImageGroup from '@/views/cms/components/ImageGroup';
import CMSCatalogSelector from "@/views/cms/contentcore/catalogSelector";
import CMSContentSelector from "@/views/cms/contentcore/contentSelector";
import CMSContentRelaDialog from '@/views/cms/contentcore/contentRelaDialog';
import CMSContentOpLogDialog from '@/views/cms/contentcore/contentOpLogDialog';
import CMSTemplateSelector from '@/views/cms/contentcore/templateSelector';
import CMSEXModelEditor from '@/views/cms/components/EXModelEditor';
import CMSTagEditor from '@/views/cms/components/TagEditor';

import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";

export default {
  name: "CMSContentEditor",
  dicts: ['CMSContentAttribute'],
  components: {
    Treeselect, 
    Sticky,
    'ueditor': UEditor,
    'cms-template-selector': CMSTemplateSelector,
    'cms-progress': CMSProgress,
    "cms-image-editor": CMSImageEditor,
    "cms-audio-editor": CMSAudioEditor,
    "cms-video-editor": CMSVideoEditor,
    "cms-image-group": CMSImageGroup,
    'cms-catalog-selector': CMSCatalogSelector,
    'cms-content-selector': CMSContentSelector,
    'cms-content-rela-dialog': CMSContentRelaDialog,
    "cms-content-oplog-dialog": CMSContentOpLogDialog,
    "cms-exmodel-editor": CMSEXModelEditor,
    "cms-tag-editor": CMSTagEditor
  },
  computed: {
    isLock () {
      return this.form.isLock === 'Y' && this.form.lockUser != '';
    },
    xmodelVisible() {
      return this.form.catalogConfigProps 
        && this.form.catalogConfigProps.ContentExtendModel != null 
        && this.form.catalogConfigProps.ContentExtendModel.length > 0;
    }
  },
  data() {
    return {
      // 遮罩层
      loading: false,
      openProgress: false,
      progressTitle: "",
      taskId: "",
      showOtherTitle: false,
      showTemplate: false,
      activeName: "basic",
      catalogId: this.$route.query.catalogId || '0',
      contentId: this.$route.query.id || '0',
      contentType: this.$route.query.type,
      openCatalogSelector: false,
      disableLinkCatalog: false,
      catalogSelectorFor: undefined,
      openContentSelector: false,
      contentTypeOptions: [],
      addContentType: "",
      articleBodyFormatOptions: [],
      addArticleBodyFormat: "RichText",
      articleFormat: '',
      addPopoverVisible: false,
      // 表单参数
      form: {
        attributes: [],
        contentJson: [],
        contentHtml: "",
        downloadRemoteImage: "Y",
        original: "N",
        publishPipe: [],
        imageList: [],
        tags:[],
        keywords:[]
      },
      isUpdateOperate: false,
      initDataStr: undefined, // 初始化数据jsonString
      publishPipeProps: [],
      rules: {
        title: [{ required: true, message: this.$t('CMS.Content.RuleTips.Title'), trigger: "blur" }]
      },
      summaryInputSize: { minRows: 3, maxRows: 6 },
      openTemplateSelector: false, // 模板选择弹窗
      publishPipeActiveName: "",
      selectExTemplate: false,
      publishAfterSave: false,
      toPublishAfterSave: false,
      openRelaContentDialog: false,
      openContentOpLogDialog: false,
      ueditorImportCss: "",
      shortTitleLabel: this.$t('CMS.Content.ShortTitle'),
      subTitleLabel: this.$t('CMS.Content.SubTitle')
    };
  },
  created() {
    this.loadUEditorCSS();
    this.loadArticleBodyFormats();
    getContentTypes().then(response => {
      this.contentTypeOptions = response.data;
      this.addContentType = this.contentTypeOptions[0].id;
    });
  },
  mounted() {
    this.initData();
  },
  methods: {
    handleTabClick(tab, event) {
    },
    toggleOtherTitle() {
      this.showOtherTitle = !this.showOtherTitle;
    },
    initData() {
      this.loading = true;
      getInitContentEditorData(this.catalogId, this.contentType, this.contentId).then(response => {
        this.loading = false;
        response.data.attributes = response.data.attributes || [];
        response.data.tags = response.data.tags || [];
        response.data.keywords = response.data.keywords || [];
        this.isUpdateOperate = this.$tools.isNotEmpty(response.data.createTime)
        if (response.data.contentType == "article") {
          this.articleFormat = this.isUpdateOperate ? response.data.format : this.$route.query.format;
        }
        if (!this.$tools.isEmpty(response.data.shortTitleLabel)) {
          this.shortTitleLabel = response.data.shortTitleLabel;
        }
        if (!this.$tools.isEmpty(response.data.subTitleLabel)) {
          this.subTitleLabel = response.data.subTitleLabel;
        }
        this.$nextTick(() => {
          this.form = response.data;
          this.catalogId = this.form.catalogId;
          this.contentId = this.form.contentId;
          this.contentType = this.form.contentType;
          this.publishPipeProps = this.form.publishPipeProps;
          this.showOtherTitle = 'Y' === this.form.showSubTitle || (this.form.shortTitle && this.form.shortTitle.length > 0) 
            || (this.form.subTitle && this.form.subTitle.length > 0);
          this.publishPipeProps.forEach(pp => {
            if (pp.props.template && pp.props.template.length > 0) {
              this.showTemplate = true;
            }
          });
          if (response.data.contentType == "article") {
            this.form.format = this.articleFormat;
          }
          this.initDataStr = JSON.stringify(this.form);
        })
      });
    },
    handleShowOtherTitle() {
      this.showOtherTitle = !this.showOtherTitle;
    },
    isFormChanged() {
      return JSON.stringify(this.form) != this.initDataStr;
    },
    handleClose() {
      const that = this;
      if (this.isFormChanged()) {
        this.$confirm(this.$t('CMS.Content.CloseContentEditorTip'), this.$t('Common.SystemTip'), {
          confirmButtonText: this.$t('Common.Confirm'),
          cancelButtonText: this.$t('Common.Cancel'),
          type: "warning",
        }).then(function () {
          that.closePage();
        }).catch(function () { });
      } else {
        this.closePage();
      }
    },
    closePage() {
      if (this.$route.path.endsWith('editorW')) {
        window.close();
      } else {
        const obj = { path: "/configs/content", name: "CmsContentcoreContent" };
        this.$tab.closeOpenPage(obj).then(() => {
          this.$tab.refreshPage(obj);
        });
      }
    },
    // 表单重置
    reset() {
      this.form = {};
      this.resetForm("form");
    },
    handleShowTemplateChange() {
      if (!this.showTemplate) {
        this.publishPipeProps.forEach((pp, i) => {
          pp.props.template = "";
        });
      }
    },
    handleSelectTemplate(publishPipe, extend = false) {
      this.publishPipeActiveName = publishPipe.pipeCode;
      this.selectExTemplate = extend
      this.openTemplateSelector = true;
    },
    handleTemplateSelected (template) {
      this.publishPipeProps.map(pp => {
        if (pp.pipeCode == this.publishPipeActiveName) {
          if (this.selectExTemplate) {
            pp.props.contentExTemplate = template;
          } else {
            pp.props.template = template;
          }
        }
      });
      this.openTemplateSelector = false;
    },
    handleTemplateSelectorCancel () {
      this.openTemplateSelector = false;
    },
    handleSetLogo(path, src) {
      this.form.images.push(path);
      this.form.imagesSrc.push(src);
      // this.$set(this.form, "logoSrc", src);
    },
    /** 提交 */
    handleSave: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.form.template = {};
          this.publishPipeProps.map(item => {
            this.form.template[item.pipeCode] = item.props.template;
          });
          if (this.xmodelVisible) {
            this.form.params = this.$refs.EXModelEditor.getDatas();
          }
          if (this.isUpdateOperate) {
            this.form.opType = "UPDATE"
            saveContent(this.form).then(response => {
              this.taskId = response.data.taskId;
              this.openProgress = true;
              this.progressTitle = this.$t('CMS.Content.SaveProgressTitle')
            });
          } else {
            this.form.catalogId = this.catalogId;
            this.form.contentType = this.contentType;
            addContent(this.form).then(response => {
              this.taskId = response.data.taskId;
              this.openProgress = true;
              this.progressTitle = this.$t('CMS.Content.SaveProgressTitle')
            });
          }
        }
      });
    },
    handleToPublish() {
      if (!this.isUpdateOperate || this.isFormChanged()) {
        this.handleSave();
        this.toPublishAfterSave = true;
        return;
      }
      this.doToPublishContent();
    },
    doToPublishContent() {
      toPublishContent([ this.form.contentId ]).then(response => {
          this.$modal.msgSuccess(this.$t('CMS.ContentCore.ToPublishSuccess'), "message-top-right");
      });
    },
    handlePublish () {
      if (!this.isUpdateOperate || this.isFormChanged()) {
        this.handleSave();
        this.publishAfterSave = true;
        return;
      }
      this.doPublishContent();
    },
    doPublishContent() {
      publishContent([ this.form.contentId ]).then(response => {
          this.$modal.msgSuccess(this.$t('CMS.ContentCore.PublishSuccess'), "message-top-right");
          this.$cache.local.set('publish_flag', "true")
      });
    },
    handleProgressClose (result) {
      if (result.status == 'SUCCESS') {
        if (this.publishAfterSave) {
          this.publishAfterSave = false;
          this.doPublishContent();
        }
        if (this.toPublishAfterSave) {
          this.toPublishAfterSave = false;
          this.doToPublishContent();
        }
        this.initData();
        this.$router.push({ path: this.$route.path, query: { type: this.contentType, catalogId: this.catalogId, id: this.contentId } });
      }
    },
    handlePreview () {
      let routeData = this.$router.resolve({
        path: "/cms/preview",
        query: { type: "content", dataId: this.form.contentId },
      });
      window.open(routeData.href, '_blank');
    },
    handleChangeLockState () {
      if (this.isLock) {
        unLockContent(this.form.contentId).then(response => {
          this.form.isLock = 'N';
          this.$modal.msgSuccess(this.$t('Common.OpSuccess'), "message-top-right");
        });
      } else {
        lockContent(this.form.contentId).then(response => {
          this.form.isLock = 'Y';
          this.form.lockUser = response.data;
          this.$modal.msgSuccess(this.$t('Common.OpSuccess'), "message-top-right");
        });
      }
    },
    handleCatalogChange() {
      this.openCatalogSelector = true;
      this.disableLinkCatalog = false;
      this.catalogSelectorFor = "change";
    },
    handleCatalogSelectorOk(catalogs) {
      if (this.catalogSelectorFor === 'change') {
        if (this.form.contentId && this.form.contentId != null) {
          // 编辑内容
          if (this.form.catalogId != catalogs[0].id) {
            const data = {
              contentIds: [ this.form.contentId ],
              catalogId: catalogs[0].id
            };
            moveContent(data).then(response => {
              if (response.code == 200) {
                this.$modal.msgSuccess(this.$t('Common.OpSuccess'), "message-top-right");
                this.form.catalogId = catalogs[0].id;
                this.form.catalogName = catalogs[0].name;
              }
            });
          }
        } else {
          // 新建内容
          this.form.catalogId = catalogs[0].id;
          this.form.catalogName = catalogs[0].name;
        }
      } else if(this.catalogSelectorFor === 'linkflag') {
        if (catalogs && catalogs.length > 0) {
          this.form.redirectUrl = catalogs[0].props.internalUrl;
        }
      }
      this.openCatalogSelector = false;
    },
    handleCatalogSelectorClose() {
      this.openCatalogSelector = false;
    },
    handleLinkTo(type) {
      if (type === 'content') {
        this.openContentSelector = true;
      } else if (type === 'catalog') {
        this.openCatalogSelector = true;
        this.catalogSelectorFor = 'linkflag';
        this.disableLinkCatalog = true;
      }
    },
    handleContentSelectorOk(contents) {
      if (contents && contents.length > 0) {
        this.form.redirectUrl = contents[0].internalUrl;
        if (!this.form.images || this.form.images.length == 0) {
          this.form.images = contents[0].images;
          this.form.imagesSrc = contents[0].imagesSrc;
        }
        if (!this.form.title || this.form.title.length == 0) {
          this.form.title = contents[0].title;
        }
        if (!this.form.shortTitle || this.form.shortTitle.length == 0) {
          this.form.shortTitle = contents[0].shortTitle;
        }
        if (!this.form.subTitle || this.form.subTitle.length == 0) {
          this.form.subTitle = contents[0].subTitle;
        }
        if (!this.form.author || this.form.author.length == 0) {
          this.form.author = contents[0].author;
        }
        if (!this.form.editor || this.form.editor.length == 0) {
          this.form.editor = contents[0].editor;
        }
        if (!this.form.tags || this.form.tags.length == 0) {
          this.form.tags = contents[0].tags;
        }
        if (!this.form.keywords || this.form.keywords.length == 0) {
          this.form.keywords = contents[0].keywords;
        }
        if (!this.form.summary || this.form.summary.length == 0) {
          this.form.summary = contents[0].summary;
        }
        this.showOtherTitle = 'Y' === this.form.showSubTitle || (this.form.shortTitle && this.form.shortTitle.length > 0) 
          || (this.form.subTitle && this.form.subTitle.length > 0);
        this.openContentSelector = false;
      } else {
        this.$modal.msgWarning(this.$t('Common.SelectFirst'));
      }
    },
    handleContentSelectorClose() {
      this.openContentSelector = false;
    },
    handleRelaContent() {
      this.openRelaContentDialog = true;
    },
    handleRelaContentClose() {
      this.openRelaContentDialog = false;
    },
    handleOpLogs() {
      this.openContentOpLogDialog = true;
    },
    handleOpLogsClose() {
      this.openContentOpLogDialog = false;
    },
    handlePushToBaidu() {
      pushToBaidu([ this.form.contentId ]).then(response => {
        let msg = "";
        response.data.forEach(item => {
          msg + this.$t('CMS.Content.CloseContentEditorTip', [ item.publishPipeCode, item.success, item.remain ])
          msg += "【" + item.publishPipeCode + "】成功 " + item.success + " 条，剩余 " + item.remain + " 条。<br/>"
        })
        this.$modal.alert(msg)
      })
    },
    loadUEditorCSS() {
      if (this.contentType == 'article') {
        getUEditorCSS({ catalogId: this.catalogId }).then(response => {
          this.ueditorCss = response.data;
        })
      }
    },
    findUEIframes(element) {
      const children = element.children[0].children;
      for (let i = 0; i < children.length; i++) {
        if (children[i].classList.contains("edui-editor-iframeholder")) {
          return children[i].children[0]
        }
      }
      return;
    },
    handleChangeUEditorCSS() {
      const iframe = this.findUEIframes(document.getElementById('ue_article'));
      const links = iframe.contentDocument.getElementsByTagName('link');
      for (let i = 0; i < links.length; i++) {
          if (links[i].id === 'import_css') {
              links[i].parentNode.removeChild(links[i]);
          }
      }
      if (this.ueditorImportCss && this.ueditorImportCss.length > 0) {
        const css = this.ueditorCss[this.ueditorImportCss]
        if (css && css.length > 0) {
          const link = document.createElement('link');
          link.setAttribute('rel', 'stylesheet');
          link.setAttribute('type', 'text/css');
          link.setAttribute("id", "import_css");
          link.setAttribute('href', css);
          iframe.contentDocument.head.appendChild(link);
        }
      }
    },
    loadArticleBodyFormats() {
      getArticleBodyFormats().then(response => {
        this.articleBodyFormatOptions = response.data;
      })
    },
    handleAdd () {
      if (this.isFormChanged()) {
        let that = this;
        this.$confirm(this.$t('CMS.Content.CloseContentEditorTip'), this.$t('Common.SystemTip'), {
          confirmButtonText: this.$t('Common.Confirm'),
          cancelButtonText: this.$t('Common.Cancel'),
          type: "warning",
        }).then(function () {
          that.handleNewContent();
        }).catch(function (e) { console.log(e) });
      } else {
        this.handleNewContent();
      }
    },
    handleNewContent() {
      this.addPopoverVisible = false;
      this.contentId = '0';
      this.contentType = this.addContentType;
      let query = { type: this.contentType, catalogId: this.catalogId, id: this.contentId }
      if (this.contentType == 'article') {
        query.format = this.addArticleBodyFormat;
      }
      this.$router.push({ path: this.$route.path, query: query });
      this.initData();
    }
  }
};
</script>
<style scoped>
.content-editor-container .el-form {
    width: 100%;
}
.content-editor-container .el-form-item {
  margin-bottom: 12px;  
}
.content-editor-container .card-title {
  margin-bottom: 5px;
}
.content-editor-container .card-title .el-card__body {
  padding-bottom: 10px;
}
.content-editor-container .card-editor {
  margin-top: 10px;
  min-height: 700px;
}
.content-editor-container .art-editor-container {
  margin-top: 10px;
  /* max-width: 1320px; */
}
.content-editor-container #toolbar-container {
  z-index: 101;
}
.content-editor-container #editor-container {
  z-index: 100;
}
</style>