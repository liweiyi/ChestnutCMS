<template>
  <div class="pagewidget-container">
    <el-row :gutter="24" class="mb12">
      <el-col :span="8">
        <el-button 
          plain
          type="primary"
          icon="el-icon-plus"
          size="mini"
          v-hasPermi="[ $p('Site:AddPageWidget:{0}', [ siteId ]) ]"
          @click="handleAdd">{{ $t("Common.Add") }}</el-button>
      </el-col>
      <el-col :span="16" style="text-align: right;">
        <el-select 
          v-model="queryParams.type" size="mini"
          :placeholder="$t('CMS.PageWidget.Placeholder.Type')"
          clearable
          @change="loadPageWidgetList">
          <el-option 
            v-for="item in pageWidgetTypes"
            :key="item.id"
            :label="item.name"
            :value="item.id" />
        </el-select>
      </el-col>
    </el-row>
    <el-table 
      v-loading="loading"
      ref="pageWidgetList"
      size="small"
      :data="pageWidgetList"
      :height="tableHeight"
      :max-height="tableMaxHeight"
      @row-click="handleRowClick"
      @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column :label="$t('CMS.PageWidget.Name')" prop="name" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          {{ scope.row.name }}
        </template>
      </el-table-column>
      <el-table-column :label="$t('CMS.PageWidget.Code')" align="left" prop="code" />
      <el-table-column :label="$t('CMS.PageWidget.Type')" align="center" width="100">
        <template slot-scope="scope">
          {{ pageWidgetTypeName(scope.row.type) }}
        </template>
      </el-table-column>
      <el-table-column :label="$t('CMS.Content.Status')" align="center" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.CMSPageWidgetStatus" :value="scope.row.state"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('CMS.PageWidget.PublishPipe')" width="110" align="center">
        <template slot-scope="scope">
          {{ publishPipeName(scope.row.publishPipeCode) }}
        </template>
      </el-table-column>
      <el-table-column :label="$t('CMS.PageWidget.Path')" align="left" width="180" prop="path" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Common.Operation')" align="right" width="250" class-name="small-padding fixed-width">
        <template slot-scope="scope">
            <span class="btn-cell-wrap">
              <el-button 
                size="mini"
                type="text"
                icon="el-icon-s-promotion"
                v-hasPermi="[ $p('PageWidget:Publish:{0}', [ scope.row.pageWidgetId ]) ]"
                @click="handlePublish(scope.row)">{{ $t('CMS.ContentCore.Publish') }}</el-button>
            </span>
            <span class="btn-cell-wrap">
              <el-button 
                size="mini"
                type="text"
                icon="el-icon-view"
                @click="handlePreview(scope.row)">{{ $t('CMS.ContentCore.Preview') }}</el-button>
            </span>
            <span class="btn-cell-wrap">
              <el-button 
                size="mini"
                type="text"
                icon="el-icon-edit"
                v-hasPermi="[ $p('PageWidget:Edit:{0}', [ scope.row.pageWidgetId ]) ]"
                @click="handleEdit(scope.row)">{{ $t('Common.Edit') }}</el-button>
            </span>
            <span class="btn-cell-wrap">
              <el-button 
                size="mini"
                type="text"
                icon="el-icon-delete"
                v-hasPermi="[ $p('PageWidget:Delete:{0}', [ scope.row.pageWidgetId ]) ]"
                @click="handleDelete(scope.row)">{{ $t("Common.Delete") }}</el-button>
            </span>
        </template>
      </el-table-column>
    </el-table>
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="loadPageWidgetList"
    />
    <!-- 添加对话框 -->
    <el-dialog 
      :title="$t('CMS.PageWidget.AddTitle')"
      :visible.sync="dialogVisible"
      :close-on-click-modal="false"
      width="600px"
      append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-form-item :label="$t('CMS.PageWidget.Type')" prop="type">
          <el-select v-model="form.type" clearable>
            <el-option  
              v-for="item in pageWidgetTypes"
              :key="item.id"
              :label="item.name"
              :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('CMS.PageWidget.Name')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="$t('CMS.PageWidget.Code')" prop="code">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item :label="$t('CMS.PageWidget.PublishPipe')" prop="publishPipeCode">
          <el-select v-model="form.publishPipeCode">
            <el-option
              v-for="pp in publishPipes"
              :key="pp.pipeCode"
              :label="pp.pipeName"
              :value="pp.pipeCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('CMS.PageWidget.Path')" prop="path">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea" maxlength="100" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleAddDialogOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleAddDialogClose">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getPublishPipeSelectData } from "@/api/contentcore/publishpipe";
import { listPageWidgetTypes, listPageWidgets, addPageWidget, deletePageWidget, publishPageWidgets } from "@/api/contentcore/pagewidget";

export default {
  name: "CMSPageWdiget",
  dicts: [ 'CMSPageWidgetStatus' ],
  props: {
    cid: {
      type: String,
      default: undefined,
      required: false,
    }
  },
  data () {
    return {
      loading: false,
      tableHeight: 600, // 表格高度
      tableMaxHeight: 600, // 表格最大高度
      selectedRows: [], // 表格选中行
      single: true,
      multiple: true,
      siteId: this.$cache.local.get("CurrentSite"),
      catalogId: this.cid,
      dialogVisible: false,
      publishPipes: [],
      pageWidgetTypes: [],
      pageWidgetList: [],
      total: 0,
      queryParams: {
        type: undefined,
        pageSize: 20,
        pageNo: 1
      },
      form: {
        path: 'include/pagewidget/'
      },
      rules: {
        type: [
          { required: true, message: this.$t('CMS.PageWidget.RuleTips.Type'), trigger: "blur" }
        ],
        name: [
          { required: true, message: this.$t('CMS.PageWidget.RuleTips.Name'), trigger: "blur" }
        ],
        code: [
          { required: true, pattern: "^[A-Za-z0-9_]+$", message: this.$t('CMS.PageWidget.RuleTips.Code'), trigger: "blur" }
        ],
        publishPipeCode: [
          { required: true, message: this.$t('CMS.PageWidget.RuleTips.PublishPipe'), trigger: "blur" }
        ],
        path: [
          { required: true, pattern: "^[A-Za-z0-9_\/]+$", message: this.$t('CMS.PageWidget.RuleTips.Path'), trigger: "blur" }
        ]
      }
    };
  },
  watch: {
    cid(newVal) { 
      this.catalogId = newVal;
    },
    catalogId(newVal) {
      this.loadPageWidgetList();
    },
  },
  created() {
    this.changeTableHeight();
    this.loadPageWidgetList();
    this.loadPageWdigetTypes();
    this.loadPublishPipes();
  },
  methods: {
    changeTableHeight () {
      let height = document.body.offsetHeight // 网页可视区域高度
      this.tableHeight = height - 330;
      this.tableMaxHeight = this.tableHeight;
    },
    loadPageWdigetTypes() {
      listPageWidgetTypes().then(response => {
        this.pageWidgetTypes = response.data.rows;
      });
    },
    pageWidgetTypeName(type) {
      let pt = this.pageWidgetTypes.find(v => v.id == type);
      return pt ? pt.name : type;
    },
    loadPublishPipes() {
      getPublishPipeSelectData().then(response => {
        this.publishPipes = response.data.rows;
      });
    },
    publishPipeName(code) {
      let pp = this.publishPipes.find(v => v.pipeCode == code);
      return pp ? pp.pipeName : code
    },
    loadPageWidgetList () {
      this.loading = true;
      this.queryParams.catalogId = this.catalogId;
      listPageWidgets(this.queryParams).then(response => {
        this.pageWidgetList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    handleSelectionChange (selection) {
      this.selectedRows = selection;
      this.single = selection.length != 1;
      this.multiple = !selection.length;
    },
    handleRowClick (currentRow) {
      this.toggleAllCheckedRows();
      this.$refs.pageWidgetList.toggleRowSelection(currentRow);
      this.selectedRows.push(currentRow);
    },
    toggleAllCheckedRows() {
      this.selectedRows.forEach(row => {
          this.$refs.pageWidgetList.toggleRowSelection(row, false);
      });
      this.selectedRows = [];
    },
    handleAdd() {
      this.dialogVisible = true;
      this.form = { path: 'include/pagewidget/' };
    },
    handleAddDialogClose() {
      this.dialogVisible = false;
    },
    handleAddDialogOk () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.form.catalogId = (this.catalogId && this.catalogId.length > 0) ? this.catalogId : 0;
          addPageWidget(this.form).then(response => {
            this.$modal.msgSuccess(response.msg);
            this.loadPageWidgetList();
            this.handleAddDialogClose();
          });
        }
      });
    },
    handleDelete(row) {
      const pageWidgetIds = [ row.pageWidgetId ];
      this.$modal.confirm(this.$t('Common.ConfigmDelete')).then(function() {
        return deletePageWidget(pageWidgetIds);
      }).then(response => {
        this.$modal.msgSuccess(response.msg);
        this.loadPageWidgetList();
      }).catch(() => {});
    },
    handleEdit(row) {
      this.$router.push({ path: row.route, query: { id: row.pageWidgetId, from: "pagewidget" } });
    },
    handlePublish(row) {
      const pageWidgetIds = [ row.pageWidgetId ];
      publishPageWidgets(pageWidgetIds).then(response => {
        if (response.code === 200) {
          this.$modal.msgSuccess(response.msg);
          this.loadPageWidgetList();
        }
      });
    },
    handlePreview(row) {
      let routeData = this.$router.resolve({
        path: "/cms/preview",
        query: { type: "pagewidget", dataId: row.pageWidgetId },
      });
      window.open(routeData.href, '_blank');
    }
  }
};
</script>
<style>
.pagewidget-container .el-card {
  margin-bottom: 10px;
}
.pagewidget-container .el-card__body {
  padding: 0;
}
.pagewidget-container .item-state {
  text-align: left;
}
.pagewidget-container .item-toolbar {
  text-align: right;
}
.pagewidget-container .item-catalog {
  margin-right: 5px;
}
.pagewidget-container .item-row1, .item-row2 {
  padding: 10px;
}
.pagewidget-container .item-row1 .item-name {
  font-size: 16px;
  font-weight: 400;
}
.pagewidget-container .pagination-container {
  height: 30px;
}
.pagewidget-container .item-row2 {
  color: #999;
}
.pagewidget-container .item-row3 {
  background-color: #f9f9f9;
  padding: 10px;
}
</style>