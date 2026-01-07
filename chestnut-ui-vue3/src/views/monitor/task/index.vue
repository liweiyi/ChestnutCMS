<template>
  <div class="app-container">
    <el-row justify="space-between">
      <el-col :span="12">
        <el-row :gutter="10">
          <el-col :span="1.5">
            <el-button
              type="primary"
              plain
              icon="Plus"
              @click="handleAdd"
            >{{ $t('Common.Add') }}</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button
              type="success"
              plain
              icon="Edit"
              :disabled="single"
              @click="handleUpdate"
            >{{ $t('Common.Edit') }}</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button
              type="danger"
              plain
              icon="Delete"
              :disabled="multiple"
              @click="handleDelete"
            >{{ $t('Common.Delete') }}</el-button>
          </el-col>
        </el-row>
      </el-col>
      <el-col :span="12" style="text-align: right;">
        <el-form :model="queryParams" ref="queryFormRef" class="el-form-search" :inline="true">
          <el-form-item prop="status">
            <el-select 
              clearable
              v-model="queryParams.status"
              :placeholder="$t('Monitor.ScheduledTask.Status')"
              style="width: 100px"
              @keyup.enter="handleQuery">
              <el-option
                v-for="dict in EnableOrDisable"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button-group>
              <el-button type="primary" icon="Search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
              <el-button icon="Refresh" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
            </el-button-group>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="taskList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="$t('Monitor.ScheduledTask.Name')" align="left" prop="taskTypeName" />
      <el-table-column :label="$t('Monitor.ScheduledTask.Type')" align="left" prop="taskType" />
      <el-table-column :label="$t('Monitor.ScheduledTask.Status')" align="center" width="80">
        <template #default="scope">
          <dict-tag :options="EnableOrDisable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Monitor.Async.ReadyTime')" align="center" prop="startTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.startTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.BeginTime')" align="center" prop="startTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.startTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.EndTime')" align="center" prop="endTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.endTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" width="360" align="center">
        <template #default="scope">
          <el-button 
            v-if="scope.row.status=='1'"
            link
            type="success"
            icon="CircleCheck"
            @click="handleEnable(scope.row)"
          >{{ $t('Common.Enable') }}</el-button>
          <el-button 
            v-if="scope.row.status=='0'"
            link
            type="danger"
            icon="CircleClose"
            @click="handleDisable(scope.row)"
          >{{ $t('Common.Disable') }}</el-button>
          <el-button 
            v-if="scope.row.status=='1'"
            type="text"
            icon="VideoPlay"
            @click="handleExecOnce(scope.row)"
          >{{ $t('Monitor.ScheduledTask.ExecOnce') }}</el-button>
          <el-button 
            type="text"
            icon="Timer"
            @click="handleShowLogs(scope.row)"
          >{{ $t('Monitor.ScheduledTask.Logs') }}</el-button>
          <el-button 
            v-if="scope.row.status=='1'"
            type="text"
            icon="Edit"
            @click="handleUpdate(scope.row)"
          >{{ $t('Common.Edit') }}</el-button>
          <el-button 
            v-if="scope.row.status=='1'"
            link
            type="danger"
            icon="Delete"
            @click="handleDelete(scope.row)"
          >{{ $t('Common.Delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog v-model="open" :title="title" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="160px">
        <el-form-item :label="$t('Monitor.ScheduledTask.Type')" prop="taskType">
          <el-select v-model="form.taskType" :disabled="form.taskId&&form.taskId>0">
            <el-option
              v-for="dict in taskTypeOptions"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('Monitor.ScheduledTask.TriggerType')" prop="taskTrigger">
          <el-radio-group v-model="form.taskTrigger">
            <el-radio value="Cron">Cron</el-radio>
            <el-radio value="Periodic">{{ $t('Monitor.ScheduledTask.TriggerTypePeriodic') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.taskTrigger=='Cron'" :label="$t('Monitor.ScheduledTask.CronExpression')" prop="data.cron">
          <el-input v-model="form.data.cron" />
        </el-form-item>
        <el-form-item v-if="form.taskTrigger=='Periodic'" :label="$t('Monitor.ScheduledTask.FixedRate')" prop="data.fixedRate">
          <el-radio-group v-model="form.data.fixedRate">
            <el-radio
              v-for="dict in YesOrNo"
              :key="dict.value"
              :value="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
          <el-tooltip class="item ml10" effect="dark" content="" placement="top">
            <template #content>
              {{ $t('Monitor.ScheduledTask.FixedRateTipY') }}<br/>{{ $t('Monitor.ScheduledTask.FixedRateTipN') }}
            </template>
            <el-icon><InfoFilled /></el-icon>
          </el-tooltip>
        </el-form-item>
        <el-form-item v-if="form.taskTrigger=='Periodic'" :label="$t('Monitor.ScheduledTask.PeriodicSeconds')" prop="data.seconds">
          <el-input-number v-model="form.data.seconds" :min="1" />
        </el-form-item>
        <el-form-item v-if="form.taskTrigger=='Periodic'" :label="$t('Monitor.ScheduledTask.PeriodicDelaySeconds')" prop="data.delaySeconds">
          <el-input-number v-model="form.data.delaySeconds"  :min="0" />
        </el-form-item>
        <el-form-item :label="$t('Monitor.ScheduledTask.Status')" prop="status">
          <el-radio-group v-model="form.status" :disabled="form.taskId&&form.taskId>0">
            <el-radio
              v-for="dict in EnableOrDisable"
              :key="dict.value"
              :value="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">{{ $t('Common.Submit') }}</el-button>
          <el-button @click="cancel">{{ $t('Common.Cancel') }}</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="openLogs" :title="$t('Monitor.ScheduledTask.LogDialogTitle')" width="880px" append-to-body>
      <el-button 
        type="danger"
        plain
        icon="Delete"
        @click="handleDeleteLogs"
        class="mb8"
      >{{ $t('Common.Delete') }}</el-button>
      <el-table v-loading="logLoading" :data="logList" @selection-change="handleLogSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column :label="$t('Monitor.ScheduledTask.Type')" align="center" prop="taskType" />
        <el-table-column :label="$t('Common.BeginTime')" align="center" prop="startTime" width="160">
          <template #default="scope">
            <span>{{ parseTime(scope.row.startTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Common.EndTime')" align="center" prop="endTime" width="160">
          <template #default="scope">
            <span>{{ parseTime(scope.row.endTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Monitor.ScheduledTask.ExecResult')" align="center" width="100">
          <template #default="scope">
            <dict-tag :options="SuccessOrFail" :value="scope.row.result"/>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Monitor.ScheduledTask.ErrMsg')" align="center" width="100">
          <template #default="scope">
            <el-link v-if="scope.row.result=='1'" icon="View" @click="handleShowMessage(scope.row)"></el-link>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="logTotal>0"
        :total="logTotal"
        v-model:page="logQueryParams.pageNum"
        v-model:limit="logQueryParams.pageSize"
        @pagination="loadTaskLogs"
      />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCloseTaskLogs">{{ $t('Common.Close') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MonitorTaskIndex">
import { getTaskTypes, listTask, getTask, delTask, addTask, updateTask, enableTask, disableTask, executeTask, getTaskLogs, delTaskLogs } from "@/api/monitor/task";

const { proxy } = getCurrentInstance();
const { YesOrNo, EnableOrDisable, SuccessOrFail } = proxy.useDict('YesOrNo', 'EnableOrDisable', 'SuccessOrFail');

const loading = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const taskList = ref([]);
const title = ref("");
const open = ref(false);
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  status: undefined,
});
const taskTypeOptions = ref([]);
const form = ref({
  status: "1",
  taskTrigger: "Cron",
  fixedRate: "N",
  data: {}
});
const rules = reactive({
  taskType: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  status: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  taskTrigger: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  'data.cron': [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  'data.fixedRate': [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  'data.seconds': [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  'data.delaySeconds': [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ]
});
const logLoading = ref(false);
const openLogs = ref(false);
const logIds = ref([]);
const logList = ref([]);
const logTotal = ref(0);
const logQueryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  taskId: undefined
});

function loadTaskTypes() {
  getTaskTypes().then(response => {
    taskTypeOptions.value = response.data
  })
}

function getList() {
  loading.value = true;
  listTask(queryParams).then(response => {
      taskList.value = response.data.rows;
      total.value = parseInt(response.data.total);
      loading.value = false;
    }
  );
}

function cancel() {
  open.value = false;
  reset();
}

function reset() {
  form.value = {
    status: "1",
    taskTrigger: "Cron",
    fixedRate: "N",
    data: {}
  };
  proxy.resetForm("formRef");
}

function handleQuery() {
  queryParams.pageNum = 1;
  getList();
}

function resetQuery() {
  proxy.resetForm("queryFormRef");
  handleQuery();
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = proxy.$t('Monitor.ScheduledTask.AddTitle');
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.taskId)
  single.value = selection.length!=1
  multiple.value = !selection.length
}

function handleUpdate(row) {
  reset();
  const taskId = row.taskId || ids.value[0]
  getTask(taskId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = proxy.$t('Monitor.ScheduledTask.EditTitle');
  });
}

function submitForm() {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      if (form.value.taskId != undefined) {
        updateTask(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t("Common.OpSuccess"));
          open.value = false;
          getList();
        });
      } else {
        addTask(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t("Common.OpSuccess"));
          open.value = false;
          getList();
        });
      }
    }
  });
}

function handleDelete(row) {
  const taskIds = row.taskId ? [ row.taskId ] : ids.value;
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete', [ taskIds ])).then(function() {
      return delTask(taskIds);
    }).then(() => {
      getList();
      proxy.$modal.msgSuccess(proxy.$t("Common.DeleteSuccess"));
    }).catch(() => {});
}

function handleEnable(row) {
  if (!row.taskId && ids.value.length == 0) {
    return;
  }
  const taskId = row.taskId ? row.taskId : ids.value[0];
  enableTask(taskId).then(response => {
    proxy.$modal.msgSuccess(proxy.$t("Common.OpSuccess"));
    getList();
  });
}

function handleDisable(row) {
  if (!row.taskId && ids.value.length == 0) {
    return;
  }
  const taskId = row.taskId ? row.taskId : ids.value[0];
  disableTask(taskId).then(response => {
    proxy.$modal.msgSuccess(proxy.$t("Common.OpSuccess"));
    getList();
  });
}

function handleExecOnce(row) {
  if (!row.taskId && ids.value.length == 0) {
    return;
  }

  const taskId = row.taskId ? row.taskId : ids.value[0];
  executeTask(taskId).then(response => {
    proxy.$modal.msgSuccess(proxy.$t("Common.OpSuccess"));
    getList();
  });
}

function handleShowLogs(row) {
  if (!row.taskId) {
    return;
  }
  openLogs.value = true;
  logQueryParams.taskId = row.taskId
  loadTaskLogs();
}

function handleLogSelectionChange(selection) {
  logIds.value = selection.map(item => item.logId)
}

function loadTaskLogs() {
  if (!logQueryParams.taskId) {
    return;
  }
  logLoading.value = true
  getTaskLogs(logQueryParams).then(response => {
      logList.value = response.data.rows;
      logTotal.value = parseInt(response.data.total);
      logLoading.value = false;
  });
}

function handleCloseTaskLogs() {
  openLogs.value = false;
  logQueryParams.taskId = undefined;
}

function handleDeleteLogs() {
  if (logIds.value.length == 0) {
    return;
  }
  const logIdArr = logIds.value;
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function() {
    return delTaskLogs(logIdArr);
  }).then(() => {
    proxy.$modal.msgSuccess(proxy.$t("Common.DeleteSuccess"));
    loadTaskLogs();
  }).catch(() => {});
}

function handleShowMessage(row) {
  if (!row.logId) {
    return;
  }
  proxy.$modal.alert(row.message)
}

onMounted(() => {
  loadTaskTypes();
  getList();
});
</script>
