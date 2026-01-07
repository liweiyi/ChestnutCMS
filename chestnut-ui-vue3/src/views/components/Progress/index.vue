<template>
    <div class="app-container-progress">
      <el-dialog :title="title"
                 v-model="visible"
                 width="500px"
                 :close-on-click-modal="false"
                 append-to-body
                 center>
      <div class="percent_info">{{progressMessage}}</div>
      <div class="err_messages" v-if="hasErrMessages()" v-html="formatErrMsg"></div>
      <el-progress :text-inside="true" :stroke-width="15" :percentage="percent" :status="progressStatus"></el-progress>
    </el-dialog>
  </div>
</template>
<style scoped>
.percent_info {
  padding: 10px 5px;
  width: 450px;
  line-height: 20px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.err_messages {
  max-height: 100px;
  overflow-y: scroll;
}
</style>
<script setup name="Progress">
import { getTaskInfo } from "@/api/system/async";

// 获取当前实例
const { proxy } = getCurrentInstance();

// Props定义
const props = defineProps({
  open: {
    type: Boolean,
    default: false,
    required: true
  },
  taskId: {
    type: String,
    required: true,
    default: ""
  },
  title: {
    type: String,
    default: "任务进度",
    required: false
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
  },
  messageClass: {
    type: String,
    default: "",
    required: false
  }
})

// Emits定义
const emit = defineEmits(['update:open', 'close'])

// 响应式数据
const visible = ref(props.open)
const progressMessage = ref(proxy.$t('Component.Progress.TaskIsRunning'))
const errMessages = ref("")
const percent = ref(0)
const progressStatus = ref(null)
const timer = ref(undefined)
const resultStatus = ref(undefined)

// 计算属性
const formatErrMsg = computed(() => {
  return hasErrMessages() ? errMessages.value.join("<br/>") : "";
})

// 方法
const startInterval = () => {
  if (props.taskId != "") {
    getProgressInfo();
    timer.value = setInterval(getProgressInfo, props.interval);
  }
}

const getProgressInfo = () => {
  if (!props.taskId || props.taskId == '') {
    return;
  }
  getTaskInfo(props.taskId).then(response => {
    progressMessage.value = response.data.progressMessage;
    percent.value = response.data.percent;
    resultStatus.value = response.data.status;
    if (response.data.status == 'SUCCESS' || response.data.status == 'INTERRUPTED') {
      clearInterval(timer.value);
      const successMsg = response.data.progressMessage ? response.data.progressMessage : props.title + proxy.$t('Component.Progress.Completed');
      proxy.$modal.msgSuccess(successMsg, props.messageClass);
      progressStatus.value = "success";
      errMessages.value = response.data.errMessages;
      if (props.autoClose && !hasErrMessages()) {
        visible.value = false;
      }
    } else if (response.data.status == 'FAILED') {
      clearInterval(timer.value);
      percent.value = 100;
      progressStatus.value = "exception"
      errMessages.value = response.data.errMessages;
    } else {

    }
  });
}

const hasErrMessages = () => {
  return errMessages.value && errMessages.value != null && errMessages.value.length > 0;
}

// 取消按钮
const handleClose = () => {
  if (!visible.value) {
    clearInterval(timer.value);
    emit('update:open', false);
    emit("close", { status: resultStatus.value });
    percent.value = 0;
    progressMessage.value = "";
    errMessages.value = [];
    progressStatus.value = undefined;
    resultStatus.value = undefined;
  }
}

// 监听器
watch(() => props.open, (newVal) => {
  visible.value = newVal;
})

watch(visible, (newVal) => {
  if (!newVal) {
    handleClose();
  } else {
    startInterval();
  }
})

// 生命周期
onBeforeUnmount(() => {
  clearInterval(timer.value);
})
</script>