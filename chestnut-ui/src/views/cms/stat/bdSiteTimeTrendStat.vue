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
            {{ sum.pv_count }}
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
            {{ sum.visitor_count }}
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
            {{ sum.ip_count }}
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div v-loading="loading" class="card-panel" @click="handleSetLineChartData('avgVisitTime')">
          <div class="card-panel-icon-wrapper icon-people">
            <svg-icon icon-class="home" class-name="card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">
              {{ $t('Stat.Site.VisitCount') }}
            </div>
            {{ sum.visit_count }}
          </div>
        </div>
      </el-col>
    </el-row>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-select v-model="queryParams.siteId" size="small">
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
          v-if="queryParams.gran!='hour'"
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
        <el-date-picker
          v-if="queryParams.gran=='hour'"
          v-model="hourDate"
          value-format="yyyy-MM-dd HH:mm:ss"
          size="small"
          :clearable="false"
          type="date">
        </el-date-picker>
      </el-col>
      <el-col :span="1.5">
        <el-radio-group v-model="queryParams.gran" size="small">
          <el-radio-button label="hour">{{ $t('Stat.Site.GranHour') }}</el-radio-button>
          <el-radio-button label="day">{{ $t('Stat.Site.GranDay') }}</el-radio-button>
          <el-radio-button label="week">{{ $t('Stat.Site.GranWeek') }}</el-radio-button>
          <el-radio-button label="month">{{ $t('Stat.Site.GranMonth') }}</el-radio-button>
        </el-radio-group>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          type="primary"
          icon="el-icon-search"
          size="small"
          @click="handleQuery">{{ $t("Common.Search") }}</el-button>
      </el-col>
    </el-row>
    <el-row :gutter="10" class="mb8">
        <el-card v-loading="loading" shadow="hover">
          <div slot="header" class="clearfix">
            <span>{{ $t('Stat.Site.TrendCharts') }}</span>
          </div>
          <line-chart :chart-data="lineChartData" />
        </el-card>
    </el-row>
  </div>
</template>
<style scoped>
</style>
<script>
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
        siteId: undefined,
        startDate: undefined,
        endDate: undefined,
        gran: 'day'
      },
      pickerOptions: {
        shortcuts: [{
          text: this.$t('Common.LastWeek'),
          onClick(picker) {
            const end = new Date();
            const start = new Date();
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
            picker.$emit('pick', [start, end]);
          }
        }, {
          text: this.$t('Common.LastMonth'),
          onClick(picker) {
            const end = new Date();
            const start = new Date();
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
            picker.$emit('pick', [start, end]);
          }
        }, {
          text: this.$t('Common.LastThreeMonth'),
          onClick(picker) {
            const end = new Date();
            const start = new Date();
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
            picker.$emit('pick', [start, end]);
          }
        }]
      },
      dateRange: [],
      hourDate: undefined,
      siteOptions: [],
      lineChartData: {
        xAxisDatas:[],
        datas: []
      },
      sum: {},
    };
  },
  created() {
    this.resetQuery();
    this.loadSiteList();
  },
  methods: {
    loadSiteList() {
      baiduTongjiApi.getSiteList().then(response => {
        this.siteOptions = response.data || [];
        if (this.siteOptions.length > 0) {
          this.queryParams.siteId = this.siteOptions[0].site_id;
          this.loadSiteTimeTrendDatas();
        }
      });
    },
    loadSiteTimeTrendDatas () {
      if (this.siteOptions.length == 0) {
        this.$modal.msgWarning(this.$t('Stat.Site.NoSite'));
        return;
      }
      this.loading = true;
      if (this.queryParams.gran == 'hour') {
        this.queryParams.startDate = this.hourDate;
        this.queryParams.endDate = this.hourDate;
      } else {
        this.queryParams.startDate = this.dateRange[0];
        this.queryParams.endDate = this.dateRange[1];
      }
      baiduTongjiApi.getSiteTimeTrendDatas(this.queryParams).then(response => {
          this.lineChartData.xAxisDatas = response.data.xaxisDatas;
          this.lineChartData.datas = response.data.datas;
          this.sum = response.data.sum;
          this.loading = false;
      });
    },
    handleQuery() {
     this.loadSiteTimeTrendDatas();
    },
    resetQuery() {
      var endDate = this.parseTime(new Date());
      var startDate = this.parseTime(new Date(new Date().getTime() - 3600 * 24 * 30 *1000));
      this.dateRange = [ startDate, endDate ];
      this.hourDate = this.parseTime(new Date());
    },
    handleSetLineChartData(type) {
     // this.$emit('handleSetLineChartData', type)
    }
  }
};
</script>

<style lang="scss" scoped>
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