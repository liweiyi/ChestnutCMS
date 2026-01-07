<template>
  <div class="siteinfo-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5" class="permi-wrap">
        <el-button  
          plain
          type="success"
          icon="Edit"
          :disabled="!props.site"
          v-hasPermi="[ $p('Site:Edit:{0}', [ props.site ]) ]"
          @click="handleUpdate">{{ $t("Common.Save") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="primary"
          :disabled="!props.site"
          @click="handlePreview">
          <svg-icon icon-class="eye-open" class="mr5"></svg-icon>{{ $t("CMS.ContentCore.Preview") }}
        </el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button 
          plain
          type="primary"
          icon="Promotion"
          :disabled="!props.site"
          v-hasPermi="[ $p('Site:Publish:{0}', [ props.site ]) ]"
          @click="handlePublish">{{ $t("CMS.Site.PublishHome") }}</el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-dropdown v-hasPermi="[ $p('Site:Publish:{0}', [ props.site ]) ]">
          <el-button 
            split-button
            plain
            type="primary"
            icon="Promotion"
            :disabled="!props.site">
            {{ $t("CMS.Site.PublishAll") }}<ArrowDown />
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item icon="Promotion" @click.native="handlePublishAll(30)">{{ $t("CMS.Site.PublishPublished") }}</el-dropdown-item>
              <el-dropdown-item icon="Promotion" @click.native="handlePublishAll(20)">{{ $t("CMS.Site.PublishToPublish") }}</el-dropdown-item>
              <el-dropdown-item icon="Promotion" @click.native="handlePublishAll(0)">{{ $t("CMS.Site.PublishDraft") }}</el-dropdown-item>
              <el-dropdown-item icon="Promotion" @click.native="handlePublishAll(60)">{{ $t("CMS.Site.PublishReEdit") }}</el-dropdown-item>
              <el-dropdown-item icon="Promotion" @click.native="handlePublishAll(40)">{{ $t("CMS.Site.PublishOffline") }}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button 
          plain
          type="primary"
          icon="MapLocation"
          :disabled="!props.site"
          v-hasPermi="[ $p('Site:Edit:{0}', [ props.site ]) ]"
          @click="handleGenSitemap">{{ $t("CMS.Site.GenSitemap") }}</el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button 
          plain
          type="primary"
          icon="Upload"
          :disabled="!props.site"
          v-hasPermi="[ $p('Site:Edit:{0}', [ props.site ]) ]"
          @click="handleImportTheme">{{ $t("CMS.Site.ImportTheme") }}</el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button 
          plain
          type="primary"
          icon="Promotion"
          :disabled="!props.site"
          v-hasPermi="[ $p('Site:Edit:{0}', [ props.site ]) ]"
          @click="handleExportTheme">{{ $t("CMS.Site.ExportTheme") }}</el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-dropdown v-hasPermi="[ $p('Site:Publish:{0}', [ props.site ]) ]">
          <el-button 
            split-button
            plain
            type="primary"
            icon="Refresh"
            :disabled="!props.site">
            {{ $t("CMS.ContentCore.RefreshCdn") }}<ArrowDown />
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item icon="Refresh" @click.native="handleRefreshCdn(false)">{{ $t("CMS.Site.RefreshCdn") }}</el-dropdown-item>
              <el-dropdown-item icon="Refresh" @click.native="handleRefreshCdn(true)">{{ $t("CMS.Site.RefreshCdnAll") }}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-col>
    </el-row>
    <el-form 
      ref="formInfoRef"
      :model="formInfo"
      v-loading="loading"
      :rules="rules"
      :disabled="!props.site"
      label-width="135px">
      <el-card shadow="hover">
        <template #header>
          <span>{{ $t("CMS.Site.BasicCardTitle") }}</span>
        </template>
        <el-form-item :label="$t('CMS.Site.SiteId')" prop="siteId">
          <span class="span_siteId" v-if="formInfo.siteId!=undefined">{{ formInfo.siteId }}</span>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Name')" prop="name">
          <el-input v-model="formInfo.name" />
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Path')" prop="path">
          <el-input v-model="formInfo.path" disabled />
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.ResourceUrl')" prop="resourceUrl">
          <el-input v-model="formInfo.resourceUrl" placeholder="http(s)://" />
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Logo')" prop="logo">
          <cms-logo-view v-model="formInfo.logo" :siteId="formInfo.siteId" :width="218" :height="150"></cms-logo-view>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Desc')" prop="description">
          <el-input v-model="formInfo.description" type="textarea" :rows="5" :maxlength="300" />
        </el-form-item>
      </el-card>
      <el-card shadow="hover">
        <template #header>
          <div class="display-flex justify-content-between">
            <div class="card-header-text">{{ $t("CMS.Site.PublishPipeCardTitle") }}</div>
            <el-button type="primary" icon="Plus" @click="handleAddPublishPipe">{{ $t("Common.Add") }}</el-button>
          </div>
        </template>
        <el-tabs 
          v-if="formInfo.publishPipeDatas&&formInfo.publishPipeDatas.length>0"
          v-model="publishPipeActiveName">
          <el-tab-pane 
            v-for="pp in formInfo.publishPipeDatas"
            :key="pp.pipeCode"
            :name="pp.pipeCode"
            :label="pp.pipeName">
            <el-form-item :label="$t('CMS.Site.Domain')">
              <el-input v-model="pp.props.url" placeholder="http(s)://www.test.com/" />
            </el-form-item>
            <el-form-item :label="$t('CMS.Site.StaticFileType')">
              <el-select v-model="pp.props.staticSuffix" style="width:325px">
                <el-option
                  v-for="dict in CMSStaticSuffix"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('CMS.Site.IndexTemplate')">
              <el-input v-model="pp.props.indexTemplate">
                <template #append>
                  <el-button 
                    type="primary"
                    @click="handleSelectTemplate()"
                  >{{ $t("Common.Select") }}</el-button>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item :label="$t('CMS.Site.EnableSitemap')">
              <el-switch
                v-model="pp.props.enableSitemap"
                :active-text="$t('Common.Yes')"
                :inactive-text="$t('Common.No')"
                active-value="Y"
                inactive-value="N">
              </el-switch>
            </el-form-item>
            <el-form-item v-if="pp.props.enableSitemap=='Y'" :label="$t('CMS.Site.SitemapPageType')">
              <el-select v-model="pp.props.sitemapPageType" style="width:325px">
                <el-option
                  v-for="dict in CMSSitemapPageType"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item v-if="pp.props.enableSitemap=='Y'" :label="$t('CMS.Site.SitemapUrlLimit')">
              <el-input-number v-model="pp.props.sitemapUrlLimit" :min="1" :max="50000"></el-input-number>
            </el-form-item>
            <el-form-item :label="$t('CMS.Site.UEditorCss')">
              <el-input v-model="pp.props.ueditorCss">
                <template #append>
                  <el-button 
                    type="primary"
                    @click="handleSelectFile()"
                  >{{ $t("Common.Select") }}</el-button>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item :label="$t('CMS.Site.PrefixMode')">
              <el-radio-group v-model="pp.props.PrefixMode">
                <el-radio-button label="absolute">{{ $t('CMS.Site.PrefixMode_Absolute') }}</el-radio-button>
                <el-radio-button label="relative">{{ $t('CMS.Site.PrefixMode_Relative') }}</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-show="pp.props.PrefixMode=='relative'" :label="$t('CMS.Site.RelativePrefix')">
              <el-input v-model="pp.props.RelativePrefix" />
            </el-form-item>
            <el-form-item :label="$t('CMS.Site.ErrPageLink')">
              <el-input v-model="pp.props.errPageLink" placeholder="/err.html" />
            </el-form-item>
          </el-tab-pane>
        </el-tabs>
        <div v-else style="background-color: #f4f4f5;color: #909399;font-size:12px;line-height: 30px;padding-left:10px;display: flex;align-items: center;">
          <el-icon class="mr5" style="width: 14px;"><InfoFilled /></el-icon>{{ $t("CMS.Site.NoPublishPipeTip") }}
          <el-button type="text" @click="handleAddPublishPipe">{{ $t("CMS.Site.GoAddPublishPipe") }}</el-button>
        </div>
      </el-card>
      <el-card shadow="hover">
        <template #header>
          <span>{{ $t("CMS.ContentCore.SEOConfig") }}</span>
        </template>
        <el-form-item :label="$t('CMS.ContentCore.SEOTitle')" prop="seoTitle">
          <el-input v-model="formInfo.seoTitle" />
        </el-form-item>
        <el-form-item :label="$t('CMS.ContentCore.SEOKeyword')" prop="seoKeywords">
          <el-input v-model="formInfo.seoKeywords" />
        </el-form-item>
        <el-form-item :label="$t('CMS.ContentCore.SEODescription')" prop="seoDescription">
          <el-input  v-model="formInfo.seoDescription" type="textarea" :maxlength="100"/>
        </el-form-item>
      </el-card>
      <el-card v-if="showEXModel" shadow="hover">
        <template #header>
          <span>{{ $t("CMS.Site.ExModelCardTitle") }}</span>
        </template>
        <cms-exmodel-editor 
          ref="exModelEditorRef"
          :xmodel="formInfo.configProps.SiteExtendModel" 
          type="site"
          :id="formInfo.siteId">
        </cms-exmodel-editor>
      </el-card>
    </el-form>
    <!-- 导入主题对话框 -->
    <el-dialog 
      :title="title"
      v-model="open"
      width="500px"
      append-to-body>
      <el-form 
        ref="importFormRef"
        :model="importForm"
        label-width="80px">
        <el-form-item :label="$t('CMS.Site.ImportTheme')">
          <el-upload 
            ref="uploadRef"
            drag
            :data="importForm"
            :action="upload.url"
            :headers="upload.headers"
            :file-list="upload.fileList"
            :on-progress="handleFileUploadProgress"
            :on-success="handleFileSuccess"
            :auto-upload="false"
            :on-change="handleUploadChange"
            :limit="1">
              <template #default>
                <el-icon :size="36"><Upload /></el-icon>
                <div class="upload-tip">{{ $t("CMS.Resource.UploadTip1") }}</div>
              </template>
              <template #tip>
                <div class="upload-limit-tip">{{ $t("CMS.Resource.UploadTip2", [ '[.zip]',  '100M']) }}</div>
              </template>
            </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" :loading="upload.isUploading" @click="uploadSiteThemeZipFile">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancelImportSiteTheme">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
    <!-- 添加发布通道对话框 -->
    <el-dialog 
      :title="$t('CMS.PublishPipe.AddDialogTitle')"
      v-model="openAddPublishPipe"
      width="500px"
      append-to-body>
      <el-form ref="formPublishPipeRef" :model="formPublishPipe" :rules="publishPipeRules" label-width="80px">
        <el-form-item :label="$t('CMS.PublishPipe.Name')" prop="name">
          <el-input v-model="formPublishPipe.name"/>
        </el-form-item>
        <el-form-item :label="$t('CMS.PublishPipe.Code')" prop="code">
          <el-input v-model="formPublishPipe.code" :disabled="$tools.isNotEmpty(formPublishPipe.publishpipeId)" />
        </el-form-item>
        <el-form-item :label="$t('CMS.PublishPipe.Status')" prop="state">
          <el-radio-group v-model="formPublishPipe.state">
            <el-radio
              v-for="dict in EnableOrDisable"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('Common.Sort')" prop="sort">
          <el-input-number v-model="formPublishPipe.sort" :min="1"></el-input-number>
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="formPublishPipe.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitFormPublishPipe">{{ $t("Common.Save") }}</el-button>
        <el-button @click="cancelPublishPipe">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
    <!-- 模板选择组件 -->
    <cms-template-selector 
      v-model:open="openTemplateSelector" 
      :publishPipeCode="publishPipeActiveName" 
      :siteId="formInfo.siteId"
      @ok="handleTemplateSelected"
      @cancel="handleTemplateSelectorCancel" />
    <!-- 站点文件选择组件 -->
    <cms-file-selector v-model:open="openFileSelector" :path="fileSelectorPath" suffix="css" @ok="handleSetUEditorStyle" @close="openFileSelector=false"></cms-file-selector>
    <!-- 进度条 -->
    <cms-progress :title="progressTitle" v-model:open="openProgress" v-model:taskId="taskId" @close="handleProgressClose"></cms-progress>
    <!-- 下载进度条 -->
    <download-progress v-model:open="openDownloadThemeProgress" v-model:url="siteThemeDownloadUrl" fileName="SiteTheme.zip"></download-progress>
  </div>
</template>
<script setup name="CMSSiteInfo">
import { codeValidator } from '@/utils/validate';
import { getSite, publishSite, updateSite, exportSiteTheme, setCurrentSite  } from "@/api/contentcore/site";
import { addPublishPipe } from "@/api/contentcore/publishpipe";
import { genSitemap  } from "@/api/seo/sitemap";
import CmsTemplateSelector from '@/views/cms/contentcore/templateSelector';
import CmsProgress from '@/views/components/Progress';
import CmsLogoView from '@/views/cms/components/LogoView';
import CmsExmodelEditor from '@/views/cms/components/EXModelEditor';
import CmsFileSelector from '@/views/cms/components/FileSelector';
import DownloadProgress from '@/views/components/DownloadProgress';

const { proxy } = getCurrentInstance()
const { CMSStaticSuffix, CMSSitemapPageType, EnableOrDisable } = proxy.useDict('CMSStaticSuffix', 'CMSSitemapPageType', 'EnableOrDisable')

const props = defineProps({
  site: {
    type: String,
    default: undefined,
    required: false,
  }
})

const loading = ref(false)
const title = ref("")
const progressTitle = ref("")
const open = ref(false)
const openTemplateSelector = ref(false)
const publishPipeActiveName = ref("")
const openFileSelector = ref(false)
const openProgress = ref(false)
const taskId = ref("")
const rules = ref({
  name: [
    { required: true, message: proxy.$t("Common.RuleTips.NotEmpty"), trigger: "blur" }
  ],
  path: [
    { required: true, message: proxy.$t("Common.RuleTips.NotEmpty"), trigger: "blur" },
    { validator: codeValidator, trigger: "change" }
  ]
})
const openDownloadThemeProgress = ref(false)
const siteThemeDownloadUrl = ref("")
const openAddPublishPipe = ref(false)

const objects = reactive({
  formInfo: {},
  upload: {
    // 是否禁用上传
    isUploading: false,
    // 设置上传的请求头部
    headers: { ...proxy.$auth.getTokenHeader(), ...proxy.$cms.currentSiteHeader() },
    // 上传的地址
    url: import.meta.env.VITE_APP_BASE_API + "/cms/site/importTheme",
    // 上传的文件列表
    fileList: []
  },
  importForm: {
    siteId: props.site
  },
  formPublishPipe: {},
  publishPipeRules: {
    name: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    ],
    code: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
      { validator: codeValidator, trigger: "change" },
    ],
    sort: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    ],
    state: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    ],
  }
})
const { formInfo, upload, importForm, formPublishPipe, publishPipeRules } = toRefs(objects)

const showEXModel = computed(() => {
  return formInfo.value.configProps && formInfo.value.configProps.SiteExtendModel != null && formInfo.value.configProps.SiteExtendModel.length > 0;
})

const fileSelectorPath = computed(() => {
  return formInfo.value.path + "_" + publishPipeActiveName.value + "/"
})

watch(() => props.site, (newVal, oldVal) => {
  if (proxy.$tools.isNotEmpty(newVal) && newVal != oldVal) {
    loadSiteInfo();
  }
})

onMounted(() => {
  loadSiteInfo();
})

const loadSiteInfo = () => {
  if (proxy.$tools.isEmpty(props.site)) {
    proxy.$modal.msgError("Invalid siteId: " + props.site);
    return;
  }
  loading.value = true;
  getSite(props.site).then(response => {
    formInfo.value = response.data;
    if (formInfo.value.publishPipeDatas.length > 0) {
      publishPipeActiveName.value = formInfo.value.publishPipeDatas[0].pipeCode;
    }
    loading.value = false;
  });
}

const handleUpdate = () => {
  proxy.$refs.formInfoRef.validate(valid => {
    if (valid) {
      if (showEXModel.value) {
        formInfo.value.params = proxy.$refs.exModelEditorRef.getDatas();
      }
      updateSite(formInfo.value).then(response => {
        proxy.$modal.msgSuccess(proxy.$t("Common.SaveSuccess"));
      });
    }
  });
}

const handleSelectTemplate = () => {
  openTemplateSelector.value = true;
}

const handleTemplateSelected = (template) => {
  formInfo.value.publishPipeDatas.map(item => {
    if (item.pipeCode == publishPipeActiveName.value) {
      item.props.indexTemplate = template;
    }
  });
  openTemplateSelector.value = false;
}

const handleTemplateSelectorCancel = () => {
  openTemplateSelector.value = false;
}

const handlePreview = () => {
  let routeData = proxy.$router.resolve({
    path: "/cms/preview",
    query: { type: "site", dataId: props.site },
  });
  window.open(routeData.href, '_blank');
}

const handlePublish = () => {
  publishSite({ siteId: props.site, publishIndex: true }).then(response => {
    if (response.code == 200) {
      proxy.$modal.msgSuccess(proxy.$t("CMS.ContentCore.PublishSuccess"));
    }
  });
}

const handlePublishAll = (status) => {  
  const params = { siteId: props.site, publishIndex: false, contentStatus: status }
  publishSite(params).then(response => {
    if (response.code == 200) {
      if (response.data && response.data != "") {
        taskId.value = response.data;
        progressTitle.value = proxy.$t('CMS.ContentCore.PublishProgressTitle');
        openProgress.value = true;
        proxy.$cms.setPublishFlag("true")
      }
    } else {
      proxy.$modal.msgError(response.msg);
    }
  });
}

const handleGenSitemap = () => {
  const params = { siteId: props.site }
  genSitemap(params).then(response => {
    if (response.data && response.data != "") {
      taskId.value = response.data;
      progressTitle.value = proxy.$t('CMS.Site.SitemapProgressTitle');
      openProgress.value = true;
    }
  });
}

const handleImportTheme = () => {
  open.value = true;
}

const cancelImportSiteTheme = () => {
  open.value = false;
  proxy.$refs.uploadRef.clearFiles();
  proxy.$refs.importFormRef.resetFields();
}

const handleFileUploadProgress = (event, file, fileList) => {
  upload.value.isUploading = true;
}

const handleFileSuccess = (response, file, fileList) => {
  upload.value.isUploading = false;
  if (response.code == 200) {
    open.value = false;
    taskId.value = response.data;
    progressTitle.value = proxy.$t('CMS.Site.ImportTheme');
    openProgress.value = true;
  } else {
    proxy.$modal.msgError(response.msg);
  }
  proxy.$refs.uploadRef.clearFiles();
  proxy.$refs.importFormRef.resetFields();
}

const handleUploadChange = (file) => {
  file.name = file.name.toLowerCase();
  if (!file.name.endsWith(".zip")) {
    proxy.$modal.msgError(proxy.$t('CMS.Site.ThemeFileTypeErrMsg'));
    upload.value.fileList = [];
    return;
  }
}

const uploadSiteThemeZipFile = () => {
  proxy.$refs.importFormRef.validate(valid => {
    if (valid) {
      proxy.$refs.uploadRef.submit();
    }
  });
}

const handleExportTheme = () => {
  const data = { siteId: props.site }
  exportSiteTheme(data).then(response => {
    if (response.code == 200) {
      taskId.value = response.data;
      progressTitle.value = proxy.$t('CMS.Site.ExportTheme');
      openProgress.value = true;
    } else {
      proxy.$modal.msgError(response.msg);
    }
  });
}

const handleProgressClose = (resultStatus) => {
  if (progressTitle.value == proxy.$t('CMS.Site.ExportTheme')) {
    downloadTheme();
  } else if (progressTitle.value == proxy.$t('CMS.Site.ImportTheme')) {
    proxy.$nextTick(() => {
      proxy.$router.replace({
        path: '/redirect' + proxy.$route.fullPath
      })
    });
  }
}

const downloadTheme = () => {
  siteThemeDownloadUrl.value = `${import.meta.env.VITE_APP_BASE_API}/cms/site/downloadTheme/${props.site}`;
  openDownloadThemeProgress.value = true;
}

const handleSelectFile = () => {
  openFileSelector.value = true;
}

const handleSetUEditorStyle = (files) => {
  if (files.length > 0) {
    formInfo.value.publishPipeDatas.map(item => {
      if (item.pipeCode == publishPipeActiveName.value) {
        item.props.ueditorCss = files[0].filePath;
      }
    });
    openFileSelector.value = false;
  }
}

const handleAddPublishPipe = () => {
  formPublishPipe.value = {
    siteId: formInfo.value.siteId,
    state: "0",
    sort: 1,
  };
  openAddPublishPipe.value = true;
}

const cancelPublishPipe = () => {
  openAddPublishPipe.value = false;
}

const submitFormPublishPipe = () => {
  proxy.$refs.formPublishPipeRef.validate(valid => {
    if (valid) {
      addPublishPipe(formPublishPipe.value).then(response => {
        openAddPublishPipe.value = false;
        loadSiteInfo();
        proxy.$modal.msgSuccess(proxy.$t("Common.AddSuccess"));
      });
    }
  });
}

</script>
<style scoped>
.siteinfo-container .el-form-item {
  margin-bottom: 12px;
  width: 460px;
}
.siteinfo-container .el-card {
  margin-bottom: 10px;
}
</style>