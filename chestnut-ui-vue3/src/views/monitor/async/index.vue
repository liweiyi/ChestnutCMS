<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      class="el-form-search"
    >
      <el-form-item prop="type">
        <el-input
          v-model="queryParams.type"
          :placeholder="$t('Monitor.Async.Type')"
        />
      </el-form-item>
      <el-form-item prop="id">
        <el-input
          v-model="queryParams.id"
          :placeholder="$t('Monitor.Async.TaskID')"
        />
      </el-form-item>
      <el-form-item>
        <el-button-group>
          <el-button type="primary" icon="Search" @click="handleQuery">{{
            $t("Common.Search")
          }}</el-button>
          <el-button icon="Refresh" @click="resetQuery">{{
            $t("Common.Reset")
          }}</el-button>
        </el-button-group>
      </el-form-item>
    </el-form>
    <el-table v-loading="loading" :data="taskList">
      <el-table-column :label="$t('Common.RowNo')" type="index" width="55" align="center" />
      <el-table-column
        :label="$t('Monitor.Async.Type')"
        align="center"
        width="120"
        prop="type"
      />
      <el-table-column
        :label="$t('Monitor.Async.TaskID')"
        align="left"
        prop="taskId"
      />
      <el-table-column
        :label="$t('Monitor.Async.Status')"
        align="center"
        width="100"
      >
        <template #default="scope">
          <el-tag :type="statusType(scope.row.status)">{{
            scope.row.status
          }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('Monitor.Async.Percent')"
        align="center"
        width="100"
        prop="percent"
      >
        <template #default="scope">
          <span>{{ scope.row.percent }}%</span>
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('Monitor.Async.ErrMessage')"
        align="center"
        width="150"
      >
        <template #default="scope">
          <el-button
            v-if="hasErrMessages(scope.row)"
            @click="showErrMessages(scope.row)"
            >{{ $t("Common.View") }}</el-button
          >
          <span v-else>{{ $t("Common.None") }}</span>
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('Monitor.Async.ReadyTime')"
        align="center"
        prop="startTime"
        width="160"
      >
        <template #default="scope">
          <span>{{ parseTime(scope.row.startTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('Common.BeginTime')"
        align="center"
        prop="startTime"
        width="160"
      >
        <template #default="scope">
          <span>{{ parseTime(scope.row.startTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('Common.EndTime')"
        align="center"
        prop="endTime"
        width="160"
      >
        <template #default="scope">
          <span>{{ parseTime(scope.row.endTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('Common.Operation')"
        align="center"
        width="100"
       
      >
        <template #default="scope">
          <el-button
            v-if="scope.row.status == 'READY' || scope.row.status == 'RUNNING'"
            type="text"
            icon="VideoPause"
            @click="handleStop(scope.row)"
            >{{ $t("Monitor.Async.Stop") }}</el-button
          >
          <el-button
            type="text"
            icon="Delete"
            @click="handleDelete(scope.row)"
            >{{ $t("Common.Delete") }}</el-button
          >
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
<script setup name="MonitorAsyncIndex">
import { ElMessageBox } from "element-plus";
import { getTaskList, stopTask, removeTask } from "@/api/system/async";

const { proxy } = getCurrentInstance();

// 遮罩层
const loading = ref(true);
// 总条数
const total = ref(0);
// 异步任务表格数据
const taskList = ref([]);
// 查询参数
const queryParams = reactive({
  type: "",
  id: "",
});

function loadAsyncTaskList() {
  loading.value = true;
  getTaskList(queryParams).then((response) => {
    taskList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
}

function statusType(status) {
  if (status === "INTERRUPTED") {
    return "warning";
  } else if (status === "FAILED") {
    return "danger";
  } else if (status === "SUCCESS") {
    return "success";
  }
  return "default";
}

function hasErrMessages(row) {
  return (
    row.errMessages && row.errMessages != null && row.errMessages.length > 0
  );
}

function showErrMessages(row) {
  ElMessageBox.alert(
    row.errMessages.join("<br/>"),
    proxy.$t("Monitor.Async.ErrMessage"),
    {
      dangerouslyUseHTMLString: true,
    }
  );
}

function handleQuery() {
  loadAsyncTaskList();
}

function resetQuery() {
  proxy.resetForm("queryFormRef");
  handleQuery();
}

function handleStop(row) {
  stopTask([row.taskId]).then((response) => {
    proxy.$modal.msgSuccess(response.msg);
    loadAsyncTaskList();
  });
}

function handleDelete(row) {
  removeTask([row.taskId]).then((response) => {
    proxy.$modal.msgSuccess(response.msg);
    loadAsyncTaskList();
  });
}

onMounted(() => {
  loadAsyncTaskList();
});
</script>
