<template>
  <div class="app-container adspace-editor-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button 
          plain
          type="success"
          icon="Edit"
          v-hasPermi="[ $p('PageWidget:Edit:{0}', [ pageWidgetId ]) ]"
          @click="handleSave">{{ $t("Common.Save") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="primary"
          icon="Promotion"
          v-hasPermi="[ $p('PageWidget:Publish:{0}', [ pageWidgetId ]) ]"
          @click="handlePublish">{{ $t('CMS.ContentCore.Publish') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="primary"
          icon="View"
          @click="handlePreview">{{ $t('CMS.ContentCore.Preview') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="info"
          :icon="Back"
          @click="handleGoBack">{{ $t('Common.GoBack') }}</el-button>
      </el-col>
    </el-row>
    <el-row :gutter="10">
      <el-col :lg="16" :md="24">
        <el-card shadow="hover">
          <template #header>
            <div class="clearfix">
              <span>{{ $t('CMS.Adv.AdList') }}</span>
            </div>
          </template>
          <el-row :gutter="24" class="mb8">
            <el-col :span="12">
              <el-row :gutter="10">
                <el-col :span="1.5">
                  <el-button 
                    plain
                    type="primary"
                    icon="Plus"
                    @click="handleAddAdvertisement">{{ $t("Common.Add") }}</el-button>
                </el-col>
                <el-col :span="1.5">
                  <el-button 
                    plain
                    type="success"
                    icon="Edit"
                    :disabled="selectedRows.length!==1"
                    @click="handleEditAdvertisement">{{ $t('Common.Edit') }}</el-button>
                </el-col>
                <el-col :span="1.5">
                  <el-button 
                    plain
                    type="danger"
                    icon="Delete"
                    :disabled="selectedRows.length===0"
                    @click="handleDeleteAdvertisements">{{ $t("Common.Delete") }}</el-button>
                </el-col>
              </el-row>
            </el-col>
            <el-col :span="12">
              <el-row :gutter="10" style="justify-content: end;">
                <el-col :span="1.5">
                  <el-input :placeholder="$t('CMS.Adv.Placeholder.Name')" v-model="queryParams.name" style="width: 200px;" class="mr10"></el-input>
                </el-col>
                <el-col :span="1.5">
                  <el-button-group>
                    <el-button 
                      type="primary"
                      icon="Search"
                      @click="handleQuery">{{ $t("Common.Search") }}</el-button>
                    <el-button 
                      icon="Refresh"
                      @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
                  </el-button-group>
                </el-col>
              </el-row>
            </el-col>
          </el-row>
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
              <template #default="scope">
                <dict-tag :options="EnableOrDisable" :value="scope.row.state"/>
              </template>
            </el-table-column>
            <el-table-column :label="$t('CMS.Adv.OnlineDate')" align="center" prop="onlineDate" width="160">
              <template #default="scope">
                <span>{{ scope.row.onlineDate }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('CMS.Adv.OfflineDate')" align="center" prop="offlineDate" width="160">
              <template #default="scope">
                <span>{{ scope.row.offlineDate }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('Common.Operation')" align="center" width="220">
              <template #default="scope">
                <el-button 
                  v-if="scope.row.state==='1'"
                  type="text"
                  icon="SwitchButton"
                  @click="handleEnableAdvertisements(scope.row)">{{ $t('Common.Enable') }}</el-button>
                <el-button 
                  v-if="scope.row.state==='0'"
                  type="text"
                  icon="SwitchButton"
                  @click="handleDisableAdvertisements(scope.row)">{{ $t('Common.Disable') }}</el-button>
                <el-button 
                  type="text"
                  icon="Edit"
                  @click="handleEditAdvertisement(scope.row)">{{ $t("Common.Edit") }}</el-button>
                <el-button 
                  type="text"
                  icon="Delete"
                  @click="handleDeleteAdvertisements(scope.row)">{{ $t("Common.Delete") }}</el-button>
              </template>
            </el-table-column>
          </el-table>
          <pagination 
            v-show="total>0"
            :total="total"
            v-model:page="queryParams.pageNum"
            v-model:limit="queryParams.pageSize"
            @pagination="loadAdvertisementList" />
        </el-card>
      </el-col>
      <el-col :lg="8" :md="24">
        <el-form 
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="110px">
          <el-card shadow="hover">
            <template #header>
              <div class="clearfix">
                <span>{{ $t('CMS.Adv.Basic') }}</span>
              </div>
            </template>
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
              <el-form-item :label="$t('Common.Remark')" prop="remark">
                <el-input type="textarea" v-model="form.remark" />
              </el-form-item>
            </div>
          </el-card>
          <el-card shadow="hover" class="mt10">
            <template #header>
              <div class="clearfix">
                <span>{{ $t('CMS.Block.PublishPipeProp') }}</span>
              </div>
            </template>
            <div class="form-col">
              <el-form-item 
                v-for="item in form.templates" 
                :label="item.name" 
                :key="item.code">
                <el-input v-model="item.template">
                  <template #append>
                    <el-button icon="FolderOpened" @click="handleSelectTemplate(item.code)"></el-button>
                  </template>
                </el-input>
              </el-form-item>
            </div>
          </el-card>
        </el-form>
      </el-col>
    </el-row>
    <!-- 模板选择组件 -->
    <cms-template-selector 
      :open="openTemplateSelector" 
      :publishPipeCode="form.publishPipeCode"
      @ok="handleTemplateSelected"
      @cancel="handleTemplateSelectorCancel" />
  </div>
</template>
<script setup name="CMSAdSpaceEditor">
import { codeValidator, pathValidator } from '@/utils/validate';
import { getPublishPipeSelectData } from "@/api/contentcore/publishpipe";
import { getPageWidget, addPageWidget, editPageWidget, publishPageWidgets } from "@/api/contentcore/pagewidget";
import { listAdvertisements, deleteAdvertisement, enableAdvertisement, disableAdvertisement } from "@/api/advertisement/advertisement";
import CmsTemplateSelector from '@/views/cms/contentcore/templateSelector';

const { proxy } = getCurrentInstance();
const { EnableOrDisable } = proxy.useDict('EnableOrDisable');

const loading = ref(true);
const pageWidgetId = ref(proxy.$route.query.id);
const publishPipes = ref([]);
const form = reactive({
  publishPipeCode: '',
  content: {}
});

const rules = reactive({
  name: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  code: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    { validator: codeValidator, trigger: "change" },
  ],
  publishPipeCode: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  path: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    { validator: pathValidator, trigger: "change" },
  ]
});

const openTemplateSelector = ref(false);
const templatePublishPipeCode = ref('');
const dataList = ref(undefined);
const total = ref(0);
const selectedRows = ref([]);
const adSpaceId = ref(proxy.$route.query.id);
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  adSpaceId: proxy.$route.query.id,
  name: undefined
});

const initDataStr = ref("");
const publishAfterSave = ref(false);

function loadPublishPipes() {
  getPublishPipeSelectData().then(response => {
    publishPipes.value = response.data.rows;
  });
}

function loadPageWidgetInfo() {
  getPageWidget(pageWidgetId.value).then(response => {
    Object.assign(form, response.data);
    initDataStr.value = JSON.stringify(form);
  });
}

function isFormChanged() {
  return JSON.stringify(form) != initDataStr.value;
}

function handleSave() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      form.content = JSON.stringify(dataList.value);
      if (pageWidgetId.value) {
        editPageWidget(form).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.SaveSuccess'));
          initDataStr.value = JSON.stringify(form);
          if (publishAfterSave.value) {
            publishAfterSave.value = false;
            handlePublish();
          }
        });
      } else {
        addPageWidget(form).then(response => {
          proxy.$modal.msgSuccess(response.msg);
          proxy.$router.push({ path: form.route, query: { id: response.data } });
        });
      }
    }
  });
}

function handleSelectTemplate(publishPipeCode) {
  templatePublishPipeCode.value = publishPipeCode;
  openTemplateSelector.value = true;
}

function handleTemplateSelected(template) {
  let prop = form.templates.find(item => item.code == templatePublishPipeCode.value);
  if (prop) {
    prop.template = template;
  }
  openTemplateSelector.value = false;
}

function handleTemplateSelectorCancel() {
  openTemplateSelector.value = false;
}

function handlePublish() {
  if (isFormChanged()) {
    publishAfterSave.value = true;
    handleSave();
    return;
  }
  const pageWidgetIds = [ pageWidgetId.value ];
  publishPageWidgets(pageWidgetIds).then(response => {
    proxy.$modal.msgSuccess(proxy.$t('CMS.ContentCore.PublishSuccess'));
  });
}

function handlePreview() {
  let routeData = proxy.$router.resolve({
    path: "/cms/preview",
    query: { type: "pagewidget", dataId: pageWidgetId.value },
  });
  window.open(routeData.href, '_blank');
}

function loadAdvertisementList() {
  loading.value = true;
  listAdvertisements(queryParams).then(response => {
    if (response.code == 200) {
      dataList.value = response.data.rows;
      total.value = parseInt(response.data.total);
      loading.value = false;
    }
  });
}

function handleQuery() {
  queryParams.pageNum = 1;
  loadAdvertisementList();
}

function resetQuery() {
  queryParams.name = undefined;
  queryParams.pageNum = 1;
  handleQuery();
}

function handleSelectionChange(selection) {
  selectedRows.value = selection.map(item => item);
}

function handleAddAdvertisement() {
  proxy.$router.push({ path: "/cms/ad/editor", query: { adSpaceId: adSpaceId.value } });
}

function handleEditAdvertisement(row) {
  const advertisementId = row.advertisementId || selectedRows.value[0].advertisementId;
  proxy.$router.push({ path: "/cms/ad/editor", query: { adSpaceId: adSpaceId.value, id: advertisementId } });
}

function handleDeleteAdvertisements(row) {
  const advertisementIds = row.advertisementId ? [ row.advertisementId ] : selectedRows.value.map(item => item.advertisementId);
  if (advertisementIds.length == 0) {
    return;
  }
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function() {
    return deleteAdvertisement(advertisementIds);
  }).then(response => {
    if (response.code === 200) {
      proxy.$modal.msgSuccess(response.msg);
      loadAdvertisementList();
    }
  }).catch(() => {});
}

function handleEnableAdvertisements(row) {
  const advertisementIds = row.advertisementId ? [ row.advertisementId ] : selectedRows.value.map(item => item.advertisementId);
  enableAdvertisement(advertisementIds).then(response => {
    proxy.$modal.msgSuccess(response.msg);
    loadAdvertisementList();
  });
}

function handleDisableAdvertisements(row) {
  const advertisementIds = row.advertisementId ? [ row.advertisementId ] : selectedRows.value.map(item => item.advertisementId);
  disableAdvertisement(advertisementIds).then(response => {
    proxy.$modal.msgSuccess(response.msg);
    loadAdvertisementList();
  });
}

function handleGoBack() {
  if (proxy.$route.query.from == 'pagewidget') {
    const obj = { name: "CmsContentcoreContent", params: { tab: "pageWdiget" } };
    proxy.$tab.closeOpenPage(obj);
  } else {
    const obj = { name: "CmsAdAdSpace" };
    proxy.$tab.closeOpenPage(obj);
  }
}

onMounted(() => {
  if (pageWidgetId.value) {
    loadPublishPipes();
    loadPageWidgetInfo();
    loadAdvertisementList();
  } else {
    proxy.$modal.msgError(proxy.$t('CMS.PageWidget.InvalidPageWidgetId', [ pageWidgetId.value ]));
  }
});
</script>
<style scoped>
.adspace-editor-container .form-row {
  display: inline-block;
}
.adspace-editor-container .el-form-item {
  width: 100%;
  float: left;
}
</style>