<template>
  <div class="catalog-extend-container">
    <el-row :gutter="10" class="mb12">
      <el-col :span="1.5">
        <el-button 
          plain
          type="success"
          icon="el-icon-edit"
          size="mini"
          :disabled="!this.catalogId"
          v-hasPermi="[ $p('Catalog:Edit:{0}', [ catalogId ]) ]"
          @click="handleSaveExtends">{{ $t("Common.Save") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain 
          type="primary" 
          icon="el-icon-bottom-right" 
          size="mini"
          :disabled="!this.catalogId"
          v-hasPermi="[ $p('Catalog:Edit:{0}', [ catalogId ]) ]"
          @click="handleApplyAllToCatalog()">{{ $t('CMS.Catalog.ApplyToChildren') }}</el-button>
      </el-col>
    </el-row>
    <el-form 
      class="catalog-extend-form"
      ref="form_extend"
      :model="form_extend"
      v-loading="loading"
      :disabled="!this.catalogId"
      label-width="160px">
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>{{ $t('CMS.Catalog.Extend.Basic') }}</span>
        </div>
        <el-form-item :label="$t('CMS.Catalog.Extend.EnableIndex')" prop="EnableIndex">
          <el-switch
            v-model="form_extend.EnableIndex"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.Extend.CatalogExModel')" prop="CatalogExtendModel">
          <el-select 
            v-model="form_extend.CatalogExtendModel" 
            filterable 
            clearable >
            <el-option
              v-for="item in exmodelOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
          <el-button 
            class="ml5 btn-apply-child"
            icon="el-icon-finished" 
            type="primary" 
            plain 
            @click="handleApplyToCatalog('CatalogExtendModel')">{{ $t('CMS.ContentCore.ApplyToCatalog') }}</el-button>
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.Extend.ContentExModel')" prop="ContentExtendModel">
          <el-select 
            v-model="form_extend.ContentExtendModel" 
            filterable 
            clearable>
            <el-option
              v-for="item in exmodelOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
          <el-button 
            class="ml5 btn-apply-child"
            icon="el-icon-finished" 
            type="primary" 
            plain 
            @click="handleApplyToCatalog('ContentExtendModel')">{{ $t('CMS.ContentCore.ApplyToCatalog') }}</el-button>
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.Extend.CatalogPageSize')" prop="CatalogPageSize">
          <el-input-number v-model="form_extend.CatalogPageSize" controls-position="right" :min="0"></el-input-number>
          <div style="color: #909399;font-size:12px;line-height: 30px;">
            <i class="el-icon-info mr5"></i>{{ $t('CMS.Catalog.Extend.CatalogPageSizeTip') }}
          </div>
        </el-form-item>
      </el-card>
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>{{ $t('CMS.Catalog.Extend.ContentConfig') }}</span>
        </div>
        <!-- <el-form-item 
          label="文章正文图片尺寸">
          宽：<el-input v-model="form_extend.ArticleImageWidth" style="width:100px"></el-input>
          高：<el-input v-model="form_extend.ArticleImageHeight" style="width:100px"></el-input>
        </el-form-item> -->
        <el-form-item :label="$t('CMS.Catalog.Extend.EnableContribute')" prop="EnableContribute">
          <el-switch
            v-model="form_extend.EnableContribute"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
      </el-card>
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>{{ $t('CMS.Catalog.Extend.WordConfig') }}</span>
        </div>
        <el-form-item :label="$t('CMS.Catalog.Extend.HotWordGroup')" prop="HotWordGroups">
          <el-checkbox-group v-model="form_extend.HotWordGroups">
            <el-checkbox v-for="group in hotWordGroups" :label="group.code" :key="group.code">{{ group.name }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-card>
    </el-form>
    <!-- 栏目选择组件 -->
    <cms-catalog-selector
      :open.sync="openCatalogSelector"
      multiple
      @ok="doApplyToCatalog"
      @close="handleCatalogSelectorClose"></cms-catalog-selector>
  </div>
</template>
<script>
import { getCatalogExtends, saveCatalogExtends, applyConfigPropsToChildren } from "@/api/contentcore/catalog";
import { getHotWordGroupOptions } from "@/api/contentcore/word";
import { listXModelOptions } from "@/api/contentcore/exmodel";
import CMSCatalogSelector from "@/views/cms/contentcore/catalogSelector";

export default {
  name: "CMSCatalogExtend",
  components: {
    'cms-catalog-selector': CMSCatalogSelector
  },
  props: {
    cid: {
      type: String,
      default: undefined,
      required: true,
    }
  },
  data () {
    return {
      loading: false,
      siteId: undefined,
      catalogId: this.cid,
      openCatalogSelector: false,
      applyConfigPropKey: "",
      exmodelOptions: [],
      form_extend: {
        HotWordGroups: []
      },
      hotWordGroups: []
    };
  },
  watch: {
    cid(newVal) { 
      this.catalogId = newVal;
    },
    catalogId(newVal) {
      if (newVal && newVal > 0) {
        this.loadCatalogExtends();
      } else {
        this.form_extend = { HotWordGroups: [] };
      }
    },
  },
  created() {
    this.loadEXModelList();
    this.loadHotWordGroups();
    this.loadCatalogExtends();
  },
  methods: {
    loadCatalogExtends () {
      this.loading = true;
      const params = { catalogId: this.catalogId };
      getCatalogExtends(params).then(response => {
        this.form_extend = response.data;
        this.siteId = response.data.siteId;
        this.loading = false;
      });
    },
    loadEXModelList() {
      listXModelOptions().then(response => {
        this.exmodelOptions = response.data.rows.map(m => {
          return { label: m.name, value: m.modelId };
        });
      });
    },
    loadHotWordGroups() {
      getHotWordGroupOptions().then(response => {
        this.hotWordGroups = response.data.rows;
      });
    },
    handleSaveExtends () {
      console.log("handleSaveExtends", 1)
      this.$refs["form_extend"].validate(valid => {
        if (valid) {
          const data = {};
          Object.keys(this.form_extend).forEach(key => {
            if (typeof this.form_extend[key] == 'object') {
            data[key] = JSON.stringify(this.form_extend[key]);
            } else {
              data[key] = this.form_extend[key];
            }
          })
          saveCatalogExtends(this.catalogId, data).then(response => {
            this.$modal.msgSuccess(this.$t('Common.SaveSuccess'));
          });
        }
      });
    },
    handleApplyAllToCatalog() {
      const data = { 
        catalogId: this.catalogId,
        allExtends: true
      }
      this.$modal.loading("Loading...");
      applyConfigPropsToChildren(data).then(res => {
        this.$modal.closeLoading();
        this.$modal.msgSuccess(res.msg);
      });
    },
    handleApplyToCatalog(propKey) {
      this.openCatalogSelector = true;
      this.applyConfigPropKey = propKey;
    },
    doApplyToCatalog (catalogs) {
      const data = { 
        catalogId: this.catalogId,
        toCatalogIds: catalogs.map(c => c.id),
        configPropKeys: [ this.applyConfigPropKey ]
      }
      applyConfigPropsToChildren(data).then(res => {
        this.$modal.msgSuccess(res.msg);
        this.openCatalogSelector = false;
      });
    },
    handleCatalogSelectorClose() {
      this.applyConfigPropKey = "";
      this.openCatalogSelector = false;
    }
  }
};
</script>
<style scoped>
.catalog-extend-form .el-form-item {
  margin-bottom: 12px;
  width: 700px;
}
.catalog-extend-form .el-card {
  margin-bottom: 10px;
}
.catalog-extend-form .el-input, .el-select, 
.catalog-extend-form .el-input-number  {
  width: 301.5px;
}
.catalog-extend-form .el-upload-list {
  width: 300px;
}
.catalog-extend-form .btn-apply-child {
  padding: 10px;
}
</style>