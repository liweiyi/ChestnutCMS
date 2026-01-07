<template>
  <div class="app-container">
    <el-tabs v-model="activeName">
      <el-tab-pane :label="$t('CMS.Staticize.Tag')" name="tag">
        <cms-template-tag></cms-template-tag>
      </el-tab-pane>
      <el-tab-pane :label="$t('CMS.Staticize.Func')" name="function">
        <cms-template-func></cms-template-func>
      </el-tab-pane>
      <el-tab-pane :label="$t('CMS.Staticize.DynamicTemplate')" name="dynamic">
        <cms-dynamic-template></cms-dynamic-template>
      </el-tab-pane>
      <el-tab-pane :label="$t('CMS.Staticize.CustomDynamicTemplate')" name="customDynamic">
        <cms-custom-dynamic-template></cms-custom-dynamic-template>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<script setup name="CmsStaticizeIndex">
import CmsTemplateTag from '@/views/cms/staticize/tagList';
import CmsTemplateFunc from '@/views/cms/staticize/funcList';
import CmsDynamicTemplate from '@/views/cms/staticize/dynamicList';
import CmsCustomDynamicTemplate from '@/views/cms/staticize/customDynamicList';

const activeName = ref('tag');
</script>
<style scoped>
.el-tabs__header {
  margin-bottom: 10px;
}
</style>