<template>
  <div class="app-container">
    <el-row :gutter="20" v-loading="loading">
      <el-col :span="4" :xs="24">
        <cms-catalog-tree 
          ref="catalogTree"
          :new-btn="true"    
          @node-click="handleTreeNodeClick">
        </cms-catalog-tree>
      </el-col>
      <el-col :span="20" :xs="24">
        <el-tabs v-model="activeName" @tab-click="handleTabClick">
          <el-tab-pane :label="$t('CMS.Content.Tab.ContentList')" name="contentList">
              <cms-content-list v-if="activeName==='contentList'" :cid="selectedCatalogId"></cms-content-list>
          </el-tab-pane>
          <el-tab-pane :label="$t('CMS.Content.Tab.PageWidget')" name="pageWdiget">
              <cms-pagewidget-list v-if="activeName==='pageWdiget'" :cid="selectedCatalogId"></cms-pagewidget-list>
          </el-tab-pane>
          <el-tab-pane :label="$t('CMS.Content.Tab.RecycleBin')" name="recycle">
            <cms-recycle-list v-if="activeName==='recycle'" :cid="selectedCatalogId"></cms-recycle-list>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>
  </div>
</template>
<script>
import CMSCatalogTree from '@/views/cms/contentcore/catalogTree';
import CMSContentList from '@/views/cms/contentcore/contentList';
import CMSPageWidget from '@/views/cms/contentcore/pageWidget';
import CMSContentRecycleList from '@/views/cms/contentcore/contentRecycleList';

export default {
  name: "CmsContentcoreContent",
  components: {
    'cms-catalog-tree': CMSCatalogTree,
    'cms-content-list': CMSContentList,
    'cms-pagewidget-list': CMSPageWidget,
    'cms-recycle-list': CMSContentRecycleList
  },
  data () {
    return {
      loading: false,
      activeName: this.$route.params.tab || 'contentList',
      selectedCatalogId: '',
    };
  },
  methods: {
    handleTabClick (tab, event) {
    },
    handleTreeNodeClick(data) {
      this.selectedCatalogId = data && data != null ? data.id : '';
    }
  }
};
</script>