<template>
  <div class="catalog-info-container">
    <el-row :gutter="10" class="mb8 btn-row">
      <el-col :span="1.5" class="permi-wrap">
        <el-button 
          plain
          type="success"
          icon="Edit"
          :disabled="!props.cid"
          v-hasPermi="[ $p('Catalog:Edit:{0}', [ props.cid ]) ]"
          @click="handleUpdate">{{ $t("Common.Save") }}</el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button 
          plain
          type="primary"
          icon="View"
          :disabled="!props.cid"
          @click="handlePreview">{{ $t('CMS.ContentCore.Preview') }}</el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-dropdown v-hasPermi="[ $p('Catalog:Publish:{0}', [ props.cid ]) ]">
          <el-button 
            plain
            type="primary"
            icon="Promotion"
            :disabled="!props.cid"
            @click="handlePublish(-1)">
            {{ $t('CMS.ContentCore.Publish') }}<el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item 
                v-for="dict in CMSContentStatus" 
                :key="dict.value" 
                icon="Promotion" 
                @click.native="handlePublish(dict.value)"
              >{{dict.label}}{{ $t('CMS.Catalog.Content') }}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button 
          plain
          type="warning"
          :icon="catalogVisible?'Close':'Check'"
          :disabled="!props.cid"
          v-hasPermi="[ $p('Catalog:ShowHide:{0}', [ props.cid ]) ]"
          @click="handleChangeVisible">{{ catalogVisible ? $t("Common.Hide") : $t("Common.Show") }}</el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button 
          type="primary"
          icon="Rank"
          plain
          :disabled="!props.cid"
          v-hasPermi="[ $p('Catalog:Move:{0}', [ props.cid ]) ]"
          @click="handleMoveCatalog">{{ $t('Common.Move') }}</el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button 
          type="primary"
          icon="DocumentCopy"
          plain
          :disabled="!props.cid"
          v-hasPermi="[ $p('Catalog:Edit:{0}', [ props.cid ]) ]"
          @click="handleMergeCatalogs">{{ $t('CMS.Catalog.Merge') }}</el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <div class="btn-permi" v-hasPermi="[ $p('Catalog:Sort:{0}', [ props.cid ]) ]">
          <el-popover
            width="226"
            :disabled="!props.cid"
            v-model="showSortPop">
            <el-input-number v-model="sortValue" style="width:200px;" />
            <div style="color: #909399; line-height: 30px; display: flex; align-items: center;">
              <InfoFilled class="mr5" style="width: var(--el-popover-font-size)" />{{ $t('CMS.Catalog.SortTip') }}
            </div>
            <div class="mt5 text-align-right">
              <el-button type="text" @click="handleSortCatalogCancel">{{ $t('Common.Cancel') }}</el-button>
              <el-button type="primary" @click="handleSortCatalog">{{ $t('Common.Confirm') }}</el-button>
            </div>
            <template #reference>
              <el-button 
                plain
                type="primary"
                icon="Sort"
                :disabled="!props.cid"
              >{{ $t('Common.Sort') }}</el-button>
            </template>
          </el-popover>
        </div>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <div class="btn-permi" v-hasPermi="[ $p('Catalog:Delete:{0}', [ props.cid ]) ]">
          <el-popconfirm :title="$t('CMS.Catalog.DeleteTip')" :width="260" @confirm="handleDelete">
            <template #reference>
              <el-button 
                type="danger" 
                icon="Delete"
                plain
                :disabled="!props.cid"
              >{{ $t("Common.Delete") }}</el-button>
            </template>
          </el-popconfirm>
        </div>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <div class="btn-permi" v-hasPermi="[ $p('Catalog:Edit:{0}', [ props.cid ]) ]">
          <el-popconfirm :title="$t('CMS.Catalog.ClearTip')" :width="260" @confirm="handleClearCatalog">
            <template #reference>
              <el-button 
                type="danger"
                icon="DeleteFilled"
                plain
                :disabled="!props.cid"
              >{{ $t('CMS.Catalog.Clear') }}</el-button>
            </template>
          </el-popconfirm>
        </div>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button 
          type="primary"
          icon="Rank"
          plain
          @click="handleExportCatalogTree">{{ $t('CMS.Catalog.ExportCatalogTree') }}</el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-dropdown v-hasPermi="[ $p('Catalog:Publish:{0}', [ props.cid ]) ]">
          <el-button 
            plain
            type="primary"
            icon="Refresh"
            :disabled="!props.cid">
            {{ $t('CMS.ContentCore.RefreshCdn') }}<el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item 
                icon="Refresh" 
                @click.native="handleRefreshCdn(false)"
              >{{ $t('CMS.Catalog.RefreshCdn') }}</el-dropdown-item>
              <el-dropdown-item 
                icon="Refresh" 
                @click.native="handleRefreshCdn(true)"
              >{{ $t('CMS.Catalog.RefreshCdnAll') }}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-col>
    </el-row>
    <el-form 
      ref="formInfoRef"
      v-loading="loading"
      :model="formInfo"
      :rules="rules"
      :disabled="!props.cid"
      label-width="165px">
      <el-card shadow="hover">
        <template #header>
          <span>{{ $t('CMS.Catalog.Basic') }}</span>
        </template>
        <el-form-item :label="$t('CMS.Catalog.CatalogId')" prop="catalogId">
          <span class="span_catalogid" v-if="formInfo.catalogId!=undefined">{{formInfo.catalogId}}</span>
          <span v-if="!catalogVisible" style="color: #ffba00;"> [ {{ $t('Common.Hide') }} ]</span>
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.Name')" prop="name">
          <el-input v-model="formInfo.name" />
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.Alias')" prop="alias">
          <el-input v-model="formInfo.alias" />
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.Path')" prop="path">
          <el-input v-model="formInfo.path" />
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.CatalogType')" prop="catalogType">
          <el-select v-model="formInfo.catalogType" :placeholder="$t('CMS.Catalog.CatalogType')">
            <el-option 
              v-for="ct in catalogTypeOptions"
              :key="ct.id"
              :label="ct.name"
              :value="ct.id" />
          </el-select>
        </el-form-item>
        <el-form-item
          :label="$t('CMS.Catalog.RedirectUrl')"
          v-if="formInfo.catalogType==='link'"
          prop="redirectUrl">
          <el-input v-model="formInfo.redirectUrl" placeholder="http(s)://">
            <template #append>
              <el-dropdown @command="handleLinkTo">
                <el-button>
                  {{ $t('CMS.ContentCore.InternalUrl') }}<i class="el-icon-arrow-down el-icon--right"></i>
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
        <el-form-item :label="$t('CMS.Catalog.Desc')" prop="description">
          <el-input v-model="formInfo.description" type="textarea" maxlength="250" />
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.ContentPathRule')" prop="detailNameRule">
          <el-select v-model="formInfo.detailNameRule" :placeholder="$t('CMS.Catalog.ContentPathRule')">
            <el-option 
              v-for="ct in contentPathRuleOptions"
              :key="ct.id"
              :label="ct.name"
              :value="ct.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.StaticFlag')" prop="staticFlag">
          <el-switch
            v-model="formInfo.staticFlag"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.TagIgnore')" prop="tagIgnore">
          <el-switch
            v-model="formInfo.tagIgnore"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.Logo')" prop="logo">
          <cms-logo-view v-model="formInfo.logo" :height="150"></cms-logo-view>
        </el-form-item>
      </el-card>
      <el-card shadow="hover">
        <template #header>
          <span>{{ $t('CMS.Catalog.PublishPipeConf') }}</span>
        </template>
        <el-tabs v-model="publishPipeActiveName">
          <el-tab-pane 
            v-for="pp in formInfo.publishPipeDatas"
            :key="pp.pipeCode"
            :command="pp"
            :name="pp.pipeCode"
            :label="pp.pipeName">
            <el-divider content-position="left">{{ $t('CMS.Catalog.TemplateConfig') }}</el-divider>
            <el-form-item :label="$t('CMS.Catalog.IndexTemplate')" prop="indexTemplate">
              <el-input v-model="pp.props.indexTemplate">
                <template #append>
                  <el-button 
                    type="primary"
                    @click="handleSelectTemplate('indexTemplate')"
                  >{{ $t("Common.Select") }}</el-button>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item :label="$t('CMS.Catalog.ListTemplate')" prop="listTemplate">
              <el-input class="mr5" v-model="pp.props.listTemplate">
                <template #append>
                  <el-button 
                    type="primary"
                    @click="handleSelectTemplate('listTemplate')"
                  >{{ $t("Common.Select") }}</el-button>
                </template>
              </el-input>
              <el-button 
                plain 
                icon="BottomRight" 
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
                <template #append>
                  <el-button 
                    type="primary"
                    @click="handleSelectTemplate(`detailTemplate_${ct.id}`)"
                  >{{ $t("Common.Select") }}</el-button>
                </template>
              </el-input>
              <el-button 
                icon="BottomRight" 
                type="primary" 
                plain 
                @click="handleApplyToChildren(`detailTemplate_${ct.id}`)"
              >{{ $t('CMS.Catalog.ApplyToChildren') }}</el-button>
            </el-form-item>
            <el-form-item :label="$t('CMS.Catalog.ContentExTemplate')" prop="contentExTemplate">
              <el-input class="mr5" v-model="pp.props.contentExTemplate">
                <template #append>
                  <el-button 
                    type="primary"
                    @click="handleSelectTemplate('contentExTemplate')"
                  >{{ $t("Common.Select") }}</el-button>
                </template>
              </el-input>
              <el-button 
                plain 
                icon="BottomRight" 
                type="primary" 
                @click="handleApplyToChildren('contentExTemplate')"
              >{{ $t('CMS.Catalog.ApplyToChildren') }}</el-button>
            </el-form-item>
            <el-divider content-position="left">{{ $t('CMS.Catalog.OtherConfig') }}</el-divider>
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
          </el-tab-pane>
        </el-tabs>
      </el-card>
      <el-card shadow="hover">
        <template #header>
          <span>{{ $t('CMS.ContentCore.SEOConfig') }}</span>
        </template>
        <el-form-item :label="$t('CMS.ContentCore.SEOTitle')" prop="seoTitle">
          <el-input v-model="formInfo.seoTitle" />
        </el-form-item>
        <el-form-item :label="$t('CMS.ContentCore.SEOKeyword')" prop="seoKeywords">
          <el-input v-model="formInfo.seoKeywords" />
        </el-form-item>
        <el-form-item :label="$t('CMS.ContentCore.SEODescription')" prop="seoDescription">
          <el-input v-model="formInfo.seoDescription" type="textarea" :maxlength="100" />
        </el-form-item>
      </el-card>
      <el-card v-if="showEXModel" shadow="hover">
        <template #header>
          <span>{{ $t('CMS.Catalog.ExModelProps') }}</span>
        </template>
        
        <cms-exmodel-editor 
          ref="exModelEditorRef"
          :xmodel="formInfo.configProps.CatalogExtendModel" 
          type="catalog"
          :id="formInfo.catalogId">
        </cms-exmodel-editor>
      </el-card>
    </el-form>
    <el-dialog 
      :title="$t('CMS.Catalog.PublishDialogTitle')"
      v-model="publishDialogVisible"
      width="500px"
      class="publish-dialog">
      <div>
        <p>{{ $t('Common.Tips') }}</p>
        <p>{{ $t('CMS.Catalog.PublishTips') }}</p>
        <el-checkbox v-model="publishChild">{{ $t('CMS.Catalog.ContainsChildren') }}</el-checkbox>
      </div>
      <template #footer>
        <el-button @click="publishDialogVisible = false">{{ $t("Common.Cancel") }}</el-button>
        <el-button type="primary" @click="handleDoPublish">{{ $t("Common.Confirm") }}</el-button>
      </template>
    </el-dialog>
    <!-- 导出栏目树 -->
    <el-dialog 
      :title="$t('CMS.Catalog.ExportCatalogTree')"
      v-model="catalogTreeDialogVisible"
      width="500px"
      class="catalog-tree-dialog">
      <div>
        <el-input type="textarea" :rows="10" v-model="catalogTree" readonly style="width:100%;"></el-input>
      </div>
      <template #footer>
        <el-button type="primary" v-copyText="catalogTree" v-copyText:callback="clipboardSuccess">{{ $t("Common.Copy") }}</el-button>
        <el-button @click="catalogTreeDialogVisible = false">{{ $t("Common.Close") }}</el-button>
      </template>
    </el-dialog>
    <!-- 模板选择组件 -->
    <cms-template-selector 
      v-model:open="openTemplateSelector" 
      :publishPipeCode="publishPipeActiveName"
      @ok="handleTemplateSelected"
      @cancel="handleTemplateSelectorCancel" />
    <!-- 栏目选择组件 -->
    <cms-catalog-selector
      v-model:open="openCatalogSelector"
      :showRootNode="showCatalogSelectorRootNode"
      :disableLink="disableLinkCatalog"
      :multiple="multipleCatalogSeletor"
      @ok="handleCatalogSelectorOk"
      @close="handleCatalogSelectorClose"></cms-catalog-selector>
    <!-- 内容选择组件 -->
    <cms-content-selector
      v-model:open="openContentSelector"
      @ok="handleContentSelectorOk"
      @close="handleContentSelectorClose"></cms-content-selector>
    <!-- 站点文件选择组件 -->
    <cms-file-selector v-model:open="openFileSelector" suffix="css" @ok="handleSetUEditorStyle" @close="openFileSelector=false"></cms-file-selector>
    <!-- 进度条 -->
    <cms-progress :title="progressTitle" v-model:open="openProgress" :taskId="taskId" @close="handleCloseProgress"></cms-progress>
  </div>
</template>
<script setup name="CMSCatalogInfo">
import { codeValidator, pathValidator } from '@/utils/validate';
import * as catalogApi from "@/api/contentcore/catalog";
import * as cdnApi from '@/api/contentcore/cdn';
import CmsCatalogSelector from "@/views/cms/contentcore/catalogSelector";
import CmsContentSelector from "@/views/cms/contentcore/contentSelector";
import CmsTemplateSelector from '@/views/cms/contentcore/templateSelector';
import CmsProgress from '@/views/components/Progress';
import CmsLogoView from '@/views/cms/components/LogoView';
import CmsExmodelEditor from '@/views/cms/components/EXModelEditor';
import CmsFileSelector from '@/views/cms/components/FileSelector';

const { proxy } = getCurrentInstance();

const props = defineProps({
  cid: {
    type: String,
    default: undefined,
    required: false,
  },
});

const emit = defineEmits(['reload']);

const { CMSContentStatus } = proxy.useDict('CMSContentStatus');

const showEXModel = computed(() => {
  return formInfo.value.configProps && formInfo.value.configProps.CatalogExtendModel != null && formInfo.value.configProps.CatalogExtendModel.length > 0;
})

const catalogVisible = computed(() => {
  return formInfo.value.visibleFlag == "Y";
})

const loading = ref(false);
const openCatalogSelector = ref(false);
const catalogSelectorFor = ref(undefined);
const showCatalogSelectorRootNode = ref(false);
const multipleCatalogSeletor = ref(false);
const disableLinkCatalog = ref(false);
const openContentSelector = ref(false);
const openTemplateSelector = ref(false); // 是否显示模板选择弹窗
const propKey = ref(""); // 选择模板时记录变更的模板对应属性Key
const openProgress = ref(false); // 是否显示任务进度条
const progressTitle = ref("");
const progressType = ref("");
const taskId = ref(""); // 任务ID
const publishDialogVisible = ref(false);
const publishChild = ref(false);
const publishStatus = ref(-1);
const publishPipeActiveName = ref(""); // 当前选中的发布通道Tab
const showSortPop = ref(false);
const sortValue = ref(0);
const contentTypes = ref([]);
// 栏目信息表单
const objects = reactive({
  formInfo: {
    siteId: ""
  }
});
const { formInfo } = toRefs(objects);
const contentPathRuleOptions = ref([]);
const catalogTypeOptions = ref([]);
const publishPipes = ref([]); // 栏目发布通道数据
      // 表单校验
const rules = ref({
  name: [
    { required: true, message: proxy.$t("Common.RuleTips.NotEmpty"), trigger: "blur" },
    { max: 100, messag: proxy.$t('Common.RuleTips.MaxLength', [ 100 ]), trigger: "change" }
  ],
  alias: [
    { required: true, message: proxy.$t("Common.RuleTips.NotEmpty"), trigger: "blur" },
    { validator: codeValidator, trigger: "change" },
    { max: 100, messag: proxy.$t('Common.RuleTips.MaxLength', [ 100 ]), trigger: "change" }
  ],
  path: [
    { required: true, message: proxy.$t("Common.RuleTips.NotEmpty"), trigger: "blur" },
    { validator: pathValidator, trigger: "change" },
    { max: 255, messag: proxy.$t('Common.RuleTips.MaxLength', [ 255 ]), trigger: "change" }
  ],
  catalogType: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  description: [
    { max: 255, messag: proxy.$t('Common.RuleTips.MaxLength', [ 255 ]), trigger: "change" }
  ],
  redirectUrl: [
    { max: 255, messag: proxy.$t('Common.RuleTips.MaxLength', [ 255 ]), trigger: "change" }
  ],
  seoTitle: [
    { max: 200, messag: proxy.$t('Common.RuleTips.MaxLength', [ 200 ]), trigger: "change" }
  ],
  seoKeywords: [
    { max: 400, messag: proxy.$t('Common.RuleTips.MaxLength', [ 400 ]), trigger: "change" }
  ],
  seoDescription: [
    { max: 1000, messag: proxy.$t('Common.RuleTips.MaxLength', [ 1000 ]), trigger: "change" }
  ]
});
const openFileSelector = ref(false);
const catalogTreeDialogVisible = ref(false);
const catalogTree = ref("");

onMounted(() => {
  loadCatalogTypes();
  loadContentTypes();
  loadCatalogInfo();
  loadContentPathRules();
});

watch(() => props.cid, (newVal) => {
  if (proxy.$tools.isNotEmpty(newVal)) {
    loadCatalogInfo();
  } else {
    formInfo.value = { siteId: "" };
  }
});

const loadContentPathRules = () => {
  catalogApi.getContentPathRules().then(response => {
    contentPathRuleOptions.value = response.data;
  });
}

const loadContentTypes = () => {
  catalogApi.getContentTypes().then(response => {
    contentTypes.value = response.data;
  });
}

const loadCatalogTypes = () => {
  catalogApi.getCatalogTypes().then(response => {
    catalogTypeOptions.value = response.data;
  });
}

const loadCatalogInfo = () => {
  if (!props.cid) {
    // proxy.$modal.msgError(proxy.$t('CMS.Catalog.SelectCatalogFirst'));
    return;
  }
  loading.value = true;
  catalogApi.getCatalogData(props.cid).then(response => {
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
      catalogApi.updateCatalog(formInfo.value).then(response => {
        proxy.$modal.msgSuccess(proxy.$t('Common.SaveSuccess'));
        emit("reload");
      });
    }
  });
}

const handleChangeVisible = () => {
  const visible = formInfo.value.visibleFlag == "Y" ? "N" : "Y";
  catalogApi.changeVisible(formInfo.value.catalogId, visible).then(response => {
    proxy.$modal.msgSuccess(response.msg);
    formInfo.value.visibleFlag = visible;
  });
}

const handlePreview = () => {
  let routeData = proxy.$router.resolve({
    path: "/cms/preview",
    query: { type: "catalog", dataId: formInfo.value.catalogId },
  });
  window.open(routeData.href, '_blank');
}

const handlePublish = (publishStatusV) => {
  publishStatus.value = publishStatusV;
  publishDialogVisible.value = true;
}

const handleDoPublish = () => {
  const data = {
    catalogId: formInfo.value.catalogId,
    publishDetail: publishStatus.value != -1,
    publishStatus: publishStatus.value,
    publishChild: publishChild.value
  };
  publishDialogVisible.value = false;
  publishChild.value = false;
  catalogApi.publishCatalog(data).then(response => {
    taskId.value = response.data;
    progressTitle.value = proxy.$t('CMS.Catalog.PublishProgressTitle');
    progressType.value = "Publish";
    openProgress.value = true;
    proxy.$cms.setPublishFlag("true")
  }); 
}

const handleDelete = () => {
  if (!props.cid) {
    proxy.$modal.msgWarning(proxy.$t('CMS.Catalog.SelectCatalogFirst'));
    return;
  }
  catalogApi.delCatalog(props.cid).then(response => {
    if (response.data && response.data != "") {
      taskId.value = response.data;
      progressTitle.value = proxy.$t('CMS.Catalog.DeleteProgressTitle');
      progressType.value = "Delete";
      openProgress.value = true;
      proxy.$cms.setLastSelectedCatalog(formInfo.value.parentId);
    }
  });
}

const handleMoveCatalog = () => {
  catalogSelectorFor.value = "MoveCatalog";
  openCatalogSelector.value = true;
  showCatalogSelectorRootNode.value = true;
  disableLinkCatalog.value = false;
  multipleCatalogSeletor.value = false;
}

const handleCloseProgress = () => {
  if (progressType.value == 'Delete' || progressType.value == 'Move' || progressType.value == 'Merge') {
    proxy.$refs.formInfoRef.resetFields();
    emit("reload"); 
  }
}

const handleSelectTemplate = (propKeyV) => {
  propKey.value = propKeyV;
  openTemplateSelector.value = true;
}

const handleTemplateSelected = (template) => {
  formInfo.value.publishPipeDatas.map(item => {
    if (item.pipeCode == publishPipeActiveName.value) {
      item.props[propKey.value] = template;
    }
  });
  openTemplateSelector.value = false;
}

const handleTemplateSelectorCancel = () => {
  openTemplateSelector.value = false;
}

const handleApplyToChildren = (propKey) => {
  const data = { 
    catalogId: props.cid,
    publishPipeCode: publishPipeActiveName.value,
    publishPipePropKeys: [ propKey ]
  }
  catalogApi.applyPublishPipeToChildren(data).then(res => {
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
  });
}

const handleLinkTo = (type) => {
  if (type === 'content') {
    openContentSelector.value = true;
  } else if (type === 'catalog') {
    openCatalogSelector.value = true;
    showCatalogSelectorRootNode.value = false;
    disableLinkCatalog.value = true;
    multipleCatalogSeletor.value = false;
    catalogSelectorFor.value = "";
  }
}

const handleCatalogSelectorOk = (args) => {
  const catalogs = args[0];
  if (catalogSelectorFor.value == 'MoveCatalog') {
    let toCatalog = "0";
    if (catalogs && catalogs.length > 0) {
      toCatalog = catalogs[0].id;
    }
    catalogApi.moveCatalog(props.cid, toCatalog).then(response => {
      if (response.data && response.data != "") {
        taskId.value = response.data;
        progressTitle.value = proxy.$t('CMS.Catalog.MoveProgressTitle');;
        progressType.value = "Move";
        openProgress.value = true;
      }
    })
  } if (catalogSelectorFor.value == 'MergeCatalog') {
    doMergeCatalogs(catalogs)
  } else {
    if (catalogs && catalogs.length > 0) {
      formInfo.value.redirectUrl = catalogs[0].props.internalUrl;
    }
  }
  openCatalogSelector.value = false;
  catalogSelectorFor.value = undefined;
}

const handleCatalogSelectorClose = () => {
  openCatalogSelector.value = false;
}

const handleContentSelectorOk = (contents) => {
  if (contents && contents.length > 0) {
    formInfo.value.redirectUrl = contents[0].internalUrl;
  }
  openContentSelector.value = false;
}

const handleContentSelectorClose = () => {
  openContentSelector.value = false;
}

const handleSortCatalog = () => {
  if (sortValue.value == 0) {
    return;
  }
  let data = { catalogId: props.cid, sort: sortValue.value }
  catalogApi.sortCatalog(data).then(response => {
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
    showSortPop.value = false;
    sortValue.value = 0;
    emit("reload"); 
  });
}

const handleSortCatalogCancel = () => {
  showSortPop.value = false;
  sortValue.value = 0;
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
  }
  openFileSelector.value = false;
}

const handleMergeCatalogs = () => {
  catalogSelectorFor.value = "MergeCatalog";
  openCatalogSelector.value = true;
  showCatalogSelectorRootNode.value = false;
  disableLinkCatalog.value = true;
  multipleCatalogSeletor.value = true;
}

const doMergeCatalogs = (catalogs) => {
  if (!catalogs || catalogs.length == 0) {
    proxy.$modal.msgWarning(proxy.$t('CMS.Catalog.SelectCatalogFirst'));
    return;
  }
  const mergeCatalogIds = catalogs.map(c => c.id);
  const data = { catalogId: props.cid, mergeCatalogIds: mergeCatalogIds }
  catalogApi.mergeCatalogs(data).then(response => {
    if (response.data && response.data != "") {
      taskId.value = response.data;
      progressTitle.value = proxy.$t('CMS.Catalog.MergeProgressTitle');;
      progressType.value = "Merge";
      openProgress.value = true;
    }
  })
}

const handleClearCatalog = () => {
  const data = { catalogId: props.cid }
  catalogApi.clearCatalog(data).then(response => {
    if (response.data && response.data != "") {
      taskId.value = response.data;
      progressTitle.value = proxy.$t('CMS.Catalog.ClearProgressTitle');;
      progressType.value = "Clear";
      openProgress.value = true;
    }
  });
}

const handleExportCatalogTree = () => {
  catalogTreeDialogVisible.value = true;
  const params = { catalogId: props.cid || 0 }
  catalogApi.getCatalogTree(params).then(response => {
    catalogTree.value = response.data;
  });
}

const clipboardSuccess = () => {
  proxy.$modal.msgSuccess(proxy.$t('Common.CopySuccess'));
}

const handleRefreshCdn = (refreshAll) => {
  cdnApi.refreshCatalog(props.cid, refreshAll).then(res => {
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
  });
}
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