
<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="4" :xs="24">
        <div class="head-container">
          <el-input
            v-model="statName"
            clearable
            size="small"
            prefix-icon="el-icon-search"
            style="margin-bottom: 20px"
          />
        </div>
        <div class="head-container">
          <el-tree
            :data="statMenuTree"
            :props="defaultProps"
            :expand-on-click-node="false"
            :filter-node-method="filterNode"
            ref="tree"
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

<script>
import { getStatMenuTreeData } from "@/api/stat/stat";
import CMSAdvertisementStat from '@/views/cms/ad/adStat';
import CMSAdvertisementClickLog from '@/views/cms/ad/adClickLog';
import CMSAdvertisementViewLog from '@/views/cms/ad/adViewLog';
import CMSBdSiteVisitSource from '@/views/cms/stat/bdSiteVisitSourceStat';
import CMSBdSiteEngineSource from '@/views/cms/stat/bdSiteEngineSourceStat';
import CMSBdSiteSearchWordSource from '@/views/cms/stat/bdSiteSearchWordSourceStat';
import CMSBdSiteTimeTrend from '@/views/cms/stat/bdSiteTimeTrendStat';
import CMSBdSiteTrendOverview from '@/views/cms/stat/bdSiteStat';
import CMSContentDynamicStat from '@/views/cms/stat/contentDynamicStat';
import ContentStatByCatalog from '@/views/cms/stat/contentStatByCatalog';
import ContentStatByUser from '@/views/cms/stat/contentStatByUser';

import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";

export default {
  name: "StatIndex",
  components: { 
    Treeselect,
    'cms-ad-stat': CMSAdvertisementStat,
    'cms-ad-click': CMSAdvertisementClickLog,
    'cms-ad-view': CMSAdvertisementViewLog,
    'cms-bd-trend-overview': CMSBdSiteTrendOverview,
    'cms-bd-timetrend': CMSBdSiteTimeTrend,
    'cms-bd-visit-source': CMSBdSiteVisitSource,
    'cms-bd-engine-source': CMSBdSiteEngineSource,
    'cms-bd-searchword-source': CMSBdSiteSearchWordSource,
    'cms-content-stat': CMSContentDynamicStat,
    'cms-content-stat-by-catalog': ContentStatByCatalog,
    'cms-content-stat-by-user': ContentStatByUser,
  },
  data() {
    return {
      loading: true,
      statMenuTree: [],
      currentMenu: undefined,
      statName: undefined,
      defaultProps: {}
    };
  },
  watch: {
    statName(val) {
      this.$refs.tree.filter(val);
    }
  },
  created() {
    this.getStatType();
  },
  methods: {
    getStatType() {
      getStatMenuTreeData().then(response => {
        this.statMenuTree = response.data.treeData;
        this.currentMenu = response.data.defaultMenu
      });
    },
    filterNode(value, data) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
    },
    handleNodeClick(data) {
      this.currentMenu = data.id;
    }
  }
};
</script>