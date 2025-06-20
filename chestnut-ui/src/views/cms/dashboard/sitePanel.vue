<template>
  <div class="site-panel-container mb10">
    <el-card class="box-card" shadow="hover">
      <div class="site-wrap">
        <div class="site-info">
          <el-tag type="success">当前站点：{{ this.site.name }}</el-tag>
        </div>
        <div class="site-op">
          <el-row :gutter="10">
            <el-col :span="1.5">
              <el-button 
                plain
                type="primary"
                size="mini"
                @click="handleSitePreview">
                <svg-icon icon-class="eye-open" class="mr5"></svg-icon>站点预览
              </el-button>
            </el-col>
            <el-col :span="1.5">
              <el-dropdown class="btn-permi">
                <el-button 
                  split-button
                  plain
                  size="mini" 
                  type="primary"
                  icon="el-icon-link"
                >站点浏览<i class="el-icon-arrow-down el-icon--right"></i>
                </el-button>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item icon="el-icon-link" v-for="item in site.urls" :key="item.pipeCode" @click.native="handleSiteView(item.props.url)">{{ item.pipeName }}</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </el-col>
            <el-col :span="1.5">
              <el-button 
                plain
                type="primary"
                icon="el-icon-s-promotion"
                size="mini"
                :disabled="!this.site.siteId"
                v-hasPermi="[ $p('Site:Publish:{0}', [ site.siteId ]) ]"
                @click="handlePublish">{{ $t("CMS.Site.PublishHome") }}</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-dropdown class="btn-permi" v-hasPermi="[ $p('Site:Publish:{0}', [ site.siteId ]) ]">
                <el-button 
                  split-button
                  plain
                  size="mini" 
                  type="primary"
                  icon="el-icon-s-promotion"
                >{{ $t("CMS.Site.PublishAll") }}<i class="el-icon-arrow-down el-icon--right"></i>
                </el-button>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item icon="el-icon-s-promotion" @click.native="handlePublishAll(30)">{{ $t("CMS.Site.PublishPublished") }}</el-dropdown-item>
                  <el-dropdown-item icon="el-icon-s-promotion" @click.native="handlePublishAll(20)">{{ $t("CMS.Site.PublishToPublish") }}</el-dropdown-item>
                  <el-dropdown-item icon="el-icon-s-promotion" @click.native="handlePublishAll(0)">{{ $t("CMS.Site.PublishDraft") }}</el-dropdown-item>
                  <el-dropdown-item icon="el-icon-s-promotion" @click.native="handlePublishAll(60)">{{ $t("CMS.Site.PublishReEdit") }}</el-dropdown-item>
                  <el-dropdown-item icon="el-icon-s-promotion" @click.native="handlePublishAll(40)">{{ $t("CMS.Site.PublishOffline") }}</el-dropdown-item>
                </el-dropdown-menu>
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
<script>
import { getDashboardSiteInfo, publishSite } from "@/api/contentcore/site";
import CMSProgress from '@/views/components/Progress';

export default {
  name: "CmsSitePanelDashboard",
  components: {
    'cms-progress': CMSProgress,
  },
  data () {
    return {
      site: {},
      openProgress: false,
      progressTitle: "",
      taskId: ""
    };
  },
  created() {
    this.loadDashbordSiteInfo();
  },
  methods: {
    loadDashbordSiteInfo() {
      getDashboardSiteInfo().then(response => {
        this.site = response.data;
      })
    },
    handleSitePreview() {
      let routeData = this.$router.resolve({
        path: "/cms/preview",
        query: { type: "site", dataId: this.site.siteId },
      });
      window.open(routeData.href, '_blank');
    },
    handleSiteView(url) {
      if (!url || url.length == 0) {
        this.$modal.msgError(this.$t("CMS.Site.Dashboard.MissingSiteUrl"))
        return;
      }
      window.open(url, '_blank');
    },
    handlePublish() {
      publishSite({ siteId: this.site.siteId, publishIndex: true }).then(response => {
        if (response.code == 200) {
          this.$modal.msgSuccess(this.$t("CMS.ContentCore.PublishSuccess"));
        }
      });
    },
    handlePublishAll (status) {
      const params = { siteId: this.site.siteId, publishIndex: false, contentStatus: status }
      publishSite(params).then(response => {
        if (response.code == 200) {
          if (response.data && response.data != "") {
            this.taskId = response.data;
            this.progressTitle = this.$t('CMS.ContentCore.PublishProgressTitle');
            this.openProgress = true;
            this.$cache.local.set('publish_flag', "true")
          }
        } else {
          this.$modal.msgError(response.msg);
        }
      });
    },
    handleProgressClose(resultStatus) {
    },
  }
};
</script>
<style scoped>
.site-wrap {
  display: flex;
  justify-content: space-between;
}
</style>