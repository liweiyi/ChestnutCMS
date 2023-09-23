<template>
    <div class="app-container-progress">
      <el-dialog :title="title"
                 :visible.sync="visible"
                 width="500px"
                 :close-on-click-modal="false"
                 append-to-body
                 center>
      <div class="percent_info">{{progressMessage}}</div>
      <div class="err_messages" v-if="this.hasErrMessages()" v-html="formatErrMsg"></div>
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
<script>
import { getTaskInfo } from "@/api/system/async";

export default {
  name: "AsyncTaskProgress",
  props: {
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
    }
  },
  watch: {
    open (newVal) {
      this.visible = newVal;
    },
    visible (newVal) {
      if (!newVal) {
        this.handleClose();
      } else {
        this.startInterval();
      }
    }
  },
  computed: {
    formatErrMsg() {
      return this.hasErrMessages() ? this.errMessages.join("<br/>") : "";
    }
  },
  data () {
    return {
      visible: this.open,
      progressMessage: this.$t('Component.Progress.TaskIsRunning'),
      errMessages: "",
      percent: 0,
      progressStatus: null,
      timer: undefined,
      resultStatus: undefined,
    };
  },
  methods: {
    startInterval () {
      if (this.taskId != "") {
        this.getProgressInfo();
        this.timer = setInterval(this.getProgressInfo, this.interval);
      }
    },
    getProgressInfo () {
      if (!this.taskId || this.taskId == '') {
        return;
      }
      getTaskInfo(this.taskId).then(response => {
        this.progressMessage = response.data.progressMessage;
        this.percent = response.data.percent;
        this.resultStatus = response.data.status;
        if (response.data.status == 'SUCCESS' || response.data.status == 'INTERRUPTED') {
          clearInterval(this.timer);
          const successMsg = response.data.progressMessage ? response.data.progressMessage : this.title + this.$t('Component.Progress.Completed');
          this.$modal.msgSuccess(successMsg);
          this.progressStatus = "success";
          this.errMessages = response.data.errMessages;
          if (this.autoClose && !this.hasErrMessages()) {
            this.visible = false;
          }
        } else if (response.data.status == 'FAILED') {
          clearInterval(this.timer);
          this.percent = 100;
          this.progressStatus = "exception"
          this.errMessages = response.data.errMessages;
        } else {

        }
      });
    },
    hasErrMessages() {
      return this.errMessages && this.errMessages != null && this.errMessages.length > 0;
    },
    // 取消按钮
    handleClose () {
      if (!this.visible) {
        clearInterval(this.timer);
        this.$emit('update:open', false);
        this.$emit("close", { status: this.resultStatus });
        this.percent = 0;
        this.progressMessage = "";
        this.errMessages = [];
        this.progressStatus = undefined;
        this.resultStatus = undefined;
      }
    }
  },
  beforeDestroy() {
    clearInterval(this.timer);
  }
};
</script>