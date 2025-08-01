<template>
  <div class="app-container">
    <el-dialog 
      :title="$t('CMS.Template.SelectorTitle')"
      :visible.sync="visible"
      width="700px"
      :close-on-click-modal="false"
      append-to-body>
      <el-form 
        :model="queryParams"
        ref="queryForm"
        :inline="true"
        label-width="68px"
        class="el-form-search mb12">
        <el-form-item prop="filename">
          <el-input v-model="queryParams.filename" :placeholder="$t('CMS.Template.Name')" size="small">
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button-group>
            <el-button 
              type="primary"
              icon="el-icon-search"
              size="small"
              @click="handleQuery">{{ $t("Common.Search") }}</el-button>
            <el-button 
              icon="el-icon-refresh"
              size="small"
              @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
          </el-button-group>
        </el-form-item>
      </el-form>
      <el-table 
        v-loading="loading"
        :height="435"
        :data="templateList"
        highlight-current-row
        @cell-dblclick="handleDbClick"
        @current-change="handleSelectionChange">
        <el-table-column 
          type="index"
          :label="$t('Common.RowNo')"
          align="center"
          width="50" />
        <el-table-column 
          :label="$t('CMS.Template.Name')"
          align="left"
          prop="path">
          <template slot-scope="scope">
            <span v-html="scope.row.displayPath"></span>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="total>0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getList"
      />
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :disabled="okBtnDisabled" @click="handleOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleCancel">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<style scoped>
</style>
<script>
import { getTemplateList } from "@/api/contentcore/template";

export default {
  name: "CMSTemplateSelector",
  props: {
    publishPipeCode: {
      type: String,
      default: "",
      required: true
    },
    open: {
      type: Boolean,
      default: false,
      required: true
    },
    siteId: {
      type: String,
      default: "0",
      required: false,
    }
  },
  watch: {
    open () {
      this.visible = this.open;
    },
    visible (newVal) {
      if (!newVal) {
        this.handleCancel();
      } else {
        if (!this.inited) {
          this.inited = true;
          this.getList();
        }
      }
    },
    publishPipeCode (newVal) {
      if (this.queryParams.publishPipeCode !== newVal) {
        this.queryParams.publishPipeCode = newVal;
        this.queryParams.pageNum = 1;
        if (this.inited) this.getList();
      }
    },
    siteId(newVal) {
      if (this.queryParams.siteId !== newVal) {
       this.queryParams.siteId = newVal;
       this.queryParams.pageNum = 1;
       if (this.inited) this.getList();
      }
    },
  },
  computed: {
    okBtnDisabled() {
      return this.selectedTemplate == undefined;
    }
  },
  data () {
    return {
      // 遮罩层
      loading: false,
      visible: this.open,
      inited: false,
      // 选中数组
      selectedTemplate: undefined,
      // 资源表格数据
      templateList: [],
      total: 0,
      // 查询参数
      queryParams: {
        publishPipeCode: this.publishPipeCode,
        filename: undefined,
        siteId: this.siteId,
        pageSize: 8,
        pageNum: 1
      }
    };
  },
  methods: {
    getList () {
      if (!this.inited) {
        return;
      }
      this.loading = true;
      getTemplateList(this.queryParams).then(response => {
        this.templateList = response.data.rows.map(item => {
          let arr = item.path.split("/");
          if (arr.length > 1) {
            for(let i = 0; i < arr.length - 1; i++) {
              item.displayPath = '<span style="color: #1890ff">' + arr[i] + '</span> / ';
            }
            item.displayPath += arr[arr.length - 1];
          } else {
            item.displayPath = arr[0];
          }
          return item;
        });
        this.total = parseInt(response.data.total);
        this.selectedTemplate = undefined;
        this.loading = false;
      });
    },
    handleSelectionChange (selection) {
      if (selection) {
        this.selectedTemplate = selection.path;
      }
    },
    handleDbClick (row) {
      console.log(row)
      this.$emit("ok", row.path);
    },
    handleOk () {
      this.$emit("ok", this.selectedTemplate);
    },
    handleCancel () {
      this.$emit("cancel");
      this.queryParams.filename = undefined;
      this.selectedTemplate = undefined;
      // this.templateList = [];
    },
    handleQuery () {
      this.getList();
    },
    resetQuery () {
      this.queryParams.filename = undefined;
      this.handleQuery();
    }
  }
};
</script>