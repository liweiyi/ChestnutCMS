<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb12">
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="small"
          :disabled="multiple"
          @click="handleDelete"
        >{{ $t('Common.Delete') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="small"
          @click="handleClean"
        >{{ $t('Common.Clean') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="small"
          @click="handleExport"
        >{{ $t('Common.Export') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="el-icon-close"
          size="small"
          @click="handleClose"
        >{{ $t('Common.Close') }}</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-row v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="small" class="el-form-search mb12" :inline="true">
        <el-form-item prop="title">
          <el-input
            v-model="queryParams.title"
            clearable
            :placeholder="$t('System.OpLog.Title')"
            style="width: 140px;"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item prop="operName">
          <el-input
            v-model="queryParams.operName"
            clearable
            :placeholder="$t('System.OpLog.Operator')"
            style="width: 140px;"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item prop="businessType">
          <el-input
            v-model="queryParams.businessType"
            clearable
            style="width: 140px;"
            :placeholder="$t('System.OpLog.OpType')"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item prop="responseCode">
          <el-input
            v-model="queryParams.responseCode"
            clearable
            style="width: 140px;"
            :placeholder="$t('System.OpLog.ResponseCode')"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item :label="$t('System.OpLog.OpTime')">
          <el-date-picker
            v-model="dateRange"
            style="width: 240px"
            value-format="yyyy-MM-dd"
            type="daterange"
            range-separator="-"
            :start-placeholder="$t('Common.BeginDate')"
            :end-placeholder="$t('Common.EndDate')"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button-group>
            <el-button type="primary" icon="el-icon-search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
            <el-button icon="el-icon-refresh" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
          </el-button-group>
        </el-form-item>
      </el-form>
    </el-row>

    <el-table ref="tables" v-loading="loading" :data="list" @selection-change="handleSelectionChange" :default-sort="defaultSort" @sort-change="handleSortChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="$t('System.OpLog.LogId')" align="center" prop="operId" />
      <el-table-column :label="$t('System.OpLog.Title')" align="center" prop="title" />
      <el-table-column :label="$t('System.OpLog.OpType')" align="center" prop="businessType" />
      <el-table-column :label="$t('System.OpLog.RequestMethod')" align="center" prop="requestMethod" />
      <el-table-column :label="$t('System.OpLog.Operator')" align="center" prop="operName" width="100" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.OpLog.IP')" align="center" prop="operIp" width="130" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.OpLog.Location')" align="center" prop="operLocation" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.OpLog.ResponseCode')" align="center" prop="responseCode" />
      <el-table-column :label="$t('System.OpLog.OpTime')" align="center" prop="operTime" sortable="custom" :sort-orders="['descending', 'ascending']" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.operTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('System.OpLog.Cost')" align="center" prop="cost" width="100">
        <template slot-scope="scope">
          <span>{{ scope.row.cost == null ? 0 : scope.row.cost }} ms</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="small"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row,scope.index)"
          >{{ $t('System.OpLog.Details') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 操作日志详细 -->
    <el-dialog :title="$t('System.OpLog.DetailsTitle')" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" label-width="130px" size="mini">
        <el-row>
          <el-col :span="24">
            <el-form-item :label="$t('System.OpLog.Title')">{{ form.title }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('System.OpLog.OpType')">{{ form.businessType }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item
              :label="$t('System.OpLog.LoginInfo')"
            >{{ form.operName }} / {{ form.operIp }} / {{ form.operLocation }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('System.OpLog.RequestUrl')">{{ form.operUrl }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('System.OpLog.RequestMethod')">{{ form.requestMethod }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('System.OpLog.Method')">{{ form.method }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('System.OpLog.RequestParams')">{{ form.requestArgs }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="User-Agent">{{ form.userAgent }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('System.OpLog.Response')">{{ form.responseResult }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('System.OpLog.OpTime')">{{ parseTime(form.operTime) }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('System.OpLog.Cost')">{{ form.cost == null ? 0 : form.cost }} ms</el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open = false">{{ $t('Common.Close') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { list, delOperlog, cleanOperlog } from "@/api/monitor/operlog";

export default {
  name: "MonitorLogsOperation",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 表格数据
      list: [],
      // 是否显示弹出层
      open: false,
      // 日期范围
      dateRange: [],
      // 默认排序
      defaultSort: {prop: 'operTime', order: 'descending'},
      // 表单参数
      form: {},
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        operName: undefined,
        businessType: undefined,
        responseCode: undefined
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询登录日志 */
    getList() {
      this.loading = true;
      list(this.addDateRange(this.queryParams, this.dateRange)).then( response => {
          this.list = response.data.rows;
          this.total = parseInt(response.data.total);
          this.loading = false;
        }
      );
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.queryParams.pageNum = 1;
      this.$refs.tables.sort(this.defaultSort.prop, this.defaultSort.order)
    },
    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.operId)
      this.multiple = !selection.length
    },
    /** 排序触发事件 */
    handleSortChange(column, prop, order) {
      this.queryParams.orderByColumn = column.prop;
      this.queryParams.isAsc = column.order;
      this.getList();
    },
    /** 详细按钮操作 */
    handleView(row) {
      this.open = true;
      this.form = row;
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const operIds = row.operId ? [ row.operId ] : this.ids;
      this.$modal.confirm(this.$t('System.OpLog.ConfirmDelete', [ operIds ])).then(function() {
        return delOperlog(operIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess(this.$t('Common.DeleteSuccess'));
      }).catch(() => {});
    },
    /** 清空按钮操作 */
    handleClean() {
      this.$modal.confirm(this.$t('System.OpLog.ConfirmClean')).then(function() {
        return cleanOperlog();
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess(this.$t('Common.Success'));
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.exportExcel('monitor/operlog/list', {
        ...this.queryParams
      }, `operlog_${this.parseTime(new Date(),'{y}{m}{d}{h}{i}{s}')}.xlsx`)
    },
    /** 返回按钮操作 */
    handleClose() {
      const obj = { path: "/monitor/logs" };
      this.$tab.closeOpenPage(obj);
    }
  }
};
</script>

