<template>
  <div class="cms-content-list">
    <el-row :gutter="24" class="mb12">
      <el-col :span="12">
        <el-row :gutter="10">
          <el-col :span="1.5">
            <el-button 
              plain
              type="primary"
              icon="el-icon-refresh-left"
              size="mini"
              :disabled="multiple"
              @click="handleRecover">{{ $t('CMS.Content.Restore') }}
            </el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button 
              plain
              type="danger"
              icon="el-icon-delete"
              size="mini"
              :disabled="multiple"
              @click="handleDelete">{{ $t("Common.Delete") }}
            </el-button>
          </el-col>
        </el-row>
      </el-col>
      <el-col :span="12" style="text-align:right">
        <el-form 
          :model="queryParams"
          ref="queryForm"
          size="mini"
          :inline="true"
          class="el-form-search">
          <el-form-item prop="title">
            <el-input 
              v-model="queryParams.title"
              :placeholder="$t('CMS.Content.Placeholder.Title')"
              clearable
              style="width: 200px"
              @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item prop="contentType">
            <el-select 
              v-model="queryParams.contentType"
              :placeholder="$t('CMS.Content.ContentType')"
              clearable
              style="width: 125px">
              <el-option 
                v-for="ct in contentTypeOptions"
                :key="ct.id"
                :label="ct.name"
                :value="ct.id" />
            </el-select>
          </el-form-item>
          <el-form-item prop="status">
            <el-select  
              v-model="queryParams.status"
              :placeholder="$t('CMS.Content.Status')"
              clearable
              style="width: 110px">
              <el-option 
                v-for="dict in dict.type.CMSContentStatus"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value" />
            </el-select>
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

    <el-table 
      v-loading="loading"
      ref="tablecontentRecycleList"
      :data="contentRecycleList"
      :height="tableHeight"
      :max-height="tableMaxHeight"
      @row-click="handleRowClick"
      @selection-change="handleSelectionChange">
      <el-table-column 
        type="selection"
        width="50"
        align="center" />
      <el-table-column :label="$t('CMS.Content.Title')" :show-overflow-tooltip="true" prop="title" />
      <el-table-column 
        :label="$t('CMS.Content.ContentType')" 
        width="110"
        align="center"
        prop="contentType"
        :formatter="contentTypeFormat" />
      <el-table-column 
        :label="$t('CMS.Content.StatusBefore')"
        width="110"
        align="center">
        <template slot-scope="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusFormat(scope.row, 'status') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column 
        :label="$t('CMS.Content.DeleteTime')"
        align="center"
        prop="updateTime"
        width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('CMS.Content.DeleteUser')" align="center" :show-overflow-tooltip="true" prop="updateBy" width="140" />
      <el-table-column 
        :label="$t('Common.Operation')"
        align="center"
        width="100"
        class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button 
            size="small"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)">{{ $t("Common.Delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination 
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="loadRecyclecontentRecycleList" />
  </div>
</template>
<script>
import { getContentTypes } from "@/api/contentcore/catalog";
import { getRecycleContentList, recoverRecycleContent, deleteRecycleContents } from "@/api/contentcore/recycle";

export default {
  name: "CMScontentRecycleList",
  dicts: ['CMSContentStatus'],
  props: {
    cid: {
      type: String,
      default: undefined,
      required: false,
    }
  },
  data () {
    return {
      // 遮罩层
      loading: false,
      contentTypeOptions: [],
      catalogId: this.cid,
      contentRecycleList: null,
      total: 0,
      tableHeight: 600, // 表格高度
      tableMaxHeight: 600, // 表格最大高度
      selectedRows: [], // 表格选中行
      single: true,
      multiple: true,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        contentType: undefined,
        status: undefined,
        catalogId: undefined,
      },
      openCatalogSelector: false, // 栏目选择弹窗
    };
  },
  watch: {
    cid(newVal) { 
      this.catalogId = newVal;
    },
    catalogId(newVal) {
      this.loadRecyclecontentRecycleList();
    },
  },
  created () {
    this.changeTableHeight();
    getContentTypes().then(response => {
      this.contentTypeOptions = response.data;
    });
    if (this.catalogId && this.catalogId > 0) {
      this.loadRecyclecontentRecycleList();
    }
  },
  methods: {
    loadRecyclecontentRecycleList () {
      this.loading = true;
      this.queryParams.catalogId = this.catalogId;
      getRecycleContentList(this.queryParams).then(response => {
        this.contentRecycleList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    statusFormat (row, column) {
      return this.selectDictLabel(this.dict.type.CMSContentStatus, row[column]);
    },
    contentTypeFormat (row, column) {
      var hitValue = [];
      this.contentTypeOptions.forEach(ct => {
        if (ct.id == ('' + row.contentType)) {
          hitValue = ct.name;
          return;
        }
      });
      return hitValue;
    },
    handleSelectionChange (selection) {
      this.selectedRows = selection;
      this.single = selection.length != 1;
      this.multiple = !selection.length;
    },
    handleRowClick (currentRow) {
      this.toggleAllCheckedRows();
      this.$refs.tablecontentRecycleList.toggleRowSelection(currentRow);
      this.selectedRows.push(currentRow);
    },
    toggleAllCheckedRows() {
      this.selectedRows.forEach(row => {
          this.$refs.tablecontentRecycleList.toggleRowSelection(row, false);
      });
      this.selectedRows = [];
    },
    statusTagType(status) {
      if (status == 40) {
        return "warning";
      } else if (status == 20 || status == 30) {
        return "success";
      } else if (status == 0) {
        return "info";
      }
      return "";
    },
    handleQuery () {
      this.queryParams.page = 1;
      this.loadRecyclecontentRecycleList();
    },
    resetQuery () {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleDelete (row) {
      const backupIds = row.backupId ? [ row.backupId ] : this.selectedRows.map(row => row.backupId);
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function () {
        return deleteRecycleContents(backupIds);
      }).then(() => {
        this.loadRecyclecontentRecycleList();
        this.$modal.msgSuccess(this.$t('Common.DeleteSuccess'));
      }).catch(function () { });
    },
    changeTableHeight () {
      let height = document.body.offsetHeight // 网页可视区域高度
      this.tableHeight = height - 330;
      this.tableMaxHeight = this.tableHeight;
    },
    handleRecover(row) {
      const backupIds = row.backupId ? [ row.backupId ] : this.selectedRows.map(row => row.backupId);
      recoverRecycleContent(backupIds).then(response => {
        this.loadRecyclecontentRecycleList();
        this.$modal.msgSuccess(this.$t('Common.OpSuccess'));
      });
    }
  }
};
</script>
<style scoped>
.head-container .el-select .el-input {
  width: 110px;
}
.el-divider {
  margin-top: 10px;
}
.el-tabs__header {
  margin-bottom: 10px;
}
.pagination-container {
  height: 30px;
}
.row-more-btn {
  padding-left: 10px;
}
.top-icon {
  font-weight: bold;
  font-size: 12px;
  color:green;
}
.content_attr {
  margin-left: 2px;
}
</style>