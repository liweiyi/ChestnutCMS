<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb12">
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="el-icon-sort"
          size="small"
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
      <el-table-column prop="name" label="栏目"></el-table-column>
      <el-table-column prop="draftTotal" label="初稿" align="center" width="200"></el-table-column>
      <el-table-column prop="toPublishTotal" label="待发布" align="center" width="200"></el-table-column>
      <el-table-column prop="publishedTotal" label="已发布" align="center" width="200"></el-table-column>
      <el-table-column prop="offlineTotal" label="已下线" align="center" width="200"></el-table-column>
      <el-table-column prop="editingTotal" label="重新编辑" align="center" width="200"></el-table-column>
    </el-table>
  </div>
</template>

<script>
import { getContentStatByCatalog } from "@/api/stat/content";

export default {
  name: "ContentStatByCatalog",
  data() {
    return {
      loading: true,
      dataList: [],
      isExpandAll: true,
      refreshTable: true,
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      getContentStatByCatalog().then(response => {
        this.dataList = this.handleTree(response.data, '0', "catalogId");
        this.loading = false;
      });
    },
    normalizer(node) {
      if (node.children && !node.children.length) {
        delete node.children;
      }
      return {
        id: node.catalogId,
        label: node.name,
        children: node.children
      };
    },
    toggleExpandAll() {
      this.refreshTable = false;
      this.isExpandAll = !this.isExpandAll;
      this.$nextTick(() => {
        this.refreshTable = true;
      });
    }
  }
};
</script>
