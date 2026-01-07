<template>
  <div>
    <el-row :gutter="24" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="danger"
          icon="Delete"
          plain
          :disabled="selectedIds.length == 0"
          @click="handleDelete"
          >{{ $t("Common.Delete") }}</el-button
        >
      </el-col>
      <right-toolbar
        v-model:showSearch="showSearch"
        @queryTable="loadLogList"
      ></right-toolbar>
    </el-row>
    <el-row v-show="showSearch">
      <el-form
        :model="queryParams"
        ref="queryFormRef"
        class="el-form-search"
        :inline="true"
      >
        <el-form-item prop="query">
          <el-input
            v-model="queryParams.query"
            :placeholder="$t('Search.WordStat.Placeholder.Word')"
            clearable
            style="width: 240px"
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
    </el-row>

    <el-table
      v-loading="loading"
      :data="logList"
      :height="tableHeight"
      :max-height="tableMaxHeight"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column
        :label="$t('Search.Log.Word')"
        align="left"
        prop="word"
        show-overflow-tooltip
      />
      <el-table-column
        :label="$t('Search.Log.ClientType')"
        align="left"
        width="140"
        prop="clientType"
      />
      <el-table-column
        :label="$t('Search.Log.Location')"
        align="left"
        width="140"
        prop="location"
        show-overflow-tooltip
      />
      <el-table-column label="IP" align="left" width="130" prop="ip" />
      <el-table-column
        label="Referer"
        align="left"
        prop="referer"
        show-overflow-tooltip
      />
      <el-table-column
        :label="$t('Search.Log.Source')"
        align="left"
        prop="source"
        width="200"
        show-overflow-tooltip
      />
      <el-table-column
        :label="$t('Search.Log.LogTime')"
        align="center"
        width="160"
      >
        <template #default="scope">
          <span>{{ parseTime(scope.row.logTime) }}</span>
        </template>
      </el-table-column>
    </el-table>
    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="loadLogList"
    />
  </div>
</template>
<script setup name="SearchLog">
import { getSiteSearchLogs, deleteSearchLogs } from "@/api/search/searchlog";
const { proxy } = getCurrentInstance();

const loading = ref(false);
const showSearch = ref(true);
const tableHeight = ref(600);
const tableMaxHeight = ref(600);
const selectedIds = ref([]);
const logList = ref([]);
const total = ref(0);
const queryParams = reactive({
  query: undefined,
  pageSize: 20,
  pageNum: 1,
});

onMounted(() => {
  changeTableHeight();
  loadLogList();
});

const changeTableHeight = () => {
  let height = document.body.offsetHeight; // 网页可视区域高度
  tableHeight.value = height - 330;
  tableMaxHeight.value = tableHeight.value;
};

const loadLogList = () => {
  loading.value = true;
  getSiteSearchLogs(queryParams).then((response) => {
    logList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
};

const handleQuery = () => {
  queryParams.pageNum = 1;
  loadLogList();
};

const resetQuery = () => {
  proxy.resetForm("queryFormRef");
  handleQuery();
};

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map((item) => item.logId);
};

const handleDelete = (row) => {
  const logIds = row.logId ? [row.logId] : selectedIds.value;
  proxy.$modal
    .confirm(proxy.$t("Common.ConfirmDelete"))
    .then(function () {
      return deleteSearchLogs(logIds);
    })
    .then((response) => {
      proxy.$modal.msgSuccess(response.msg);
      resetQuery();
    })
    .catch(() => {});
};
</script>
