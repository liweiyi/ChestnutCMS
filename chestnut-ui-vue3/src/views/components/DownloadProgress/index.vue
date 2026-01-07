<template>
  <div class="app-container-progress">
    <el-dialog :title="props.title" v-model="visible" width="500px" :close-on-click-modal="false" append-to-body center>
      <div class="percent_info">{{ progressMessage }}</div>
      <el-progress :text-inside="true" :stroke-width="15" :percentage="percent" :status="progressStatus" text-color="#FFF"></el-progress>
    </el-dialog>
  </div>
</template>
<script setup name="AsyncDownloadProgress">
import axios from "axios";

const { proxy } = getCurrentInstance();

const props = defineProps({
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
});

const emit = defineEmits(['update:open', 'update:url']);

const visible = ref(false);
const progressMessage = ref(proxy.$t("Common.Downloading"));
const downloadUrl = ref("");
const percent = ref(0);
const progressStatus = ref(null);
const cancelToken = ref(undefined);
const timer = ref(undefined);
const lastUpdateTime = ref(0);
const idleTimeout = ref(30000); // 空闲超时时间30秒

watch(() => props.open, (newVal) => {
  visible.value = newVal;
  if (visible.value && downloadUrl.value.length > 0) {
    startDownload();
  }
}, { immediate: true });

watch(visible, (newVal) => {
  if (!newVal) {
    handleClose();
  }
});

watch(() => props.url, (newVal) => {
  if (newVal && newVal.length > 0) {
    downloadUrl.value = newVal;
    if (visible.value) {
      startDownload();
    }
  }
});

const startDownload = async () => {
  const CancelToken = axios.CancelToken;
  cancelToken.value = CancelToken.source();

  initActivityMonitor();

  axios({
    method: 'get',
    url: downloadUrl.value,
    responseType: 'blob',
    headers: { ...proxy.$auth.getTokenHeader() },
    timeout: 0,
    onDownloadProgress: (event) => {
      handleProgress(event)
      resetActivityTimer();
    }
  }).then(response => {
    clearInterval(timer.value);
    progressMessage.value = proxy.$t("Common.Downloaded")
    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', props.fileName);
    document.body.appendChild(link);
    link.click();
    // 清理资源
    window.URL.revokeObjectURL(url);
    document.body.removeChild(link);
    if (props.autoClose) {
      visible.value = false;
    }
  }).catch((error) => {
    progressStatus.value = "exception";
    progressMessage.value = error;
    clearInterval(timer.value);
  });
}

const handleProgress = (event) => {
  if (event.total) {
    percent.value = Math.round((event.loaded * 100) / event.total);
    lastUpdateTime.value = Date.now();
  }
}
const initActivityMonitor = () => {
  lastUpdateTime.value = Date.now();
  timer.value = setInterval(() => {
    const idleTime = Date.now() - lastUpdateTime.value;
    if (idleTime > idleTimeout.value) {
      cancelDownload(proxy.$t("Common.DownloadTimeout"));
    }
  }, props.interval);
}
const resetActivityTimer = () => {
  lastUpdateTime.value = Date.now();
}
const cancelDownload = (reason) => {
  cancelToken.value && cancelToken.value.cancel(reason);
  clearInterval(timer.value);
  progressMessage.value = `${reason}`;
}
const handleClose = () => {
  if (!visible.value) {
    clearInterval(timer.value);
    emit('update:open', false);
    emit('update:url', "");
    percent.value = 0;
    progressMessage.value = "";
    lastUpdateTime.value = 0;
    progressStatus.value = undefined;
    downloadUrl.value = "";
  }
};

onBeforeUnmount(() => {
  clearInterval(timer.value);
})
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