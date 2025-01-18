<template>
  <div class="app-container">
    <el-row>
      <el-col :span="24" class="card-box">
        <el-card>
          <div slot="header">
            <span>{{ $t('Monitor.Server.ApplicationInfo') }}</span>
          </div>
          <div class="el-table el-table--enable-row-hover el-table--medium">
            <table cellspacing="0" style="width: 100%;">
              <tbody>
                <tr>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.AppName') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.sys">{{ server.app.name }} [ {{ server.app.alias }} ]</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.AppVersion') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.sys">{{ server.app.version }}<span v-if="hasNewVersion"> [ {{ $t('Monitor.Server.LatestVersion') }}: {{ latestVersion }} ]</span></div></td>
                </tr>
              </tbody>
            </table>
          </div>
        </el-card>
      </el-col>
      <el-col :span="24" class="card-box">
        <el-card>
          <div slot="header">
            <span>{{ $t("Monitor.Server.DbInfo") }}</span>
          </div>
          <el-card v-for="(db, index) in server.dataSources.list" :key="index" style="margin: 15px 0;">
            <div slot="header">
              <span>{{ $t("Monitor.Server.DbPoolName") }}：{{ db.poolName }}</span>
            </div>
            <div class="el-table el-table--enable-row-hover el-table--medium">
              <table cellspacing="0" style="width: 100%;">
                <tbody>
                  <tr>
                    <td width="10%" class="el-table__cell is-leaf"><div class="cell attrname">{{ $t("Monitor.Server.DbDriverClass") }}</div></td>
                    <td class="el-table__cell is-leaf"><div class="cell">{{ db.driverClass }}</div></td>
                  </tr>
                  <tr>
                    <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t("Monitor.Server.DbUrl") }}</div></td>
                    <td class="el-table__cell is-leaf"><div class="cell">{{ db.url }}</div></td>
                  </tr>
                  <tr>
                    <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t("Monitor.Server.DbUserName") }}</div></td>
                    <td class="el-table__cell is-leaf"><div class="cell">{{ db.username }}</div></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </el-card>
        </el-card>
      </el-col>

      <el-col :span="12" class="card-box">
        <el-card>
          <div slot="header"><span>{{ $t('Monitor.Server.CPU') }}</span></div>
          <div class="el-table el-table--enable-row-hover el-table--medium">
            <table cellspacing="0" style="width: 100%;">
              <thead>
                <tr>
                  <th class="el-table__cell is-leaf"><div class="cell">{{ $t('Monitor.Server.Attribute') }}</div></th>
                  <th class="el-table__cell is-leaf"><div class="cell">{{ $t('Monitor.Server.Value') }}</div></th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.CPUCoreNum') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.cpu">{{ server.cpu.cpuNum }}</div></td>
                </tr>
                <tr>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.CPUUserUsage') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.cpu">{{ server.cpu.used }}%</div></td>
                </tr>
                <tr>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.CPUSysUsage') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.cpu">{{ server.cpu.sys }}%</div></td>
                </tr>
                <tr>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.CPUIdle') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.cpu">{{ server.cpu.free }}%</div></td>
                </tr>
              </tbody>
            </table>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12" class="card-box">
        <el-card>
          <div slot="header"><span>{{ $t('Monitor.Server.Memory') }}</span></div>
          <div class="el-table el-table--enable-row-hover el-table--medium">
            <table cellspacing="0" style="width: 100%;">
              <thead>
                <tr>
                  <th class="el-table__cell is-leaf"><div class="cell">{{ $t('Monitor.Server.Attribute') }}</div></th>
                  <th class="el-table__cell is-leaf"><div class="cell">{{ $t('Monitor.Server.Memory') }}</div></th>
                  <th class="el-table__cell is-leaf"><div class="cell">{{ $t('Monitor.Server.JVM') }}</div></th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.TotalMemory') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.mem">{{ server.mem.total }}G</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.jvm">{{ server.jvm.total }}M</div></td>
                </tr>
                <tr>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.UsedMemory') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell " v-if="server.mem">{{ server.mem.used}}G</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.jvm">{{ server.jvm.used}}M</div></td>
                </tr>
                <tr>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.LeftMemory') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.mem">{{ server.mem.free }}G</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.jvm">{{ server.jvm.free }}M</div></td>
                </tr>
                <tr>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.MemoryUsage') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.mem" :class="{'text-danger': server.mem.usage > 80}">{{ server.mem.usage }}%</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.jvm" :class="{'text-danger': server.jvm.usage > 80}">{{ server.jvm.usage }}%</div></td>
                </tr>
              </tbody>
            </table>
          </div>
        </el-card>
      </el-col>

      <el-col :span="24" class="card-box">
        <el-card>
          <div slot="header">
            <span>{{ $t('Monitor.Server.ServerInfo') }}</span>
          </div>
          <div class="el-table el-table--enable-row-hover el-table--medium">
            <table cellspacing="0" style="width: 100%;">
              <tbody>
                <tr>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.ServerName') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.sys">{{ server.sys.computerName }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.OSName') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.sys">{{ server.sys.osName }}</div></td>
                </tr>
                <tr>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.ServerIP') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.sys">{{ server.sys.computerIp }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.OSArch') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.sys">{{ server.sys.osArch }}</div></td>
                </tr>
              </tbody>
            </table>
          </div>
        </el-card>
      </el-col>

      <el-col :span="24" class="card-box">
        <el-card>
          <div slot="header">
            <span>{{ $t('Monitor.Server.JVMInfo') }}</span>
          </div>
          <div class="el-table el-table--enable-row-hover el-table--medium">
            <table cellspacing="0" style="width: 100%;table-layout:fixed;">
              <tbody>
                <tr>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.JVMName') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.jvm">{{ server.jvm.name }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.JVMVersion') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.jvm">{{ server.jvm.version }}</div></td>
                </tr>
                <tr>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.JVMStartTime') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.jvm">{{ server.jvm.startTime }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.JVMRunTime') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.jvm">{{ server.jvm.runTime }}</div></td>
                </tr>
                <tr>
                  <td colspan="1" class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.JVMHome') }}</div></td>
                  <td colspan="3" class="el-table__cell is-leaf"><div class="cell" v-if="server.jvm">{{ server.jvm.home }}</div></td>
                </tr>
                <tr>
                  <td colspan="1" class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.ProjectDir') }}</div></td>
                  <td colspan="3" class="el-table__cell is-leaf"><div class="cell" v-if="server.sys">{{ server.sys.userDir }}</div></td>
                </tr>
                <tr>
                  <td colspan="1" class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Server.JVMArgs') }}</div></td>
                  <td colspan="3" class="el-table__cell is-leaf"><div class="cell" v-if="server.jvm">{{ server.jvm.inputArgs }}</div></td>
                </tr>
              </tbody>
            </table>
          </div>
        </el-card>
      </el-col>

      <el-col :span="24" class="card-box">
        <el-card>
          <div slot="header">
            <span>{{ $t('Monitor.Server.Disk') }}</span>
          </div>
          <div class="el-table el-table--enable-row-hover el-table--medium">
            <table cellspacing="0" style="width: 100%;">
              <thead>
                <tr>
                  <th class="el-table__cell el-table__cell is-leaf"><div class="cell">{{ $t('Monitor.Server.DiskPath') }}</div></th>
                  <th class="el-table__cell is-leaf"><div class="cell">{{ $t('Monitor.Server.FileSystem') }}</div></th>
                  <th class="el-table__cell is-leaf"><div class="cell">{{ $t('Monitor.Server.DiskType') }}</div></th>
                  <th class="el-table__cell is-leaf"><div class="cell">{{ $t('Monitor.Server.DiskSize') }}</div></th>
                  <th class="el-table__cell is-leaf"><div class="cell">{{ $t('Monitor.Server.DiskLeftSize') }}</div></th>
                  <th class="el-table__cell is-leaf"><div class="cell">{{ $t('Monitor.Server.DiskUsedSize') }}</div></th>
                  <th class="el-table__cell is-leaf"><div class="cell">{{ $t('Monitor.Server.DiskUsedPercent') }}</div></th>
                </tr>
              </thead>
              <tbody v-if="server.sysFiles">
                <tr v-for="(sysFile, index) in server.sysFiles" :key="index">
                  <td class="el-table__cell is-leaf"><div class="cell">{{ sysFile.dirName }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell">{{ sysFile.sysTypeName }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell">{{ sysFile.typeName }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell">{{ sysFile.total }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell">{{ sysFile.free }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell">{{ sysFile.used }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" :class="{'text-danger': sysFile.usage > 80}">{{ sysFile.usage }}%</div></td>
                </tr>
              </tbody>
            </table>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getServer } from "@/api/monitor/server";

export default {
  name: "MonitorServerIndex",
  data() {
    return {
      // 服务器信息
      server: {
        dataSources: { list: []}
      },
      hasNewVersion: false,
      latestVersion: ""
    };
  },
  created() {
    this.getList();
    this.openLoading();
  },
  methods: {
    /** 查询服务器信息 */
    getList() {
      getServer().then(response => {
        this.server = response.data;
        this.$modal.closeLoading();
        this.checkLatestVersion();
      });
    },
    // 打开加载层
    openLoading() {
      this.$modal.loading(this.$t('Monitor.Server.Loading'));
    },
    async checkLatestVersion() {
      try {
        const version = await this.jsonpRequest(this.$cache.officialLink(), 'checkLatestVersionCallback');
        if (version && version.length > 0 && version != this.server.app.version) {
          this.latestVersion = version;
          this.hasNewVersion = true;
        }
      } catch (error) {
        // error
      }
    }
  }
};
</script>
<style>
.attrname {
  font-weight: bold;
}
</style>