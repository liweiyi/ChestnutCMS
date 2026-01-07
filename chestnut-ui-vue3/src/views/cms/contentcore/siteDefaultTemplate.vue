<template>
  <div class="site-def-temp-container">
    <el-row class="mb8">
      <el-button 
        plain
        type="success"
        icon="Edit"
        :disabled="!props.site"
        v-hasPermi="[ $p('Site:Edit:{0}', [ props.site ]) ]"
        @click="handleSave">{{ $t("Common.Save") }}</el-button>
    </el-row>
    <el-form 
      ref="formRef"
      :model="form"
      v-loading="loading"
      :disabled="!props.site"
      label-width="260px">
      <el-card shadow="hover">
        <template #header>
          <span>{{ $t('CMS.Site.DefaultTemplate.Title') }}</span>
        </template>
        <el-tabs v-model="publishPipeActiveName">
          <el-tab-pane 
            v-for="pp in form.publishPipeProps"
            :key="pp.pipeCode"
            :command="pp"
            :name="pp.pipeCode"
            :label="pp.pipeName">
            <el-divider content-position="left">{{ $t('CMS.Site.DefaultTemplate.StaticizePageConfig') }}</el-divider>
            <el-form-item 
              :label="$t('CMS.Site.DefaultTemplate.CatalogList')"
              prop="listTemplate">
              <el-input class="mr5" v-model="pp.props.listTemplate">
                <template #append>
                  <el-button 
                    type="primary"
                    @click="handleSelectTemplate('listTemplate')">{{ $t("Common.Select") }}</el-button>
                </template>
              </el-input>
              <el-button 
                icon="Finished" 
                type="primary" 
                plain 
                @click="handleApplyToCatalog('listTemplate')">{{ $t('CMS.ContentCore.ApplyToCatalog') }}</el-button>
            </el-form-item>
            <el-form-item 
              v-for="ct of contentTypes" 
              :key="ct.id" 
              :command="ct"
              :label="ct.name + $t('CMS.Site.DefaultTemplate.ContentDetail')"
              :prop="`detailTemplate_${ct.id}`">
              <el-input class="mr5" v-model="pp.props[`detailTemplate_${ct.id}`]">
                <template #append>
                  <el-button type="primary" @click="handleSelectTemplate(`detailTemplate_${ct.id}`)">{{ $t("Common.Select") }}</el-button>
                </template>
              </el-input>
              <el-button 
                icon="Finished" 
                type="primary" 
                plain 
                @click="handleApplyToCatalog(`detailTemplate_${ct.id}`)">{{ $t('CMS.ContentCore.ApplyToCatalog') }}</el-button>
            </el-form-item>
            <el-form-item 
              :label="$t('CMS.Site.DefaultTemplate.CustomForm')"
              prop="defaultCustomFormTemplate">
              <el-input class="mr5" v-model="pp.props.defaultCustomFormTemplate">
                <template #append>
                  <el-button type="primary" @click="handleSelectTemplate('defaultCustomFormTemplate')">{{ $t("Common.Select") }}</el-button>
                </template>
              </el-input>
              <el-button 
                icon="Finished" 
                type="primary" 
                plain 
                @click="handleApplyToCatalog('defaultCustomFormTemplate')"
              >{{ $t('CMS.ContentCore.ApplyToCatalog') }}</el-button>
            </el-form-item>
            <el-divider content-position="left">{{ $t('CMS.Site.DefaultTemplate.DynamicPageConfig') }}</el-divider>
            <el-form-item 
              v-for="dpt in dynamicPageTypes"
              :key="dpt.type"
              :label="dpt.name"
              :prop="dpt.publishPipeKey">
              <el-input v-model="pp.props[dpt.publishPipeKey]">
                <template #append>
                  <el-button type="primary" @click="handleSelectTemplate(dpt.publishPipeKey)">{{ $t("Common.Select") }}</el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </el-form>
    <!-- 栏目选择组件 -->
    <cms-catalog-selector
      v-model:open="openCatalogSelector"
      multiple
      @ok="doApplyToCatalog"
      @close="handleCatalogSelectorClose"></cms-catalog-selector>
    <!-- 模板选择组件 -->
    <cms-template-selector 
      v-model:open="openTemplateSelector" 
      :siteId="props.site"
      :publishPipeCode="publishPipeActiveName"
      @ok="handleTemplateSelected"
      @cancel="handleTemplateSelectorCancel" />
  </div>
</template>
<script setup name="CMSSiteDefaultTemplate">
import { getDefaultTemplates, saveDefaultTemplates, applyDefaultTemplate, getDynamicPageTypes } from "@/api/contentcore/site";
import { getContentTypes } from "@/api/contentcore/catalog";
import CmsTemplateSelector from '@/views/cms/contentcore/templateSelector';
import CmsCatalogSelector from "@/views/cms/contentcore/catalogSelector";

const { proxy } = getCurrentInstance();

const props = defineProps({
  site: {
    type: String,
    default: undefined,
    required: false,
  }
});

const loading = ref(false);
const contentTypes = ref([]);
const dynamicPageTypes = ref([]);
const openCatalogSelector = ref(false);
const publishPipeActiveName = ref("");
const openTemplateSelector = ref(false);
const templatePropKey = ref("");
const form = ref({});

watch(() => props.siteId, (newVal, oldVal) => {
  if (proxy.$tools.isNotEmpty(newVal) && newVal != oldVal) {
    loadDefaultTemplates();
  }
});

onMounted(() => {
  loadContentTypes();
  loadDynamicPageTypes();
  loadDefaultTemplates();
});

const loadContentTypes = () => {
  getContentTypes().then(response => {
    contentTypes.value = response.data;
  });
};
const loadDynamicPageTypes = () => {
  getDynamicPageTypes().then(response => {
    dynamicPageTypes.value = response.data;
  });
};
const loadDefaultTemplates = () => {
  loading.value = true;
  const params = { siteId: props.site };
  getDefaultTemplates(params).then(response => {
    form.value = response.data;
    if (form.value.publishPipeProps.length > 0) {
      publishPipeActiveName.value = form.value.publishPipeProps[0].pipeCode;
    }
    loading.value = false;
  });
};
const handleSave = () => {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      saveDefaultTemplates(form.value).then(response => {
        proxy.$modal.msgSuccess(proxy.$t('SaveSuccess'));
      });
    }
  });
};
const handleSelectTemplate = (propKey) => {
  templatePropKey.value = propKey;
  openTemplateSelector.value = true;
};
const handleTemplateSelected = (template) => {
  form.value.publishPipeProps.map(item => {
    if (item.pipeCode == publishPipeActiveName.value) {
      item.props[templatePropKey.value] = template;
    }
  });
  openTemplateSelector.value = false;
};
const handleTemplateSelectorCancel = () => {
  openTemplateSelector.value = false;
};
const handleApplyToCatalog = (propKey) => {
  openCatalogSelector.value = true;
  templatePropKey.value = propKey;
};
const doApplyToCatalog = (catalogs) => {
  if (catalogs.length == 0) {
    proxy.$modal.msgWarning(proxy.$t("CMS.Site.DefaultTemplate.SelectCatalogFirst"));
    return;
  }
  let data = {
    siteId: props.site,
    toCatalogIds: catalogs.map(c => c.id)
  }
  form.value.publishPipeProps.forEach(item => {
    if (item.pipeCode == publishPipeActiveName.value) {
      data.publishPipeProps = [{ pipeCode: item.pipeCode, props: {} }];
      data.publishPipeProps[0].props[templatePropKey.value] = item.props[templatePropKey.value];
    }
  });
  applyDefaultTemplate(data).then(res => {
    proxy.$modal.msgSuccess(res.msg);
    openCatalogSelector.value = false;
  });
};
const handleCatalogSelectorClose = () => {
  openCatalogSelector.value = false;
};
</script>
<style scoped>
.site-def-temp-container .el-form-item {
  margin-bottom: 18px;
  width: 800px;
}
.site-def-temp-container .el-input, .el-select {
  width: 320px;
}
.site-def-temp-container .el-card {
  margin-bottom: 10px;
}
</style>