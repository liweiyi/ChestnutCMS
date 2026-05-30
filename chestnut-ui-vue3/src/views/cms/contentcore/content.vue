<template>
  <div class="app-container">
    <cc-spliter storage-key="cms-content">
      <cc-spliter-column width="15%" style="padding-right: 10px;">
        <cms-catalog-tree 
          ref="catalogTreeRef"
          :new-btn="true"
          :refresh-btn="true"
          @node-click="handleTreeNodeClick">
        </cms-catalog-tree>
      </cc-spliter-column>
      <cc-spliter-column>
        <el-tabs v-model="activeName" @tab-click="handleTabClick">
          <el-tab-pane :label="$t('CMS.Content.Tab.ContentList')" name="contentList">
            <cms-content-list v-if="activeName==='contentList'" v-model:cid="selectedCatalogId"></cms-content-list>
          </el-tab-pane>
          <el-tab-pane :label="$t('CMS.Content.Tab.PageWidget')" name="pageWdiget">
            <cms-pagewidget-list v-if="activeName==='pageWdiget'" v-model:cid="selectedCatalogId"></cms-pagewidget-list>
          </el-tab-pane>
          <el-tab-pane :label="$t('CMS.Content.Tab.RecycleBin')" name="recycle">
            <cms-recycle-list v-if="activeName==='recycle'" v-model:cid="selectedCatalogId"></cms-recycle-list>
          </el-tab-pane>
        </el-tabs>
      </cc-spliter-column>
    </cc-spliter>
  </div>
</template>
<script setup name="CmsContentcoreContent">
import CmsCatalogTree from '@/views/cms/contentcore/catalogTree';
import CmsContentList from '@/views/cms/contentcore/contentList';
import CmsPagewidgetList from '@/views/cms/contentcore/pageWidget';
import CmsRecycleList from '@/views/cms/contentcore/contentRecycleList';

const { proxy } = getCurrentInstance();

const loading = ref(false);
const activeName = ref(proxy.$route.params.tab || 'contentList');
const selectedCatalogId = ref('');

function handleTabClick (tab, event) {
    }

function handleTreeNodeClick(data) {
  selectedCatalogId.value = data && data != null ? data.id : '';
}
</script>