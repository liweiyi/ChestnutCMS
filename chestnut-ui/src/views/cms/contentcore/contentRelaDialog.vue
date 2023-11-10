<template>
  <div class="app-container-content-sort">
    <el-dialog 
      :title="$t('CMS.Content.RelaContent')"
      :visible.sync="visible"
      width="800px"
      :close-on-click-modal="false"
      append-to-body>
      <el-row>
        <el-col>
          <el-button 
            plain
            type="primary"
            icon="el-icon-add"
            size="mini"
            @click="handleAdd">{{ $t("Common.Add") }}
          </el-button>
          <el-button 
            plain
            type="danger"
            icon="el-icon-delete"
            size="mini"
            :disabled="multiple"
            @click="handleDelete">{{ $t("Common.Delete") }}
          </el-button>
        </el-col>
        <el-col>
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
        </el-col>
      </el-row>
      
      <el-table 
        v-loading="loading"
        :data="relaContentList"
        size="mini"
        highlight-current-row
        @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
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
        @pagination="loadRelaContentList" />
      <div slot="footer" class="dialog-footer">
        <el-button @click="handleClose">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
    <!-- 内容选择组件 -->
    <cms-content-selector
      :open="openContentSelector"
      @ok="handleContentSelectorOk"
      @close="handleContentSelectorClose"></cms-content-selector>
  </div>
</template>
<script>
import { getRelaContentList, addRelaContents, delRelaContents } from "@/api/contentcore/rela";

import CMSContentSelector from "@/views/cms/contentcore/contentSelector";

export default {
  name: "CMSContentRelaDialog",
  dicts: ['CMSContentStatus'],
  components: {
    'cms-content-selector': CMSContentSelector
  },
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
        this.loadRelaContentList();
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
      relaContentList: [],
      multiple: true,
      total: 0,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        contentId: this.cid,
        title: undefined
      },
      openContentSelector: false,
    };
  },
  methods: {
    loadRelaContentList () {
      if (!this.visible) {
        return;
      }
      this.loading = true;
      getRelaContentList(this.queryParams).then(response => {
        this.relaContentList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.selectedContents = [];
        this.loading = false;
      });
    },
    handleSelectionChange (selection) {
      this.selectedContents = selection;
      this.multiple = !selection.length;
    },
    handleAdd () {
      this.openContentSelector = true;
    },
    handleContentSelectorOk(contents) {
      if (contents && contents.length > 0) {
        const data = { contentId: this.cid, relaContentIds: contents.map(c => c.contentId) }
        addRelaContents(data).then(response => {
            this.openContentSelector = false;
            this.$modal.msgSuccess(this.$t('Common.AddSuccess'));
            this.handleQuery();
        });
      } else {
        this.$modal.msgWarning(this.$t('Common.SelectFirst'));
      }
    },
    handleContentSelectorClose() {
      this.openContentSelector = false;
    },
    handleDelete () {
      if (this.selectedContents.length == 0) {
        this.$modal.msgWarning(this.$t('Common.SelectFirst'));
        return;
      }
      const data = { contentId: this.cid, relaContentIds: this.selectedContents.map(c => c.contentId) }
      delRelaContents(data).then(response => {
            this.$modal.msgSuccess(this.$t('Common.DeleteSuccess'));
            this.handleQuery();
        });
    },
    handleClose () {
      this.$emit("close");
      this.resetForm("queryForm");
      this.selectedContents = [];
      this.relaContentList = [];
    },
    handleQuery () {
      this.loadRelaContentList();
    },
    resetQuery () {
      this.resetForm("queryForm");
      this.handleQuery();
    }
  }
};
</script>