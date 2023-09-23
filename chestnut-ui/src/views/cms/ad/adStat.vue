<template>
  <div class="">
    <el-row :gutter="10" class="mb12">
      <el-col :span="1.5">
        <el-date-picker
          v-model="dateRange"
          value-format="yyyy-MM-dd HH:mm:ss"
          type="datetimerange"
          size="small"
          range-separator="-"
          :start-placeholder="$t('Common.BeginDate')"
          :end-placeholder="$t('Common.EndDate')"
          style="width:320px"
        ></el-date-picker>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          type="primary"
          icon="el-icon-search"
          size="small"
          @click="handleQuery">{{ $t("Common.Search") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          icon="el-icon-refresh"
          size="small"
          @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
      </el-col>
    </el-row>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column :label="$t('Stat.Adv.AdName')" align="center" prop="adName" />
      <el-table-column :label="$t('Stat.Adv.Click')" align="center" prop="click" />
      <el-table-column :label="$t('Stat.Adv.View')" align="center" prop="view" />
      <el-table-column :label="$t('Stat.Adv.ClickRatio')" align="center">
        <template slot-scope="scope">
          <span v-if="scope.row.view > 0">{{ Math.round(scope.row.click * 10000 / scope.row.view) / 100 }} %</span>
          <span v-else> - </span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" align="center" width="180" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-s-data" @click="handleCharts(scope.row)">{{ $t('Stat.Adv.Trend') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination 
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="loadAdStatList" />

    <el-dialog 
      :title="$t('Stat.Adv.TrendDialogTitle')"
      :visible.sync="open"
      :close-on-click-modal="false"
      width="1000px"
      append-to-body>
      <el-row :gutter="10" class="mb12">
        <el-col :span="1.5">
          <el-date-picker
            v-model="chartDateRange"
            value-format="yyyy-MM-dd HH:mm:ss"
            type="datetimerange"
            size="small"
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
            icon="el-icon-search"
            size="small"
            @click="loadLineChartDatas">{{ $t("Common.Search") }}</el-button>
        </el-col>
      </el-row>
      <line-chart :chart-data="lineChartData" />
      <div slot="footer" class="dialog-footer">
        <el-button @click="handleChartsClose">{{ $t("Common.Close") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<style scoped>
</style>
<script>
import { getAdStatList, getLineChartStatDatas  } from "@/api/advertisement/statistics";
import LineChart from '@/views/dashboard/LineChart'

export default {
  name: "CMSAdvertisementStat",
  components: {
    LineChart
  },
  data () {
    return {
      loading: false,
      total: 0,
      dataList: [],
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 50,
        beginTime: undefined,
        endTime: undefined
      },
      dateRange: [],
      chartDateRange: [],
      chartAdvertisemenId: undefined,
      lineChartData: {
        xAxisDatas:[],
        datas: []
      }
    };
  },
  created() {
    this.loadAdStatList();
  },
  methods: {
    loadAdStatList () {
      this.loading = true;
      this.queryParams.beginTime = this.dateRange[0];
      this.queryParams.endTime = this.dateRange[1];
      getAdStatList(this.queryParams).then(response => {
        this.dataList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    handleQuery () {
      this.queryParams.pageNum = 1;
      this.loadAdStatList();
    },
    resetQuery () {
      this.queryParams.pageNum = 1;
      this.dateRange = []
      this.handleQuery();
    },
    handleCharts (row) {
      var endDate = this.parseTime(new Date());
      var startDate = this.parseTime(new Date(new Date().getTime() - 3600 * 24 *1000));
      this.chartDateRange = [ startDate, endDate ];
      this.chartAdvertisemenId = row.advertisementId;
      this.open = true;
      this.loadLineChartDatas();
    },
    loadLineChartDatas() {
      const params = { advertisementId: this.chartAdvertisemenId, beginTime: this.chartDateRange[0], endTime: this.chartDateRange[1] }
      getLineChartStatDatas(params).then(response => {
          this.lineChartData.xAxisDatas = response.data.xAxisDatas;
          this.lineChartData.datas = response.data.lineDatas;
        }
      );
    },
    handleChartsClose () {
      this.open = false;
      this.resetQuery();
    }
  }
};
</script>