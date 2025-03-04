<template>
    <div class="app-container-progress">
      <el-dialog :title="title"
                 :visible.sync="visible"
                 width="500px"
                 :close-on-click-modal="false"
                 append-to-body
                 center>
      <div class="percent_info">{{progressMessage}}</div>
      <el-progress :text-inside="true" :stroke-width="15" :percentage="percent" :status="progressStatus" text-color="#FFF"></el-progress>
    </el-dialog>
  </div>
</template>
<script>
import axios from "axios";
import { getToken } from "@/utils/auth";

export default {
  name: "AsyncDownloadProgress",
  props: {
    open: {
      type: Boolean,
      default: false,
      required: true
    },
    url: {
      type: String,
      required: true,
      default: ""
    },
    title: {
      type: String,
      default: "",
      required: false
    },
    fileName: {
      type: String,
      default: "download.zip",
      required: false,
    },
    autoClose: {
      type: Boolean,
      default: true,
      required: false
    },
    interval: {
      type: Number,
      default: 1000,
      required: false
    }
  },
  watch: {
    open (newVal) {
      this.visible = newVal;
      if (this.visible && this.downloadUrl.length > 0) {
        this.startDownload();
      }
    },
    visible (newVal) {
      if (!newVal) {
        this.handleClose();
      }
    },
    url (newVal) {
      if (newVal && newVal.length > 0) {
        this.downloadUrl = newVal;
        if (this.visible) {
          this.startDownload();
        }
      }
    }
  },
  data () {
    return {
      visible: this.open,
      progressMessage: this.$t("Common.Downloading"),
      downloadUrl: "",
      percent: 0,
      progressStatus: null,
      cancelToken: undefined,
      timer: undefined,
      lastUpdateTime: 0,
      idleTimeout: 30_000, // 空闲超时时间30秒
    };
  },
  methods: {
    async startDownload() {
      const CancelToken = axios.CancelToken;
      this.cancelToken = CancelToken.source();

      this.initActivityMonitor();

      axios({
        method: 'get',
        url: this.downloadUrl,
        responseType: 'blob',
        headers: { Authorization: "Bearer " + getToken() },
        timeout: 0,
        onDownloadProgress: (event) => {
          this.handleProgress(event)
          this.resetActivityTimer();
        }
      }).then(response => {
        clearInterval(this.timer);
        this.progressMessage = this.$t("Common.Downloaded")
        // 创建下载链接
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', 'SiteTheme.zip');
        document.body.appendChild(link);
        link.click();
        // 清理资源
        window.URL.revokeObjectURL(url);
        document.body.removeChild(link);
        if (this.autoClose) {
          this.visible = false;
        }
      }).catch((error) => {
        this.progressStatus = "exception";
        this.progressMessage = error;
        clearInterval(this.timer);
      });
    },
    handleProgress(event) {
      if (event.total) {
        this.percent = Math.round((event.loaded * 100) / event.total);
        this.lastUpdateTime = Date.now();
      }
    },
    initActivityMonitor() {
      this.lastUpdateTime = Date.now();
      this.timer = setInterval(() => {
        const idleTime = Date.now() - this.lastUpdateTime;
        if (idleTime > this.idleTimeout) {
          this.cancelDownload(this.$t("Common.DownloadTimeout"));
        }
      }, this.interval);
    },
    resetActivityTimer() {
      this.lastUpdateTime = Date.now();
    },
    cancelDownload(reason) {
      this.cancelToken && this.cancelToken.cancel(reason);
      clearInterval(this.timer);
      this.progressMessage = `${reason}`;
    },
    handleClose () {
      if (!this.visible) {
        clearInterval(this.timer);
        this.$emit('update:open', false);
        this.$emit('update:url', "");
        this.percent = 0;
        this.progressMessage = "";
        this.lastUpdateTime = 0;
        this.progressStatus = undefined;
        this.downloadUrl = "";
      }
    }
  },
  beforeDestroy() {
    clearInterval(this.timer);
  }
};
</script>
<style scoped>
.percent_info {
  padding: 10px 5px;
  width: 450px;
  line-height: 20px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
</style>