<template>
  <div class="dashboard-container">
    <el-card shadow="hover" class="mb10">
      <div slot="header" class="clearfix">
        <span>系统信息</span>
      </div>
      <div class="el-table el-table--enable-row-hover el-table--medium">
        <table cellspacing="0" style="width: 100%;">
          <tbody>
            <tr>
              <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.AppName') }}</div></td>
              <td class="el-table__cell is-leaf"><div class="cell" v-if="serverInfo.sys">{{ serverInfo.app.name }}</div></td>
              <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.AppVersion') }}</div></td>
              <td class="el-table__cell is-leaf"><div class="cell" v-if="serverInfo.sys">{{ serverInfo.app.version }}</div></td>
            </tr>
            <tr>
              <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.JVMStartTime') }}</div></td>
              <td class="el-table__cell is-leaf"><div class="cell" v-if="serverInfo.jvm">{{ serverInfo.jvm.startTime }}</div></td>
              <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.JVMRunTime') }}</div></td>
              <td class="el-table__cell is-leaf"><div class="cell" v-if="serverInfo.jvm">{{ serverInfo.jvm.runTime }}</div></td>
            </tr>
          </tbody>
        </table>
      </div>
    </el-card>
    
  </div>
</template>
<script>
import { getServer } from "@/api/monitor/server";

export default {
  name: "ServerInfoDashboard",
  data () {
    return {
      serverInfo: {}
    };
  },
  created() {
    this.loadServerInfo();
  },
  methods: {
    loadServerInfo() {
      getServer().then(response => {
        this.serverInfo = response.data;
      })
    }
  }
};
</script>