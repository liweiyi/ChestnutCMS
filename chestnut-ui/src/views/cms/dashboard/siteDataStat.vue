<template>
  <div class="site-container mb10">
    <el-card shadow="hover">
      <div slot="header" class="clearfix">
        <span>{{ $t('CMS.Site.Dashboard.DataCountCard') }}</span>
      </div>
      <div class="body">
        <el-row :gutter="15" class="mt10">
          <el-col :span="6">
            <el-card shadow="hover" v-loading="siteStatLoading">
              <el-row>
                <el-col :span="8">
                  <svg-icon icon-class="catalog" class-name="cc-card-panel-icon" />
                </el-col>
                <el-col :span="16">
                  <el-statistic :title="$t('CMS.Site.Dashboard.Catalog')">
                    <template slot="formatter"> {{ siteStat.catalogCount }} </template>
                  </el-statistic>
                </el-col>
              </el-row>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" v-loading="siteStatLoading">
              <el-row>
                <el-col :span="8">
                  <svg-icon icon-class="content" class-name="cc-card-panel-icon" />
                </el-col>
                <el-col :span="16">
                  <el-statistic :title="$t('CMS.Site.Dashboard.Content')">
                    <template slot="formatter"> {{ siteStat.contentCount }} </template>
                  </el-statistic>
                </el-col>
              </el-row>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" v-loading="siteStatLoading">
              <el-row>
                <el-col :span="8">
                  <svg-icon icon-class="resource" class-name="cc-card-panel-icon" />
                </el-col>
                <el-col :span="16">
                  <el-statistic :title="$t('CMS.Site.Dashboard.Resource')">
                    <template slot="formatter"> {{ siteStat.resourceCount }} </template>
                  </el-statistic>
                </el-col>
              </el-row>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" v-loading="siteStatLoading">
              <el-row>
                <el-col :span="8">
                  <svg-icon icon-class="template" class-name="cc-card-panel-icon" />
                </el-col>
                <el-col :span="16">
                  <el-statistic :title="$t('CMS.Site.Dashboard.Template')">
                    <template slot="formatter"> {{ siteStat.templateCount }} </template>
                  </el-statistic>
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
<script>
import { getSiteStatData } from "@/api/contentcore/stat";

export default {
  name: "CMSSiteStatOverview",
  data () {
    return {
      loading: false,
      siteStatLoading: false,
      siteStat: {
        catalogCount: 0,
        contentCount: 0,
        resourceCount: 0,
        templateCount: 0,
      }
    };
  },
  created() {
    this.loadSiteStatData();
  },
  methods: {
    loadSiteStatData() {
      this.siteStatLoading = true;
      getSiteStatData().then(response => {
        this.siteStatLoading = false
        this.siteStat = response.data
      })
    }
  }
};
</script>

<style lang="scss" scoped>
.cc-card-panel-icon {
  font-size: 48px;
  color: #40c9c6
}
.panel-group {
  margin-top: 18px;

  .card-panel-col {
    margin-bottom: 32px;
  }

  .card-panel {
    height: 108px;
    cursor: pointer;
    font-size: 12px;
    position: relative;
    overflow: hidden;
    color: #666;
    background: #fff;
    box-shadow: 4px 4px 40px rgba(0, 0, 0, .05);
    border-color: rgba(0, 0, 0, .05);

    &:hover {
      .card-panel-icon-wrapper {
        color: #fff;
      }

      .icon-people {
        background: #40c9c6;
      }

      .icon-message {
        background: #36a3f7;
      }

      .icon-money {
        background: #f4516c;
      }

      .icon-shopping {
        background: #34bfa3
      }
    }

    .icon-people {
      color: #40c9c6;
    }

    .icon-message {
      color: #36a3f7;
    }

    .icon-money {
      color: #f4516c;
    }

    .icon-shopping {
      color: #34bfa3
    }

    .card-panel-icon-wrapper {
      float: left;
      margin: 14px 0 0 14px;
      padding: 16px;
      transition: all 0.38s ease-out;
      border-radius: 6px;
    }

    .card-panel-icon {
      float: left;
      font-size: 48px;
    }

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