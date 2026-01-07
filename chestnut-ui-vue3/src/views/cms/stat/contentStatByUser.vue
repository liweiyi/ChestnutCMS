<template>
  <div class="stat-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Refresh"
          @click="getList"
        >{{ $t('Common.Refresh') }}</el-button>
      </el-col>
    </el-row>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column prop="userName" :label="$t('CMS.Content.UserName')" align="center"></el-table-column>
      <el-table-column prop="draftTotal" :label="$t('CMS.Content.StatusOption.Draft')" align="center" width="200"></el-table-column>
      <el-table-column prop="toPublishTotal" :label="$t('CMS.Content.StatusOption.ToPublish')" align="center" width="200"></el-table-column>
      <el-table-column prop="publishedTotal" :label="$t('CMS.Content.StatusOption.Published')" align="center" width="200"></el-table-column>
      <el-table-column prop="offlineTotal" :label="$t('CMS.Content.StatusOption.Offline')" align="center" width="200"></el-table-column>
      <el-table-column prop="editingTotal" :label="$t('CMS.Content.StatusOption.Editing')" align="center" width="200"></el-table-column>
    </el-table>
  </div>
</template>

<script setup name="ContentStatByUser">
import { getContentStatByUser } from "@/api/stat/content";

const loading = ref(true);
const dataList = ref([]);

onMounted(() => {
  getList();
});

function getList() {
  loading.value = true;
  getContentStatByUser().then(response => {
    dataList.value = response.data;
    loading.value = false;
  });
}
</script>
