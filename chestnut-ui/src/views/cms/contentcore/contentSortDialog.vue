<template>
  <div class="app-container-content-sort">
    <el-dialog 
      :title="$t('CMS.Content.SortDialogTitle')"
      :visible.sync="visible"
      width="800px"
      :close-on-click-modal="false"
      custom-class="content-selector-dialog"
      append-to-body>
      <div style="background-color: #f4f4f5;color: #909399;font-size:12px;line-height: 30px;padding-left:10px;">
        <i class="el-icon-info mr5"></i>{{ $t("CMS.Content.SortDialogTip") }}
      </div>
      <el-form 
        :model="queryParams"
        ref="queryForm"
        :inline="true"
        size="small"
        @submit.native.prevent
        class="el-form-search mt10 mb10"
        style="text-align:left">
        <el-form-item label="" prop="title">
          <el-input 
            v-model="queryParams.title" 
            :placeholder="$t('CMS.Content.Placeholder.Title')" 
            @keyup.enter.native="handleQuery">
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button-group>
            <el-button type="primary" icon="el-icon-search" @click="handleQuery">{{ $t("Common.Search") }}</el-button>
            <el-button icon="el-icon-refresh" @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
          </el-button-group>
        </el-form-item>
      </el-form>
      <el-table 
        v-loading="loading"
        :data="contentList"
        size="mini"
        highlight-current-row
        @current-change="handleSelectionChange">
        <el-table-column type="index" :label="$t('Common.RowNo')" align="center" width="50" />
        <el-table-column :label="$t('CMS.Content.Title')" align="left" prop="title">
          <template slot-scope="scope">
            <span><i v-if="scope.row.topFlag>0" class="el-icon-top top-icon" :title="$t('CMS.Content.SetTop')"></i> {{ scope.row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('CMS.Content.Status')" align="center" prop="status">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.CMSContentStatus" :value="scope.row.status"/>
          </template>
        </el-table-column>
        <el-table-column :label="$t('CMS.Content.PublishDate')" align="center" prop="publishDate" width="160">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.publishDate) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <pagination 
        v-show="total>0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="loadContentList" />
      <div slot="footer" class="dialog-footer">
        <el-button type="primary"  @click="handleOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleClose">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getContentList } from "@/api/contentcore/content";

export default {
  name: "CMSContentSortDialog",
  dicts: ['CMSContentStatus'],
  props: {
    open: {
      type: Boolean,
      default: false,
      required: true
    },
    cid: {
      type: String,
      default: undefined,
      required: false
    }
  },
  watch: {
    open () {
      this.visible = this.open;
    },
    visible (newVal) {
      if (!newVal) {
        this.handleClose();
      } else {
        this.loadContentList();
      }
    },
    cid () {
      this.queryParams.catalogId = this.cid;
    }
  },
  data () {
    return {
      loading: false,
      visible: this.open,
      selectedContents: [],
      contentList: [],
      total: 0,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        catalogId: this.cid,
        title: undefined
      }
    };
  },
  methods: {
    loadContentList () {
      if (!this.visible) {
        return;
      }
      this.loading = true;
      getContentList(this.queryParams).then(response => {
        this.contentList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.selectedContents = [];
        this.loading = false;
      });
    },
    handleSelectionChange (selection) {
      if (selection && selection.contentId) {
        this.selectedContents = [ selection ];
      }
    },
    handleOk () {
      this.$emit("ok", this.selectedContents);
    },
    // 取消按钮
    handleClose () {
      this.$emit("close");
      this.resetForm("queryForm");
      this.selectedContents = [];
      this.contentList = [];
    },
    /** 搜索按钮操作 */
    handleQuery () {
      this.loadContentList();
    },
    /** 重置按钮操作 */
    resetQuery () {
      this.resetForm("queryForm");
      this.handleQuery();
    }
  }
};
</script>
<style>
.content-selector-dialog .top-icon {
  font-weight: bold;
  font-size: 12px;
  color:green;
}
</style>