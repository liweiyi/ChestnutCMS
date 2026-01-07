<template>
  <div class="site-panel-container">
    <el-card class="box-card" shadow="hover">
      <div class="site-wrap">
        <div class="site-info">
          <el-tag :size="size" type="success">{{ site.name }}</el-tag>
        </div>
        <div class="site-op">
          <el-row :gutter="10">
            <el-col :span="1.5">
              <el-button 
                plain
                type="primary"
                :size="size"
                @click="handleSitePreview">
                <svg-icon icon-class="eye-open" class="mr5"></svg-icon>{{ $t("CMS.ContentCore.Preview") }}
              </el-button>
            </el-col>
            <el-col :span="1.5">
              <el-dropdown class="btn-permi" @command="handleSiteView">
                <el-button 
                  split-button
                  plain
                  :size="size" 
                  type="primary"
                  icon="Link"
                >{{ $t("CMS.Site.View") }}<el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item icon="Link" v-for="item in site.urls" :key="item.pipeCode" :command="item.props.url">{{ item.pipeName }}</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </el-col>
            <el-col :span="1.5">
              <el-button 
                plain
                type="primary"
                icon="Promotion"
                :size="size"
                :disabled="!site.siteId"
                v-hasPermi="[ $p('Site:Publish:{0}', [ site.siteId ]) ]"
                @click="handlePublish">{{ $t("CMS.Site.PublishHome") }}</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-dropdown 
                class="btn-permi" 
                v-hasPermi="[ $p('Site:Publish:{0}', [ site.siteId ]) ]"
                @command="handlePublishAll"
              >
                <el-button 
                  split-button
                  plain
                  :size="size" 
                  type="primary"
                  icon="Promotion"
                >{{ $t("CMS.Site.PublishAll") }}<el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item icon="Promotion" command="30">{{ $t("CMS.Site.PublishPublished") }}</el-dropdown-item>
                    <el-dropdown-item icon="Promotion" command="20">{{ $t("CMS.Site.PublishToPublish") }}</el-dropdown-item>
                    <el-dropdown-item icon="Promotion" command="0">{{ $t("CMS.Site.PublishDraft") }}</el-dropdown-item>
                    <el-dropdown-item icon="Promotion" command="60">{{ $t("CMS.Site.PublishReEdit") }}</el-dropdown-item>
                    <el-dropdown-item icon="Promotion" command="40">{{ $t("CMS.Site.PublishOffline") }}</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-card>
    <!-- 进度条 -->
    <cms-progress :title="progressTitle" :open.sync="openProgress" :taskId.sync="taskId" @close="handleProgressClose"></cms-progress>
  </div>
</template>
<script setup name="CMSSitePanel">
import useAppStore from "@/store/modules/app"
const appStore = useAppStore()
const size = computed(() => appStore.size)

import { useRouter } from 'vue-router'
import { getDashboardSiteInfo, publishSite } from "@/api/contentcore/site";
import CmsProgress from '@/views/components/Progress';

const router = useRouter()
const { proxy } = getCurrentInstance();

const site = ref({})
const openProgress = ref(false)
const progressTitle = ref("")
const taskId = ref("")

const loadDashbordSiteInfo = () => {
  getDashboardSiteInfo().then(response => {
    site.value = response.data;
  })
}

const handleSitePreview = () => {
  let routeData = router.resolve({
    path: "/cms/preview",
    query: { type: "site", dataId: site.value.siteId },
  });
  window.open(routeData.href, '_blank');
}

const handleSiteView = (url) => {
  if (!url || url.length == 0) {
    proxy.$modal.msgError(proxy.$t("CMS.Site.Dashboard.MissingSiteUrl"))
    return;
  }
  window.open(url, '_blank');
}

const handlePublish = () => {
  publishSite({ siteId: site.value.siteId, publishIndex: true }).then(response => {
    if (response.code == 200) {
      proxy.$modal.msgSuccess(proxy.$t("CMS.ContentCore.PublishSuccess"));
    }
  });
}

const handlePublishAll = (status) => {
  const params = { siteId: site.value.siteId, publishIndex: false, contentStatus: status }
  publishSite(params).then(response => {
    if (response.code == 200) {
      if (response.data && response.data != "") {
        taskId.value = response.data;
        progressTitle.value = proxy.$t('CMS.ContentCore.PublishProgressTitle');
        openProgress.value = true;
        proxy.$cms.setPublishFlag("true")
      }
    } else {
      proxy.$modal.msgError(response.msg);
    }
  });
}

const handleProgressClose = (resultStatus) => {
}

onMounted(() => {
  loadDashbordSiteInfo();
})
</script>
<style scoped>
.site-panel-container {
  margin-bottom: 10px;
  
  .site-wrap {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  :deep(.el-card__body) {
    padding-bottom: 15px !important;
  }
}
</style>
