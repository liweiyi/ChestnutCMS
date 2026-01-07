<template>
  <div class="site-container mb10">
    <el-card shadow="hover">
      <template #header>
        <span>{{ $t('CMS.Site.Dashboard.DataCountCard') }}</span>
      </template>
      <div class="body panel-group">
        <el-row :gutter="10">
          <el-col :xs="12" :sm="12" :xl="6" class="card-panel">
            <el-card shadow="hover" v-loading="siteStatLoading">
              <el-row>
                <el-col :span="8">
                  <svg-icon icon-class="catalog" class-name="cc-card-panel-icon" />
                </el-col>
                <el-col :span="16">
                  <el-statistic :value="siteStat.catalogCount" :title="$t('CMS.Site.Dashboard.Catalog')" />
                </el-col>
              </el-row>
            </el-card>
          </el-col>
          <el-col :xs="12" :sm="12" :xl="6" class="card-panel">
            <el-card shadow="hover" v-loading="siteStatLoading">
              <el-row>
                <el-col :span="8">
                  <svg-icon icon-class="content" class-name="cc-card-panel-icon" />
                </el-col>
                <el-col :span="16">
                  <el-statistic :value="siteStat.contentCount" :title="$t('CMS.Site.Dashboard.Content')" />
                </el-col>
              </el-row>
            </el-card>
          </el-col>
          <el-col :xs="12" :sm="12" :xl="6" class="card-panel">
            <el-card shadow="hover" v-loading="siteStatLoading">
              <el-row>
                <el-col :span="8">
                  <svg-icon icon-class="resource" class-name="cc-card-panel-icon" />
                </el-col>
                <el-col :span="16">
                  <el-statistic :value="siteStat.resourceCount" :title="$t('CMS.Site.Dashboard.Resource')" />
                </el-col>
              </el-row>
            </el-card>
          </el-col>
          <el-col :xs="12" :sm="12" :xl="6" class="card-panel">
            <el-card shadow="hover" v-loading="siteStatLoading">
              <el-row>
                <el-col :span="8">
                  <svg-icon icon-class="template" class-name="cc-card-panel-icon" />
                </el-col>
                <el-col :span="16">
                  <el-statistic :value="siteStat.templateCount" :title="$t('CMS.Site.Dashboard.Template')" />
                </el-col>
              </el-row>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>
<style scoped>
</style>
<script setup name="CMSSiteDataStat">
import { getSiteStatData } from "@/api/contentcore/stat";

const siteStatLoading = ref(false)
const siteStat = ref({
  catalogCount: 0,
  contentCount: 0,
  resourceCount: 0,
  templateCount: 0,
})

const loadSiteStatData = () => {
  siteStatLoading.value = true;
  getSiteStatData().then(response => {
    siteStatLoading.value = false
    siteStat.value.catalogCount = parseInt(response.data.catalogCount)
    siteStat.value.contentCount = parseInt(response.data.contentCount)
    siteStat.value.resourceCount = parseInt(response.data.resourceCount)
    siteStat.value.templateCount = parseInt(response.data.templateCount)
  })
}

onMounted(() => {
  loadSiteStatData();
})
</script>

<style lang="scss" scoped>
.cc-card-panel-icon {
  font-size: 48px;
  color: #40c9c6
}
.panel-group {
  margin-top: -5px;

  .card-panel {
    cursor: pointer;
    margin-top: 10px;

    .card-panel-description {
      float: right;
      font-weight: bold;
      margin: 26px;
      margin-left: 0px;

      .card-panel-text {
        line-height: 18px;
        color: rgba(0, 0, 0, 0.45);
        font-size: 16px;
        margin-bottom: 12px;
      }

      .card-panel-num {
        font-size: 20px;
      }
    }
  }
}

@media (max-width:550px) {
  .card-panel-description {
    display: none;
  }

  .card-panel-icon-wrapper {
    float: none !important;
    width: 100%;
    height: 100%;
    margin: 0 !important;

    .svg-icon {
      display: block;
      margin: 14px auto !important;
      float: none !important;
    }
  }
}
</style>