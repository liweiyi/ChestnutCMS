<template>
  <div class="app-container block-manual-container">
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
          icon="Back"
          @click="handleGoBack">{{ $t('Common.GoBack') }}</el-button>
      </el-col>
    </el-row>
    <el-row :gutter="10">
      <el-col :lg="16" :md="24">
        <el-card shadow="hover" class="mb10">
          <template #header>
            <span>{{ $t('CMS.Block.ManualList') }}</span>
          </template>
          <el-table :data="form.content" style="width: 100%">
            <el-table-column type="index" :label="$t('Common.RowNo')" width="50">
            </el-table-column>
            <el-table-column :label="$t('CMS.Block.Title')" prop="title">
              <template #default="scope">
                <span class="row-insert">
                  <el-button 
                    icon="Plus" 
                    circle 
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
                        type="primary"
                        @click="handleAddItem(scope.$index, index + 1)">
                        <el-icon><CirclePlus /></el-icon>
                      </el-link>
                      <el-link 
                        class="item-op-del"
                        :underline="false" 
                        type="danger"
                        @click="handleDeleteItem(scope.$index, index)">
                        <el-icon><CircleClose /></el-icon>
                      </el-link>
                    </span>
                  </el-tag>
                </span>
              </template>
            </el-table-column>
            <el-table-column width="220" align="right">
              <template #header>
                <el-button
                  plain
                  type="primary"
                  icon="Plus"
                  @click="handleAddRow(form.content.length)">{{ $t('CMS.Block.AddRow') }}</el-button>
                <el-button
                  plain
                  type="danger"
                  icon="Delete"
                  @click="handleClear">{{ $t('CMS.Block.Clean') }}</el-button>
              </template>
              <template #default="scope">
                <el-button
                  plain
                  icon="Plus"
                  @click="handleAddRow(scope.$index)">{{ $t('CMS.Block.InsertRow') }}</el-button>
                <el-button
                  plain
                  icon="Delete"
                  type="danger"
                  @click="handleDeleteRow(scope.$index)">{{ $t("Common.Delete") }}</el-button>
              </template>
            </el-table-column>
          </el-table>
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
              <span>{{ $t('CMS.Block.Basic') }}</span>
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
              <span>{{ $t('CMS.Block.PublishPipeProp') }}</span>
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
    <!-- 链接编辑弹窗 -->
    <el-dialog 
      :title="title"
      v-model="dialogVisible"
      :close-on-click-modal="false"
      width="600px"
      append-to-body>
      <el-form 
        ref="formItemRef"
        :model="formItem"
        label-width="80px"
        class="form_item">
        <div class="form-row">
          <el-form-item :label="$t('CMS.Block.Title')" prop="title">
            <el-input v-model="formItem.title">
              <template #append>
                <el-dropdown @command="handleLinkTo">
                  <el-button>
                    {{ $t('Common.Select') }}<el-icon class="el-icon--right"><arrow-down /></el-icon>
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
          <el-form-item :label="$t('CMS.Block.Link')" prop="url">
            <el-input v-model="formItem.url" />
          </el-form-item>
          <el-form-item :label="$t('CMS.Block.Summary')" prop="summary">
            <el-input type="textarea" v-model="formItem.summary" />
          </el-form-item>
          <el-form-item :label="$t('CMS.Block.Date')" prop="publishDate">
            <el-date-picker
              v-model="formItem.publishDate"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss">
            </el-date-picker>
          </el-form-item>
          <el-form-item label="LOGO" prop="logo">
            <cms-logo-view v-model="formItem.logo" :width="218" :height="150"></cms-logo-view>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleDialogOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleDialogClose">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
    <!-- 模板选择组件 -->
    <cms-template-selector 
      v-model:open="openTemplateSelector" 
      :publishPipeCode="templatePublishPipeCode"
      @ok="handleTemplateSelected"
      @cancel="handleTemplateSelectorCancel" />
    <!-- 栏目选择组件 -->
    <cms-catalog-selector
      v-model:open="openCatalogSelector"
      @ok="handleCatalogSelectorOk"
      @close="handleCatalogSelectorClose"></cms-catalog-selector>
    <!-- 内容选择组件 -->
    <cms-content-selector
      v-model:open="openContentSelector"
      @ok="handleContentSelectorOk"
      @close="handleContentSelectorClose"></cms-content-selector>
  </div>
</template>
<script setup name="CMSBlockManualEditor">
import { codeValidator, pathValidator } from '@/utils/validate';
import { getPublishPipeSelectData } from "@/api/contentcore/publishpipe";
import { getPageWidget, addPageWidget, editPageWidget, publishPageWidgets } from "@/api/contentcore/pagewidget";
import CmsLogoView from '@/views/cms/components/LogoView';
import CmsTemplateSelector from '@/views/cms/contentcore/templateSelector';
import CmsCatalogSelector from "@/views/cms/contentcore/catalogSelector";
import CmsContentSelector from "@/views/cms/contentcore/contentSelector";

const { proxy  } = getCurrentInstance();

const pageWidgetId = computed(() => {
  return proxy.$route.query.id;
});

const loading = ref(false);
const publishPipes = ref([]);
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
const templatePublishPipeCode = ref("");
const title = ref("");
const dialogVisible = ref(false);
const addItem = ref(false);
const current = ref(undefined);
const openCatalogSelector = ref(false);
const openContentSelector = ref(false);
const publishAfterSave = ref(false);
const initDataStr = ref(undefined);
const objects = reactive({
  form: {
    publishPipeCode: '',
    content: []
  },
  formItem: {},
});
const { form, formItem } = toRefs(objects);

onMounted(() => {
  if (pageWidgetId.value) {
    loadPublishPipes();
    loadPageWidgetInfo();
  } else {
    proxy.$modal.msgError(proxy.$t('CMS.PageWidget.InvalidPageWidgetId', [ pageWidgetId.value ]));
  }
});

function loadPublishPipes() {
  getPublishPipeSelectData().then(response => {
    publishPipes.value = response.data.rows;
  });
}
 
function loadPageWidgetInfo() {
  loading.value = true;
  getPageWidget(pageWidgetId.value).then(response => {
    form.value = response.data;
    initDataStr.value = JSON.stringify(form.value);
    loading.value = false;
  });
}

function isFormChanged() {
  return JSON.stringify(form.value) != initDataStr.value;
}

function handleSave () {
  proxy.$refs.formRef.validate(valid => { 
    if (valid) {
      form.value.contentStr = JSON.stringify(form.value.content);
      if (pageWidgetId.value) {
        editPageWidget(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.SaveSuccess'));
          initDataStr.value = JSON.stringify(form.value);
          if (publishAfterSave.value) {
            publishAfterSave.value = false;
            handlePublish();
          }
        });
      } else {
        addPageWidget(form.value).then(response => {
          proxy.$modal.msgSuccess(response.msg);
          proxy.$router.push({ path: form.value.route, query: { id: response.data } });
        });
      }
    }
  });
}

function handleSelectTemplate (code) {
  templatePublishPipeCode.value = code;
  openTemplateSelector.value = true;
}

function handleTemplateSelected (template) {
  let prop = form.value.templates.find(item => item.code == templatePublishPipeCode.value);
  if (prop) {
    prop.template = template;
  }
  openTemplateSelector.value = false;
}

function handleTemplateSelectorCancel () {
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

function handleAddRow(index) {
  form.value.content.splice(index, 0, {items:[]});
}

function handleDeleteRow(index) {
  form.value.content.splice(index, 1);
}

function handleAddItem(rowIndex, itemIndex) {
  title.value = proxy.$t('CMS.Block.AddItem');
  formItem.value = {};
  current.value = { row: rowIndex, col: itemIndex || 0 };
  addItem.value = true;
  dialogVisible.value = true;
}

function handleDialogOk() {
  if (addItem.value) {
    form.value.content[current.value.row].items.splice(current.value.col, 0, formItem.value);
  } else {
    proxy.$set(form.value.content[current.value.row].items, current.value.col, formItem.value);
  }
  dialogVisible.value = false;
}

function handleDialogClose() {
  dialogVisible.value = false;
}

function handleEditItem(rowIndex, itemIndex) {
  current.value = { row: rowIndex, col: itemIndex };
  title.value = proxy.$t('CMS.Block.EditItem');
  addItem.value = false;
  formItem.value = form.value.content[rowIndex].items[itemIndex];
  dialogVisible.value = true;
}

function handleDeleteItem(rowIndex, itemIndex) {
  form.value.content[rowIndex].items.splice(itemIndex, 1);
}

function handleClear() {
  form.value.content = [];
}

function handleLinkTo(type) {
  if (type === 'content') {
    openContentSelector.value = true;
  } else if (type === 'catalog') {
    openCatalogSelector.value = true;
  }
}

function handleContentSelectorOk(contents) {
  if (contents && contents.length > 0) {
    formItem.value = {
      title: contents[0].title,
      logo: contents[0].logo || '',
      logoSrc: contents[0].logoSrc || '',
      publishDate: contents[0].publishDate || '',
      url: contents[0].internalUrl || '',
      summary: contents[0].summary || ''
    }
    openContentSelector.value = false;
  } else {
    proxy.$modal.msgWarning(proxy.$t('Common.SelectFirst'));
  }
}

function handleContentSelectorClose() {
  openContentSelector.value = false;
}

function handleCatalogSelectorOk(args) {
  const catalogs = args[0];
  if (catalogs && catalogs.length > 0) {
    formItem.value = {
      title: catalogs[0].name,
      logo: catalogs[0].props.logo || '',
      logoSrc: catalogs[0].props.logoSrc || '',
      publishDate: '',
      url: catalogs[0].props.internalUrl || '',
      summary: catalogs[0].props.description || ''
    }
  }
  openCatalogSelector.value = false;
}

function handleCatalogSelectorClose() {
  openCatalogSelector.value = false;
}

function handleGoBack() {
  const obj = { name: "CmsContentcoreContent", params: { tab: "pageWdiget" } };
  proxy.$tab.closeOpenPage(obj);
}
</script>
<style lang="scss" scoped>
.block-manual-container {

  .form-row {
    display: inline-block;
  }

  .el-form-item {
    width: 100%;
    float: left;
  }

  .row {
    line-height: 2rem;

    .item-data {
      margin-left: 5px ;
      border-radius: 15px;
      height: 2rem;
 
      .item-op {
        .el-link {
          margin-left: 2px;

          .el-icon {
            width: 1.25rem;
            height: 1.25rem;

            svg {
              width: 100%;
              height: 100%;
            }
          }
        }
      }
    }
  }
}
</style>