<template>
  <div class="bdsite-container">
    <el-row :gutter="20" class="panel-group">
      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div v-loading="loading" class="card-panel" @click="handleSetLineChartData('pv')">
          <div class="card-panel-icon-wrapper icon-people">
            <svg-icon icon-class="server" class-name="card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">
              {{ $t('Stat.Site.PageView') }}
            </div>
            {{ sum.pv }}
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div v-loading="loading" class="card-panel" @click="handleSetLineChartData('uv')">
          <div class="card-panel-icon-wrapper icon-people">
            <svg-icon icon-class="peoples" class-name="card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">
              {{ $t('Stat.Site.UserView') }}
            </div>
            {{ sum.uv }}
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div v-loading="loading" class="card-panel" @click="handleSetLineChartData('ip')">
          <div class="card-panel-icon-wrapper icon-people">
            <svg-icon icon-class="international" class-name="card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">
              {{ $t('Stat.Site.IPView') }}
            </div>
            {{ sum.ip }}
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div v-loading="loading" class="card-panel" @click="handleSetLineChartData('avgVisitTime')">
          <div class="card-panel-icon-wrapper icon-people">
            <svg-icon icon-class="time-range" class-name="card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">
              {{ $t('Stat.Site.AvgVisitTime') }}
            </div>
            {{ sum.avgVisitTime }} {{ $t('Stat.Site.UnitSecond') }}
          </div>
        </div>
      </el-col>
    </el-row>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-select v-model="queryParams.bdSiteId" :placeholder="$t('Stat.Site.SelectSite')" style="width: 260px">
          <el-option
            v-for="site in siteOptions"
            :key="site.site_id"
            :label="site.domain"
            :value="site.site_id"
          />
        </el-select>
      </el-col>
      <el-col :span="1.5">
        <el-date-picker
          v-model="dateRange"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          unlink-panels
          range-separator="-"
          :clearable="false"
          :shortcuts="shortcuts"
          style="width:235px"
        ></el-date-picker>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          type="primary"
          icon="Search"
          @click="handleQuery">{{ $t("Common.Search") }}</el-button>
      </el-col>
    </el-row>
    <el-card v-loading="loading" shadow="hover" class="mb8">
      <template #header>
        <div class="clearfix">
          <span>{{ $t('Stat.Site.VisitTrend') }}</span>
        </div>
      </template>
      <line-chart :chart-data="lineChartData" />
    </el-card>
    
    <el-row :gutter="10" class="mb8">
      <el-col :span="12">
        <el-card v-loading="loading" shadow="hover" class="mb8">
          <template #header>
            <div class="clearfix">
              <span>{{ $t('Stat.Site.Top10EntryPage') }}</span>
            </div>
          </template>
          <el-table v-loading="loadingOther" :data="top10LandingPage" height="405">
            <el-table-column 
              :label="$t('Stat.Site.URL')"
              align="left"
              prop="label" />
            <el-table-column 
              label="PV"
              align="center"
              width="100"
              prop="pv_count" />
            <el-table-column 
              :label="$t('Stat.Site.Ratio')"
              align="right"
              width="80"
              prop="ratio">
              <template #default="scope">
                {{ scope.row.ratio }} %
              </template>
            </el-table-column>
          </el-table>
        </el-card>
        <el-card v-loading="loading" shadow="hover" class="mb8">
          <template #header>
            <div class="clearfix">
              <span>{{ $t('Stat.Site.Top10SourceSite') }}</span>
            </div>
          </template>
          <el-table v-loading="loadingOther" :data="top10SourceSite" height="405">
            <el-table-column 
              :label="$t('Stat.Site.SourceSite')"
              align="left"
              prop="label" />
            <el-table-column 
              label="PV"
              align="center"
              width="100"
              prop="pv_count" />
            <el-table-column 
              :label="$t('Stat.Site.Ratio')"
              align="right"
              width="80"
              prop="ratio">
              <template #default="scope">
                {{ scope.row.ratio }} %
              </template>
            </el-table-column>
          </el-table>
        </el-card>
        <el-card v-loading="loading" shadow="hover">
          <template #header>
            <div class="clearfix">
              <span>{{ $t('Stat.Site.Top10VisitPage') }}</span>
            </div>
          </template>
          <el-table v-loading="loadingDistrict" :data="top10VisitPage" height="405">
            <el-table-column 
              label="URL"
              align="left"
              prop="label" />
            <el-table-column 
              label="PV"
              align="center"
              width="100"
              prop="pv_count" />
            <el-table-column 
              :label="$t('Stat.Site.Ratio')"
              align="center"
              width="80"
              prop="ratio">
              <template #default="scope">
                {{ scope.row.ratio }} %
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="mb8">
          <template #header>
            <div class="clearfix">
              <span>{{ $t('Stat.Site.NewOldVisitor') }}</span>
            </div>
          </template>
          <el-table v-loading="loadingOther" :data="newOldVisitor" height="260">
            <el-table-column 
              :label="$t('Stat.Site.Metrics')"
              align="left"
              prop="label" />
            <el-table-column 
              :label="$t('Stat.Site.NewVisitor')"
              align="left"
              prop="newVisitorData" />
            <el-table-column 
              :label="$t('Stat.Site.OldVisitor')"
              align="left"
              prop="oldVisitorData">
            </el-table-column>
          </el-table>
        </el-card>
        <el-card shadow="hover">
          <template #header>
            <div class="clearfix">
              <span>{{ $t('Stat.Site.VisitLocation') }}</span>
            </div>
          </template>
          <el-table v-loading="loadingDistrict" :data="districtList">
            <el-table-column 
              :label="$t('Stat.Site.Location')"
              align="center"
              prop="name" />
            <el-table-column 
              label="PV"
              align="center"
              prop="pv_count" />
            <el-table-column 
              :label="$t('Stat.Site.Ratio')"
              align="center"
              prop="ratio">
              <template #default="scope">
                {{ scope.row.ratio }} %
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
<style scoped>
</style>
<script setup name="CMSSiteBdTrendOverview">
import * as baiduTongjiApi from "@/api/stat/baidu";
import LineChart from '../../dashboard/LineChart'

const { proxy } = getCurrentInstance();

const loading = ref(false);
const queryParams = reactive({
  bdSiteId: undefined,
  startDate: undefined,
  endDate: undefined
});

const shortcuts = [
  {
    text: proxy.$t('Common.LastWeek'),
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
      return [start, end];
    }
  },
  {
    text: proxy.$t('Common.LastMonth'),
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
      return [start, end];
    }
  },
  {
    text: proxy.$t('Common.LastThreeMonth'),
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
      return [start, end];
    }
  }
];

const dateRange = ref([]);
const siteOptions = ref([]);
const lineChartData = ref({
  xAxisDatas:[],
  datas: []
});
const sum = ref({
  pv: 0,
  uv: 0,
  ip: 0,
  avgVisitTime: 0
});
const loadingDistrict = ref(false);
const districtList = ref([]);
const loadingOther = ref(false);
const top10LandingPage = ref([]);
const top10SourceSite = ref([]);
const top10VisitPage = ref([]);
const newOldVisitor = ref([]);

function loadSiteList() {
  baiduTongjiApi.getSiteList().then(response => {
    siteOptions.value = response.data || [];
    if (siteOptions.value.length > 0) {
      queryParams.bdSiteId = siteOptions.value[0].site_id;
      loadSiteTrendOverviewDatas();
      loadSiteDistrictOverviewDatas();
      loadSiteOtherOverviewDatas();
    }
  });
}

function loadSiteTrendOverviewDatas() {
  if (siteOptions.value.length == 0) {
    proxy.$modal.msgWarning(proxy.$t('Stat.Site.NoSite'));
    return;
  }
  loading.value = true;
  queryParams.startDate = dateRange.value[0];
  queryParams.endDate = dateRange.value[1];
  baiduTongjiApi.getSiteTrendOverviewDatas(queryParams).then(response => {
    lineChartData.value.xAxisDatas = response.data.xaxisDatas;
    lineChartData.value.datas = response.data.datas;
    sum.value = { pv: 0, uv: 0, ip: 0, avgVisitTime: 0 };
    lineChartData.value.datas.pv_count.forEach(v => sum.value.pv+=v);
    lineChartData.value.datas.ip_count.forEach(v => sum.value.ip+=v);
    lineChartData.value.datas.visitor_count.forEach(v => sum.value.uv+=v);
    lineChartData.value.datas.avg_visit_time.forEach(v => sum.value.avgVisitTime+=v);
    sum.value.avgVisitTime = Math.round(sum.value.avgVisitTime / lineChartData.value.datas.avg_visit_time.length);
    loading.value = false;
  });
}

function loadSiteDistrictOverviewDatas() {
  if (siteOptions.value.length == 0) {
    proxy.$modal.msgWarning(proxy.$t('Stat.Site.NoSite'));
    return;
  }
  loadingDistrict.value = true;
  queryParams.startDate = dateRange.value[0];
  queryParams.endDate = dateRange.value[1];
  baiduTongjiApi.getSiteDistrictOverviewDatas(queryParams).then(response => {
    let rows = response.data.xaxisList.map(item => {
      let row = { name: item.xaxis }
      item.yaxisDataList.forEach(y => {
        row[y.yaxis] = y.value
      })
      return row;
    })
    rows.sort((a, b) => {
      let c = b.pv_count - a.pv_count
      return c > 0 ? 1 : (c < 0 ? -1 : 0);
    });
    districtList.value = rows;
    loadingDistrict.value = false;
  });
}

function loadSiteOtherOverviewDatas() {
  if (siteOptions.value.length == 0) {
    proxy.$modal.msgWarning(proxy.$t('Stat.Site.NoSite'));
    return;
  }
  loadingOther.value = true;
  queryParams.startDate = dateRange.value[0];
  queryParams.endDate = dateRange.value[1];
  baiduTongjiApi.getSiteOtherOverviewDatas(queryParams).then(response => {
    top10LandingPage.value = response.data.landingPageRatioList;
    top10SourceSite.value = response.data.sourceSiteRatioList;
    top10VisitPage.value = response.data.visitPageRatioList;
    newOldVisitor.value = [];
    response.data.visitorData.newVisitor.forEach(item => {
      let oldData = response.data.visitorData.oldVisitor.find(oldItem => oldItem.name == item.name)
      newOldVisitor.value.push({
        label: item.label,
        newVisitorData: item.value,
        oldVisitorData: oldData.value
      });
    })
    loadingOther.value = false;
  });
}

function handleQuery() {
  loadSiteTrendOverviewDatas();
}

function resetQuery() {
  var endDate = proxy.parseTime(new Date());
  var startDate = proxy.parseTime(new Date(new Date().getTime() - 3600 * 24 * 30 *1000));
  dateRange.value = [ startDate, endDate ];
}

function handleSetLineChartData(type) {
  // proxy.$emit('handleSetLineChartData', type)
}

onMounted(() => {
  resetQuery();
  loadSiteList();
});
</script>
<style lang="scss" scoped>
.panel-group {

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