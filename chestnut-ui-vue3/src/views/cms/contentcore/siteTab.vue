<template>
  <div class="app-container">
    <el-tabs v-model="activeName" @tab-click="handleTabClick">
      <el-tab-pane :label="$t('CMS.Site.Tab.Basic')" name="basicInfo">
        <cms-site-info v-if="activeName=='basicInfo'" :site="siteId"></cms-site-info>
      </el-tab-pane>
      <el-tab-pane :label="$t('CMS.Site.Tab.Extend')" name="extends">
        <cms-site-extend v-if="activeName=='extends'" :site="siteId"></cms-site-extend>
      </el-tab-pane>
      <el-tab-pane :label="$t('CMS.Site.Tab.Property')" name="props">
        <cms-site-property v-if="activeName=='props'" :site="siteId"></cms-site-property>
      </el-tab-pane>
      <el-tab-pane :label="$t('CMS.Site.Tab.DefaultTemplate')" name="defaultTemplate">
        <cms-site-default-template v-if="activeName=='defaultTemplate'" :site="siteId"></cms-site-default-template>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<script setup name="CMSSiteTab">
import CmsSiteInfo from '@/views/cms/contentcore/siteInfo';
import CmsSiteExtend from '@/views/cms/contentcore/siteExtend';
import CmsSiteProperty from '@/views/cms/contentcore/siteProperty';
import CmsSiteDefaultTemplate from '@/views/cms/contentcore/siteDefaultTemplate';

const { proxy } = getCurrentInstance()

const activeName = ref('basicInfo')
const siteId = ref(proxy.$route.query.siteId)

const handleTabClick = (tab, event) => {
  // console.log(tab, event)
}
</script>