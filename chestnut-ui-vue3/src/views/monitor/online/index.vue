<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      class="el-form-search"
    >
      <el-form-item prop="tokenId">
        <el-input
          v-model="queryParams.tokenId"
          placeholder="TokenID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item prop="ipaddr">
        <el-input
          v-model="queryParams.ipaddr"
          :placeholder="$t('Monitor.Online.LoginIP')"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item prop="userName">
        <el-input
          v-model="queryParams.userName"
          :placeholder="$t('Monitor.Online.UserName')"
          clearable
          @keyup.enter="handleQuery"
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
    <el-table
      v-loading="loading"
      :data="list_data.slice((pageNum - 1) * pageSize, pageNum * pageSize)"
      style="width: 100%"
    >
      <el-table-column :label="$t('Common.RowNo')" type="index" width="55" align="center">
        <template #default="scope">
          <span>{{ (pageNum - 1) * pageSize + scope.$index + 1 }}</span>
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('Monitor.Online.TokenID')"
        align="center"
        prop="tokenId"
        width="300"
        :show-overflow-tooltip="true"
      />
      <el-table-column
        :label="$t('Monitor.Online.UserName')"
        align="center"
        prop="userName"
        :show-overflow-tooltip="true"
      />
      <el-table-column
        :label="$t('Monitor.Online.DeptName')"
        align="center"
        prop="deptName"
      />
      <el-table-column
        :label="$t('Monitor.Online.LoginIP')"
        align="center"
        prop="ipaddr"
        :show-overflow-tooltip="true"
      />
      <el-table-column
        :label="$t('Monitor.Online.LoginLocation')"
        align="center"
        prop="loginLocation"
        :show-overflow-tooltip="true"
      />
      <el-table-column
        :label="$t('Monitor.Online.Browser')"
        align="center"
        prop="browser"
        :show-overflow-tooltip="true"
      />
      <el-table-column
        :label="$t('Monitor.Online.OS')"
        align="center"
        prop="os"
      />
      <el-table-column
        :label="$t('Monitor.Online.LoginTime')"
        align="center"
        prop="loginTime"
        width="180"
      >
        <template #default="scope">
          <span>{{ parseTime(scope.row.loginTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('Common.Operation')"
        align="center"
       
        width="80"
      >
        <template #default="scope">
          <el-button
            link
            type="danger"
            icon="Delete"
            @click="handleForceLogout(scope.row)"
            >{{ $t("Monitor.Online.ForceExit") }}</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="pageNum"
      v-model:limit="pageSize"
    />
  </div>
</template>

<script setup name="MonitorOnlineIndex">
import { list, forceLogout } from "@/api/monitor/online";

const { proxy } = getCurrentInstance();

// 遮罩层
const loading = ref(true);
// 总条数
const total = ref(0);
// 表格数据
const list_data = ref([]);
const pageNum = ref(1);
const pageSize = ref(10);
// 查询参数
const queryParams = reactive({
  tokenId: undefined,
  ipaddr: undefined,
  userName: undefined,
});

/** 查询登录日志列表 */
function getList() {
  loading.value = true;
  list(queryParams).then((response) => {
    list_data.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
}

/** 搜索按钮操作 */
function handleQuery() {
  pageNum.value = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryFormRef");
  handleQuery();
}

/** 强退按钮操作 */
function handleForceLogout(row) {
  proxy.$modal
    .confirm(proxy.$t("Monitor.Online.ConfirmForceExit", [row.userName]))
    .then(function () {
      return forceLogout([row.tokenId]);
    })
    .then(() => {
      getList();
      proxy.$modal.msgSuccess(proxy.$t("Common.OpSuccess"));
    })
    .catch(() => {});
}

onMounted(() => {
  getList();
});
</script>
