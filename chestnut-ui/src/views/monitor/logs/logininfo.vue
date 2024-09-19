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
        <el-form-item prop="ipaddr">
          <el-input
            v-model="queryParams.ipaddr"
            clearable
            :placeholder="$t('System.LoginInfo.IP')"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item prop="userName">
          <el-input
            v-model="queryParams.userName"
            clearable
            :placeholder="$t('System.LoginInfo.UserName')"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item prop="status">
          <el-select
            v-model="queryParams.status"
            clearable
            :placeholder="$t('System.LoginInfo.Status')"
            style="width:100px"
          >
            <el-option
              v-for="dict in dict.type.SuccessOrFail"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('System.LoginInfo.LoginTime')">
          <el-date-picker
            v-model="dateRange"
            value-format="yyyy-MM-dd"
            type="daterange"
            range-separator="-"
            :start-placeholder="$t('Common.BeginDate')"
            :end-placeholder="$t('Common.EndDate')"
            style="width:240px"
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
      <el-table-column :label="$t('System.LoginInfo.LogId')" align="center" prop="infoId" />
      <el-table-column :label="$t('System.LoginInfo.UserType')" align="center" prop="userType" />
      <el-table-column :label="$t('System.LoginInfo.UserId')" align="center" prop="userId" />
      <el-table-column :label="$t('System.LoginInfo.UserName')" align="center" prop="userName" :show-overflow-tooltip="true" sortable="custom" :sort-orders="['descending', 'ascending']" />
      <el-table-column :label="$t('System.LoginInfo.IP')" align="center" prop="ipaddr" width="130" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.LoginInfo.Location')" align="center" prop="loginLocation" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.LoginInfo.Browser')" align="center" prop="browser" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.LoginInfo.OS')" align="center" prop="os" />
      <el-table-column :label="$t('System.LoginInfo.Status')" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.SuccessOrFail" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('System.LoginInfo.OpMsg')" align="center" prop="msg" />
      <el-table-column :label="$t('System.LoginInfo.LoginTime')" align="center" prop="loginTime" sortable="custom" :sort-orders="['descending', 'ascending']" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.loginTime) }}</span>
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
  </div>
</template>

<script>
import { list, delLogininfor, cleanLogininfor } from "@/api/monitor/logininfor";

export default {
  name: "MonitorLogsLogininfor",
  dicts: ['SuccessOrFail'],
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 选择用户名
      selectName: "",
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 表格数据
      list: [],
      // 日期范围
      dateRange: [],
      // 默认排序
      defaultSort: {prop: 'loginTime', order: 'descending'},
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        ipaddr: undefined,
        userName: undefined,
        status: undefined
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询登录日志列表 */
    getList() {
      this.loading = true;
      list(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
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
      this.ids = selection.map(item => item.infoId)
      this.single = selection.length!=1
      this.multiple = !selection.length
      this.selectName = selection.map(item => item.userName);
    },
    /** 排序触发事件 */
    handleSortChange(column, prop, order) {
      this.queryParams.orderByColumn = column.prop;
      this.queryParams.isAsc = column.order;
      this.getList();
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const infoIds = row.infoId || this.ids;
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function() {
        return delLogininfor(infoIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess(this.$t('Common.DeleteSuccess'));
      }).catch(() => {});
    },
    /** 清空按钮操作 */
    handleClean() {
      this.$modal.confirm(this.$t('Common.ConfirmClean')).then(function() {
        return cleanLogininfor();
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess(this.$t('Common.Success'));
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.exportExcel('monitor/logininfor/list', {
        ...this.queryParams
      }, `logininfor_${this.parseTime(new Date(),'{y}{m}{d}{h}{i}{s}')}.xlsx`)
    },
    /** 返回按钮操作 */
    handleClose() {
      const obj = { path: "/monitor/logs" };
      this.$tab.closeOpenPage(obj);
    }
  }
};
</script>

