<template>
  <div>
    <el-row :gutter="24" class="mb12">
      <el-col :span="1.5">
        <el-button type="danger"
                    icon="el-icon-delete"
                    size="mini"
                    plain
                    :disabled="selectedIds.length==0"
                    @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="loadLogList"></right-toolbar>
    </el-row>
    <el-row v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="mini" class="el-form-search mb12" :inline="true">
        <el-form-item prop="query">
          <el-input
            v-model="queryParams.query"
            :placeholder="$t('Search.WordStat.Placeholder.Word')"
            clearable
            style="width: 240px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button-group>
            <el-button type="primary" icon="el-icon-search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
            <el-button icon="el-icon-refresh" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
          </el-button-group>
        </el-form-item>
      </el-form>
    </el-row>

    <el-table 
      v-loading="loading"
      :data="logList"
      :height="tableHeight"
      :max-height="tableMaxHeight"
      @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column :label="$t('Search.Log.Word')" align="left" prop="word" show-overflow-tooltip />
      <el-table-column :label="$t('Search.Log.ClientType')" align="left" width="140" prop="clientType" />
      <el-table-column :label="$t('Search.Log.Location')" align="left" width="140" prop="location" show-overflow-tooltip />
      <el-table-column label="IP" align="left" width="130" prop="ip" />
      <el-table-column label="Referer" align="left" prop="referer" show-overflow-tooltip />
      <el-table-column :label="$t('Search.Log.Source')" align="left" prop="source" width="200" show-overflow-tooltip />
      <el-table-column :label="$t('Search.Log.LogTime')" align="center" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.logTime) }}</span>
        </template>
      </el-table-column>
    </el-table>
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="loadLogList"
    />
  </div>
</template>
<style scoped>
</style>
<script>
import { getSiteSearchLogs, deleteSearchLogs } from "@/api/search/searchlog";
export default {
  name: "SearchWord",
  data () {
    return {
      loading: false,
      showSearch: true,
      tableHeight: 600,
      tableMaxHeight: 600,
      selectedIds: [],
      logList: [],
      total: 0,
      queryParams: {
        query: undefined,
        pageSize: 20,
        pageNo: 1
      }
    };
  },
  created () {
    this.changeTableHeight();
    this.loadLogList();
  },
  methods: {
    changeTableHeight () {
      let height = document.body.offsetHeight // 网页可视区域高度
      this.tableHeight = height - 330;
      this.tableMaxHeight = this.tableHeight;
    },
    loadLogList () {
      this.loading = true;
      getSiteSearchLogs(this.queryParams).then(response => {
        this.logList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    reset () {
      this.form = {};
    },
    handleQuery () {
      this.queryParams.pageNo = 1;
      this.loadLogList();
    },
    resetQuery () {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange (selection) {
      this.selectedIds = selection.map(item => item.logId);
    },
    handleDelete (row) {
      const logIds = row.logId ? [ row.logId ] : this.selectedIds;
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function() {
        return deleteSearchLogs(logIds);
      }).then((response) => {
        this.$modal.msgSuccess(response.msg);
        this.resetQuery ();
      }).catch(() => {});
    }
  }
};
</script>