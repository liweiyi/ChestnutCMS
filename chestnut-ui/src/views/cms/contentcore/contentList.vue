<template>
  <div class="cms-content-list">
    <el-row :gutter="10" class="mb12">
      <el-col :span="1.5">
        <el-popover v-model="addPopoverVisible" class="btn-permi" placement="bottom-start" :width="420" trigger="click" v-hasPermi="[ $p('Catalog:AddContent:{0}', [ catalogId ]) ]">
          <el-row style="margin-bottom:20px;">
            <el-divider content-position="left">{{ $t('CMS.Content.ContentType') }}</el-divider>
            <el-radio-group v-model="addContentType">
              <el-radio-button 
                v-for="ct in contentTypeOptions"
                :key="ct.id"
                :label="ct.id"
              >{{ct.name}}</el-radio-button>
            </el-radio-group>
            <el-divider v-if="addContentType=='article'" content-position="left">{{ $t('CMS.Article.Format') }}</el-divider>
            <el-radio-group v-if="addContentType=='article'" v-model="addArticleBodyFormat">
              <el-radio-button 
                v-for="item in articleBodyFormatOptions"
                :key="item.id"
                :label="item.id"
              >{{ item.name }}</el-radio-button>
            </el-radio-group>
          </el-row>
          <el-row style="text-align:right;">
            <el-button 
              plain
              type="primary"
              size="small"
              @click="handleAdd">{{ $t('Common.Confirm') }}</el-button>
          </el-row>
          <el-button 
            type="primary"
            slot="reference"
            icon="el-icon-plus"
            size="mini"
            plain>{{ $t("Common.Add") }}<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
        </el-popover>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="danger"
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          v-hasPermi="[ $p('Catalog:DeleteContent:{0}', [ catalogId ]) ]"
          @click="handleDelete">{{ $t("Common.Delete") }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="primary"
          icon="el-icon-timer"
          size="mini"
          :disabled="multiple"
          v-hasPermi="[ $p('Catalog:EditContent:{0}', [ catalogId ]) ]"
          @click="handleToPublish">{{ $t("CMS.ContentCore.ToPublish") }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="success"
          icon="el-icon-s-promotion"
          size="mini"
          :disabled="multiple"
          v-hasPermi="[ $p('Catalog:EditContent:{0}', [ catalogId ]) ]"
          @click="handlePublish">{{ $t("CMS.ContentCore.Publish") }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="warning"
          icon="el-icon-download"
          size="mini"
          :disabled="multiple"
          v-hasPermi="[ $p('Catalog:EditContent:{0}', [ catalogId ]) ]"
          @click="handleOffline">{{ $t("CMS.Content.Offline") }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="primary"
          icon="el-icon-document-copy"
          size="mini"
          :disabled="multiple"
          @click="handleCopy">{{ $t("Common.Copy") }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="primary"
          icon="el-icon-right"
          size="mini"
          :disabled="multiple"
          @click="handleMove">{{ $t("Common.Move") }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-dropdown class="btn-permi" v-hasPermi="[ $p('Catalog:EditContent:{0}', [ catalogId ]) ]">
          <el-button 
            plain
            size="mini" 
            type="primary"
            :disabled="multiple">
            <svg-icon icon-class="recommend" /> {{ $t('CMS.Content.Recommend') }}<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item icon="el-icon-plus" :disabled="multiple" @click.native="handleRecommend">{{ $t('CMS.Content.Recommend') }}</el-dropdown-item>
            <el-dropdown-item icon="el-icon-minus" :disabled="multiple" @click.native="handleCancelRecommend">{{ $t('CMS.Content.CancelRecommend') }}</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </el-col>
      <el-col :span="1.5">
        <el-dropdown class="btn-permi" v-hasPermi="[ $p('Catalog:EditContent:{0}', [ catalogId ]) ]">
          <el-button 
            plain
            size="mini" 
            type="primary"
            :disabled="multiple">
            <svg-icon icon-class="fire" /> {{ $t('CMS.Content.Hot') }}<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item icon="el-icon-plus" :disabled="multiple" @click.native="handleHot">{{ $t('CMS.Content.Hot') }}</el-dropdown-item>
            <el-dropdown-item icon="el-icon-minus" :disabled="multiple" @click.native="handleCancelHot">{{ $t('CMS.Content.CancelHot') }}</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </el-col>
    </el-row>
    <el-row>
      <el-form :model="queryParams" ref="queryForm" size="small" class="el-form-search" style="text-align:left;" :inline="true">
        <div>
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
            <el-select  v-model="queryParams.sorts" @change="loadContentList" style="width: 140px">
              <el-option value="" :label="$t('CMS.Content.SortOption.Default')"></el-option>
              <el-option value="createTime#ASC" :label="$t('CMS.Content.SortOption.CreateTimeAsc')"></el-option>
              <el-option value="createTime#DESC" :label="$t('CMS.Content.SortOption.CreateTimeDesc')"></el-option>
              <el-option value="publishDate#ASC" :label="$t('CMS.Content.SortOption.PublishDateAsc')"></el-option>
              <el-option value="publishDate#DESC" :label="$t('CMS.Content.SortOption.PublishDateDesc')"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button-group>
              <el-button type="primary" icon="el-icon-search" @click="handleQuery">{{ $t("Common.Search") }}</el-button>
              <el-button icon="el-icon-refresh" @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
            </el-button-group>
          </el-form-item>
          <el-form-item>
            <el-button icon="el-icon-plus" @click="showSearch=!showSearch">{{ $t("Common.More") }}</el-button>
          </el-form-item>
        </div>
        <div v-show="showSearch">
          <el-form-item :label="$t('Common.CreateTime')">
            <el-date-picker 
              v-model="dateRange"
              style="width: 386px"
              value-format="yyyy-MM-dd HH:mm:ss"
              type="datetimerange"
              range-separator="-"
              :start-placeholder="$t('Common.BeginDate')"
              :end-placeholder="$t('Common.EndDate')"></el-date-picker>
          </el-form-item>
        </div>
      </el-form>
    </el-row>

    <el-table 
      v-loading="loading"
      ref="tableContentList"
      size="small"
      :data="contentList"
      :height="tableHeight"
      :max-height="tableMaxHeight"
      @row-click="handleRowClick"
      @cell-dblclick="handleEdit"
      @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column :label="$t('CMS.Content.Title')" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <span class="content_attr" v-if="scope.row.topFlag>0" :title="$t('CMS.Content.SetTop')">[<svg-icon icon-class="top" />]</span>
          <span 
            v-for="dict in dict.type.CMSContentAttribute"
            :key="dict.value"
            :title="dict.label">
            <span class="content_attr" v-if="scope.row.attributes.indexOf(dict.value)>-1">[<svg-icon :icon-class="dict.value" />]</span>
          </span>
          {{ scope.row.title }}
          <!-- <el-link type="primary" @click="handleEdit(scope.row)" :title="scope.row.title" class="link-type">
            {{ scope.row.title }}
          </el-link> -->
        </template>
      </el-table-column>
      <el-table-column :label="$t('CMS.Content.ContentType')" width="110" align="center" prop="contentType" :formatter="contentTypeFormat" />
      <el-table-column :label="$t('CMS.Content.Status')" align="center" width="110">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.CMSContentStatus" :value="scope.row.status"/>
          </template>
      </el-table-column>
      <el-table-column :label="$t('Common.CreateTime')" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column 
        :label="$t('Common.Operation')"
        align="center"
        width="260"
        class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <span class="btn-cell-wrap">
            <el-button
              size="small"
              type="text"
              icon="el-icon-view"
              @click="handlePreview(scope.row)">{{ $t('CMS.ContentCore.Preview') }}</el-button>
          </span>
          <span class="btn-cell-wrap">
            <el-button 
              size="small"
              type="text"
              icon="el-icon-s-promotion"
              v-hasPermi="[ $p('Catalog:EditContent:{0}', [ scope.row.catalogId ]) ]"
              @click="handlePublish(scope.row)">{{ $t('CMS.ContentCore.Publish') }}</el-button>
          </span>
          <span class="btn-cell-wrap">
            <el-button 
              size="small"
              type="text"
              icon="el-icon-timer"
              v-hasPermi="[ $p('Catalog:EditContent:{0}', [ scope.row.catalogId ]) ]"
              @click="handleToPublish(scope.row)">{{ $t('CMS.ContentCore.ToPublish') }}</el-button>
          </span>
          <el-dropdown size="small">
            <el-link :underline="false" class="row-more-btn" icon="el-icon-more"></el-link>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item icon="el-icon-edit" @click.native="handleEdit(scope.row)" v-hasPermi="[ $p('Catalog:EditContent:{0}', [ scope.row.catalogId ]) ]">{{ $t('Common.Edit') }}</el-dropdown-item>
              <el-dropdown-item icon="el-icon-delete" @click.native="handleDelete(scope.row)" v-hasPermi="[ $p('Catalog:DeleteContent:{0}', [ scope.row.catalogId ]) ]">{{ $t('Common.Delete') }}</el-dropdown-item>
              <el-dropdown-item icon="el-icon-sort" @click.native="handleSort(scope.row)" v-hasPermi="[ $p('Catalog:EditContent:{0}', [ scope.row.catalogId ]) ]">{{ $t('Common.Sort') }}</el-dropdown-item>
              <el-dropdown-item v-show="scope.row.topFlag<=0" icon="el-icon-top" @click.native="handleSetTop(scope.row)" v-hasPermi="[ $p('Catalog:EditContent:{0}', [ scope.row.catalogId ]) ]">{{ $t('CMS.Content.SetTop') }}</el-dropdown-item>
              <el-dropdown-item v-show="scope.row.topFlag>0" icon="el-icon-bottom" @click.native="handleCancelTop(scope.row)" v-hasPermi="[ $p('Catalog:EditContent:{0}', [ scope.row.catalogId ]) ]">{{ $t('CMS.Content.CancelTop') }}</el-dropdown-item>
              <el-dropdown-item icon="el-icon-document-copy" @click.native="handleCopy(scope.row)">{{ $t('Common.Copy') }}</el-dropdown-item>
              <el-dropdown-item icon="el-icon-right" @click.native="handleMove(scope.row)">{{ $t('Common.Move') }}</el-dropdown-item>
              <el-dropdown-item icon="el-icon-download" @click.native="handleOffline(scope.row)" v-hasPermi="[ $p('Catalog:EditContent:{0}', [ scope.row.catalogId ]) ]">{{ $t('CMS.Content.Offline') }}</el-dropdown-item>
              <!-- <el-dropdown-item icon="el-icon-document" @click.native="handleArchive(scope.row)">{{ $t('CMS.Content.Archive') }}</el-dropdown-item> -->
              <el-dropdown-item icon="el-icon-search" @click.native="handleCreateIndex(scope.row)">{{ $t('CMS.Content.GenIndex') }}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>
    <pagination 
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="loadContentList" />
              
    <!-- 置顶时间设置弹窗 -->  
    <el-dialog 
      :title="$t('CMS.Content.SetTop')"
      width="400px"
      :visible.sync="topDialogVisible"
      :close-on-click-modal="false"
      append-to-body>
      <el-form ref="top_form" label-width="100px" :model="topForm">
        <el-form-item :label="$t('CMS.Content.TopEndTime')" prop="topEndTime">
          <el-date-picker 
            v-model="topForm.topEndTime" 
            :picker-options="topEndTimePickerOptions"
            value-format="yyyy-MM-dd HH:mm:ss"
            type="datetime">
          </el-date-picker>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleTopDialogOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="topDialogVisible=false">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
    <!-- 进度条 -->
    <cms-progress :title="$t('CMS.ContentCore.PublishProgressTitle')" :open.sync="openProgress" :taskId="taskId"></cms-progress>
    <!-- 栏目选择组件 -->
    <cms-catalog-selector
      :open="openCatalogSelector"
      :showCopyToolbar="isCopy"
      :multiple="isCopy"
      @ok="handleCatalogSelectorOk"
      @close="handleCatalogSelectorClose"></cms-catalog-selector>
    <!-- 内容排序组件 -->
    <cms-content-sort
      :open="openContentSortDialog"
      :cid="catalogId"
      @ok="handleContentSortDialogOk"
      @close="handleContentSortDialogClose"></cms-content-sort>
  </div>
</template>
<script>
import { getUserPreference } from "@/api/system/user";
import { getContentTypes } from "@/api/contentcore/catalog";
import { getArticleBodyFormats } from "@/api/contentcore/article"
import { 
  getContentList, delContent, publishContent, createIndexes, 
  copyContent, moveContent, setTopContent, cancelTopContent, sortContent, 
  offlineContent, archiveContent, toPublishContent, addContentAttribute, removeContentAttribute
} from "@/api/contentcore/content";
import CMSCatalogSelector from "@/views/cms/contentcore/catalogSelector";
import CMSContentSort from "@/views/cms/contentcore/contentSortDialog";
import CMSProgress from '@/views/components/Progress';

export default {
  name: "CMSContentList",
  components: { 
    'cms-catalog-selector': CMSCatalogSelector,
    'cms-content-sort': CMSContentSort,
    'cms-progress': CMSProgress
  },
  dicts: [ 'CMSContentStatus', 'CMSContentAttribute' ],
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
      addPopoverVisible: false,
      showSearch: false,
      contentTypeOptions: [],
      catalogId: this.cid,
      contentList: null,
      total: 0,
      tableHeight: 600, // 表格高度
      tableMaxHeight: 600, // 表格最大高度
      selectedRows: [], // 表格选中行
      single: true,
      multiple: true,
      dateRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        contentType: undefined,
        status: undefined,
        catalogId: undefined,
        sorts: ''
      },
      topDialogVisible: false,
      topForm: {
        topEndTime: undefined
      },
      topEndTimePickerOptions: {
        disabledDate(time) {
            return time.getTime() < Date.now() - 8.64e7;//如果没有后面的-8.64e7就是不可以选择今天的 
         }
      },
      openProgress: false,
      taskId: "",
      addContentType: "",
      articleBodyFormatOptions: [],
      addArticleBodyFormat: "RichText",
      openCatalogSelector: false, // 栏目选择弹窗
      isCopy: false,
      openContentSortDialog: false, // 内容选择弹窗
      openEditorW: false
    };
  },
  watch: {
    cid(newVal) { 
      this.catalogId = newVal;
    },
    catalogId(newVal) {
      this.loadContentList();
    },
  },
  created () {
    this.changeTableHeight();
    getContentTypes().then(response => {
      this.contentTypeOptions = response.data;
      this.addContentType = this.contentTypeOptions[0].id;
    });
    this.loadArticleBodyFormats();
    if (this.catalogId && this.catalogId > 0) {
      this.loadContentList();
    }
    getUserPreference('OpenContentEditorW').then(response => {
      this.openEditorW = response.data == 'Y'
    })
  },
  methods: {
    loadArticleBodyFormats() {
      getArticleBodyFormats().then(response => {
        this.articleBodyFormatOptions = response.data;
        this.addArticleBodyFormat = this.articleBodyFormatOptions[0].id;
      })
    },
    loadContentList () {
      this.loading = true;
      this.queryParams.catalogId = this.catalogId;
      getContentList({
        beginTime: this.dateRange && this.dateRange.length == 2 ? this.dateRange[0] : null,
        endTime: this.dateRange && this.dateRange.length == 2 ? this.dateRange[1] : null,
        ... this.queryParams
      }).then(response => {
        this.contentList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
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
      this.$refs.tableContentList.toggleRowSelection(currentRow);
    },
    toggleAllCheckedRows() {
      this.selectedRows.forEach(row => {
          this.$refs.tableContentList.toggleRowSelection(row, false);
      });
      this.selectedRows = [];
    },
    handleQuery () {
      this.queryParams.page = 1;
      this.loadContentList();
    },
    resetQuery () {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleAdd () {
      if (!this.catalogId) {
        this.$modal.msgError(this.$t("CMS.Content.SelectCatalogFirst"));
        return;
      }
      this.addPopoverVisible = false;
      this.openEditor(this.catalogId, 0, this.addContentType, this.addArticleBodyFormat);
    },
    handleEdit (row) {
      this.openEditor(row.catalogId, row.contentId, row.contentType);
    },
    openEditor(catalogId, contentId, contentType, articleBodyFormat = "") {
      let queryParams = { type: contentType, catalogId: catalogId, id: contentId }
      if (articleBodyFormat.length > 0) {
        queryParams.format = articleBodyFormat;
      }
      if (this.openEditorW) {
        let routeData = this.$router.resolve({
          path: "/cms/content/editorW",
          query: queryParams,
        });
        let winEditor = window.open(routeData.href, '_blank');
        let that = this;
        winEditor.onbeforeunload = function () {
          that.loadContentList();
        }
      } else {
        this.$router.push({ path: "/cms/content/editor", query: queryParams });
      }
    },
    handleDelete (row) {
      const contentIds = row.contentId ? [ row.contentId ] : this.selectedRows.map(row => row.contentId);
      this.$modal.confirm(this.$t("Common.ConfirmDelete")).then(function () {
        return delContent(contentIds);
      }).then(() => {
        this.loadContentList();
        this.$modal.msgSuccess(this.$t('Common.DeleteSuccess'));
      }).catch(function () { });
    },
    handlePublish (row) {
      const contentIds = row.contentId ? [ row.contentId ] : this.selectedRows.map(row => row.contentId);
      if (contentIds.length == 0) {
        this.$modal.msgWarning(this.$t('CMS.Content.SelectRowFirst'));
        return;
      }
      this.$modal.loading("Loading...");
      publishContent(contentIds).then(response => {
        this.$modal.closeLoading();
        this.$modal.msgSuccess(this.$t('CMS.ContentCore.PublishSuccess'))
        this.loadContentList();
        this.$cache.local.set('publish_flag', "true")
      }).catch(() => {
        this.$modal.closeLoading();
      });
    },
    handlePreview (row) {
      let contentId = undefined;
      if (row) {
        contentId = row.contentId;
      } else if (this.selectedRows.length > 0) {
        contentId = this.selectedRows[0].contentId;
      } else {
        this.$modal.msgWarning(this.$t('CMS.Content.SelectRowFirst'));
        return;
      }
      let routeData = this.$router.resolve({
        path: "/cms/preview",
        query: { type: "content", dataId: contentId },
      });
      window.open(routeData.href, '_blank');
    },
    handleDropdownBtn (command, row) {
    },
    changeTableHeight () {
      let height = document.body.offsetHeight // 网页可视区域高度
      this.tableHeight = height - 330;
      this.tableMaxHeight = this.tableHeight;
    },
    handleCreateIndex(row) {
      createIndexes(row.contentId).then(response => {
        this.$modal.msgSuccess(this.$t('Common.OpSuccess'));
      });
    },
    handleCopy(row) {
      if (row.contentId) {
        this.selectedRows = [ row ];
      }
      this.isCopy = true;
      this.openCatalogSelector = true;
    },
    doCopy(catalogs, copyType) {
      const data = {  
        contentIds: this.selectedRows.map(item => item.contentId),
        catalogIds: catalogs.map(item => item.id),
        copyType: copyType
      };
      copyContent(data).then(response => {
        this.$modal.msgSuccess(this.$t('Common.OpSuccess'));
        this.openCatalogSelector = false;
        if (this.catalogId && data.catalogIds.indexOf(this.catalogId) > -1) {
          this.loadContentList();
        }
      });
    },
    handleMove(row) {
      if (row.contentId) {
        this.selectedRows = [ row ];
      }
      this.isCopy = false;
      this.openCatalogSelector = true;
    },
    doMove (catalogs) {
      const data = {
        contentIds: this.selectedRows.map(item => item.contentId),
        catalogId: catalogs[0].id
      };
      moveContent(data).then(response => {
        this.$modal.msgSuccess(this.$t('Common.OpSuccess'));
        this.openCatalogSelector = false;
        this.loadContentList();
      });
    },
    handleCatalogSelectorOk(catalogs, copyType) {
      if (this.isCopy) {
        this.doCopy(catalogs, copyType);
      } else {
        this.doMove(catalogs);
      }
    },
    handleCatalogSelectorClose() {
      this.openCatalogSelector = false; 
    },
    handleSetTop(row) {
      if (row.contentId) {
        this.toggleAllCheckedRows();
        this.selectedRows.push(row);
      }
      this.topDialogVisible = true;
    },
    handleTopDialogOk() {
      const contentIds = this.selectedRows.map(item => item.contentId);
      if (contentIds.length == 0) {
        this.$modal.msgWarning(this.$t('CMS.Content.SelectRowFirst'));
        return;
      }
      this.$refs["top_form"].validate(valid => {
        if (valid) {
          setTopContent({ contentIds: contentIds, topEndTime: this.topForm.topEndTime }).then(response => {
            this.$modal.msgSuccess(this.$t('Common.OpSuccess'));
            this.topDialogVisible = false;
            this.topForm.topEndTime = undefined;
            this.loadContentList();
          });
        }
      });
    },
    handleCancelTop(row) {
      const contentIds = row.contentId ? [ row.contentId ] : this.selectedRows.map(item => item.contentId);
      cancelTopContent(contentIds).then(response => {
        this.$modal.msgSuccess(this.$t('Common.OpSuccess'));
        this.loadContentList();
      });
    },
    handleSort(row) {
      if (row.contentId) {
        this.toggleAllCheckedRows();
        this.selectedRows.push(row);
      }
      this.openContentSortDialog = true;
    },
    handleContentSortDialogOk(contents) {
      if (contents && contents.length > 0) {
        this.doSort(contents[0].contentId);
      }
    },
    handleContentSortDialogClose() {
      this.openContentSortDialog = false;
    },
    doSort(targetContentId) {
      const data = { contentId: this.selectedRows[0].contentId, targetContentId: targetContentId };
      sortContent(data).then(response => {
        this.$modal.msgSuccess(this.$t('Common.OpSuccess'));
        this.openContentSortDialog = false;
        this.loadContentList();
      });
    },
    handleOffline(row) {
      const contentIds = row.contentId ? [ row.contentId ] : this.selectedRows.map(item => item.contentId);
      offlineContent(contentIds).then(response => {
        this.$modal.msgSuccess(this.$t('Common.OpSuccess'));
        this.loadContentList();
      });
    },
    handleToPublish(row) {
      const contentIds = row.contentId ? [ row.contentId ] : this.selectedRows.map(item => item.contentId);
      toPublishContent(contentIds).then(response => {
        this.$modal.msgSuccess(this.$t('CMS.ContentCore.ToPublishSuccess'));
        this.loadContentList();
      });
    },
    handleArchive(row) {
      const contentIds = row.contentId ? [ row.contentId ] : this.selectedRows.map(item => item.contentId);
      archiveContent(contentIds).then(response => {
        this.$modal.msgSuccess(this.$t('Common.OpSuccess'));
        this.loadContentList();
      });
    },
    handleRecommend(row) {
      const contentIds = row.contentId ? [ row.contentId ] : this.selectedRows.map(item => item.contentId);
      addContentAttribute({ "contentIds": contentIds, "attr": "recommend"}).then(response => {
        this.$modal.msgSuccess(this.$t('Common.OpSuccess'));
        this.loadContentList();
      });
    },
    handleCancelRecommend(row) {
      const contentIds = row.contentId ? [ row.contentId ] : this.selectedRows.map(item => item.contentId);
      removeContentAttribute({ "contentIds": contentIds, "attr": "recommend"}).then(response => {
        this.$modal.msgSuccess(this.$t('Common.OpSuccess'));
        this.loadContentList();
      });
    },
    handleHot(row) {
      const contentIds = row.contentId ? [ row.contentId ] : this.selectedRows.map(item => item.contentId);
      addContentAttribute({ "contentIds": contentIds, "attr": "hot"}).then(response => {
        this.$modal.msgSuccess(this.$t('Common.OpSuccess'));
        this.loadContentList();
      });
    },
    handleCancelHot(row) {
      const contentIds = row.contentId ? [ row.contentId ] : this.selectedRows.map(item => item.contentId);
      removeContentAttribute({ "contentIds": contentIds, "attr": "hot"}).then(response => {
        this.$modal.msgSuccess(this.$t('Common.OpSuccess'));
        this.loadContentList();
      });
    }
  }
};
</script>
<style scoped>
.cms-content-list .head-container .el-select .el-input {
  width: 110px;
}
.cms-content-list .el-divider {
  margin-top: 10px;
}
.cms-content-list .el-tabs__header {
  margin-bottom: 10px;
}
.cms-content-list .pagination-container {
  height: 30px;
}
.cms-content-list .row-more-btn {
  padding-left: 10px;
}
.cms-content-list .top-icon {
  font-weight: bold;
  font-size: 12px;
  color:green;
}
.cms-content-list .content_attr {
  margin-left: 2px;
}
</style>