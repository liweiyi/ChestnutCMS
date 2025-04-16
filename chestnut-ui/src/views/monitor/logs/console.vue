<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb12" style="display: flex; align-items: center;">
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-delete"
          size="small"
          @click="handleClean"
        >{{ $t('Common.Clean') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-refresh"
          size="small"
          @click="handleRefresh"
        >{{ $t('Common.Refresh') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-checkbox v-model="autoRefresh" @change="handleAutoRefreshChange">自动刷新</el-checkbox>
      </el-col>
      <el-col :span="1.5">
        <el-checkbox v-model="lockScroll" @change="handleAutoRefreshChange">锁定滚动</el-checkbox>
      </el-col>
    </el-row>
    <el-row>
      <el-input ref="logInput" type="textarea" rows="30" v-model="logStr" :readonly="true"></el-input>
    </el-row>
  </div>
</template>

<script>
import { getConsoleLogs } from "@/api/monitor/console";
import { mount } from "sortablejs";

export default {
  name: "MonitorConsoleLogs",
  data() {
    return {
      autoRefresh: true,
      interval: 3000,
      lockScroll: false,
      logStr: "",
      logs: [],
      sinceIndex: 0,
      maxLogLines: 1000,
    };
  },
  created() {
    this.refreshLogs(() => {
      this.handleAutoRefreshChange()
    });
  },
  methods: {
    refreshLogs(func) {
      getConsoleLogs(this.sinceIndex).then(response => {
          this.sinceIndex = response.data.index;
          response.data.logs.forEach(item => this.logs.push(item));
          if (this.logs.length > this.maxLogLines) {
            this.logs.splice(0, this.logs.length - this.maxLogLines);
          }
          this.logStr = this.logs.join('');
          this.$nextTick(() => {
            if (!this.lockScroll && this.$refs.logInput) {
              let el = this.$refs.logInput.$el.querySelector('textarea')
              el.scrollTop = el.scrollHeight;
            }
          });
          if (func) {
            func();
          }
        }
      );
    },
    handleRefresh() {
      this.refreshLogs();
    },
    handleAutoRefreshChange() {
      if (this.autoRefresh) {
        this.timer = setInterval(() => {
          this.refreshLogs()
        }, this.interval);
      } else {
        clearInterval(this.timer);
      }
    },
    handleClean() {
      this.logs = [];
      this.logStr = "";
    },
    handleClose() {
      const obj = { path: "/monitor/logs" };
      this.$tab.closeOpenPage(obj);
    }
  },
  beforeDestroy() {
    clearInterval(this.timer);
  }
};
</script>

