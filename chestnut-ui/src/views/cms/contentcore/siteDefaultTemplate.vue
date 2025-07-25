<template>
  <div class="site-def-temp-container">
    <el-row class="mb12">
      <el-button 
        plain
        type="success"
        icon="el-icon-edit"
        size="mini"
        :disabled="!this.siteId"
        v-hasPermi="[ $p('Site:Edit:{0}', [ siteId ]) ]"
        @click="handleSave">{{ $t("Common.Save") }}</el-button>
    </el-row>
    <el-form 
      ref="form"
      :model="form"
      v-loading="loading"
      :disabled="!this.siteId"
      label-width="260px">
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>{{ $t('CMS.Site.DefaultTemplate.Title') }}</span>
        </div>
        <el-tabs v-model="publishPipeActiveName">
          <el-tab-pane 
            v-for="pp in this.form.publishPipeProps"
            :key="pp.pipeCode"
            :command="pp"
            :name="pp.pipeCode"
            :label="pp.pipeName">
            <el-divider content-position="left">{{ $t('CMS.Site.DefaultTemplate.StaticizePageConfig') }}</el-divider>
            <el-form-item 
              :label="$t('CMS.Site.DefaultTemplate.CatalogList')"
              prop="defaultListTemplate">
              <el-input class="mr5" v-model="pp.props.defaultListTemplate">
                <el-button 
                  slot="append"
                  type="primary"
                  @click="handleSelectTemplate('defaultListTemplate')">{{ $t("Common.Select") }}</el-button>
              </el-input>
              <el-button 
                icon="el-icon-finished" 
                type="primary" 
                plain 
                @click="handleApplyToCatalog('defaultListTemplate')">{{ $t('CMS.ContentCore.ApplyToCatalog') }}</el-button>
            </el-form-item>
            <el-form-item 
              v-for="ct of contentTypes" 
              :key="ct.id" 
              :command="ct"
              :label="ct.name + $t('CMS.Site.DefaultTemplate.ContentDetail')"
              :prop="`defaultDetailTemplate_${ct.id}`">
              <el-input class="mr5" v-model="pp.props[`defaultDetailTemplate_${ct.id}`]">
                <el-button 
                  slot="append"
                  type="primary"
                  @click="handleSelectTemplate(`defaultDetailTemplate_${ct.id}`)">{{ $t("Common.Select") }}</el-button>
              </el-input>
              <el-button 
                icon="el-icon-finished" 
                type="primary" 
                plain 
                @click="handleApplyToCatalog(`defaultDetailTemplate_${ct.id}`)">{{ $t('CMS.ContentCore.ApplyToCatalog') }}</el-button>
            </el-form-item>
            <el-form-item 
              :label="$t('CMS.Site.DefaultTemplate.CustomForm')"
              prop="defaultCustomFormTemplate">
              <el-input class="mr5" v-model="pp.props.defaultCustomFormTemplate">
                <el-button 
                  slot="append"
                  type="primary"
                  @click="handleSelectTemplate('defaultCustomFormTemplate')">{{ $t("Common.Select") }}</el-button>
              </el-input>
              <el-button 
                icon="el-icon-finished" 
                type="primary" 
                plain 
                @click="handleApplyToCatalog('defaultCustomFormTemplate')">{{ $t('CMS.ContentCore.ApplyToCatalog') }}</el-button>
            </el-form-item>
            <el-divider content-position="left">{{ $t('CMS.Site.DefaultTemplate.DynamicPageConfig') }}</el-divider>
            <el-form-item 
              v-for="dpt in dynamicPageTypes"
              :key="dpt.type"
              :label="dpt.name"
              :prop="dpt.publishPipeKey">
              <el-input v-model="pp.props[dpt.publishPipeKey]">
                <el-button 
                  slot="append"
                  type="primary"
                  @click="handleSelectTemplate(dpt.publishPipeKey)">{{ $t("Common.Select") }}</el-button>
              </el-input>
            </el-form-item>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </el-form>
    <!-- 栏目选择组件 -->
    <cms-catalog-selector
      :open.sync="openCatalogSelector"
      multiple
      @ok="doApplyToCatalog"
      @close="handleCatalogSelectorClose"></cms-catalog-selector>
    <!-- 模板选择组件 -->
    <cms-template-selector 
      :open="openTemplateSelector" 
      :siteId="siteId"
      :publishPipeCode="publishPipeActiveName"
      @ok="handleTemplateSelected"
      @cancel="handleTemplateSelectorCancel" />
  </div>
</template>
<script>
import { getDefaultTemplates, saveDefaultTemplates, applyDefaultTemplate, getDynamicPageTypes } from "@/api/contentcore/site";
import { getContentTypes } from "@/api/contentcore/catalog";
import CMSTemplateSelector from '@/views/cms/contentcore/templateSelector';
import CMSCatalogSelector from "@/views/cms/contentcore/catalogSelector";

export default {
  name: "CMSSiteDefaultTemplate",
  components: {
    'cms-catalog-selector': CMSCatalogSelector,
    'cms-template-selector': CMSTemplateSelector
  },
  props: {
    site: {
      type: String,
      default: undefined,
      required: false,
    }
  },
  watch: {
    site(newVal) { 
      this.siteId = newVal;
    },
    siteId(newVal) {
      if (newVal != undefined && newVal != null && newVal.length > 0) {
        this.loadDefaultTemplates();
      }
    },
  },
  data () {
    return {
      loading: false,
      siteId: this.site,
      contentTypes: [],
      dynamicPageTypes: [],
      openCatalogSelector: false,
      publishPipeActiveName: "",
      openTemplateSelector: false,
      templatePropKey: "",
      form: {
      }
    };
  },
  created() {
    this.loadContentTypes();
    this.loadDynamicPageTypes();
    this.loadDefaultTemplates();
  },
  methods: {
    loadContentTypes() {
      getContentTypes().then(response => {
        this.contentTypes = response.data;
      });
    },
    loadDynamicPageTypes() {
      getDynamicPageTypes().then(response => {
        this.dynamicPageTypes = response.data;
      });
    },
    loadDefaultTemplates () {
      this.loading = true;
      const params = { siteId: this.siteId };
      getDefaultTemplates(params).then(response => {
        this.form = response.data;
        if (this.form.publishPipeProps.length > 0) {
          this.publishPipeActiveName = this.form.publishPipeProps[0].pipeCode;
        }
        this.loading = false;
      });
    },
    handleSave () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          saveDefaultTemplates(this.form).then(response => {
            this.$modal.msgSuccess(this.$t('SaveSuccess'));
          });
        }
      });
    },
    handleSelectTemplate(propKey) {
      this.templatePropKey = propKey;
      this.openTemplateSelector = true;
    },
    handleTemplateSelected(template) {
      this.form.publishPipeProps.map(item => {
        if (item.pipeCode == this.publishPipeActiveName) {
          item.props[this.templatePropKey] = template;
        }
      });
      this.openTemplateSelector = false;
    },
    handleTemplateSelectorCancel() {
      this.openTemplateSelector = false;
    },
    handleApplyToCatalog(propKey) {
      this.openCatalogSelector = true;
      this.templatePropKey = propKey;
    },
    doApplyToCatalog (catalogs) {
      if (catalogs.length == 0) {
        this.$modal.msgWarning(this.$t("CMS.Site.DefaultTemplate.SelectCatalogFirst"));
        return;
      }
      let data = {
        siteId: this.siteId,
        toCatalogIds: catalogs.map(c => c.id)
      }
      this.form.publishPipeProps.forEach(item => {
        if (item.pipeCode == this.publishPipeActiveName) {
          data.publishPipeProps = [{ pipeCode: item.pipeCode, props: {} }];
          data.publishPipeProps[0].props[this.templatePropKey] = "";
        }
      });
      applyDefaultTemplate(data).then(res => {
        this.$modal.msgSuccess(res.msg);
        this.openCatalogSelector = false;
      });
    },
    handleCatalogSelectorClose() {
      this.openCatalogSelector = false;
    }
  }
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