<template>
  <div class="app-container">
    <el-form v-show="showSearch" :model="queryParams" ref="queryFormRef" class="el-form-search" :inline="true">
      <el-form-item prop="title">
        <el-input
          v-model="queryParams.title"
          clearable
          :placeholder="$t('System.OpLog.Title')"
          style="width: 140px;"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item prop="operName">
        <el-input
          v-model="queryParams.operName"
          clearable
          :placeholder="$t('System.OpLog.Operator')"
          style="width: 140px;"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item prop="businessType">
        <el-input
          v-model="queryParams.businessType"
          clearable
          style="width: 140px;"
          :placeholder="$t('System.OpLog.OpType')"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item prop="responseCode">
        <el-input
          v-model="queryParams.responseCode"
          clearable
          style="width: 140px;"
          :placeholder="$t('System.OpLog.ResponseCode')"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="$t('System.OpLog.OpTime')">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          :start-placeholder="$t('Common.BeginDate')"
          :end-placeholder="$t('Common.EndDate')"
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
      <right-toolbar v-model:showSearch="showSearch" @queryTable="loadList"></right-toolbar>
    </el-row>

    <el-table ref="tablesRef" v-loading="loading" :data="list" @selection-change="handleSelectionChange" :default-sort="defaultSort" @sort-change="handleSortChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="$t('System.OpLog.Title')" align="center" prop="title" />
      <el-table-column :label="$t('System.OpLog.OpType')" align="center" prop="businessType" />
      <el-table-column :label="$t('System.OpLog.RequestMethod')" align="center" prop="requestMethod" />
      <el-table-column :label="$t('System.OpLog.Operator')" align="center" prop="operName" width="100" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.OpLog.IP')" align="center" prop="operIp" width="130" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.OpLog.Location')" align="center" prop="operLocation" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.OpLog.ResponseCode')" align="center" prop="responseCode" />
      <el-table-column :label="$t('System.OpLog.OpTime')" align="center" prop="operTime" sortable="custom" :sort-orders="['descending', 'ascending']" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.operTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('System.OpLog.Cost')" align="center" prop="cost" width="100">
        <template #default="scope">
          <span>{{ scope.row.cost == null ? 0 : scope.row.cost }} ms</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" align="center">
        <template #default="scope">
          <el-button
            size="small"
            type="text"
            icon="View"
            @click="handleView(scope.row,scope.index)"
          >{{ $t('System.OpLog.Details') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="loadList"
    />

    <!-- 操作日志详细 -->
    <el-dialog :title="$t('System.OpLog.DetailsTitle')" v-model="open" width="800px" append-to-body>
      <el-form ref="formRef" :model="form" label-width="130px">
        <el-row>
          <el-col :span="24">
            <el-form-item :label="$t('System.OpLog.Title')">{{ form.title }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('System.OpLog.OpType')">{{ form.businessType }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item
              :label="$t('System.OpLog.LoginInfo')"
            >{{ form.operName }} / {{ form.operIp }} / {{ form.operLocation }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('System.OpLog.RequestUrl')">{{ form.operUrl }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('System.OpLog.RequestMethod')">{{ form.requestMethod }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('System.OpLog.Method')">{{ form.method }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('System.OpLog.RequestParams')">{{ form.requestArgs }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="User-Agent">{{ form.userAgent }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('System.OpLog.Response')">{{ form.responseResult }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('System.OpLog.OpTime')">{{ parseTime(form.operTime) }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('System.OpLog.Cost')">{{ form.cost == null ? 0 : form.cost }} ms</el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer class="dialog-footer">
        <el-button @click="open = false">{{ $t('Common.Close') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MonitorLogsOperation">
import { getList, delOperlog, cleanOperlog } from "@/api/monitor/operlog";

const { proxy } = getCurrentInstance();

const loading = ref(false);

const ids = ref([]);
const multiple = ref(true);
const showSearch = ref(true);
const total = ref(0);
const list = ref([]);
const open = ref(false);
const dateRange = ref([]);
const defaultSort = ref({prop: 'operTime', order: 'descending'});
const form = ref({});
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  title: undefined,
  operName: undefined,
  businessType: undefined,
  responseCode: undefined
});

onMounted(() => {
  loadList();
});

/** 查询登录日志 */
function loadList() {
  loading.value = true;
  getList(proxy.addDateRange(queryParams.value, dateRange.value)).then( response => {
      list.value = response.data.rows;
      total.value = parseInt(response.data.total);
      loading.value = false;
    }
  );
}
/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  loadList();
}
/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = [];
  proxy.$refs.queryFormRef.resetFields();
  queryParams.value.pageNum = 1;
  proxy.$refs.tablesRef.sort(defaultSort.value.prop, defaultSort.value.order)
}
/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.operId)
  multiple.value = !selection.length
}
/** 排序触发事件 */
function handleSortChange(column, prop, order) {
  queryParams.value.sorts = column.prop + "#" + (column.order === 'ascending' ? 'ASC' : 'DESC');
  loadList();
}
/** 详细按钮操作 */
function handleView(row) {
  open.value = true;
  form.value = row;
}
/** 删除按钮操作 */
function handleDelete(row) {
  const operIds = row.operId ? [ row.operId ] : ids.value;
  proxy.$modal.confirm(proxy.$t('System.OpLog.ConfirmDelete', [ operIds ])).then(function() {
    return delOperlog(operIds);
  }).then(() => {
    loadList();
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
  }).catch(() => {});
}
/** 清空按钮操作 */
function handleClean() {
  proxy.$modal.confirm(proxy.$t('System.OpLog.ConfirmClean')).then(function() {
    return cleanOperlog();
  }).then(() => {
    loadList();
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
  }).catch(() => {});
}
/** 导出按钮操作 */
function handleExport() {
  proxy.exportExcel('monitor/operlog/list', {
    ...queryParams.value
  }, `operlog_${proxy.parseTime(new Date(),'{y}{m}{d}{h}{i}{s}')}.xlsx`)
}
/** 返回按钮操作 */
function handleClose() {
  const obj = { path: "/monitor/logs" };
  proxy.$tab.closeOpenPage(obj);
}
</script>

