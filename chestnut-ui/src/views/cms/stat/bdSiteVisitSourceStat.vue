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
        <el-radio-group v-model="queryParams.viewType" size="small">
          <el-radio-button label="type">来源类型</el-radio-button>
          <el-radio-button label="site">来源网站</el-radio-button>
        </el-radio-group>
      </el-col>
      <el-col :span="1.5">
        <el-radio-group v-model="queryParams.clientDevice" size="small">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="pc">计算机</el-radio-button>
          <el-radio-button label="mobile">移动设备</el-radio-button>
        </el-radio-group>
      </el-col>
      <el-col :span="1.5">
        <el-radio-group v-model="queryParams.visitor" size="small">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="new">新访客</el-radio-button>
          <el-radio-button label="old">老访客</el-radio-button>
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
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>{{ $t('Stat.Site.VisitSource') }}</span>
        </div>
        <el-table v-loading="loading" :data="sourceDataList" size="mini">
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
            <template slot-scope="scope">
              {{ scope.row.dataList.find(data => data.yaxis == item.name).value }}
            </template>
          </el-table-column>
        </el-table>
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
  name: "CMSSiteBdVisitSource",
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
        viewType: 'type',
        clientDevice: '',
        visitor: ''
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
      siteOptions: [],
      lineChartData: {
        xAxisDatas:[],
        datas: []
      },
      metrics: [],
      sum: [],
      sourceDataList: []
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
          this.loadSiteVisitSourceDatas();
        }
      });
    },
    loadSiteVisitSourceDatas () {
      if (this.siteOptions.length == 0) {
        this.$modal.msgWarning(this.$t('Stat.Site.NoSite'));
        return;
      }
      this.loading = true;
      this.sourceList = []
      this.queryParams.startDate = this.dateRange[0];
      this.queryParams.endDate = this.dateRange[1];
      baiduTongjiApi.getSiteVisitSource(this.queryParams).then(response => {
          this.sum = response.data.sumDataList.filter(item => [ 'pv_count', 'visitor_count', 'ip_count', 'bounce_ratio', 'avg_visit_time', 'avg_visit_pages' ].indexOf(item.metrics) > -1)
          this.metrics = response.data.sumDataList.map(item => {
            return {
              "name": item.metrics,
              "label": item.label
            };
          });
          this.sourceDataList = response.data.sourceDataList;
          this.loading = false;
      });
    },
    handleQuery() {
     this.loadSiteVisitSourceDatas();
    },
    resetQuery() {
      var endDate = this.parseTime(new Date());
      var startDate = this.parseTime(new Date(new Date().getTime() - 3600 * 24 * 30 *1000));
      this.dateRange = [ startDate, endDate ];
      this.queryParams.viewType = 'type';
      this.queryParams.clientDevice = '';
      this.queryParams.visitor = '';

    },
    handleSetLineChartData(type) {
     // this.$emit('handleSetLineChartData', type)
    }
  }
};
</script>

<style lang="scss" scoped>
.panel-group {
  margin: 40px 0;
}
</style>