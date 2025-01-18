<template>
  <div class="app-container-content-sort">
    <el-dialog 
      :title="$t('CMS.Content.OpLog')"
      :visible.sync="visible"
      width="1000px"
      :close-on-click-modal="false"
      append-to-body>
      <el-row>
        <el-col>
          <el-form 
            :model="queryParams"
            ref="queryForm"
            :inline="true"
            size="small"
            @submit.native.prevent
            class="el-form-search mt10 mb10"
            style="text-align:left">
            <el-form-item label="" prop="type">
              <el-select 
                v-model="queryParams.type"
                :placeholder="$t('CMS.ContentOpLog.Type')"
                clearable
                style="width: 110px">
                <el-option 
                  v-for="dict in dict.type.CMSContentOpType"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="" prop="operator">
              <el-input v-model="queryParams.operator" :placeholder="$t('CMS.ContentOpLog.Operator')" @keyup.enter.native="handleQuery"></el-input>
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
        :data="dataList"
        size="mini">
        <el-table-column type="index" :label="$t('Common.RowNo')" align="center" width="50" />
        <el-table-column :label="$t('CMS.ContentOpLog.Type')" align="center" prop="type">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.CMSContentOpType" :value="scope.row.type"/>
          </template>
        </el-table-column>
        <el-table-column :label="$t('CMS.ContentOpLog.OperatorType')" align="center" prop="operatorType"></el-table-column>
        <el-table-column :label="$t('CMS.ContentOpLog.Operator')" align="center" prop="operator" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column :label="$t('CMS.ContentOpLog.LogTime')" align="center" prop="logTime" width="160">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.logTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('CMS.ContentOpLog.Details')" align="center" prop="details" :show-overflow-tooltip="true"></el-table-column>
      </el-table>
      <pagination 
        v-show="total>0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="loadContentOpLogList" />
      <div slot="footer" class="dialog-footer">
        <el-button @click="handleClose">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getContentOpLogList } from "@/api/contentcore/content";

export default {
  name: "CMSContentOpLogDialog",
  dicts: [ 'CMSContentOpType' ],
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
        this.loadContentOpLogList();
      }
    },
    cid () {
      this.queryParams.contentId = this.cid;
    }
  },
  data () {
    return {
      loading: false,
      visible: this.open,
      dataList: [],
      total: 0,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        contentId: this.cid,
        type: undefined,
        operator: undefined
      }
    };
  },
  methods: {
    loadContentOpLogList () {
      if (!this.visible) {
        return;
      }
      this.loading = true;
      getContentOpLogList(this.queryParams).then(response => {
        this.dataList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    handleClose () {
      this.$emit("close");
      this.resetForm("queryForm");
      this.dataList = [];
    },
    handleQuery () {
      this.loadContentOpLogList();
    },
    resetQuery () {
      this.resetForm("queryForm");
      this.handleQuery();
    }
  }
};
</script>