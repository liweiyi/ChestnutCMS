<template>
  <div class="">
    <el-row :gutter="10" class="mb12">
      <el-col :span="1.5">
        <el-button 
          type="primary"
          icon="el-icon-plus"
          size="mini"
          plain
          @click="handleAdd">{{ $t("Common.Add") }}</el-button>
      </el-col>
          <el-col :span="1.5">
            <el-button 
              plain
              type="success"
              icon="el-icon-edit"
              size="mini"
              :disabled="single"
              @click="handleEdit">{{ $t("Common.Edit") }}</el-button>
          </el-col>
      <el-col :span="1.5">
        <el-button 
          type="danger"
          icon="el-icon-delete"
          size="mini"
          plain
          :disabled="multiple"
          @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="loadWordStatList"></right-toolbar>
    </el-row>
    <el-row v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="mini" class="el-form-search mb12" :inline="true">
        <el-form-item prop="query">
          <el-input
            v-model="queryParams.query"
            :placeholder="$t('Search.WordStat.Placeholder.Word')"
            clearable
            style="width: 240px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button-group>
            <el-button type="primary" icon="el-icon-search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
            <el-button icon="el-icon-refresh" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
          </el-button-group>
        </el-form-item>
      </el-form>
    </el-row>

    <el-table ref="tableWordList" :height="tableHeight" :max-height="tableMaxHeight" v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="$t('Search.WordStat.Word')" align="left" prop="word" />
      <el-table-column :label="$t('Search.WordStat.Total')" align="center" prop="searchTotal" width="200" />
      <el-table-column :label="$t('Search.WordStat.Topping')" align="center" width="100">
          <template slot-scope="scope">
            <svg-icon icon-class="top" style="color: #11A983;" v-if="scope.row.topFlag > 0" />
          </template>
      </el-table-column>
      <el-table-column :label="$t('Search.WordStat.TopEndTime')" align="center" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.topDate) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" align="right" width="360" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="small" type="text" icon="el-icon-edit" @click="handleEdit(scope.row)">{{ $t('Common.Edit') }}</el-button>
          <el-button size="small" type="text" icon="el-icon-top" @click="handleSetTop(scope.row)" v-if="scope.row.topFlag==0">{{ $t('Search.WordStat.Topping') }}</el-button>
          <el-button size="small" type="text" icon="el-icon-bottom" @click="handleCancelTop(scope.row)" v-if="scope.row.topFlag>0">{{ $t('Search.WordStat.CancelTop') }}</el-button>
          <el-button size="small" type="text" icon="el-icon-s-data" @click="handleCharts(scope.row)">{{ $t('Search.WordStat.Trend') }}</el-button>
          <span class="btn-cell-wrap">
            <el-dropdown size="small" @command="(command) => handleCommand(command, scope.row)">
              <el-button size="small" type="text" icon="el-icon-more"></el-button>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="handleAddExtWord" icon="el-icon-plus"
                  v-hasPermi="['system:user:resetPwd']">{{ $t('Search.WordStat.AddExtWord') }}</el-dropdown-item>
                <el-dropdown-item command="handleAddStopWord" icon="el-icon-plus"
                  v-hasPermi="['system:user:edit']">{{ $t('Search.WordStat.AddStopWord') }}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </span>
        </template>
      </el-table-column>
    </el-table>
    <pagination 
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="loadWordStatList" />

    <!-- 搜索词表单弹窗 -->
    <el-dialog 
      :title="title"
      :visible.sync="open"
      :close-on-click-modal="false"
      width="500px"
      append-to-body>
      <el-form ref="form" :model="form" label-width="100px">
        <el-form-item :label="$t('Search.WordStat.Word')" prop="word">
          <el-input v-model="form.word" />
        </el-form-item>
        <el-form-item :label="$t('Search.WordStat.Total')" prop="searchTotal">
          <el-input-number v-model="form.searchTotal" />
        </el-form-item>
      </el-form>
      <div slot="footer"
           class="dialog-footer">
        <el-button type="primary" @click="submitForm">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>

    <!-- 置顶时间设置弹窗 -->  
    <el-dialog 
      :title="$t('Search.WordStat.Topping')"
      width="400px"
      :visible.sync="topDialogVisible"
      :close-on-click-modal="false"
      append-to-body>
      <el-form ref="top_form" label-width="100px" :model="topForm">
        <el-form-item :label="$t('Search.WordStat.TopEndTime')" prop="topEndTime">
          <el-date-picker 
            v-model="topForm.topEndTime" 
            :picker-options="topEndTimePickerOptions"
            value-format="yyyy-MM-dd HH:mm:ss"
            type="datetime">
          </el-date-picker>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleTopDialogOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="this.topDialogVisible=false">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>

    <!-- 搜索趋势图表 -->
    <el-dialog 
      :title="$t('Search.WordStat.TrendDialogTitle')"
      :visible.sync="openTrend"
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
import { getSiteSearchWordList, addSiteSearchWord, editSearchWord, deleteSearchWords, setTop, cancelTop, getTrendDatas } from "@/api/search/wordstat";
import { addDictWord } from "@/api/search/dictword";
import LineChart from '@/views/dashboard/LineChart'

export default {
  name: "SearchWordStat",
  components: {
    LineChart
  },
  data () {
    return {
      loading: false,
      showSearch: true,
      tableHeight: 600,
      tableMaxHeight: 600,
      total: 0,
      dataList: [],
      selectedRows: [],
      single: true,
      multiple: true,
      title: "",
      open: false,
      form: {},
      topDialogVisible: false,
      openTrend: false,
      queryParams: {
        pageNum: 1,
        pageSize: 50,
        beginTime: undefined,
        endTime: undefined
      },
      topForm: {
        topEndTime: undefined
      },
      topEndTimePickerOptions: {
        disabledDate(time) {
            return time.getTime() < Date.now() - 8.64e7;//如果没有后面的-8.64e7就是不可以选择今天的 
         }
      },
      dateRange: [],
      chartDateRange: [],
      chartWordId: undefined,
      lineChartData: {
        xAxisDatas:[],
        datas: []
      }
    };
  },
  created() {
    this.changeTableHeight();
    this.loadWordStatList();
  },
  methods: {
    changeTableHeight () {
      let height = document.body.offsetHeight // 网页可视区域高度
      this.tableHeight = height - 330;
      this.tableMaxHeight = this.tableHeight;
    },
    loadWordStatList () {
      this.loading = true;
      this.queryParams.beginTime = this.dateRange[0];
      this.queryParams.endTime = this.dateRange[1];
      getSiteSearchWordList(this.queryParams).then(response => {
        this.dataList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    handleQuery () {
      this.queryParams.pageNum = 1;
      this.loadWordStatList();
    },
    resetQuery () {
      this.queryParams.pageNum = 1;
      this.dateRange = []
      this.handleQuery();
    },
    handleCharts (row) {
      var endDate = this.parseTime(new Date());
      var startDate = this.parseTime(new Date(new Date().getTime() - 3600 * 24 * 7 * 1000));
      this.chartDateRange = [ startDate, endDate ];
      this.chartWordId = row.wordId;
      this.openTrend = true;
      this.loadLineChartDatas();
    },
    loadLineChartDatas() {
      const params = { wordId: this.chartWordId, beginTime: this.chartDateRange[0], endTime: this.chartDateRange[1] }
      getTrendDatas(params).then(response => {
          this.lineChartData.xAxisDatas = response.data.xAxisDatas;
          this.lineChartData.datas = response.data.lineDatas;
        }
      );
    },
    handleChartsClose () {
      this.openTrend = false;
      this.resetQuery();
    },
    handleSelectionChange (selection) {
      this.selectedRows = selection.map(item => item);
      this.single = selection.length != 1;
      this.multiple = !selection.length;
    },
    handleAdd () {
      this.reset();
      this.title = this.$t('Search.WordStat.AddTitle');
      this.open = true;
    },
    handleEdit (row) {
      const data = row.wordId ? row : this.selectedRows[0];
      this.reset();
      this.title = this.$t('Search.WordStat.EditTitle');
      this.form = data;
      this.open = true;
    },
    /** 提交按钮 */
    submitForm () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.wordId) {
            editSearchWord(this.form).then(response => {
              this.$modal.msgSuccess(response.msg);
              this.open = false;
              this.loadWordStatList();
            }); 
          } else {
            addSiteSearchWord(this.form).then(response => {
              this.$modal.msgSuccess(response.msg);
              this.open = false;
              this.loadWordStatList();
            }); 
          }
        }
      });
    },
    reset () {
      this.form = {};
    },
    cancel() {
      this.open = false;
      this.reset();
    },
    handleDelete (row) {
      const wordIds = row.wordId ? [ row.wordId ] : this.selectedRows.map(item => item.wordId)
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function() {
        return deleteSearchWords(wordIds);
      }).then((response) => {
        this.$modal.msgSuccess(response.msg);
        this.loadWordStatList();
      }).catch(() => {});
    },
    toggleAllCheckedRows() {
      this.selectedRows.forEach(row => {
          this.$refs.tableWordList.toggleRowSelection(row, false);
      });
      this.selectedRows = [];
    },
    handleSetTop(row) {
      if (row.wordId) {
        this.toggleAllCheckedRows();
        this.selectedRows.push(row);
      }
      this.topDialogVisible = true;
    },
    handleTopDialogOk() {
      const wordIds = this.selectedRows.map(item => item.wordId);
      if (wordIds.length == 0) {
        this.$modal.msgWarning(this.$t('Common.SelectFirst'));
        return;
      }
      this.$refs["top_form"].validate(valid => {
        if (valid) {
          setTop({ wordIds: wordIds, topEndTime: this.topForm.topEndTime }).then(response => {
            this.$modal.msgSuccess(this.$t('Common.OpSuccess'));
            this.topDialogVisible = false;
            this.topForm.topEndTime = undefined;
            this.loadWordStatList();
          });
        }
      });
    },
    handleCancelTop (row) {
      const wordIds = row.wordId ? [ row.wordId ] : this.selectedRows.map(item => item.wordId)
      cancelTop(wordIds).then(response => {
        this.$modal.msgSuccess(this.$t('Common.OpSuccess'));
        this.loadWordStatList();
      })
    },
    handleCommand(command, row) {
      if (command === "handleAddExtWord") {
        this.handleAddExtWord(row);
      } else if (command === "handleAddStopWord") {
          this.handleAddStopWord(row);
      }
    },
    handleAddExtWord (row) {
      const form = { wordType: "WORD", words: [ row.word ] }
      this.$modal.confirm(this.$t('Search.WordStat.ConfirmAddToDict')).then(function() {
        return addDictWord(form);
      }).then((response) => {
        this.$modal.msgSuccess(response.msg);
      }).catch(() => {});
    },
    handleAddStopWord (row) {
      const form = { wordType: "STOP", words: [ row.word ] }
      this.$modal.confirm(this.$t('Search.WordStat.ConfirmAddToDict')).then(function() {
        return addDictWord(form);
      }).then((response) => {
        this.$modal.msgSuccess(response.msg);
      }).catch(() => {});
    }
  }
};
</script>