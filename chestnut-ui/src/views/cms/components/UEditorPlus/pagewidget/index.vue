<template>
  <div class="app-container" style="padding: 0;">
    <el-dialog 
      class="pagewidget-dialog"
      :title="$t('CMS.UEditor.PageWidget.DialogTitle')"
      :visible.sync="visible"
      width="980px"
      :close-on-click-modal="false"
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
              class="el-form-search mb12">
            <el-form-item label="" prop="query">
              <el-input v-model="queryParams.query" :placeholder="$t('CMS.UEditor.PageWidget.Name')">
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
            ref="tablePageWidgetList"
            :data="pageWidgetList"
            highlight-current-row
            @row-click="handleRowClick"
            @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="50" align="center" />
            <el-table-column :label="$t('CMS.UEditor.PageWidget.Name')" align="left" prop="title">
              <template slot-scope="scope">
                <span><i v-if="scope.row.topFlag>0" class="el-icon-top top-icon" :title="$t('CMS.Content.SetTop')"></i> {{ scope.row.title }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('CMS.UEditor.PageWidget.Code')" align="center" prop="publishDate" width="160">
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
            @pagination="loadPageWidgetList" />
        </el-main>
      </el-container>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleCancel">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { listPageWidgets } from "@/api/contentcore/pagewidget";
import CMSCatalogTree from '@/views/cms/contentcore/catalogTree';

export default {
  name: "CMSUeditorPageWidget",
  components: {
    'cms-catalog-tree': CMSCatalogTree,
  },
  props: {
    open: {
      type: Boolean,
      default: false,
      required: true
    },
    type: {
      type: String,
      default: "",
      required: false
    }
  },
  data () {
    return {
      visible: false,
      loading: false,
      selectedRows: undefined,
      pageWidgetList: [],
      total: 0,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        status: 30,
        catalogId: '',
        query: '',
        type: this.type
      }
    };
  },
  watch: {
    open (newVal) {
      this.visible = newVal;
    },
    visible (newVal) {
      if (!newVal) {
        this.noticeClose();
      }
    }
  },
  methods: {
    handleTreeNodeClick(data) {
      this.queryParams.catalogId = data && data != null ? data.id : ''; 
      this.loadPageWidgetList();
    },
    loadPageWidgetList () {
      if (!this.visible) {
        return;
      }
      this.loading = true;
      listPageWidgets(this.queryParams).then(response => {
          this.pageWidgetList = response.data.rows;
          this.total = parseInt(response.data.total);
          this.selectedRows = [];
          this.loading = false;
      });
    },
    handleRowClick (row) {
      this.selectedRows.forEach(row => {
          this.$refs.tablePageWidgetList.toggleRowSelection(row, false);
      });
      this.selectedRows = [];
      this.$refs.tablePageWidgetList.toggleRowSelection(row);
    },
    handleSelectionChange (selection) {
      this.selectedRows = selection;
    },
    handleQuery () {
      this.loadPageWidgetList();
    },
    handleOk () {
      this.noticeOk();
    },
    handleCancel () {
      this.visible = false;
    },
    noticeOk () {
      if (this.visible && this.selectedRow) {
        var html = '<p class="text-align:center;"><img src="/UEditorPlus/themes/default/images/spacer.gif" pw_code="'
          + this.selectedRow.code + '" title="' + this.selectedRow.name + '" class="pw_placeholder" /></p>' // 引用页面部件占位符

        this.$emit("ok", html);
        this.visible = false;
      }
    },
    noticeClose () {
      if (!this.visible) {
        this.$emit('update:open', false);
        this.$emit("close");
        
        this.selectedRows = [];
        this.pageWidgetList = [];
        
        this.queryParams = {
          pageNum: 1,
          pageSize: 10,
          status: 30,
          catalogId: '',
          query: '',
          type: ''
        }
      }
    },
    resetQuery() {
        this.queryParams = {
          pageNum: 1,
          pageSize: 10,
          status: 30,
          catalogId: '',
          query: '',
          type: ''
        }
        this.loadPageWidgetList()
    }
  }
};
</script>