<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="88px">
      <el-form-item :label="$t('Monitor.Job.JobName')" prop="jobName">
        <el-input
          v-model="queryParams.jobName"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="$t('Monitor.Job.JobName')" prop="jobGroup">
        <el-select v-model="queryParams.jobGroup" clearable>
          <el-option
            v-for="dict in dict.type.SysJobGroup"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="$t('Monitor.Job.Status')" prop="status">
        <el-select v-model="queryParams.status" clearable>
          <el-option
            v-for="dict in dict.type.EnableOrDisable"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['monitor:job:add']"
        >{{ $t('Common.Add') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['monitor:job:edit']"
        >{{ $t('Common.Edit') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['monitor:job:remove']"
        >{{ $t('Common.Delete') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['monitor:job:export']"
        >{{ $t('Common.Export') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="el-icon-s-operation"
          size="mini"
          @click="handleJobLog"
          v-hasPermi="['monitor:job:query']"
        >{{ $t('Monitor.Job.Logs') }}</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="jobList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="$t('Monitor.Job.JobId')" width="100" align="center" prop="jobId" />
      <el-table-column :label="$t('Monitor.Job.JobName')" align="center" prop="jobName" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Monitor.Job.JobGroup')" align="center" prop="jobGroup">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.SysJobGroup" :value="scope.row.jobGroup"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Monitor.Job.InvokeTarget')" align="center" prop="invokeTarget" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Monitor.Job.CronExpression')" align="center" prop="cronExpression" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Monitor.Job.Status')" align="center">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <span class="btn-cell-wrap">
            <el-button
              size="small"
              type="text"
              icon="el-icon-edit"
              @click="handleUpdate(scope.row)"
              v-hasPermi="['monitor:job:edit']"
            >{{ $t('Common.Edit') }}</el-button>
          </span>
          <span class="btn-cell-wrap">
            <el-button
              size="small"
              type="text"
              icon="el-icon-delete"
              @click="handleDelete(scope.row)"
              v-hasPermi="['monitor:job:remove']"
            >{{ $t('Common.Delete') }}</el-button>
          </span>
          <el-dropdown size="small" @command="(command) => handleCommand(command, scope.row)">
            <el-button size="small" type="text" icon="el-icon-d-arrow-right">{{ $t('Common.More') }}</el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="handleRun" icon="el-icon-caret-right"
                v-hasPermi="['monitor:job:changeStatus']">{{ $t('Monitor.Job.RunOnce') }}</el-dropdown-item>
              <el-dropdown-item command="handleView" icon="el-icon-view"
                v-hasPermi="['monitor:job:query']">{{ $t('Monitor.Job.Details') }}</el-dropdown-item>
              <el-dropdown-item command="handleJobLog" icon="el-icon-s-operation"
                v-hasPermi="['monitor:job:query']">{{ $t('Monitor.Job.Logs') }}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
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

    <!-- 添加或修改定时任务对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="140px">
        <el-row>
          <el-col :span="12">
            <el-form-item :label="$t('Monitor.Job.JobName')" prop="jobName">
              <el-input v-model="form.jobName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('Monitor.Job.JobGroup')" prop="jobGroup">
              <el-select v-model="form.jobGroup">
                <el-option
                  v-for="dict in dict.type.SysJobGroup"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item prop="invokeTarget">
              <span slot="label">
                {{ $t('Monitor.Job.InvokeTarget') }}
                <el-tooltip placement="top">
                  <div slot="content">
                    {{ $t('Monitor.Job.BeanExample') }}
                    <br />{{ $t('Monitor.Job.ClassExample') }}
                    <br />{{ $t('Monitor.Job.ParamsTips') }}
                  </div>
                  <i class="el-icon-question"></i>
                </el-tooltip>
              </span>
              <el-input v-model="form.invokeTarget" :placeholder="$t('Monitor.Job.Placeholder.InvokeTarget')" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('Monitor.Job.CronExpression')" prop="cronExpression">
              <el-input v-model="form.cronExpression">
                <template slot="append">
                  <el-button type="primary" @click="handleShowCron">
                    {{ $t('Monitor.Job.GenCron') }}
                    <i class="el-icon-time el-icon--right"></i>
                  </el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('Monitor.Job.MisfirePolicy')" prop="misfirePolicy">
              <el-radio-group v-model="form.misfirePolicy" size="small">
                <el-radio-button label="1">{{ $t('Monitor.Job.RunNow') }}</el-radio-button>
                <el-radio-button label="2">{{ $t('Monitor.Job.RunOnce') }}</el-radio-button>
                <el-radio-button label="3">{{ $t('Monitor.Job.Manual') }}</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('Monitor.Job.Concurrent')" prop="concurrent">
              <el-radio-group v-model="form.concurrent" size="small">
                <el-radio-button label="0">{{ $t('Monitor.Job.Allow') }}</el-radio-button>
                <el-radio-button label="1">{{ $t('Monitor.Job.Forbid') }}</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('Monitor.Job.Status')">
              <el-radio-group v-model="form.status">
                <el-radio
                  v-for="dict in dict.type.EnableOrDisable"
                  :key="dict.value"
                  :label="dict.value"
                >{{dict.label}}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">{{ $t('Common.Submit') }}</el-button>
        <el-button @click="cancel">{{ $t('Common.Cancel') }}</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="$t('Monitor.Job.CronGenerator')" :visible.sync="openCron" append-to-body destroy-on-close class="scrollbar">
      <crontab @hide="openCron=false" @fill="crontabFill" :expression="expression"></crontab>
    </el-dialog>

    <!-- 任务日志详细 -->
    <el-dialog :title="$t('Monitor.Job.Details')" :visible.sync="openView" width="700px" append-to-body>
      <el-form ref="form" :model="form" label-width="120px" size="mini">
        <el-row>
          <el-col :span="12">
            <el-form-item :label="$t('Monitor.Job.JobId')">{{ form.jobId }}</el-form-item>
            <el-form-item :label="$t('Monitor.Job.JobName')">{{ form.jobName }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('Monitor.Job.JobGroup')">{{ jobGroupFormat(form) }}</el-form-item>
            <el-form-item :label="$t('Common.CreateTime')">{{ form.createTime }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('Monitor.Job.CronExpression')">{{ form.cronExpression }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('Monitor.Job.NextRunTime')">{{ parseTime(form.nextValidTime) }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('Monitor.Job.InvokeTarget')">{{ form.invokeTarget }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('Monitor.Job.Status')">
              <div v-if="form.status == 0">{{ $t('Common.Normal') }}</div>
              <div v-else-if="form.status == 1">{{ $t('Common.Fail') }}</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('Monitor.Job.Concurrent')">
              <div v-if="form.concurrent == 0">{{ $t('Monitor.Job.Allow') }}</div>
              <div v-else-if="form.concurrent == 1">{{ $t('Monitor.Job.Forbid') }}</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('Monitor.Job.MisfirePolicy')">
              <div v-if="form.misfirePolicy == 0">{{ $t('Monitor.Job.DefaultPolicy') }}</div>
              <div v-else-if="form.misfirePolicy == 1">{{ $t('Monitor.Job.RunNow') }}</div>
              <div v-else-if="form.misfirePolicy == 2">{{ $t('Monitor.Job.RunOnce') }}</div>
              <div v-else-if="form.misfirePolicy == 3">{{ $t('Monitor.Job.Manual') }}</div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="openView = false">{{ $t('Common.Close') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listJob, getJob, delJob, addJob, updateJob, runJob, changeJobStatus } from "@/api/monitor/job";
import Crontab from '@/components/Crontab'

export default {
  components: { Crontab },
  name: "Job",
  dicts: ['SysJobGroup', 'EnableOrDisable'],
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 定时任务表格数据
      jobList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 是否显示详细弹出层
      openView: false,
      // 是否显示Cron表达式弹出层
      openCron: false,
      // 传入的表达式
      expression: "",
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        jobName: undefined,
        jobGroup: undefined,
        status: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        jobName: [
          { required: true, message: this.$t('Monitor.Job.RuleTips.JobName'), trigger: "blur" }
        ],
        invokeTarget: [
          { required: true, message: this.$t('Monitor.Job.RuleTips.InvokeTarget'), trigger: "blur" }
        ],
        cronExpression: [
          { required: true, message: this.$t('Monitor.Job.RuleTips.CronExpression'), trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询定时任务列表 */
    getList() {
      this.loading = true;
      listJob(this.queryParams).then(response => {
        this.jobList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    // 任务组名字典翻译
    jobGroupFormat(row, column) {
      return this.selectDictLabel(this.dict.type.SysJobGroup, row.jobGroup);
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        jobId: undefined,
        jobName: undefined,
        jobGroup: undefined,
        invokeTarget: undefined,
        cronExpression: undefined,
        misfirePolicy: 1,
        concurrent: 1,
        status: "0"
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.jobId);
      this.single = selection.length != 1;
      this.multiple = !selection.length;
    },
    // 更多操作触发
    handleCommand(command, row) {
      switch (command) {
        case "handleRun":
          this.handleRun(row);
          break;
        case "handleView":
          this.handleView(row);
          break;
        case "handleJobLog":
          this.handleJobLog(row);
          break;
        default:
          break;
      }
    },
    // 任务状态修改
    handleStatusChange(row) {
      let text = row.status === "0" ? this.$t('Monitor.Job.Enable') : this.$t('Monitor.Job.Disable');
      this.$modal.confirm(this.$t('Monitor.Job.ConfirmChangeStatus', [ text, row.jobName ])).then(function() {
        return changeJobStatus(row.jobId, row.status);
      }).then(() => {
        this.$modal.msgSuccess(this.$t('Common.Success'));
      }).catch(function() {
        row.status = row.status === "0" ? "1" : "0";
      });
    },
    /* 立即执行一次 */
    handleRun(row) {
      this.$modal.confirm(this.$t('Monitor.Job.ConfirmRunOnce', [ row.jobName ])).then(function() {
        return runJob(row.jobId, row.jobGroup);
      }).then(() => {
        this.$modal.msgSuccess(this.$t('Common.Success'));
      }).catch(() => {});
    },
    /** 任务详细信息 */
    handleView(row) {
      getJob(row.jobId).then(response => {
        this.form = response.data;
        this.openView = true;
      });
    },
    /** cron表达式按钮操作 */
    handleShowCron() {
      this.expression = this.form.cronExpression;
      this.openCron = true;
    },
    /** 确定后回传值 */
    crontabFill(value) {
      this.form.cronExpression = value;
    },
    /** 任务日志列表查询 */
    handleJobLog(row) {
      const jobId = row.jobId || 0;
      this.$router.push('/monitor/job-log/index/' + jobId)
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = this.$t('Monitor.Job.Dialog.Add');
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const jobId = row.jobId || this.ids;
      getJob(jobId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = this.$t('Monitor.Job.Dialog.Edit');
      });
    },
    /** 提交按钮 */
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.jobId != undefined) {
            updateJob(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.Success'));
              this.open = false;
              this.getList();
            });
          } else {
            addJob(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.AddSuccess'));
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const jobIds = row.jobId ? [ row.jobId ] : this.ids;
      this.$modal.confirm(this.$t('Monitor.Job.ConfirmDelete', [ jobIds ])).then(function() {
        return delJob(jobIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess(this.$t('Common.DeleteSuccess'));
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('monitor/job/export', {
        ...this.queryParams
      }, `job_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
