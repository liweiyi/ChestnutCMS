<template>
  <div class="app-container">
    <el-row :gutter="24">
      <el-col :span="12" class="mb12">
        <el-row :gutter="10">
          <el-col :span="1.5">
            <el-button 
              type="danger"
              icon="el-icon-delete"
              size="mini"
              :disabled="multiple"
              @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button 
              icon="el-icon-refresh"
              size="mini"
              @click="handleRebuildIndex">{{ $t('CMS.ESIndex.RebuildAll') }}</el-button>
          </el-col>
        </el-row>
      </el-col>
      <el-col :span="12">
        <el-form 
          :model="queryParams"
          ref="queryForm"
          :inline="true"
          size="mini"
          class="el-form-search">
          <el-form-item prop="contentType">
            <el-select v-model="queryParams.contentType" :placeholder="$t('CMS.ContentCore.ContentType')" clearable style="width: 120px">
              <el-option 
                v-for="ct in contentTypeOptions"
                :key="ct.id"
                :label="ct.name"
                :value="ct.id" />
            </el-select>
          </el-form-item>
          <el-form-item prop="query">
            <el-input v-model="queryParams.query" :placeholder="$t('CMS.ESIndex.Query')">
            </el-input>
          </el-form-item>
          <el-form-item prop="onlyTitle">
            <el-checkbox v-model="queryParams.onlyTitle" :label="$t('CMS.ESIndex.OnlyTitle')" border></el-checkbox>
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

    <el-table v-loading="loading" :data="contentList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="contentId" width="180" />
      <el-table-column :label="$t('CMS.Content.Catalog')" align="left" width="180" prop="catalogName" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('CMS.Content.Title')" align="left" prop="title">
        <template slot-scope="scope">
          <span v-html="scope.row.title"></span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('CMS.Content.ContentType')" width="110" align="center" prop="contentType" :formatter="contentTypeFormat" />
      <el-table-column :label="$t('CMS.Content.Status')" width="110" align="center">
        <template slot-scope="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusFormat(scope.row, 'status') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('CMS.Content.PublishDate')" align="center" prop="publishDateInstance" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.publishDateInstance) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.CreateTime')" align="center" prop="createTimeInstance" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTimeInstance) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="_score" align="left" prop="hitScore" width="70" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Common.Operation')" align="center" width="180" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button 
            type="text"
            icon="el-icon-search"
            size="small"
            @click="handleShowDetail(scope.row)">{{ $t("Common.Details") }}</el-button>
          <el-button 
            type="text"
            icon="el-icon-edit"
            size="small"
            @click="handleEdit(scope.row)">{{ $t("Common.Edit") }}</el-button>
          <el-button 
            type="text"
            icon="el-icon-delete"
            size="small"
            @click="handleDelete(scope.row)">{{ $t("Common.Delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0"
                :total="total"
                :page.sync="queryParams.pageNum"
                :limit.sync="queryParams.pageSize"
                @pagination="getList" />

    <!-- 添加或修改资源对话框 -->
    <el-dialog 
      :title="$t('CMS.ESIndex.IndexDetails')"
      :visible.sync="open"
      width="960px"
      append-to-body>
      <el-row :gutter="15" class="data_row" v-for="(value, key, index) in showData" :key="index">
        <el-col :span="4" class="data_row_left">
          {{ key }}
        </el-col>
        <el-col :span="20">
          <span v-if="isHtmlField(key)" v-html="value"></span>
          <span v-else>{{ value }}</span>
        </el-col>
      </el-row>
      <div slot="footer"
           class="dialog-footer">
        <el-button @click="cancel">{{ $t('Common.Close') }}</el-button>
      </div>
    </el-dialog>
    <!-- 进度条 -->
    <cms-progress :title="$t('CMS.ESIndex.ProgressTitle')" :open.sync="openProgress" :taskId="taskId" @close="handleProgressClose"></cms-progress>
  </div>
</template>
<script>
import CMSProgress from '@/views/components/Progress';
import { getUserPreference } from "@/api/system/user";
import { getContentTypes } from "@/api/contentcore/catalog";
import { getContentIndexList, deleteContentIndex, rebuildIndex } from "@/api/contentcore/search";

export default {
  name: "CMSIndexList",
  dicts: ['CMSContentStatus', 'CMSContentAttribute'],
  components: {
    'cms-progress': CMSProgress
  },
  data () {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      taskId: undefined,
      openProgress: false,
      // 总条数
      total: 0,
      // 发布通道表格数据
      contentTypeOptions: [],
      contentList: [],
      showData: {},
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        contentType: "",
        query: "",
        onlyTitle: false,
        pageNum: 1,
        pageSize: 10,
      },
      // 表单参数
      form: {},
      openEditorW: false
    };
  },
  created () {
    getContentTypes().then(response => {
      this.contentTypeOptions = response.data;
      this.addContentType = this.contentTypeOptions[0].id;
    });
    getUserPreference('OpenContentEditorW').then(response => {
      this.openEditorW = response.data == 'Y'
    })
    this.getList();
  },
  methods: {
    getList () {
      this.loading = true;
      getContentIndexList(this.queryParams).then(response => {
        this.contentList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    isHtmlField (key) {
      return key == 'title' || key == 'fullText';
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
    handleQuery () {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery () {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    statusFormat (row, column) {
      return this.selectDictLabel(this.dict.type.CMSContentStatus, row[column]);
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
    handleSelectionChange (selection) {
      this.ids = selection.map(item => item.contentId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    handleShowDetail(row) {
      this.showData = row;
      this.open = true;
    },
    cancel () {
      this.open = false;
    },
    handleEdit (row) {
      this.openEditor(row.catalogId, row.contentId, row.contentType);
    },
    openEditor(catalogId, contentId, contentType) {
      if (this.openEditorW) {
        let routeData = this.$router.resolve({
          path: "/cms/content/editorW",
          query: { type: contentType, catalogId: catalogId, id: contentId },
        });
        window.open(routeData.href, '_blank');
      } else {
        this.$router.push({ path: "/cms/content/editor", query: { type: contentType, catalogId: catalogId, id: contentId, w: this.openEditorW } });
      }
    },
    handleDelete (row) {
      const contentIds = row.contentId ? [ row.contentId ] : this.ids;
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function () {
        return deleteContentIndex(contentIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess(this.$t('Common.DeleteSuccess'));
      }).catch(function () { });
    },
    handleRebuildIndex() {
      rebuildIndex().then(response => {
        if (response.data && response.data != "") {
          this.taskId = response.data;
          this.openProgress = true;
        }
      });
    },
    handleProgressClose(result) {
      if (result.status == 'SUCCESS' || result.status == 'INTERRUPTED') {
        this.getList();
      }
    }
  }
};
</script>
<style scoped>
.data_row {
  line-height: 28px;
}
.data_row_left {
  font-weight: 600;
  text-align: right;
}
</style>