<template>
  <div class="app-container">
    <el-table
      v-loading="loading"
      :data="logMenuList"
      style="width: 100%;"
    >
      <el-table-column :label="$t('Common.RowNo')" type="index" align="center" />
      <el-table-column :label="$t('Monitor.Logs.LogName')" align="left" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <router-link :to="scope.row.router" class="link-type">
            <span>{{ scope.row.name }}</span>
          </router-link>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Monitor.Logs.LogId')" align="left" prop="id" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Common.Operation')" align="right" width="100" >
        <template slot-scope="scope">
          <router-link :to="scope.row.router" class="link-type">
            <el-button
            size="mini"
            type="text"
            icon="el-icon-search"
          >{{ $t('Common.View') }}</el-button>
          </router-link>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { getLogMenus } from "@/api/monitor/logs";

export default {
  name: "MonitorLogsIndex",
  data() {
    return {
      loading: true,
      logMenuList: []
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询日志分类列表 */
    getList() {
      this.loading = true;
      getLogMenus(this.queryParams).then(response => {
        this.logMenuList = response.data.rows;
        this.loading = false;
      });
    }
  }
};
</script>

