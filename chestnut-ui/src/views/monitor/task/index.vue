<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb12">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="small"
          @click="handleAdd"
        >{{ $t('Common.Add') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="small"
          :disabled="single"
          @click="handleUpdate"
        >{{ $t('Common.Edit') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="small"
          :disabled="multiple"
          @click="handleDelete"
        >{{ $t('Common.Delete') }}</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-row v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="small" class="el-form-search mb12" :inline="true">
        <el-form-item :label="$t('Monitor.ScheduledTask.Status')" prop="status">
          <el-select 
            clearable
            v-model="queryParams.status"
            @keyup.enter.native="handleQuery">
            <el-option
              v-for="dict in dict.type.EnableOrDisable"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button-group>
            <el-button type="primary" icon="el-icon-search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
            <el-button icon="el-icon-refresh" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
          </el-button-group>
        </el-form-item>
      </el-form>
    </el-row>

    <el-table v-loading="loading" :data="taskList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" width="140" prop="taskId" />
      <el-table-column :label="$t('Monitor.ScheduledTask.Name')" align="center" prop="taskTypeName" />
      <el-table-column :label="$t('Monitor.ScheduledTask.Type')" align="center" prop="taskType" />
      <el-table-column :label="$t('Monitor.ScheduledTask.Status')" align="center" width="120">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.EnableOrDisable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Monitor.Async.ReadyTime')" align="center" prop="startTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.startTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.BeginTime')" align="center" prop="startTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.startTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.EndTime')" align="center" prop="endTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.endTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" width="330" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button 
            v-if="scope.row.status=='1'"
            size="small"
            type="text"
            icon="el-icon-circle-check"
            @click="handleEnable(scope.row)"
          >{{ $t('Common.Enable') }}</el-button>
          <el-button 
            v-if="scope.row.status=='0'"
            size="small"
            type="text"
            icon="el-icon-circle-close"
            @click="handleDisable(scope.row)"
          >{{ $t('Common.Disable') }}</el-button>
          <el-button 
            v-if="scope.row.status=='1'"
            size="small"
            type="text"
            icon="el-icon-video-play"
            @click="handleExecOnce(scope.row)"
          >{{ $t('Monitor.ScheduledTask.ExecOnce') }}</el-button>
          <el-button 
            size="small"
            type="text"
            icon="el-icon-time"
            @click="handleShowLogs(scope.row)"
          >{{ $t('Monitor.ScheduledTask.Logs') }}</el-button>
          <el-button 
            v-if="scope.row.status=='1'"
            size="small"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
          >{{ $t('Common.Edit') }}</el-button>
          <el-button 
            v-if="scope.row.status=='1'"
            size="small"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
          >{{ $t('Common.Delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="160px">
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
            <el-radio label="Cron">Cron</el-radio>
            <el-radio label="Periodic">{{ $t('Monitor.ScheduledTask.TriggerTypePeriodic') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.taskTrigger=='Cron'" :label="$t('Monitor.ScheduledTask.CronExpression')" prop="data.cron">
          <el-input v-model="form.data.cron" />
        </el-form-item>
        <el-form-item v-if="form.taskTrigger=='Periodic'" :label="$t('Monitor.ScheduledTask.FixedRate')" prop="data.fixedRate">
          <el-radio-group v-model="form.data.fixedRate">
            <el-radio
              v-for="dict in dict.type.YesOrNo"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
          <el-tooltip class="item ml10" effect="dark" content="" placement="top">
            <div slot="content">
              {{ $t('Monitor.ScheduledTask.FixedRateTipY') }}<br/>{{ $t('Monitor.ScheduledTask.FixedRateTipN') }}
            </div>
            <i class="el-icon-info" />
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
              v-for="dict in dict.type.EnableOrDisable"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">{{ $t('Common.Submit') }}</el-button>
        <el-button @click="cancel">{{ $t('Common.Cancel') }}</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="$t('Monitor.ScheduledTask.LogDialogTitle')" :visible.sync="openLogs" width="880px" append-to-body>
      <el-button 
        type="danger"
        plain
        icon="el-icon-delete"
        size="small" 
        @click="handleDeleteLogs"
        class="mb12"
      >{{ $t('Common.Delete') }}</el-button>
      <el-table v-loading="logLoading" :data="logList" @selection-change="handleLogSelectionChange" size="mini">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column :label="$t('Monitor.ScheduledTask.Type')" align="center" prop="taskType" />
        <el-table-column :label="$t('Common.BeginTime')" align="center" prop="startTime" width="160">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.startTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Common.EndTime')" align="center" prop="endTime" width="160">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.endTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Monitor.ScheduledTask.ExecResult')" align="center" width="100">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.SuccessOrFail" :value="scope.row.result"/>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Monitor.ScheduledTask.ErrMsg')" align="center" width="100">
          <template slot-scope="scope">
            <el-link v-if="scope.row.result=='1'" icon="el-icon-view" @click="handleShowMessage(scope.row)"></el-link>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="logTotal>0"
        :total="logTotal"
        :page.sync="logQueryParams.pageNum"
        :limit.sync="logQueryParams.pageSize"
        @pagination="loadTaskLogs"
      />
      <div slot="footer" class="dialog-footer">
        <el-button @click="handleCloseTaskLogs">{{ $t('Common.Close') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getTaskTypes, listTask, getTask, delTask, addTask, updateTask, enableTask, disableTask, executeTask, getTaskLogs, delTaskLogs } from "@/api/monitor/task";

export default {
  name: "MonitorTaskIndex",
  dicts: ['YesOrNo', 'EnableOrDisable', 'SuccessOrFail'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      taskList: [],
      title: "",
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        status: undefined,
      },
      taskTypeOptions: [],
      form: {},
      rules: {
        taskType: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
        ],
        status: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
        ],
        taskTrigger: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
        ],
        'data.cron': [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
        ],
        'data.fixedRate': [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
        ],
        'data.seconds': [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
        ],
        'data.delaySeconds': [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
        ]
      },
      logLoading: false,
      openLogs: false,
      logIds: [],
      logList: [],
      logTotal: 0,
      logQueryParams: {
        pageNum: 1,
        pageSize: 10,
        taskId: undefined
      },
    };
  },
  created() {
    this.loadTaskTypes();
    this.getList();
  },
  methods: {
    loadTaskTypes() {
      getTaskTypes().then(response => {
        this.taskTypeOptions = response.data
      })
    },
    getList() {
      this.loading = true;
      listTask(this.queryParams).then(response => {
          this.taskList = response.data.rows;
          this.total = parseInt(response.data.total);
          this.loading = false;
        }
      );
    },
    cancel() {
      this.open = false;
      this.reset();
    },
    reset() {
      this.form = {
        status: "1",
        taskTrigger: "Cron",
        fixedRate: "N",
        data: {}
      };
      this.resetForm("form");
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = this.$t('Monitor.ScheduledTask.AddTitle');
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.taskId)
      this.single = selection.length!=1
      this.multiple = !selection.length
    },
    handleUpdate(row) {
      this.reset();
      const taskId = row.taskId || this.ids
      getTask(taskId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = this.$t('Monitor.ScheduledTask.EditTitle');
      });
    },
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.taskId != undefined) {
            updateTask(this.form).then(response => {
              this.$modal.msgSuccess(this.$t("Common.Success"));
              this.open = false;
              this.getList();
            });
          } else {
            addTask(this.form).then(response => {
              this.$modal.msgSuccess(this.$t("Common.AddSuccess"));
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDelete(row) {
      const taskIds = row.taskId ? [ row.taskId ] : this.ids;
      this.$modal.confirm(this.$t('Common.ConfirmDelete', [ taskIds ])).then(function() {
          return delTask(taskIds);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess(this.$t("Common.DeleteSuccess"));
        }).catch(() => {});
    },
    handleEnable(row) {
      if (!row.taskId && this.ids.length == 0) {
        return;
      }
      const taskId = row.taskId ? row.taskId : this.ids[0];
      enableTask(taskId).then(response => {
        this.$modal.msgSuccess(this.$t("Common.OpSuccess"));
        this.getList();
      });
    },
    handleDisable(row) {
      if (!row.taskId && this.ids.length == 0) {
        return;
      }
      const taskId = row.taskId ? row.taskId : this.ids[0];
      disableTask(taskId).then(response => {
        this.$modal.msgSuccess(this.$t("Common.OpSuccess"));
        this.getList();
      });
    },
    handleExecOnce(row) {
      if (!row.taskId && this.ids.length == 0) {
        return;
      }

      const taskId = row.taskId ? row.taskId : this.ids[0];
      executeTask(taskId).then(response => {
        this.$modal.msgSuccess(this.$t("Common.OpSuccess"));
        this.getList();
      });
    },
    handleShowLogs(row) {
      if (!row.taskId) {
        return;
      }
      this.openLogs = true;
      this.logQueryParams.taskId = row.taskId
      this.loadTaskLogs();
    },
    handleLogSelectionChange(selection) {
      this.logIds = selection.map(item => item.logId)
    },
    loadTaskLogs() {
      if (!this.logQueryParams.taskId) {
        return;
      }
      this.logLoading = true
      getTaskLogs(this.logQueryParams, ).then(response => {
          this.logList = response.data.rows;
          this.logTotal = parseInt(response.data.total);
          this.logLoading = false;
      });
    },
    handleCloseTaskLogs() {
      this.openLogs = false;
      this.logQueryParams.taskId = undefined;
    },
    handleDeleteLogs() {
      if (this.logIds.length == 0) {
        return;
      }
      const logIdArr = this.logIds;
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function() {
        return delTaskLogs(logIdArr);
      }).then(() => {
        this.$modal.msgSuccess(this.$t("Common.DeleteSuccess"));
        this.loadTaskLogs();
      }).catch(() => {});
    },
    handleShowMessage(row) {
      if (!row.logId) {
        return;
      }
      this.$modal.alert(row.message)
    }
  }
};
</script>
