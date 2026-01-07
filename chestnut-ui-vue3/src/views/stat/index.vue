
<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="4" :xs="24">
        <div class="head-container">
          <el-input
            v-model="statName"
            clearable
            prefix-icon="Search"
            style="margin-bottom: 20px"
          />
        </div>
        <div class="head-container">
          <el-tree
            v-loading="loading"
            :data="statMenuTree"
            :props="defaultProps"
            :expand-on-click-node="false"
            :filter-node-method="filterNode"
            ref="treeRef"
            node-key="type"
            default-expand-all
            highlight-current
            @node-click="handleNodeClick"
          />
        </div>
      </el-col>
      <el-col :span="20" :xs="24">
        <cms-ad-stat v-if="currentMenu=='CmsAdStat'"></cms-ad-stat>
        <cms-ad-click v-if="currentMenu=='CmsAdClickLog'"></cms-ad-click>
        <cms-ad-view v-if="currentMenu=='CmsAdViewLog'"></cms-ad-view>
        <cms-bd-trend-overview v-if="currentMenu=='BdSiteTrendOverview'"></cms-bd-trend-overview>
        <cms-bd-timetrend v-if="currentMenu=='BdSiteTimeTrend'"></cms-bd-timetrend>
        <cms-bd-visit-source v-if="currentMenu=='BdSiteVisitSource'"></cms-bd-visit-source>
        <cms-bd-engine-source v-if="currentMenu=='BdSiteEngineSource'"></cms-bd-engine-source>
        <cms-bd-searchword-source v-if="currentMenu=='BdSiteSearchWordSource'"></cms-bd-searchword-source>
        <cms-content-stat v-if="currentMenu=='ContentDynamicStat'"></cms-content-stat>
        <cms-content-stat-by-catalog v-if="currentMenu=='ContentStatByCatalog'"></cms-content-stat-by-catalog>
        <cms-content-stat-by-user v-if="currentMenu=='ContentStatByUser'"></cms-content-stat-by-user>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="StatIndex">
import { getStatMenuTreeData } from "@/api/stat/stat";
import CmsAdStat from '@/views/cms/ad/adStat';
import CmsAdClick from '@/views/cms/ad/adClickLog';
import CmsAdView from '@/views/cms/ad/adViewLog';
import CmsBdVisitSource from '@/views/cms/stat/bdSiteVisitSourceStat';
import CmsBdEngineSource from '@/views/cms/stat/bdSiteEngineSourceStat';
import CmsBdSearchwordSource from '@/views/cms/stat/bdSiteSearchWordSourceStat';
import CmsBdTimetrend from '@/views/cms/stat/bdSiteTimeTrendStat';
import CmsBdTrendOverview from '@/views/cms/stat/bdSiteStat';
import CmsContentStat from '@/views/cms/stat/contentDynamicStat';
import CmsContentStatByCatalog from '@/views/cms/stat/contentStatByCatalog';
import CmsContentStatByUser from '@/views/cms/stat/contentStatByUser';

const treeRef = ref(null);
const loading = ref(true);
const statMenuTree = ref([]);
const currentMenu = ref(undefined);
const statName = ref(undefined);
const defaultProps = ref({});

watch(statName, (val) => {
  treeRef.value?.filter(val);
});

onMounted(() => {
  getStatType();
});

function getStatType() {
  loading.value = true;
  getStatMenuTreeData().then(response => {
    statMenuTree.value = response.data.treeData;
    currentMenu.value = response.data.defaultMenu
    loading.value = false;
  });
}

function filterNode(value, data) {
  if (!value) return true;
  return data.label.indexOf(value) !== -1;
}

function handleNodeClick(data) {
  currentMenu.value = data.id;
}
</script>