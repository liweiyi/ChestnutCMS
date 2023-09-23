<template>
  <div class="app-container">
    <el-tabs v-model="activeName" @tab-click="handleTabClick">
      <el-tab-pane :label="$t('WordMgr.TAG.TAGWord')" name="tagWord">
        <cms-tag-word v-if="this.activeName=='tagWord'"></cms-tag-word>
      </el-tab-pane>
      <el-tab-pane :label="$t('WordMgr.HotWord.HotWord')" name="hotWord">
        <cms-hot-word v-if="this.activeName=='hotWord'"></cms-hot-word>
      </el-tab-pane>
      <el-tab-pane :label="$t('WordMgr.SensitiveWord.Word')" name="sensitiveWord">
        <cms-sensitive-word v-if="this.activeName=='sensitiveWord'"></cms-sensitive-word>
      </el-tab-pane>
      <el-tab-pane :label="$t('WordMgr.ErrorProneWord.Word')" name="errorProneWord">
        <cms-errorprone-word v-if="this.activeName=='errorProneWord'"></cms-errorprone-word>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<style scoped>
</style>
<script>
import CMSTagWord from '@/views/word/tagWord';
import CMSHotWord from '@/views/word/hotWord';
import CMSSensitiveWord from '@/views/word/sensitiveWord';
import CMSErrorProneWord from '@/views/word/errorProneWord';

export default {
  name: "CMSWordTab",
  components: {
    'cms-tag-word': CMSTagWord,
    'cms-hot-word': CMSHotWord,
    'cms-sensitive-word': CMSSensitiveWord,
    'cms-errorprone-word': CMSErrorProneWord
  },
  data () {
    return {
      // 遮罩层
      loading: false,
      activeName: 'tagWord',
    };
  },
  methods: {
    handleTabClick (tab, event) {
    }
  }
};
</script>