<template>
  <div class="app-container">
    <el-table
      v-loading="loading"
      :data="logMenuList"
      style="width: 100%;"
    >
      <el-table-column :label="$t('Common.RowNo')" type="index" align="center" width="55" />
      <el-table-column :label="$t('Monitor.Logs.LogName')" align="left" :show-overflow-tooltip="true">
        <template #default="scope">
          <router-link :to="scope.row.router" class="link-type">
            <span>{{ scope.row.name }}</span>
          </router-link>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Monitor.Logs.LogId')" align="left" prop="id" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Common.Operation')" align="right" width="100" >
        <template #default="scope">
          <router-link :to="scope.row.router" class="link-type">
            <el-button
            type="text"
            icon="Search"
          >{{ $t('Common.View') }}</el-button>
          </router-link>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup name="MonitorLogsIndex">
import { getLogMenus } from "@/api/monitor/logs";

const loading = ref(true);
const logMenuList = ref([]);

/** 查询日志分类列表 */
function getList() {
  loading.value = true;
  getLogMenus().then(response => {
    logMenuList.value = response.data.rows;
    loading.value = false;
  });
}

onMounted(() => {
  getList();
});
</script>

