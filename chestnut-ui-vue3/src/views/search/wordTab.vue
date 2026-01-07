<template>
  <div class="app-container">
    <el-tabs v-model="activeName" @tab-click="handleTabClick">
      <el-tab-pane :label="$t('Search.WordStat.TabName')" name="wordStat">
        <word-stat v-if="activeName=='wordStat'"></word-stat>
      </el-tab-pane>
      <el-tab-pane :label="$t('Search.Log.TabName')" name="searchLog">
        <search-log v-if="activeName=='searchLog'"></search-log>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<script setup name="SearchWordTab">
import WordStat from '@/views/search/wordStat'
import SearchLog from '@/views/search/searchLog'

const activeName = ref('wordStat')

const handleTabClick = (tab, event) => {
}
</script>