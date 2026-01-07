<template>
  <div class="stat-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Sort"
          @click="toggleExpandAll"
        >{{ $t('Common.ExpandOrCollapse') }}</el-button>
      </el-col>
    </el-row>
    <el-table
      v-if="refreshTable"
      v-loading="loading"
      :data="dataList"
      row-key="catalogId"
      :default-expand-all="isExpandAll"
      :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
    >
      <el-table-column prop="name" :label="$t('CMS.Content.Catalog')"></el-table-column>
      <el-table-column prop="draftTotal" :label="$t('CMS.Content.StatusOption.Draft')" align="center" width="200"></el-table-column>
      <el-table-column prop="toPublishTotal" :label="$t('CMS.Content.StatusOption.ToPublish')" align="center" width="200"></el-table-column>
      <el-table-column prop="publishedTotal" :label="$t('CMS.Content.StatusOption.Published')" align="center" width="200"></el-table-column>
      <el-table-column prop="offlineTotal" :label="$t('CMS.Content.StatusOption.Offline')" align="center" width="200"></el-table-column>
      <el-table-column prop="editingTotal" :label="$t('CMS.Content.StatusOption.Editing')" align="center" width="200"></el-table-column>
    </el-table>
  </div>
</template>

<script setup name="ContentStatByCatalog">
import { getContentStatByCatalog } from "@/api/stat/content";

const { proxy } = getCurrentInstance();

const loading = ref(true);
const dataList = ref([]);
const isExpandAll = ref(true);
const refreshTable = ref(true);

onMounted(() => {
  getList();
});

function getList() {
  loading.value = true;
  getContentStatByCatalog().then(response => {
    dataList.value = proxy.handleTree(response.data, '0', "catalogId");
    loading.value = false;
  });
}

function toggleExpandAll() {
  refreshTable.value = false;
  isExpandAll.value = !isExpandAll.value;
  nextTick(() => {
    refreshTable.value = true;
  });
}
</script>
