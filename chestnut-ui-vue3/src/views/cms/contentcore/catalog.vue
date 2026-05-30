<template>
  <div class="app-container catalog-container">
    <cc-spliter storage-key="cms-catalog">
      <cc-spliter-column width="15%">
        <cms-catalog-tree 
          ref="catalogTreeRef"
          :new-btn="true"
          :refresh-btn="true"
          @node-click="handleTreeNodeClick">
        </cms-catalog-tree>
      </cc-spliter-column>
      <cc-spliter-column>
        <el-tabs v-model="activeName" @tab-click="handleTabClick">
          <el-tab-pane :label="$t('CMS.Catalog.Tab.Basic')" name="basicInfo">
            <cms-catalog-info 
              v-if="activeName=='basicInfo'" 
              :cid="selectedCatalogId"
              @reload="handleCatalogReload"
            ></cms-catalog-info>
          </el-tab-pane>
          <el-tab-pane :label="$t('CMS.Catalog.Tab.Extend')" name="extend" :disabled="!selectedCatalog">
            <cms-catalog-extend v-if="activeName=='extend'" :cid="selectedCatalogId"></cms-catalog-extend>
          </el-tab-pane>
        </el-tabs>
      </cc-spliter-column>
    </cc-spliter>
  </div>
</template>
<script setup name="CmsContentcoreCatalog">
import CmsCatalogTree from '@/views/cms/contentcore/catalogTree';
import CmsCatalogInfo from '@/views/cms/contentcore/catalogInfo';
import CmsCatalogExtend from '@/views/cms/contentcore/catalogExtend';

const { proxy } = getCurrentInstance();

const activeName = ref('basicInfo');
const selectedCatalogId = ref('');

const selectedCatalog = computed(() => {
  return selectedCatalogId.value && selectedCatalogId.value.length > 0;
});

const handleTabClick = (tab, event) => {
  // console.log(tab, event)
};

const handleCatalogReload = () => {
  proxy.$refs.catalogTreeRef.loadCatalogTreeData();
};

const handleTreeNodeClick = (data) => {
  selectedCatalogId.value = data && data != null ? data.id : "";
};
</script>
<style scoped>
.catalog-container .el-tabs__header {
  margin-bottom: 10px;
}
</style>