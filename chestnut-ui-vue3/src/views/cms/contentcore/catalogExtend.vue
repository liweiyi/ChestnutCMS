<template>
  <div class="catalog-extend-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5" class="permi-wrap">
        <el-button 
          plain
          type="success"
          icon="Edit"
          :disabled="!props.cid"
          v-hasPermi="[ $p('Catalog:Edit:{0}', [ catalogId ]) ]"
          @click="handleSaveExtends">{{ $t("Common.Save") }}</el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button 
          plain 
          type="primary" 
          icon="BottomRight" 
          :disabled="!props.cid"
          v-hasPermi="[ $p('Catalog:Edit:{0}', [ catalogId ]) ]"
          @click="handleApplyAllToCatalog()">{{ $t('CMS.Catalog.ApplyToChildren') }}</el-button>
      </el-col>
    </el-row>
    <el-form 
      class="catalog-extend-form"
      ref="formExtendRef"
      :model="formExtend"
      v-loading="loading"
      :disabled="!props.cid"
      label-width="160px">
      <el-card shadow="hover">
        <template #header>
          <span>{{ $t('CMS.Catalog.Extend.Basic') }}</span>
        </template>
        <el-form-item :label="$t('CMS.Catalog.Extend.EnableIndex')" prop="EnableIndex">
          <el-switch
            v-model="formExtend.EnableIndex"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.Extend.CatalogExModel')" prop="CatalogExtendModel">
          <el-select 
            class="mr5"
            v-model="formExtend.CatalogExtendModel" 
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
            class="btn-apply-child"
            icon="Finished" 
            type="primary" 
            plain 
            @click="handleApplyToCatalog('CatalogExtendModel')">{{ $t('CMS.ContentCore.ApplyToCatalog') }}</el-button>
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.Extend.ContentExModel')" prop="ContentExtendModel">
          <el-select 
            class="mr5"
            v-model="formExtend.ContentExtendModel" 
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
            class="btn-apply-child"
            icon="Finished" 
            type="primary" 
            plain 
            @click="handleApplyToCatalog('ContentExtendModel')">{{ $t('CMS.ContentCore.ApplyToCatalog') }}</el-button>
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.Extend.CatalogPageSize')" prop="CatalogPageSize">
          <el-input-number v-model="formExtend.CatalogPageSize" controls-position="right" :min="0"></el-input-number>
          <div style="color: #909399;font-size:12px;line-height: 30px;">
            <InfoFilled class="ml5" style="width: var(--font-size);" />{{ $t('CMS.Catalog.Extend.CatalogPageSizeTip') }}
          </div>
        </el-form-item>
      </el-card>
      <el-card shadow="hover">
        <template #header>
          <span>{{ $t('CMS.Catalog.Extend.ContentConfig') }}</span>
        </template>
        <!-- <el-form-item 
          label="文章正文图片尺寸">
          宽：<el-input v-model="formEextend.ArticleImageWidth" style="width:100px"></el-input>
          高：<el-input v-model="formExtend.ArticleImageHeight" style="width:100px"></el-input>
        </el-form-item> -->
        <el-form-item :label="$t('CMS.Catalog.Extend.EnableContribute')" prop="EnableContribute">
          <el-switch
            v-model="formExtend.EnableContribute"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Extend.ShortTitleLabel')" prop="ShortTitleLabel">
          <el-input class="mr5" v-model="formExtend.ShortTitleLabel"></el-input>
          <el-button 
            class="btn-apply-child"
            icon="Finished" 
            type="primary" 
            plain 
            @click="handleApplyToChildren('ShortTitleLabel')">{{ $t('CMS.Catalog.ApplyToChildren') }}</el-button>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Extend.SubTitleLabel')" prop="SubTitleLabel">
          <el-input class="mr5" v-model="formExtend.SubTitleLabel"></el-input>
          <el-button 
            class="btn-apply-child"
            icon="Finished" 
            type="primary" 
            plain 
            @click="handleApplyToChildren('SubTitleLabel')">{{ $t('CMS.Catalog.ApplyToChildren') }}</el-button>
        </el-form-item>
      </el-card>
      <el-card shadow="hover">
        <template #header>
          <span>{{ $t('CMS.Catalog.Extend.WordConfig') }}</span>
        </template>
        <el-form-item :label="$t('CMS.Catalog.Extend.HotWordGroup')" prop="HotWordGroups">
          <el-checkbox-group v-model="formExtend.HotWordGroups">
            <el-checkbox v-for="group in hotWordGroups" :label="group.value" :key="group.value">{{ group.label }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.Extend.HotWordMaxReplaceCount')" prop="HotWordMaxReplaceCount">
          <el-input-number v-model="formExtend.HotWordMaxReplaceCount" controls-position="right" :min="0"></el-input-number>
          <el-tooltip
            :content="$t('CMS.Catalog.Extend.HotWordMaxReplaceCountTip')"
            placement="right"
          ><InfoFilled class="ml5" style="width: var(--font-size);" /></el-tooltip>
        </el-form-item>
      </el-card>
    </el-form>
    <!-- 栏目选择组件 -->
    <cms-catalog-selector
      v-model:open="openCatalogSelector"
      multiple
      @ok="doApplyToCatalog"
      @close="handleCatalogSelectorClose"></cms-catalog-selector>
  </div>
</template>
<script setup name="CMSCatalogExtend">
import * as catalogApi from "@/api/contentcore/catalog";
import * as wordApi from "@/api/contentcore/word";
import * as exmodelApi from "@/api/contentcore/exmodel";
import CmsCatalogSelector from "@/views/cms/contentcore/catalogSelector";

const { proxy } = getCurrentInstance();

const props = defineProps({
  cid: {
    type: String,
    default: undefined,
    required: true,
  }
});

const loading = ref(false);
const siteId = ref(undefined);
const catalogId = ref(props.cid);
const openCatalogSelector = ref(false);
const applyConfigPropKey = ref("");
const exmodelOptions = ref([]);
const hotWordGroups = ref([]);
const objects = reactive({
  formExtend: { HotWordGroups: [] }
});
const { formExtend } = toRefs(objects);

watch(() => props.cid, (newVal) => {
  if (proxy.$tools.isNotEmpty(newVal)) {
    loadCatalogExtends();
  } else {
    formExtend.value = { HotWordGroups: [] };
  }
});

onMounted(() => {
  loadEXModelList();
  loadHotWordGroups();
  loadCatalogExtends();
});

const loadCatalogExtends = () => {
  loading.value = true;
  const params = { catalogId: props.cid };
  catalogApi.getCatalogExtends(params).then(response => {
    formExtend.value = response.data;
    siteId.value = response.data.siteId;
    loading.value = false;
  });
}

const loadEXModelList = () => {
  exmodelApi.listXModelOptions().then(response => {
    exmodelOptions.value = response.data.rows.map(m => {
      return { label: m.name, value: m.modelId };
    });
  });
}

const loadHotWordGroups = () => {
  wordApi.getHotWordGroupOptions().then(response => {
    hotWordGroups.value = response.data;
  });
}

const handleSaveExtends = () => {
  proxy.$refs.formExtendRef.validate(valid => {
    if (valid) {
      const data = {};
      Object.keys(formExtend.value).forEach(key => {
        if (typeof formExtend.value[key] == 'object') {
        data[key] = JSON.stringify(formExtend.value[key]);
        } else {
          data[key] = formExtend.value[key];
        }
      })
      catalogApi.saveCatalogExtends(props.cid, data).then(response => {
        proxy.$modal.msgSuccess(proxy.$t('Common.SaveSuccess'));
      });
    }
  });
}

const handleApplyAllToCatalog = () => {
  const data = { 
    catalogId: props.cid,
    allExtends: true
  }
  proxy.$modal.loading("Loading...");
  catalogApi.applyConfigPropsToChildren(data).then(res => {
    proxy.$modal.closeLoading();
    proxy.$modal.msgSuccess(res.msg);
  });
}

const handleApplyToCatalog = (propKey) => {
      openCatalogSelector.value = true;
      applyConfigPropKey.value = propKey;
};

const doApplyToCatalog = (catalogs) => {
  const data = { 
    catalogId: props.cid,
    toCatalogIds: catalogs.map(c => c.id),
    configPropKeys: [ applyConfigPropKey.value ]
  }
  catalogApi.applyConfigPropsToChildren(data).then(res => {
    proxy.$modal.msgSuccess(res.msg);
    openCatalogSelector.value = false;
  });
};

const handleCatalogSelectorClose = () => {
  applyConfigPropKey.value = "";
  openCatalogSelector.value = false;
};

const handleApplyToChildren = (propKey) => {
  const data = { 
    catalogId: props.cid,
    configPropKeys: [ propKey ]
  }
  catalogApi.applyConfigPropsToChildren(data).then(res => {
    proxy.$modal.msgSuccess(res.msg);
  });
}
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