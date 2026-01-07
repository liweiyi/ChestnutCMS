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
        <el-select v-model="queryParams.siteId" :placeholder="$t('Stat.Site.SelectSite')" style="width: 260px">
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
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          unlink-panels
          range-separator="-"
          :clearable="false"
          :shortcuts="shortcuts"
          style="width:235px"
        ></el-date-picker>
        <el-date-picker
          v-if="queryParams.gran=='hour'"
          v-model="hourDate"
          value-format="YYYY-MM-DD HH:mm:ss"
          :clearable="false"
          type="date">
        </el-date-picker>
      </el-col>
      <el-col :span="1.5">
        <el-radio-group v-model="queryParams.gran">
          <el-radio-button label="hour">{{ $t('Stat.Site.GranHour') }}</el-radio-button>
          <el-radio-button label="day">{{ $t('Stat.Site.GranDay') }}</el-radio-button>
          <el-radio-button label="week">{{ $t('Stat.Site.GranWeek') }}</el-radio-button>
          <el-radio-button label="month">{{ $t('Stat.Site.GranMonth') }}</el-radio-button>
        </el-radio-group>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          type="primary"
          icon="Search"
          @click="handleQuery">{{ $t("Common.Search") }}</el-button>
      </el-col>
    </el-row>
    <el-card v-loading="loading" shadow="hover">
      <template #header>
        <div class="clearfix">
          <span>{{ $t('Stat.Site.TrendCharts') }}</span>
        </div>
      </template>
      <line-chart :chart-data="lineChartData" />
    </el-card>
  </div>
</template>
<style scoped>
</style>
<script setup name="CMSSiteBdTimeTrend">
import * as baiduTongjiApi from "@/api/stat/baidu";
import LineChart from '@/views/dashboard/LineChart'

const { proxy } = getCurrentInstance();

const loading = ref(false);
const queryParams = reactive({
  siteId: undefined,
  startDate: undefined,
  endDate: undefined,
  gran: 'day'
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
const hourDate = ref(undefined);
const siteOptions = ref([]);
const lineChartData = ref({
  xAxisDatas:[],
  datas: []
});
const sum = ref({});

function loadSiteList() {
  baiduTongjiApi.getSiteList().then(response => {
    siteOptions.value = response.data || [];
    if (siteOptions.value.length > 0) {
      queryParams.siteId = siteOptions.value[0].site_id;
      loadSiteTimeTrendDatas();
    }
  });
}

function loadSiteTimeTrendDatas() {
  if (siteOptions.value.length == 0) {
    proxy.$modal.msgWarning(proxy.$t('Stat.Site.NoSite'));
    return;
  }
  loading.value = true;
  if (queryParams.gran == 'hour') {
    queryParams.startDate = hourDate.value;
    queryParams.endDate = hourDate.value;
  } else {
    queryParams.startDate = dateRange.value[0];
    queryParams.endDate = dateRange.value[1];
  }
  baiduTongjiApi.getSiteTimeTrendDatas(queryParams).then(response => {
    lineChartData.value.xAxisDatas = response.data.xaxisDatas;
    lineChartData.value.datas = response.data.datas;
    sum.value = response.data.sum;
    loading.value = false;
  });
}

function handleQuery() {
  loadSiteTimeTrendDatas();
}

function resetQuery() {
  var endDate = proxy.parseTime(new Date());
  var startDate = proxy.parseTime(new Date(new Date().getTime() - 3600 * 24 * 30 *1000));
  dateRange.value = [ startDate, endDate ];
  hourDate.value = proxy.parseTime(new Date());
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