<template>
  <div class="app-container">
    <el-row :gutter="10">
      <el-col :span="24" class="card-box">
        <el-card>
          <template #header><span>{{ $t('Monitor.Cache.Basic') }}</span></template>
          <div class="el-table el-table--enable-row-hover el-table--medium">
            <table cellspacing="0" style="width: 100%">
              <tbody>
                <tr>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Cache.RedisVersion') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="cache.info">{{ cache.info.redis_version }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Cache.RunMode') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="cache.info">{{ cache.info.redis_mode == "standalone" ? "单机" : "集群" }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Cache.Port') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="cache.info">{{ cache.info.tcp_port }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Cache.ClientNumber') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="cache.info">{{ cache.info.connected_clients }}</div></td>
                </tr>
                <tr>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Cache.UpTimeInDays') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="cache.info">{{ cache.info.uptime_in_days }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Cache.Memory') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="cache.info">{{ cache.info.used_memory_human }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Cache.CPU') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="cache.info">{{ parseFloat(cache.info.used_cpu_user_children).toFixed(2) }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Cache.MaxMemory') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="cache.info">{{ cache.info.maxmemory_human }}</div></td>
                </tr>
                <tr>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Cache.AOFStatus') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="cache.info">{{ cache.info.aof_enabled == "0" ? $t('Common.No') : $t('Common.Yes') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Cache.RDBStatus') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="cache.info">{{ cache.info.rdb_last_bgsave_status }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Cache.DBSize') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="cache.dbSize">{{ cache.dbSize }} </div></td>
                  <td class="el-table__cell is-leaf"><div class="cell attrname">{{ $t('Monitor.Cache.InputKbps') }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="cache.info">{{ cache.info.instantaneous_input_kbps }}kps/{{cache.info.instantaneous_output_kbps}}kps</div></td>
                </tr>
              </tbody>
            </table>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12" class="card-box">
        <el-card>
          <template #header><span>{{ $t('Monitor.Cache.CommandStat') }}</span></template>
          <div class="el-table el-table--enable-row-hover el-table--medium">
            <div ref="commandstats" style="height: 420px" />
          </div>
        </el-card>
      </el-col>

      <el-col :span="12" class="card-box">
        <el-card>
          <template #header>
            <span>{{ $t('Monitor.Cache.MemoryInfo') }}</span>
          </template>
          <div class="el-table el-table--enable-row-hover el-table--medium">
            <div ref="usedmemory" style="height: 420px" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="MonitorCacheIndex">
import { getCache } from "@/api/monitor/cache";
import * as echarts from "echarts";

const { proxy } = getCurrentInstance();

// 统计命令信息
const commandstats = ref(null);
// 使用内存
const usedmemory = ref(null);
// cache信息
const cache = ref([]);

/** 查缓存询信息 */
function getList() {
  getCache().then((response) => {
    cache.value = response.data;
    proxy.$modal.closeLoading();

    commandstats.value = echarts.init(proxy.$refs.commandstats, "macarons");
    commandstats.value.setOption({
      tooltip: {
        trigger: "item",
        formatter: "{a} <br/>{b} : {c} ({d}%)",
      },
      series: [
        {
          name: proxy.$t('Monitor.Cache.Command'),
          type: "pie",
          roseType: "radius",
          radius: [15, 95],
          center: ["50%", "38%"],
          data: response.data.commandStats,
          animationEasing: "cubicInOut",
          animationDuration: 1000,
        }
      ]
    });
    usedmemory.value = echarts.init(proxy.$refs.usedmemory, "macarons");
    usedmemory.value.setOption({
      tooltip: {
        formatter: "{b} <br/>{a} : " + cache.value.info.used_memory_human,
      },
      series: [
        {
          name: proxy.$t('Monitor.Cache.PeakValue'),
          type: "gauge",
          min: 0,
          max: 1000,
          detail: {
            formatter: cache.value.info.used_memory_human,
          },
          data: [
            {
              value: parseFloat(cache.value.info.used_memory_human),
              name: proxy.$t('Monitor.Cache.MemoryCost'),
            }
          ]
        }
      ]
    });
  });
}

// 打开加载层
function openLoading() {
  proxy.$modal.loading(proxy.$t('Common.Loading'));
}

onMounted(() => {
  getList();
  openLoading();
});
</script>
<style>
.attrname {
  font-weight: bold;
}
</style>
