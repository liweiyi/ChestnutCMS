<template>
  <div class="app-container catalog-container">
    <el-row :gutter="20">
      <!--栏目数据-->
      <el-col :span="4" :xs="24">
        <cms-catalog-tree 
          ref="catalogTree"
          :new-btn="true"    
          @node-click="handleTreeNodeClick">
        </cms-catalog-tree>
      </el-col>
      <!--栏目数据-->
      <el-col :span="20" :xs="24">
        <el-tabs v-model="activeName" @tab-click="handleTabClick">
          <el-tab-pane :label="$t('CMS.Catalog.Tab.Basic')" name="basicInfo">
            <cms-catalog-info 
              v-if="this.activeName=='basicInfo'" 
              :cid="selectedCatalogId"
              @reload="handleCatalogReload"
            ></cms-catalog-info>
          </el-tab-pane>
          <el-tab-pane :label="$t('CMS.Catalog.Tab.Extend')" name="extend" :disabled="!selectedCatalog">
            <cms-catalog-extend v-if="this.activeName=='extend'" :cid="selectedCatalogId"></cms-catalog-extend>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>
  </div>
</template>
<script>
import CMSCatalogTree from '@/views/cms/contentcore/catalogTree';
import CMSCatalogInfo from '@/views/cms/contentcore/catalogInfo';
import CMSCatalogExtend from '@/views/cms/contentcore/catalogExtend';

export default {
  name: "CmsContentcoreCatalog",
  components: {
    'cms-catalog-tree': CMSCatalogTree,
    'cms-catalog-info': CMSCatalogInfo,
    'cms-catalog-extend': CMSCatalogExtend
  },
  data () {
    return {
      activeName: 'basicInfo',
      selectedCatalogId: undefined
    };
  },
  watch: {
    filterCatalogName(val) {
      this.$refs.tree.filter(val);
    }
  },
  computed: {
    selectedCatalog() {
      return this.selectedCatalogId && this.selectedCatalogId.length > 0
    }
  },
  methods: {
    handleTabClick (tab, event) {
    },
    handleCatalogReload() {
      this.$refs.catalogTree.loadCatalogTreeData();
    },
    handleTreeNodeClick(data) {
      this.selectedCatalogId = data && data != null ? data.id : "";
    }
  }
};
</script>
<style scoped>
.catalog-container .el-tabs__header {
  margin-bottom: 10px;
}
</style>