<template>
  <div class="">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-date-picker
          v-model="dateRange"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="datetimerange"
          range-separator="-"
          :start-placeholder="$t('Common.BeginDate')"
          :end-placeholder="$t('Common.EndDate')"
          style="width:365px"
        ></el-date-picker>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          type="primary"
          icon="Search"
          @click="handleQuery">{{ $t("Common.Search") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          icon="Refresh"
          @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
      </el-col>
    </el-row>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column :label="$t('Stat.Adv.AdName')" align="center" prop="adName" />
      <el-table-column :label="$t('Stat.Adv.Click')" align="center" prop="click" />
      <el-table-column :label="$t('Stat.Adv.View')" align="center" prop="view" />
      <el-table-column :label="$t('Stat.Adv.ClickRatio')" align="center">
        <template #default="scope">
          <span v-if="scope.row.view > 0">{{ Math.round(scope.row.click * 10000 / scope.row.view) / 100 }} %</span>
          <span v-else> - </span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" align="center" width="180">
        <template #default="scope">
          <el-button type="text" icon="DataAnalysis" @click="handleCharts(scope.row)">{{ $t('Stat.Adv.Trend') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination 
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="loadAdStatList" />

    <el-dialog 
      :title="$t('Stat.Adv.TrendDialogTitle')"
      v-model="open"
      :close-on-click-modal="false"
      width="1000px"
      append-to-body>
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-date-picker
            v-model="chartDateRange"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="datetimerange"
            range-separator="-"
            :clearable="false"
            :start-placeholder="$t('Common.BeginDate')"
            :end-placeholder="$t('Common.EndDate')"
            style="width:335px"
          ></el-date-picker>
        </el-col>
        <el-col :span="1.5">
          <el-button 
            type="primary"
            icon="Search"
            @click="loadLineChartDatas">{{ $t("Common.Search") }}</el-button>
        </el-col>
      </el-row>
      <line-chart :chart-data="lineChartData" />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleChartsClose">{{ $t("Common.Close") }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CMSAdvertisementStat">
import { getAdStatList, getLineChartStatDatas  } from "@/api/advertisement/statistics";
import LineChart from '@/views/dashboard/LineChart'

const { proxy } = getCurrentInstance();

const loading = ref(false);
const total = ref(0);
const dataList = ref([]);
const open = ref(false);
const queryParams = reactive({
  pageNum: 1,
  pageSize: 50,
  beginTime: undefined,
  endTime: undefined
});

const dateRange = ref([]);
const chartDateRange = ref([]);
const chartAdvertisemenId = ref(undefined);
const lineChartData = ref({
  xAxisDatas:[],
  datas: []
});

function loadAdStatList() {
  loading.value = true;
  queryParams.beginTime = dateRange.value[0];
  queryParams.endTime = dateRange.value[1];
  getAdStatList(queryParams).then(response => {
    dataList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
}

function handleQuery() {
  queryParams.pageNum = 1;
  loadAdStatList();
}

function resetQuery() {
  queryParams.pageNum = 1;
  dateRange.value = []
  handleQuery();
}

function handleCharts(row) {
  var endDate = proxy.parseTime(new Date());
  var startDate = proxy.parseTime(new Date(new Date().getTime() - 3600 * 24 *1000));
  chartDateRange.value = [ startDate, endDate ];
  chartAdvertisemenId.value = row.advertisementId;
  open.value = true;
  loadLineChartDatas();
}

function loadLineChartDatas() {
  const params = { advertisementId: chartAdvertisemenId.value, beginTime: chartDateRange.value[0], endTime: chartDateRange.value[1] }
  getLineChartStatDatas(params).then(response => {
    lineChartData.value.xAxisDatas = response.data.xAxisDatas;
    lineChartData.value.datas = response.data.lineDatas;
  });
}

function handleChartsClose() {
  open.value = false;
  resetQuery();
}

onMounted(() => {
  loadAdStatList();
});
</script>