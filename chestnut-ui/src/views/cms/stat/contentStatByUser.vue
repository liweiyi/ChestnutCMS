<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb12">
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="el-icon-sort"
          size="small"
          @click="getList"
        >{{ $t('Common.Refresh') }}</el-button>
      </el-col>
    </el-row>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column prop="userName" label="用户名"></el-table-column>
      <el-table-column prop="draftTotal" label="初稿" align="center" width="200"></el-table-column>
      <el-table-column prop="toPublishTotal" label="待发布" align="center" width="200"></el-table-column>
      <el-table-column prop="publishedTotal" label="已发布" align="center" width="200"></el-table-column>
      <el-table-column prop="offlineTotal" label="已下线" align="center" width="200"></el-table-column>
      <el-table-column prop="editingTotal" label="重新编辑" align="center" width="200"></el-table-column>
    </el-table>
  </div>
</template>

<script>
import { getContentStatByUser } from "@/api/stat/content";

export default {
  name: "ContentStatByUser",
  data() {
    return {
      loading: true,
      dataList: [],
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      getContentStatByUser().then(response => {
        this.dataList = response.data;
        this.loading = false;
      });
    }
  }
};
</script>
