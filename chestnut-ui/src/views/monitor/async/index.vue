<template>
  <div class="app-container">
    <el-form 
      :model="queryParams"
      ref="queryForm"
      :inline="true"
      size="small">
      <el-form-item prop="type">
        <el-input v-model="queryParams.type" :placeholder="$t('Monitor.Async.Type')" size="small" />
      </el-form-item>
      <el-form-item prop="id">
        <el-input v-model="queryParams.id" :placeholder="$t('Monitor.Async.TaskID')" size="small" />
      </el-form-item>
      <el-form-item>
        <el-button-group>
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
        </el-button-group>
      </el-form-item>
    </el-form>
    <el-table v-loading="loading"
              :data="taskList">
      <el-table-column :label="$t('Monitor.Async.Type')"
                       align="center"
                       width="120"
                       prop="type" />
      <el-table-column :label="$t('Monitor.Async.TaskID')"
                       align="left"
                       prop="taskId" />
      <el-table-column :label="$t('Monitor.Async.Status')"
                       align="center"
                       width="100">
        <template slot-scope="scope">
          <el-tag :type="statusType(scope.row.status)">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Monitor.Async.Percent')"
                       align="center"
                       width="100"
                       prop="percent">
        <template slot-scope="scope">
          <span>{{ scope.row.percent }}%</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Monitor.Async.ErrMessage')"
                       align="center"
                       width="150">
        <template slot-scope="scope">
          <el-button v-if="hasErrMessages(scope.row)" @click="showErrMessages(scope.row)">{{ $t('Common.View') }}</el-button>
          <span v-else>{{ $t('Common.None') }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Monitor.Async.ReadyTime')"
                       align="center"
                       prop="startTime"
                       width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.startTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.BeginTime')"
                       align="center"
                       prop="startTime"
                       width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.startTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.EndTime')"
                       align="center"
                       prop="endTime"
                       width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.endTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')"
                        align="center"
                        width="100" 
                        class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button 
            v-if="scope.row.status=='READY'||scope.row.status=='RUNNING'"
            size="small"
            type="text"
            icon="el-icon-stop"
            @click="handleStop(scope.row)">{{ $t('Monitor.Async.Stop') }}</el-button>
          <el-button size="small"
                      type="text"
                      icon="el-icon-delete"
                      @click="handleDelete(scope.row)">{{ $t('Common.Delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
<script>
import { MessageBox } from 'element-ui'
import { getTaskList, stopTask, removeTask } from "@/api/system/async";

export default {
  name: "MonitorAsyncIndex",
  data () {
    return {
      // 遮罩层
      loading: true,
      // 总条数
      total: 0,
      // 异步任务表格数据
      taskList: [],
      // 查询参数
      queryParams: {
        type: "",
        id: ""
      }
    };
  },
  created () {
    this.loadAsyncTaskList();
  },
  methods: {
    loadAsyncTaskList () {
      this.loading = true;
      getTaskList(this.queryParams).then(response => {
        this.taskList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    statusType(status) {
      if (status === 'INTERRUPTED') {
        return 'warning';
      } else if (status === 'FAILED') {
        return 'danger';
      } else if (status === 'SUCCESS') {
        return 'success';
      }
      return '';
    },
    hasErrMessages(row) {
      return row.errMessages && row.errMessages != null && row.errMessages.length > 0;
    },
    showErrMessages(row) {
      MessageBox.alert(row.errMessages.join("<br/>"), this.$t('Monitor.Async.ErrMessage'), {
          dangerouslyUseHTMLString: true
        });
    },
    handleQuery () {
      this.loadAsyncTaskList();
    },
    resetQuery () {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleStop (row) {
      stopTask([row.taskId]).then(response => {
        if (response.code == 200) {
          this.$modal.msgSuccess(response.msg);
          this.loadAsyncTaskList();
        }
      });
    },
    handleDelete (row) {
      removeTask([row.taskId]).then(response => {
        if (response.code == 200) {
          this.$modal.msgSuccess(response.msg);
          this.loadAsyncTaskList();
        }
      });
    }
  }
};
</script>