<template>
  <div class="app-container">
    <el-row :gutter="24" class="mb12">
      <el-col :span="12">
        <el-button type="danger"
                    icon="el-icon-delete"
                    size="mini"
                    plain
                    :disabled="selectedIds.length==0"
                    @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
      </el-col>
      <el-col :span="12" style="text-align:right">
        <el-form 
          :model="queryParams"
          ref="queryForm"
          :inline="true"
          size="mini"
          class="el-form-search">
          <el-form-item prop="query">
            <el-input v-model="queryParams.query">
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button-group>
              <el-button 
                type="primary"
                icon="el-icon-search"
                @click="handleQuery">{{ $t("Common.Search") }}</el-button>
              <el-button 
                icon="el-icon-refresh"
                @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
            </el-button-group>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
    <el-row>
      <el-table v-loading="loading"
                :data="logList"
                @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column type="index" :label="$t('Common.RowNo')" align="center" width="100" />
        <el-table-column :label="$t('Search.Log.Word')" align="left" prop="word" show-overflow-tooltip />
        <el-table-column :label="$t('Search.Log.ClientType')" align="left" width="140" prop="clientType" />
        <el-table-column :label="$t('Search.Log.Location')" align="left" width="140" prop="location" show-overflow-tooltip />
        <el-table-column label="IP" align="left" width="100" prop="ip" />
        <el-table-column label="Referer" align="left" prop="referer" show-overflow-tooltip />
        <el-table-column :label="$t('Search.Log.Source')" align="left" prop="source" width="200" show-overflow-tooltip />
        <el-table-column :label="$t('Search.Log.LogTime')" align="center" width="160">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.logTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Common.Operation')" align="center" width="220" class-name="small-padding fixed-width">
          <template slot-scope="scope">
            <el-button 
              size="small"
              type="text"
              icon="el-icon-plus"
              @click="handleToDictWord('WORD', scope.row)">{{ $t('Search.Log.AddExtWord') }}</el-button>
            <el-button 
              size="small"
              type="text"
              icon="el-icon-plus"
              @click="handleToDictWord('STOP', scope.row)">{{ $t('Search.Log.AddStopWord') }}</el-button>
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
    </el-row>
  </div>
</template>
<style scoped>
</style>
<script>
import { getSearchLogs, deleteSearchLogs } from "@/api/search/searchlog";
import { addDictWord } from "@/api/search/dictword";
export default {
  name: "SearchWord",
  data () {
    return {
      loading: false,
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
    this.loadLogList();
  },
  methods: {
    loadLogList () {
      this.loading = true;
      getSearchLogs(this.queryParams).then(response => {
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
    },
    handleToDictWord (wordType, row) {
      const form = { wordType: wordType, words: [ row.word ] }
      addDictWord(form).then(response => {
        this.$modal.msgSuccess(response.msg);
      });
    }
  }
};
</script>