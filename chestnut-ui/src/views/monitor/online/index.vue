<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true">
      <el-form-item :label="$t('Monitor.Online.LoginIP')" prop="ipaddr">
        <el-input
          v-model="queryParams.ipaddr"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="$t('Monitor.Online.UserName')" prop="userName">
        <el-input
          v-model="queryParams.userName"
          clearable
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
    <el-table
      v-loading="loading"
      :data="list.slice((pageNum-1)*pageSize,pageNum*pageSize)"
      style="width: 100%;"
    >
      <el-table-column :label="$t('Common.RowNo')" type="index" align="center">
        <template slot-scope="scope">
          <span>{{(pageNum - 1) * pageSize + scope.$index + 1}}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Monitor.Online.TokenID')" align="center" prop="tokenId" width="300" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Monitor.Online.UserName')" align="center" prop="userName" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Monitor.Online.DeptName')" align="center" prop="deptName" />
      <el-table-column :label="$t('Monitor.Online.LoginIP')" align="center" prop="ipaddr" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Monitor.Online.LoginLocation')" align="center" prop="loginLocation" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Monitor.Online.Browser')" align="center" prop="browser" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Monitor.Online.OS')" align="center" prop="os" />
      <el-table-column :label="$t('Monitor.Online.LoginTime')" align="center" prop="loginTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.loginTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" align="center" class-name="small-padding fixed-width" width="80">
        <template slot-scope="scope">
          <span class="btn-cell-wrap">
            <el-button
              size="small"
              type="text"
              icon="el-icon-delete"
              @click="handleForceLogout(scope.row)"
              v-hasPermi="['monitor:online:forceLogout']"
            >{{ $t('Monitor.Online.ForceExit') }}</el-button>
          </span>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="pageNum" :limit.sync="pageSize" />
  </div>
</template>

<script>
import { list, forceLogout } from "@/api/monitor/online";

export default {
  name: "MonitorOnlineIndex",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 总条数
      total: 0,
      // 表格数据
      list: [],
      pageNum: 1,
      pageSize: 10,
      // 查询参数
      queryParams: {
        ipaddr: undefined,
        userName: undefined
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
      list(this.queryParams).then(response => {
        this.list = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 强退按钮操作 */
    handleForceLogout(row) {
      this.$modal.confirm(this.$t('Monitor.Online.ConfirmForceExit', [ row.userName ])).then(function() {
        return forceLogout(row.tokenId);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess(this.$t('Common.Success'));
      }).catch(() => {});
    }
  }
};
</script>

