<template>
  <div class="app-container">
    <el-tabs v-model="activeName">
      <el-tab-pane :label="$t('CMS.Staticize.Tag')" name="tag">
        <cms-template-tag></cms-template-tag>
      </el-tab-pane>
      <el-tab-pane :label="$t('CMS.Staticize.Func')" name="function">
        <cms-template-function></cms-template-function>
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
<script>
import CMSTemplateTag from '@/views/cms/staticize/tagList';
import CMSTemplateFunc from '@/views/cms/staticize/funcList';
import CMSDynamicTemplate from '@/views/cms/staticize/dynamicList';
import CMSCustomDynamicTemplate from '@/views/cms/staticize/customDynamicList';

export default {
  name: "CmsStaticizeIndex",
  components: {
    'cms-template-tag': CMSTemplateTag,
    'cms-template-function': CMSTemplateFunc,
    'cms-dynamic-template': CMSDynamicTemplate,
    'cms-custom-dynamic-template': CMSCustomDynamicTemplate,
  },
  data () {
    return {
      activeName: 'tag'
    };
  }
};
</script>
<style scoped>
.el-tabs__header {
  margin-bottom: 10px;
}
</style>