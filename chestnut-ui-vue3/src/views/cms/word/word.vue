<template>
  <div class="app-container">
    <el-tabs v-model="activeName" @tab-click="handleTabClick">
      <el-tab-pane :label="$t('WordMgr.TAG.TAGWord')" name="tagWord">
        <cms-tag-word v-if="activeName=='tagWord'"></cms-tag-word>
      </el-tab-pane>
      <el-tab-pane :label="$t('WordMgr.HotWord.HotWord')" name="hotWord">
        <cms-hot-word v-if="activeName=='hotWord'"></cms-hot-word>
      </el-tab-pane>
      <el-tab-pane :label="$t('WordMgr.SensitiveWord.Word')" name="sensitiveWord">
        <cms-sensitive-word v-if="activeName=='sensitiveWord'"></cms-sensitive-word>
      </el-tab-pane>
      <el-tab-pane :label="$t('WordMgr.ErrorProneWord.Word')" name="errorProneWord">
        <cms-error-prone-word v-if="activeName=='errorProneWord'"></cms-error-prone-word>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<script setup name="CmsWordWord">
import CmsTagWord from '@/views/cms/word/tagWord';
import CmsHotWord from '@/views/cms/word/hotWord';
import CmsSensitiveWord from '@/views/word/sensitiveWord';
import CmsErrorProneWord from '@/views/word/errorProneWord';

const activeName = ref('tagWord');
function handleTabClick (tab, event) {
  // do nothing
}
</script>