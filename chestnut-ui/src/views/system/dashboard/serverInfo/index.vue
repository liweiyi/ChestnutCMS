<template>
  <div class="dashboard-container">
    <el-card shadow="hover" class="mb10">
      <div slot="header" class="clearfix">
        <span>{{ $t("Monitor.Server.ApplicationInfo") }}</span>
      </div>
      <div class="el-table el-table--enable-row-hover el-table--medium">
        <table cellspacing="0" style="width: 100%;">
          <tbody>
            <tr>
              <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.AppName') }}</div></td>
              <td class="el-table__cell is-leaf"><div class="cell">{{ serverInfo.app.name }} [ {{ serverInfo.app.alias }} ] </div></td>
              <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.AppVersion') }}</div></td>
              <td class="el-table__cell is-leaf"><div class="cell">{{ serverInfo.app.version }}</div></td>
            </tr>
            <tr>
              <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.JVMStartTime') }}</div></td>
              <td class="el-table__cell is-leaf"><div class="cell">{{ serverInfo.startTime }}</div></td>
              <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.JVMRunTime') }}</div></td>
              <td class="el-table__cell is-leaf"><div class="cell">{{ serverInfo.runTime }}</div></td>
            </tr>
            <tr>
              <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('CMS.Dashboard.PublishStrategy') }}</div></td>
              <td class="el-table__cell is-leaf" colspan="3"><div class="cell">{{ config.publishStrategy }}</div></td>
            </tr>
            <tr>
              <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('CMS.Dashboard.ResourceRoot') }}</div></td>
              <td class="el-table__cell is-leaf" colspan="3"><div class="cell">{{ config.resourceRoot }}</div></td>
            </tr>
          </tbody>
        </table>
      </div>
    </el-card>
    
  </div>
</template>
<script>
import { getDashboardServerInfo } from "@/api/monitor/server";
import { getCmsConfiguration } from "@/api/contentcore/dashboard";

export default {
  name: "ServerInfoDashboard",
  data () {
    return {
      serverInfo: {
        app: {}
      },
      config:{}
    };
  },
  created() {
    this.loadServerInfo();
    this.loadCmsConfiguration();
  },
  methods: {
    loadServerInfo() {
      getDashboardServerInfo().then(response => {
        this.serverInfo = response.data;
      })
    },
    loadCmsConfiguration() {
      getCmsConfiguration().then(response => {
        this.config = response.data;
      })
    }
  }
};
</script>