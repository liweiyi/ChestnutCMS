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
              value-format="yyyy-MM-dd HH:mm:ss"
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
    <el-card shadow="hover">
      <div slot="header" class="clearfix">
        <span>{{ $t("Stat.Site.VisitLocation") }}</span>
      </div>
      <el-table v-loading="loadingDistrict" :data="districtList" height="274" size="mini">
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
          <template slot-scope="scope">
            {{ scope.row.ratio }} %
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
<style scoped>
</style>
<script>
import { getSiteStatData } from "@/api/contentcore/stat";
import * as baiduTongjiApi from "@/api/stat/baidu";
import LineChart from '@/views/dashboard/LineChart'

export default {
  name: "CMSSiteBdTrendOverview",
  components: {
    LineChart
  },
  data () {
    return {
      loading: false,
      queryParams: {
        bdSiteId: undefined,
        startDate: undefined,
        endDate: undefined
      },
      pickerOptions: {
        shortcuts: [{
          text: this.$t("Common.LastWeek"),
          onClick(picker) {
            const end = new Date();
            const start = new Date();
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
            picker.$emit('pick', [start, end]);
          }
        }, {
          text: this.$t("Common.LastMonth"),
          onClick(picker) {
            const end = new Date();
            const start = new Date();
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
            picker.$emit('pick', [start, end]);
          }
        }, {
          text: this.$t("Common.LastThreeMonth"),
          onClick(picker) {
            const end = new Date();
            const start = new Date();
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
            picker.$emit('pick', [start, end]);
          }
        }]
      },
      dateRange: [],
      siteOptions: [],
      lineChartData: {
        xAxisDatas:[],
        datas: []
      },
      sum: {
        pv: 0,
        uv: 0,
        ip: 0,
        avgVisitTime: 0
      },
      loadingDistrict: false,
      districtList: [],
      top10LandingPage: [],
      siteStatLoading: false,
      siteStat: {
        catalogCount: 0,
        contentCount: 0,
        resourceCount: 0
      }
    };
  },
  created() {
    this.resetQuery();
    this.loadSiteStatData();
    this.loadSiteList();
  },
  methods: {
    loadSiteStatData() {
      this.siteStatLoading = true;
      getSiteStatData().then(response => {
        this.siteStatLoading = false
        this.siteStat = response.data
      })
    },
    loadSiteList() {
      baiduTongjiApi.getSiteList().then(response => {
        this.siteOptions = response.data || [];
        if (this.siteOptions.length > 0) {
          this.queryParams.bdSiteId = this.siteOptions[0].site_id;
          this.loadSiteTrendOverviewDatas();
          this.loadSiteDistrictOverviewDatas();
        }
      });
    },
    loadSiteTrendOverviewDatas () {
      if (this.siteOptions.length == 0) {
        return;
      }
      this.loading = true;
      this.queryParams.startDate = this.dateRange[0];
      this.queryParams.endDate = this.dateRange[1];
      baiduTongjiApi.getSiteTrendOverviewDatas(this.queryParams).then(response => {
          this.lineChartData.xAxisDatas = response.data.xaxisDatas;
          this.lineChartData.datas = response.data.datas;
          this.sum = { pv: 0, uv: 0, ip: 0, avgVisitTime: 0 };
          this.lineChartData.datas.pv_count.forEach(v => this.sum.pv+=v);
          this.lineChartData.datas.ip_count.forEach(v => this.sum.ip+=v);
          this.lineChartData.datas.visitor_count.forEach(v => this.sum.uv+=v);
          this.lineChartData.datas.avg_visit_time.forEach(v => this.sum.avgVisitTime+=v);
          this.sum.avgVisitTime = Math.round(this.sum.avgVisitTime / this.lineChartData.datas.avg_visit_time.length);
          console.log(this.lineChartData)
          this.loading = false;
      });
    },
    loadSiteDistrictOverviewDatas () {
      if (this.siteOptions.length == 0) {
        return;
      }
      this.loadingDistrict = true;
      this.queryParams.startDate = this.dateRange[0];
      this.queryParams.endDate = this.dateRange[1];
      baiduTongjiApi.getSiteDistrictOverviewDatas(this.queryParams).then(response => {
          this.districtList = response.data;
          this.loadingDistrict = false;
      });
    },
    loadSiteDistrictOverviewDatas () {
      if (this.siteOptions.length == 0) {
        return;
      }
      this.loadingDistrict = true;
      this.queryParams.startDate = this.dateRange[0];
      this.queryParams.endDate = this.dateRange[1];
      baiduTongjiApi.getSiteDistrictOverviewDatas(this.queryParams).then(response => {
          this.districtList = response.data;
          this.loadingDistrict = false;
      });
    },
    loadSiteOtherOverviewDatas () {
      if (this.siteOptions.length == 0) {
        return;
      }
      this.loadingOther = true;
      this.queryParams.startDate = this.dateRange[0];
      this.queryParams.endDate = this.dateRange[1];
      baiduTongjiApi.getSiteOtherOverviewDatas(this.queryParams).then(response => {
          this.top10LandingPage = response.data.landingPage;
          this.loadingOther = false;
      });
    },
    handleQuery() {
     this.loadSiteTrendOverviewDatas();
    },
    resetQuery() {
      var endDate = this.parseTime(new Date());
      var startDate = this.parseTime(new Date(new Date().getTime() - 3600 * 24 * 30 *1000));
      this.dateRange = [ startDate, endDate ];
    },
    handleSetLineChartData(type) {
     // this.$emit('handleSetLineChartData', type)
    }
  }
};
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