<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="Close"
          @click="handleClose"
        >{{ $t('Common.Close') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Delete"
          @click="handleClean"
        >{{ $t('Common.Clean') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Refresh"
          @click="handleRefresh"
        >{{ $t('Common.Refresh') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-checkbox v-model="autoRefresh" @change="handleAutoRefreshChange">{{ $t('Monitor.Logs.Console.AutoRefresh') }}</el-checkbox>
      </el-col>
      <el-col :span="1.5">
        <el-checkbox v-model="lockScroll" @change="handleAutoRefreshChange">{{ $t('Monitor.Logs.Console.LockScroll') }}</el-checkbox>
      </el-col>
    </el-row>
    <el-row>
      <el-input ref="logInput" type="textarea" :rows="30" v-model="logStr" :readonly="true"></el-input>
    </el-row>
  </div>
</template>

<script setup name="MonitorConsoleLogs">
import { getConsoleLogs } from "@/api/monitor/console";

const { proxy } = getCurrentInstance();

const autoRefresh = ref(true);
const interval = ref(3000);
const lockScroll = ref(false);
const logStr = ref("");
const logs = ref([]);
const sinceIndex = ref(0);
const maxLogLines = ref(1000);
const timer = ref(null);

onMounted(() => {
  refreshLogs(() => {
    handleAutoRefreshChange()
  });
});
function refreshLogs(func) {
  getConsoleLogs(sinceIndex.value).then(response => {
      sinceIndex.value = response.data.index;
      response.data.logs.forEach(item => logs.value.push(item));
      if (logs.value.length > maxLogLines.value) {
        logs.value.splice(0, logs.value.length - maxLogLines.value);
      }
      logStr.value = logs.value.join('');
      proxy.$nextTick(() => {
        if (!lockScroll.value && proxy.$refs.logInput) {
          let el = proxy.$refs.logInput.$el.querySelector('textarea')
          el.scrollTop = el.scrollHeight;
        }
      });
      if (func) {
        func();
      }
    }
  );
}
function handleRefresh() {
  refreshLogs();
}
function handleAutoRefreshChange() {
  if (autoRefresh.value) {
    timer.value = setInterval(() => {
      refreshLogs()
    }, interval.value);
  } else {
    clearInterval(timer.value);
  }
}
function handleClean() {
  logs.value = [];
  logStr.value = "";
}
function handleClose() {
  const obj = { path: "/monitor/logs" };
  proxy.$tab.closeOpenPage(obj);
}
onBeforeUnmount(() => {
  clearInterval(timer.value);
});
</script>

