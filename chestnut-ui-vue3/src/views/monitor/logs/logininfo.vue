<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" class="el-form-search" :inline="true">
      <el-form-item prop="ipaddr">
        <el-input
          v-model="queryParams.ipaddr"
          clearable
          :placeholder="$t('System.LoginInfo.IP')"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item prop="userName">
        <el-input
          v-model="queryParams.userName"
          clearable
          :placeholder="$t('System.LoginInfo.UserName')"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item prop="status">
        <el-select
          v-model="queryParams.status"
          clearable
          :placeholder="$t('System.LoginInfo.Status')"
          style="width:100px"
        >
          <el-option
            v-for="dict in SuccessOrFail"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-date-picker
          v-model="dateRange"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          :start-placeholder="$t('Common.BeginTime')"
          :end-placeholder="$t('Common.EndTime')"
          style="width:240px"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button-group>
          <el-button type="primary" icon="Search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
          <el-button icon="Refresh" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
        </el-button-group>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
        >{{ $t('Common.Delete') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          @click="handleClean"
        >{{ $t('Common.Clean') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
        >{{ $t('Common.Export') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="Close"
          @click="handleClose"
        >{{ $t('Common.Close') }}</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tables" v-loading="loading" :data="dataList" @selection-change="handleSelectionChange" :default-sort="defaultSort" @sort-change="handleSortChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="$t('System.LoginInfo.UserType')" align="center" prop="userType" />
      <el-table-column :label="$t('System.LoginInfo.UserId')" align="center" prop="userId" />
      <el-table-column :label="$t('System.LoginInfo.UserName')" align="center" prop="userName" :show-overflow-tooltip="true" sortable="custom" :sort-orders="['descending', 'ascending']" />
      <el-table-column :label="$t('System.LoginInfo.IP')" align="center" prop="ipaddr" width="130" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.LoginInfo.Location')" align="center" prop="loginLocation" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.LoginInfo.Browser')" align="center" prop="browser" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.LoginInfo.OS')" align="center" prop="os" />
      <el-table-column :label="$t('System.LoginInfo.Status')" align="center" prop="status">
        <template #default="scope">
          <dict-tag :options="SuccessOrFail" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column :label="$t('System.LoginInfo.OpMsg')" align="center" prop="msg" />
      <el-table-column :label="$t('System.LoginInfo.LoginTime')" align="center" prop="loginTime" sortable="custom" :sort-orders="['descending', 'ascending']" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.loginTime) }}</span>
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
  </div>
</template>

<script setup name="MonitorLogsLogininfor">
import { listLogs, delLogininfor, cleanLogininfor } from "@/api/monitor/logininfor";

const { proxy } = getCurrentInstance();
const { SuccessOrFail } = proxy.useDict("SuccessOrFail");

const loading = ref(false);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);

const selectName = ref("");
const showSearch = ref(true);
const total = ref(0);
const dataList = ref([]);
const dateRange = ref([]);
const defaultSort = ref({prop: 'loginTime', order: 'descending'});
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  ipaddr: undefined,
  userName: undefined,
  status: undefined
});

/** 查询登录日志列表 */
function getList() {
  loading.value = true;
  listLogs(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    dataList.value = response.data.rows;
      total.value = parseInt(response.data.total);
      loading.value = false;
    }
  );
}
/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}
/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = [];
  proxy.$refs.queryForm.resetFields();
  queryParams.value.pageNum = 1;
  proxy.$refs.tables.sort(defaultSort.value.prop, defaultSort.value.order)
}
/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.infoId)
  single.value = selection.length!=1
  multiple.value = !selection.length
  selectName.value = selection.map(item => item.userName);
}
/** 排序触发事件 */
function handleSortChange(column, prop, order) {
  if (!column.order) {
    queryParams.value.sorts = "";
  } else {
    const direction = column.order === 'ascending' ? 'ASC' : 'DESC';
    queryParams.value.sorts = column.prop + "#" + direction;
  }
  getList();
}
/** 删除按钮操作 */
function handleDelete(row) {
  const infoIds = row.infoId ? [row.infoId] : ids.value;
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function() {
    return delLogininfor(infoIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
  }).catch(() => {});
}
/** 清空按钮操作 */
function handleClean() {
  proxy.$modal.confirm(proxy.$t('Common.ConfirmClean')).then(function() {
    return cleanLogininfor();
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess(proxy.$t('Common.Success'));
  }).catch(() => {});
}
/** 导出按钮操作 */
function handleExport() {
  proxy.exportExcel('monitor/logininfor/list', {
    ...queryParams.value
  }, `logininfor_${proxy.parseTime(new Date(),'{y}{m}{d}{h}{i}{s}')}.xlsx`)
}
/** 返回按钮操作 */
function handleClose() {
  const obj = { path: "/monitor/logs" };
  proxy.$tab.closeOpenPage(obj);
}

onMounted(() => {
  getList();
});
</script>

