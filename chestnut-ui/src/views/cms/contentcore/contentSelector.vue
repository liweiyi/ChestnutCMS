<template>
  <div class="app-container" style="padding: 0px;">
    <el-dialog 
      :title="$t('CMS.ContentCore.SelectContent')"
      :visible.sync="visible"
      width="1100px"
      :close-on-click-modal="false"
      custom-class="content-selector-dialog"
      append-to-body>
      <el-container>
        <el-aside>
          <cms-catalog-tree 
            ref="catalogTree"
            @node-click="handleTreeNodeClick">
          </cms-catalog-tree>
        </el-aside>
        <el-main>
          <el-form :model="queryParams"
              ref="queryForm"
              :inline="true"
              size="small"
              @submit.native.prevent
              class="el-form-search mb12">
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
            ref="tableContentList"
            :data="contentList"
            highlight-current-row
            @row-click="handleRowClick"
            @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="50" align="center" />
            <el-table-column :label="$t('CMS.Content.Title')" align="left" prop="title">
              <template slot-scope="scope">
                <span><i v-if="scope.row.topFlag>0" class="el-icon-top top-icon" :title="$t('CMS.Content.SetTop')"></i> {{ scope.row.title }}</span>
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
        </el-main>
      </el-container>
      
      <div slot="footer"
            class="dialog-footer">
        <el-button type="primary" 
                    @click="handleOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleClose">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getContentList } from "@/api/contentcore/content";
import CMSCatalogTree from '@/views/cms/contentcore/catalogTree';

export default {
  name: "CMSContentSelector",
  components: {
    'cms-catalog-tree': CMSCatalogTree,
  },
  props: {
    open: {
      type: Boolean,
      default: false,
      required: true
    },
    contentType: {
      type: String,
      default: '',
      required: false
    }
  },
  watch: {
    contentType (newVal) {
      this.queryParams.contentType = newVal
    },
    open () {
      this.visible = this.open;
    },
    visible (newVal) {
      if (!newVal) {
        this.handleClose();
      } else {
        this.loadContentList();
      }
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
        status: 30,
        catalogId: '',
        contentType: '',
        title: ''
      }
    };
  },
  methods: {
    handleTreeNodeClick(data) {
      this.queryParams.catalogId = data && data != null ? data.id : ''; 
      this.loadContentList();
    },
    loadContentList () {
      if (!this.visible) {
        return;
      }
      this.loading = true;
      getContentList(this.queryParams).then(response => {
        if (response.code == 200) {
          this.contentList = response.data.rows;
          this.total = parseInt(response.data.total);
        }
        this.selectedContents = [];
        this.loading = false;
      });
    },
    handleRowClick (row) {
      this.selectedContents.forEach(row => {
          this.$refs.tableContentList.toggleRowSelection(row, false);
      });
      this.selectedContents = [];
      this.$refs.tableContentList.toggleRowSelection(row);
    },
    handleSelectionChange (selection) {
      this.selectedContents = selection;
    },
    handleOk () {
      this.$emit("ok", this.selectedContents);
    },
    // 取消按钮
    handleClose () {
      this.$emit("close");
      this.resetForm("queryForm");
      this.queryParams.contentType = ''
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
.content-selector-dialog .el-dialog__body {
  padding-top: 10px;
  padding-bottom: 10px;
}
.content-selector-dialog .el-aside {
  padding: 10px;
  background-color: #fff;
}
.content-selector-dialog .el-main {
  padding: 10px;
}
</style>