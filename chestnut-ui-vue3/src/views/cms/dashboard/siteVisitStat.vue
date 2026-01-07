<template>
  <div class="bdsite-container">
    <el-card v-loading="loading" shadow="hover" class="mb10">
      <div slot="header" class="clearfix">
        <span>{{ $t("Stat.Site.VisitTrend") }}</span>
        <el-row :gutter="10" class="mb8" style="float:right;">
          <el-col :span="1.5">
            <el-select v-model="queryParams.bdSiteId" size="small">
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
              size="small"
              unlink-panels
              range-separator="-"
              :clearable="false"
              :picker-options="pickerOptions"
              style="width:235px"
            ></el-date-picker>
          </el-col>
          <el-col :span="1.5">
            <el-button 
              type="primary"
              icon="el-icon-search"
              size="small"
              @click="handleQuery">{{ $t("Common.Search") }}</el-button>
          </el-col>
        </el-row>
      </div>
      <line-chart :chart-data="lineChartData" />
    </el-card>
  </div>
</template>
<style scoped>
</style>
<script setup name="CMSSiteVisitStat">
import { getSiteStatData } from "@/api/contentcore/stat";
import * as baiduTongjiApi from "@/api/stat/baidu";
import LineChart from '@/views/dashboard/LineChart'

const { proxy } = getCurrentInstance();

const loading = ref(false)
const queryParams = ref({
  bdSiteId: undefined,
  startDate: undefined,
  endDate: undefined
})

const pickerOptions = ref({
  shortcuts: [{
    text: proxy.$t("Common.LastWeek"),
    onClick(picker) {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
      picker.$emit('pick', [start, end]);
    }
  }, {
    text: proxy.$t("Common.LastMonth"),
    onClick(picker) {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
      picker.$emit('pick', [start, end]);
    }
  }, {
    text: proxy.$t("Common.LastThreeMonth"),
    onClick(picker) {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
      picker.$emit('pick', [start, end]);
    }
  }]
})

const dateRange = ref([])
const siteOptions = ref([])
const lineChartData = ref({
  xAxisDatas:[],
  datas: []
})
const sum = ref({
  pv: 0,
  uv: 0,
  ip: 0,
  avgVisitTime: 0
})
const loadingDistrict = ref(false)
const districtList = ref([])
const top10LandingPage = ref([])
const siteStatLoading = ref(false)
const siteStat = ref({
  catalogCount: 0,
  contentCount: 0,
  resourceCount: 0
})

function loadSiteStatData() {
  siteStatLoading.value = true;
  getSiteStatData().then(response => {
    siteStatLoading.value = false
    siteStat.value = response.data
  })
}

function loadSiteList() {
  baiduTongjiApi.getSiteList().then(response => {
    siteOptions.value = response.data || [];
    if (siteOptions.value.length > 0) {
      queryParams.value.bdSiteId = siteOptions.value[0].site_id;
      loadSiteTrendOverviewDatas();
    }
  });
}

function loadSiteTrendOverviewDatas() {
  if (siteOptions.value.length == 0) {
    return;
  }
  loading.value = true;
  queryParams.value.startDate = dateRange.value[0];
  queryParams.value.endDate = dateRange.value[1];
  baiduTongjiApi.getSiteTrendOverviewDatas(queryParams.value).then(response => {
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
    return;
  }
  loadingDistrict.value = true;
  queryParams.value.startDate = dateRange.value[0];
  queryParams.value.endDate = dateRange.value[1];
  baiduTongjiApi.getSiteDistrictOverviewDatas(queryParams.value).then(response => {
      districtList.value = response.data;
      loadingDistrict.value = false;
  });
}

function loadSiteOtherOverviewDatas() {
  if (siteOptions.value.length == 0) {
    return;
  }
  loadingOther.value = true;
  queryParams.value.startDate = dateRange.value[0];
  queryParams.value.endDate = dateRange.value[1];
  baiduTongjiApi.getSiteOtherOverviewDatas(queryParams.value).then(response => {
      top10LandingPage.value = response.data.landingPage;
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

const handleSetLineChartData = (type) => {
  // emit('handleSetLineChartData', type)
}

onMounted(() => {
  resetQuery();
  loadSiteStatData();
  loadSiteList();
})
</script>

<style lang="scss" scoped>
.cc-card-panel-icon {
  font-size: 48px;
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