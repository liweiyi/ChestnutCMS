<template>
  <div class="app-container content-editor-container">
    <el-row>
      <el-col :span="24">
        <div class="grid-btn-bar bg-purple-white">
          <el-row :gutter="10">
            <el-col :span="1.5" class="permi-wrap">
              <el-button plain type="success" icon="Edit" v-if="contentId=='0'" v-hasPermi="[ $p('Catalog:AddContent:{0}', [ catalogId ]) ]" @click="handleSave">{{ $t("Common.Save") }}</el-button>
              <el-button plain type="success" icon="Edit" v-else v-hasPermi="[ $p('Catalog:EditContent:{0}', [ catalogId ]) ]" @click="handleSave">{{ $t("Common.Save") }}</el-button>
            </el-col>
            <el-col :span="1.5" class="permi-wrap">
              <el-button plain type="primary" icon="Timer" v-hasPermi="[ $p('Catalog:EditContent:{0}', [ catalogId ]) ]" @click="handleToPublish">{{ $t('CMS.ContentCore.ToPublish') }}</el-button>
            </el-col>
            <el-col :span="1.5" class="permi-wrap">
              <el-button plain type="primary" icon="Promotion" v-hasPermi="[ $p('Catalog:EditContent:{0}', [ catalogId ]) ]" @click="handlePublish">{{ $t('CMS.ContentCore.Publish') }}</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button plain type="primary" icon="View" @click="handlePreview">{{ $t('CMS.ContentCore.Preview') }}</el-button>
            </el-col>
            <el-col :span="1.5" class="permi-wrap">
              <el-button plain type="warning" v-if="isLock" icon="Unlock" v-hasPermi="[ $p('Catalog:EditContent:{0}', [ catalogId ]) ]" @click="handleChangeLockState">{{ $t('CMS.Content.Unlock') }}</el-button>
              <el-button plain type="primary" v-else icon="Lock" v-hasPermi="[ $p('Catalog:EditContent:{0}', [ catalogId ]) ]" @click="handleChangeLockState">{{ $t('CMS.Content.Lock') }}</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button plain type="primary" icon="Share" @click="handleRelaContent">{{ $t('CMS.Content.RelaContent') }}</el-button>
            </el-col>
            <el-col :span="1.5" class="permi-wrap">
              <el-button plain type="primary" icon="Files" v-hasPermi="[ $p('Catalog:EditContent:{0}', [ catalogId ]) ]" @click="handlePushToBaidu">{{ $t('CMS.Content.PushToBaidu') }}</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button plain type="primary" icon="TakeawayBox" @click="handleOpLogs">{{ $t('CMS.Content.OpLog') }}</el-button>
            </el-col>
            <el-col :span="1.5" class="permi-wrap">
              <div v-hasPermi="[ $p('Catalog:AddContent:{0}', [ catalogId ]) ]">
                <el-popover v-model="addPopoverVisible" placement="bottom-start" :width="480" trigger="click">
                  <div class="btn-add-content">
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
                        @click="handleAdd"
                      >{{ $t('Common.Confirm') }}</el-button>
                    </el-row>
                  </div>
                  <template #reference>
                    <el-button
                      type="primary"
                      icon="Plus"
                      plain>{{ $t("Common.Add") }}<i class="el-icon-arrow-down el-icon--right"></i>
                    </el-button>
                  </template>
                </el-popover>
              </div>
            </el-col>
            <el-col :span="1.5">
              <el-button plain type="warning" icon="Close" @click="handleClose">{{ $t('Common.Close') }}</el-button>
            </el-col>
          </el-row>
        </div>
      </el-col>
    </el-row>
    <el-form ref="formRef" v-loading="loading" :model="form" :rules="rules" label-width="110px">
      <el-row class="art-editor-container" :gutter="10">
        <el-col :span="16">
          <el-card shadow="always" class="card-title">
            <div class="art-title bg-purple-white">
              <el-form-item :label="$t('CMS.Content.Title')" prop="title" style="margin-bottom: 18px;">
                <el-input
                  v-model="form.title"
                  maxlength="360"
                  show-word-limit>
                  <template #append>
                    <el-button
                      icon="ArrowDown"
                      @click="toggleOtherTitle"
                    ></el-button>
                  </template>
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
                <el-input v-model="form.redirectUrl" placeholder="http(s)://">
                  <template #append>
                    <el-dropdown @command="handleLinkTo">
                      <el-button>
                        {{ $t('CMS.ContentCore.InternalUrl') }}<el-icon class="el-icon--right"><arrow-down /></el-icon>
                      </el-button>
                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item command="content">{{ $t('CMS.ContentCore.SelectContent') }}</el-dropdown-item>
                          <el-dropdown-item command="catalog">{{ $t('CMS.ContentCore.SelectCatalog') }}</el-dropdown-item>
                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
                  </template>
                </el-input>
              </el-form-item>
            </div>
          </el-card>
          <el-card v-if="xmodelVisible" shadow="always" class="card-exmodel mb10 pr10">
            <cms-exmodel-editor
              ref="exModelEditorRef"
              :xmodel="form.catalogConfigProps.ContentExtendModel"
              type="content"
              :id="form.contentId">
            </cms-exmodel-editor>
          </el-card>
          <div v-if="form.linkFlag !== 'Y' && contentType === 'article'">
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
          </div>
          <div v-if="form.linkFlag !== 'Y' && contentType === 'image'">
            <cms-image-editor v-model="form.imageList" @choose="handleSetLogo"></cms-image-editor>
          </div>
          <div v-if="form.linkFlag !== 'Y' && contentType === 'audio'">
            <cms-audio-editor v-model="form.audioList" @choose="handleSetLogo" :logo="form.logoSrc"></cms-audio-editor>
          </div>
          <div v-if="form.linkFlag !== 'Y' && contentType === 'video'">
            <cms-video-editor v-model="form.videoList" @choose="handleSetLogo"></cms-video-editor>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="bg-purple-white">
            <el-card shadow="always">
              <el-tabs v-model="activeName" @tab-click="handleTabClick">
                <el-tab-pane :label="$t('CMS.Content.Basic')" name="basic">
                  <el-form-item :label="$t('CMS.Content.Catalog')" prop="catalogId">
                    <el-button-group>
                      <el-button plain type="primary" style="width:152px;" disabled>{{ form.catalogName }}</el-button>
                      <el-button type="primary" icon="Search" @click="handleCatalogChange"></el-button>
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
                      <el-checkbox v-for="dict in CMSContentAttribute" :label="dict.value" :key="dict.value">{{ dict.label }}</el-checkbox>
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
                    <el-date-picker v-model="form.publishDate" value-format="YYYY-MM-DD HH:mm:ss" type="datetime" style="width:195px;" />
                  </el-form-item>
                  <el-form-item :label="$t('CMS.Content.OfflineDate')" prop="offlineDate">
                    <el-date-picker v-model="form.offlineDate" value-format="YYYY-MM-DD HH:mm:ss" type="datetime" style="width:195px;" />
                  </el-form-item>
                  <el-form-item :label="$t('CMS.Content.StaticPath')">
                    <el-input v-model="form.staticPath" />
                  </el-form-item>
                  <el-form-item :label="$t('CMS.Content.Template')">
                    <el-switch v-model="showTemplate" @change="handleShowTemplateChange" />
                  </el-form-item>
                  <el-form-item 
                    v-show="showTemplate"
                    v-for="pp in publishPipeProps"
                    :label="pp.pipeName"
                    :key="pp.pipeCode"
                    :prop="'template_' + pp.value">
                    <el-input v-model="pp.props.template">
                      <template #append>
                        <el-button icon="FolderOpened" @click="handleSelectTemplate(pp)"></el-button>
                      </template>
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
                  <el-form-item 
                    v-for="pp in publishPipeProps"
                    :label="pp.pipeName"
                    :key="pp.pipeCode + '_ex'"
                    :prop="'contentExTemplate_' + pp.value">
                    <el-input v-model="pp.props.contentExTemplate">
                      <template #append>
                        <el-button icon="FolderOpened" @click="handleSelectTemplate(pp, true)"></el-button>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-tab-pane>
              </el-tabs>
            </el-card>
          </div>
        </el-col>
      </el-row>
    </el-form>
    <!-- 模板选择组件 -->
    <cms-template-selector 
      v-model:open="openTemplateSelector"
      :publishPipeCode="publishPipeActiveName"
      @ok="handleTemplateSelected"
      @cancel="handleTemplateSelectorCancel" />
    <!-- 栏目选择组件 -->
    <cms-catalog-selector
      v-model:open="openCatalogSelector"
      :disableLink="disableLinkCatalog"
      @ok="handleCatalogSelectorOk"
      @close="handleCatalogSelectorClose"></cms-catalog-selector>
    <!-- 内容选择组件 -->
    <cms-content-selector
      v-model:open="openContentSelector"
      @ok="handleContentSelectorOk"
      @close="handleContentSelectorClose"></cms-content-selector>
    <cms-content-rela-dialog :cid="contentId" v-model:open="openRelaContentDialog" @close="handleRelaContentClose"></cms-content-rela-dialog>
    <cms-content-oplog-dialog :cid="contentId" v-model:open="openContentOpLogDialog" @close="handleOpLogsClose"></cms-content-oplog-dialog>
    <!-- 进度条 -->
    <cms-progress :title="progressTitle" v-model:open="openProgress" :taskId="taskId" message-class="message-top-right" @close="handleProgressClose"></cms-progress>
  </div>
</template>
<script setup name="CMSContentEditor">
import { getContentTypes } from "@/api/contentcore/catalog";
import { getInitContentEditorData, addContent, saveContent, toPublishContent, publishContent, lockContent, unLockContent, moveContent } from "@/api/contentcore/content";
import { getUEditorCSS, getArticleBodyFormats } from "@/api/contentcore/article"
import { pushToBaidu } from "@/api/seo/baidupush";
import Ueditor from '@/views/cms/components/UEditorPlus'
import CmsProgress from '@/views/components/Progress';
import CmsImageEditor from '@/views/cms/imageAlbum/editor';
import CmsAudioEditor from '@/views/cms/media/audioEditor';
import CmsVideoEditor from '@/views/cms/media/videoEditor';
import CmsImageGroup from '@/views/cms/components/ImageGroup';
import CmsCatalogSelector from "@/views/cms/contentcore/catalogSelector";
import CmsContentSelector from "@/views/cms/contentcore/contentSelector";
import CmsContentRelaDialog from '@/views/cms/contentcore/contentRelaDialog';
import CmsContentOplogDialog from '@/views/cms/contentcore/contentOpLogDialog';
import CmsTemplateSelector from '@/views/cms/contentcore/templateSelector';
import CmsExmodelEditor from '@/views/cms/components/EXModelEditor';
import CmsTagEditor from '@/views/cms/components/TagEditor';

const { proxy } = getCurrentInstance();

const { CMSContentAttribute } = proxy.useDict('CMSContentAttribute');

const isLock = computed(() => {
  return form.value.isLock === 'Y' && form.value.lockUser != '';
});
const xmodelVisible = computed(() => {
  return form.value.catalogConfigProps
    && form.value.catalogConfigProps.ContentExtendModel != null
    && form.value.catalogConfigProps.ContentExtendModel.length > 0;
});

const loading = ref(false);
const openProgress = ref(false);
const progressTitle = ref("");
const taskId = ref("");
const showOtherTitle = ref(false);
const showTemplate = ref(false);
const activeName = ref("basic");
const catalogId = ref(proxy.$route.query.catalogId || '0');
const contentId = ref(proxy.$route.query.id || '0');
const contentType = ref(proxy.$route.query.type);
const openCatalogSelector = ref(false);
const disableLinkCatalog = ref(false);
const catalogSelectorFor = ref(undefined);
const openContentSelector = ref(false);
const contentTypeOptions = ref([]);
const addContentType = ref("");
const ueditorCss = ref({});
const articleBodyFormatOptions = ref([]);
const addArticleBodyFormat = ref("RichText");
const articleFormat = ref('');
const addPopoverVisible = ref(false);
const objects = reactive({
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
  }
});
const { form } = toRefs(objects);
const isUpdateOperate = ref(false);
const initDataStr = ref(undefined); // 初始化数据jsonString
const publishPipeProps = ref([]);
const rules = ref({
  title: [{ required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }]
});
const summaryInputSize = ref({ minRows: 3, maxRows: 6 });
const openTemplateSelector = ref(false); // 模板选择弹窗
const publishPipeActiveName = ref("");
const selectExTemplate = ref(false);
const publishAfterSave = ref(false);
const toPublishAfterSave = ref(false);
const publishing = ref(false);
const openRelaContentDialog = ref(false);
const openContentOpLogDialog = ref(false);
const openVersionDialog = ref(false);
const ueditorImportCss = ref("");
const shortTitleLabel = ref(proxy.$t('CMS.Content.ShortTitle'));
const subTitleLabel = ref(proxy.$t('CMS.Content.SubTitle'));

onMounted(() => {
  loadUEditorCSS();
  loadArticleBodyFormats();
  getContentTypes().then(response => {
    contentTypeOptions.value = response.data;
    addContentType.value = contentTypeOptions.value[0].id;
  });
  initData();
});

function handleTabClick(tab, event) {
}

function toggleOtherTitle() {
  showOtherTitle.value = !showOtherTitle.value;
}

function initData() {
  loading.value = true;
  getInitContentEditorData(catalogId.value, contentType.value, contentId.value).then(response => {
    loading.value = false;
    response.data.attributes = response.data.attributes || [];
    response.data.tags = response.data.tags || [];
    response.data.keywords = response.data.keywords || [];
    isUpdateOperate.value = proxy.$tools.isNotEmpty(response.data.createTime)
    if (response.data.contentType == "article") {
      articleFormat.value = isUpdateOperate.value ? response.data.format : proxy.$route.query.format;
    }
    if (!proxy.$tools.isEmpty(response.data.shortTitleLabel)) {
      shortTitleLabel.value = response.data.shortTitleLabel;
    }
    if (!proxy.$tools.isEmpty(response.data.subTitleLabel)) {
      subTitleLabel.value = response.data.subTitleLabel;
    }
    nextTick(() => {
      form.value = response.data;
      catalogId.value = form.value.catalogId;
      contentId.value = form.value.contentId;
      contentType.value = form.value.contentType;
      publishPipeProps.value = form.value.publishPipeProps;
      showOtherTitle.value = 'Y' === form.value.showSubTitle || (form.value.shortTitle && form.value.shortTitle.length > 0)
        || (form.value.subTitle && form.value.subTitle.length > 0);
      publishPipeProps.value.forEach(pp => {
        if (pp.props.template && pp.props.template.length > 0) {
          showTemplate.value = true;
        }
      });
      if (response.data.contentType == "article") {
        form.value.format = articleFormat.value;
      }
      initDataStr.value = JSON.stringify(form.value);
    })
  });
}

function handleShowOtherTitle() {
  showOtherTitle.value = !showOtherTitle.value;
}

function isFormChanged() {
  return JSON.stringify(form.value) != initDataStr.value;
}

function handleClose() {
  if (isFormChanged()) {
    proxy.$confirm(proxy.$t('CMS.Content.CloseContentEditorTip'), proxy.$t('Common.SystemTip'), {
      confirmButtonText: proxy.$t('Common.Confirm'),
      cancelButtonText: proxy.$t('Common.Cancel'),
      type: "warning",
    }).then(() => {
      closePage();
    }).catch(() => { });
  } else {
    closePage();
  }
}

function closePage() {
  if (proxy.$route.path.endsWith('editorW')) {
    window.close();
  } else {
    const obj = { path: "/configs/content", name: "CmsContentcoreContent" };
    proxy.$tab.closeOpenPage(obj).then(() => {
      proxy.$tab.refreshPage(obj);
    });
  }
}

function reset() {
  form.value = {};
  proxy.resetForm("form");
}

function handleShowTemplateChange() {
  if (!showTemplate.value) {
    publishPipeProps.value.forEach((pp, i) => {
      pp.props.template = "";
    });
  }
}
    
function handleSelectTemplate(publishPipe, extend = false) {
  publishPipeActiveName.value = publishPipe.pipeCode;
  selectExTemplate.value = extend
  openTemplateSelector.value = true;
}

function handleTemplateSelected (template) {
  publishPipeProps.value.map(pp => {
    if (pp.pipeCode == publishPipeActiveName.value) {
      if (selectExTemplate.value) {
        pp.props.contentExTemplate = template;
      } else {
        pp.props.template = template;
      }
    }
  });
  openTemplateSelector.value = false;
}
    
function handleTemplateSelectorCancel () {
  openTemplateSelector.value = false;
}

function handleSetLogo(path, src) {
  form.value.images.push(path);
  form.value.imagesSrc.push(src);
}

function handleSave() {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      form.value.template = {};
      publishPipeProps.value.map(item => {
        form.value.template[item.pipeCode] = item.props.template;
      });
      if (xmodelVisible.value) {
        form.value.params = proxy.$refs.exModelEditorRef.getDatas();
      }
      if (isUpdateOperate.value) {
        form.value.opType = "UPDATE"
        saveContent(form.value).then(response => {
          taskId.value = response.data.taskId;
          openProgress.value = true;
          progressTitle.value = proxy.$t('CMS.Content.SaveProgressTitle')
        });
      } else {
        form.value.catalogId = catalogId.value;
        form.value.contentType = contentType.value;
        addContent(form.value).then(response => {
          taskId.value = response.data.taskId;
          openProgress.value = true;
          progressTitle.value = proxy.$t('CMS.Content.SaveProgressTitle')
        });
      }
    }
  });
}

function handleToPublish() {
  if (!isUpdateOperate.value || isFormChanged()) {
    handleSave();
    toPublishAfterSave.value = true;
    return;
  }
  doToPublishContent();
}

function doToPublishContent() {
  toPublishContent([ form.value.contentId ]).then(response => {
    proxy.$modal.msgSuccess(proxy.$t('CMS.ContentCore.ToPublishSuccess'), "message-top-right");
  });
}

function handlePublish() {
  if (!isUpdateOperate.value || isFormChanged()) {
    handleSave();
    publishAfterSave.value = true;
    return;
  }
  doPublishContent();
}
    
function doPublishContent() {
  publishContent([ form.value.contentId ]).then(response => {
    proxy.$cms.setPublishFlag("true")
    taskId.value = response.data;
    publishing.value = true;
    progressTitle.value = proxy.$t('CMS.Content.PublishProgressTitle')
    openProgress.value = true;
  });
}

function handleProgressClose (result) {
  if (result.status == 'SUCCESS') {
    if (publishing.value) {
      publishing.value = false;
      return;
    }
    if (publishAfterSave.value) {
      publishAfterSave.value = false;
      doPublishContent();
    }
    if (toPublishAfterSave.value) {
      toPublishAfterSave.value = false;
      doToPublishContent();
    }
    initData();
    proxy.$router.push({ path: proxy.$route.path, query: { type: contentType.value, catalogId: catalogId.value, id: contentId.value } });
  }
}
    
function handlePreview () {
  let routeData = proxy.$router.resolve({
    path: "/cms/preview",
    query: { type: "content", dataId: form.value.contentId },
  });
  window.open(routeData.href, '_blank');
}
    
function handleChangeLockState () {
  if (isLock.value) {
    unLockContent(form.value.contentId).then(response => {
      form.value.isLock = 'N';
      proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'), "message-top-right");
    });
  } else {
    lockContent(form.value.contentId).then(response => {
      form.value.isLock = 'Y';
      form.value.lockUser = response.data;
      proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'), "message-top-right");
    });
  }
}
    
function handleCatalogChange() {
  openCatalogSelector.value = true;
  disableLinkCatalog.value = false;
  catalogSelectorFor.value = "change";
}
    
function handleCatalogSelectorOk(args) {
  var catalogs = args[0];
  if (catalogSelectorFor.value === 'change') {
    if (form.value.contentId && form.value.contentId != null) {
      // 编辑内容
      if (form.value.catalogId != catalogs[0].id) {
        const data = {
          contentIds: [ form.value.contentId ],
          catalogId: catalogs[0].id
        };
        moveContent(data).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'), "message-top-right");
          form.value.catalogId = catalogs[0].id;
          form.value.catalogName = catalogs[0].name;
          proxy.$router.push({ path: proxy.$route.path, query: { type: contentType.value, catalogId: form.value.catalogId, id: contentId.value } });
        });
      }
    } else {
      // 新建内容
      form.value.catalogId = catalogs[0].id;
      form.value.catalogName = catalogs[0].name;
    }
  } else if(catalogSelectorFor.value === 'linkflag') {
    if (catalogs && catalogs.length > 0) {
      form.value.redirectUrl = catalogs[0].props.internalUrl;
    }
  }
  openCatalogSelector.value = false;
}
    
function handleCatalogSelectorClose() {
  openCatalogSelector.value = false;
}
    
function handleLinkTo(type) {
  if (type === 'content') {
    openContentSelector.value = true;
  } else if (type === 'catalog') {
    openCatalogSelector.value = true;
    catalogSelectorFor.value = 'linkflag';
    disableLinkCatalog.value = true;
  }
}
    
function handleContentSelectorOk(contents) {
  if (contents && contents.length > 0) {
    form.value.redirectUrl = contents[0].internalUrl;
    if (!form.value.images || form.value.images.length == 0) {
      form.value.images = contents[0].images;
      form.value.imagesSrc = contents[0].imagesSrc;
    }
    if (proxy.$tools.isEmpty(form.value.title)) {
      form.value.title = contents[0].title;
    }
    if (proxy.$tools.isEmpty(form.value.shortTitle)) {
      form.value.shortTitle = contents[0].shortTitle;
    }
    if (proxy.$tools.isEmpty(form.value.subTitle)) {
      form.value.subTitle = contents[0].subTitle;
    }
    if (proxy.$tools.isEmpty(form.value.author)) {
      form.value.author = contents[0].author;
    }
    if (proxy.$tools.isEmpty(form.value.editor)) {
      form.value.editor = contents[0].editor;
    }
    if (!form.value.tags || form.value.tags.length == 0) {
      form.value.tags = contents[0].tags;
    }
    if (!form.value.keywords || form.value.keywords.length == 0) {
      form.value.keywords = contents[0].keywords;
    }
    if (proxy.$tools.isEmpty(form.value.summary)) {
      form.value.summary = contents[0].summary;
    }
    showOtherTitle.value = proxy.$tools.isNotEmpty(form.value.shortTitle) || proxy.$tools.isNotEmpty(form.value.subTitle);
    openContentSelector.value = false;
  } else {
    proxy.$modal.msgWarning(proxy.$t('Common.SelectFirst'));
  }
}
    
function handleContentSelectorClose() {
  openContentSelector.value = false;
}
    
function handleRelaContent() {
  openRelaContentDialog.value = true;
}
    
function handleRelaContentClose() {
  openRelaContentDialog.value = false;
}
    
function handleOpLogs() {
  openContentOpLogDialog.value = true;
}
    
function handleOpLogsClose() {
  openContentOpLogDialog.value = false;
}
function handlePushToBaidu() {
  pushToBaidu([ form.value.contentId ]).then(response => {
    let msg = "";
    response.data.forEach(item => {
      msg + proxy.$t('CMS.Content.CloseContentEditorTip', [ item.publishPipeCode, item.success, item.remain ])
      msg += "【" + item.publishPipeCode + "】成功 " + item.success + " 条，剩余 " + item.remain + " 条。<br/>"
    })
    proxy.$modal.alert(msg)
  })
}

function loadUEditorCSS() {
  if (contentType.value == 'article') {
    getUEditorCSS({ catalogId: catalogId.value }).then(response => {
      ueditorCss.value = response.data;
    })
  }
}
function findUEIframes(element) {
  const children = element.children[0].children;
  for (let i = 0; i < children.length; i++) {
    if (children[i].classList.contains("edui-editor-iframeholder")) {
      return children[i].children[0]
    }
  }
  return;
}
function handleChangeUEditorCSS() {
  const iframe = findUEIframes(document.getElementById('ue_article'));
  const links = iframe.contentDocument.getElementsByTagName('link');
  for (let i = 0; i < links.length; i++) {
      if (links[i].id === 'import_css') {
          links[i].parentNode.removeChild(links[i]);
      }
  }
  if (ueditorImportCss.value && ueditorImportCss.value.length > 0) {
    const css = ueditorCss.value[ueditorImportCss.value]
    if (css && css.length > 0) {
      const link = document.createElement('link');
      link.setAttribute('rel', 'stylesheet');
      link.setAttribute('type', 'text/css');
      link.setAttribute("id", "import_css");
      link.setAttribute('href', css);
      iframe.contentDocument.head.appendChild(link);
    }
  }
}

function loadArticleBodyFormats() {
  getArticleBodyFormats().then(response => {
    articleBodyFormatOptions.value = response.data;
  })
}

function handleAdd () {
  if (isFormChanged()) {
    proxy.$confirm(proxy.$t('CMS.Content.CloseContentEditorTip'), proxy.$t('Common.SystemTip'), {
      confirmButtonText: proxy.$t('Common.Confirm'),
      cancelButtonText: proxy.$t('Common.Cancel'),
      type: "warning",
    }).then(() => {
      handleNewContent();
    }).catch(() => { });
  } else {
    handleNewContent();
  }
}

function handleNewContent() {
  addPopoverVisible.value = false;
  contentId.value = '0';
  contentType.value = addContentType.value;
  let query = { type: contentType.value, catalogId: catalogId.value, id: contentId.value }
  if (contentType.value == 'article') {
    query.format = addArticleBodyFormat.value;
  }
  proxy.$router.push({ path: proxy.$route.path, query: query });
  initData();
}
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