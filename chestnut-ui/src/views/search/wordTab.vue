<template>
  <div class="app-container">
    <el-tabs v-model="activeName" @tab-click="handleTabClick">
      <el-tab-pane :label="$t('Search.WordStat.TabName')" name="wordStat">
        <word-stat v-if="this.activeName=='wordStat'"></word-stat>
      </el-tab-pane>
      <el-tab-pane :label="$t('Search.Log.TabName')" name="searchLog">
        <search-log v-if="this.activeName=='searchLog'"></search-log>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<style scoped>
</style>
<script>
import WordStat from '@/views/search/wordStat';
import SearchLog from '@/views/search/searchLog';

export default {
  name: "SearchWordTab",
  components: {
    'word-stat': WordStat,
    'search-log': SearchLog
  },
  data () {
    return {
      // 遮罩层
      loading: false,
      activeName: 'wordStat',
    };
  },
  methods: {
    handleTabClick (tab, event) {
    }
  }
};
</script>