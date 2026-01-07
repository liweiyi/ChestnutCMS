<template>
  <div class="">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button 
          icon="Refresh"
          @click="handleQuery">{{ $t("Common.Refresh") }}</el-button>
      </el-col>
    </el-row>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column :label="$t('Stat.Adv.AdName')" align="center" prop="adName" />
      <el-table-column label="IP" align="center" prop="ip" />
      <el-table-column :label="$t('Stat.Adv.Location')" align="center" prop="address" />
      <el-table-column :label="$t('Stat.Adv.Source')" align="center" :show-overflow-tooltip="true" prop="referer" />
      <el-table-column :label="$t('Stat.Adv.DeviceType')" align="center" prop="deviceType" />
      <el-table-column :label="$t('Stat.Adv.Browser')" align="center" prop="browser" />
      <el-table-column :label="$t('Stat.Adv.Time')" align="center" prop="evtTime" />
    </el-table>
    <pagination 
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="loadAdViewLogList" />
  </div>
</template>
<script setup name="CMSAdvertisementViewLog">
import { getAdViewLogList  } from "@/api/advertisement/statistics";

const loading = ref(false);
const total = ref(0);
const dataList = ref([]);
const queryParams = reactive({
  pageNum: 1,
  pageSize: 50,
  query: undefined
});

function loadAdViewLogList() {
  loading.value = true;
  getAdViewLogList(queryParams).then(response => {
    dataList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
}

function handleQuery() {
  queryParams.pageNum = 1;
  loadAdViewLogList();
}

onMounted(() => {
  loadAdViewLogList();
});
</script>