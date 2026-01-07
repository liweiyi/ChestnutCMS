<template>
  <div class="bdsite-container">
    <el-row :gutter="10" class="panel-group">
      <el-col :span="4" v-for="sumItem in sum" :key="sumItem.metrics">
        <div>
          <el-statistic
            v-loading="loading"
            group-separator=","
            :precision="2"
            :value="sumItem.value"
            :title="sumItem.label"
          ></el-statistic>
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
        <el-radio-group v-model="queryParams.viewType">
          <el-radio-button label="type">来源类型</el-radio-button>
          <el-radio-button label="site">来源网站</el-radio-button>
        </el-radio-group>
      </el-col>
      <el-col :span="1.5">
        <el-radio-group v-model="queryParams.clientDevice">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="pc">计算机</el-radio-button>
          <el-radio-button label="mobile">移动设备</el-radio-button>
        </el-radio-group>
      </el-col>
      <el-col :span="1.5">
        <el-radio-group v-model="queryParams.visitor">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="new">新访客</el-radio-button>
          <el-radio-button label="old">老访客</el-radio-button>
        </el-radio-group>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          type="primary"
          icon="Search"
          @click="handleQuery">{{ $t("Common.Search") }}</el-button>
      </el-col>
    </el-row>
    <el-card shadow="hover">
      <template #header>
        <div class="clearfix">
          <span>{{ $t('Stat.Site.VisitSource') }}</span>
        </div>
      </template>
      <el-table v-loading="loading" :data="sourceDataList">
        <el-table-column 
          :label="$t('Stat.Site.VisitSource')"
          align="center"
          prop="name" />
        <el-table-column 
          v-for="item in metrics"
          :key="item.name"
          :label="item.label"
          align="center"
          prop="ratio">
          <template #default="scope">
            {{ scope.row.dataList.find(data => data.yaxis == item.name).value }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
<script setup name="CMSSiteBdEngineSource">
import * as baiduTongjiApi from "@/api/stat/baidu";

const { proxy } = getCurrentInstance();

const loading = ref(false);
const queryParams = reactive({
  siteId: undefined,
  startDate: undefined,
  endDate: undefined,
  viewType: 'type',
  clientDevice: '',
  visitor: ''
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
const metrics = ref([]);
const sum = ref([]);
const sourceDataList = ref([]);

function loadSiteList() {
  baiduTongjiApi.getSiteList().then(response => {
    siteOptions.value = response.data || [];
    if (siteOptions.value.length > 0) {
      queryParams.siteId = siteOptions.value[0].site_id;
      loadSiteEngineSourceDatas();
    }
  });
}

function loadSiteEngineSourceDatas() {
  if (siteOptions.value.length == 0) {
    proxy.$modal.msgWarning(proxy.$t('Stat.Site.NoSite'));
    return;
  }
  loading.value = true;
  queryParams.startDate = dateRange.value[0];
  queryParams.endDate = dateRange.value[1];
  baiduTongjiApi.getSiteEngineSource(queryParams).then(response => {
    sum.value = response.data.sumDataList.filter(item => [ 'pv_count', 'visitor_count', 'ip_count', 'bounce_ratio', 'avg_visit_time', 'avg_visit_pages' ].indexOf(item.metrics) > -1);
    metrics.value = response.data.sumDataList.map(item => {
      return {
        "name": item.metrics,
        "label": item.label
      };
    });
    sourceDataList.value = response.data.sourceDataList;
    loading.value = false;
  });
}

function handleQuery() {
  loadSiteEngineSourceDatas();
}

function resetQuery() {
  var endDate = proxy.parseTime(new Date());
  var startDate = proxy.parseTime(new Date(new Date().getTime() - 3600 * 24 * 30 *1000));
  dateRange.value = [ startDate, endDate ];
  queryParams.viewType = 'type';
  queryParams.clientDevice = '';
  queryParams.visitor = '';
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
  margin: 40px 0;
}
</style>