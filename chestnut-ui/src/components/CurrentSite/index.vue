<template>
  <div v-if="showCurrentSite">
    <el-button 
      type="primary" 
      icon="el-icon-s-home" 
      @click="handleOpenSiteList" 
      plain>{{ currentSiteName }}</el-button>
    <el-dialog :title="$t('CMS.Site.CurrentSelectorTitle')"
               :visible.sync="open"
               :modal-append-to-body='false'
               width="800px">
      <el-form :model="queryParams"
               ref="queryForm"
               :inline="true"
               label-width="68px"
               class="el-form-search">
        <el-form-item prop="siteName">
          <el-input :placeholder="$t('CMS.Site.Name')" v-model="queryParams.siteName" size="mini"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary"
                     icon="el-icon-search"
                     size="mini"
                     @click="handleQuery">{{ $t("Common.Search") }}</el-button>
          <el-button icon="el-icon-refresh"
                     size="mini"
                     @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading"
                :data="siteList"
                highlight-current-row
                @current-change="handleSelectionChange"
                @row-dblclick="handleRowDblclick"
                style="width:100%;line-height: normal;">
        <el-table-column type="index" :label="$t('Common.RowNo')" width="50" />
        <el-table-column :label="$t('CMS.Site.Name')" align="center" prop="name">
          <template slot-scope="scope">
            <div slot="reference" class="name-wrapper">
              <el-tag v-if="scope.row.siteId==currentSite" size="medium">{{ scope.row.name }}</el-tag>
              <span v-else>{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="URL" align="center" width="300" prop="url" />
      </el-table>
      <pagination 
        v-show="total>0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="loadSiteList" />
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleChangeCurrentSite">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<style>
  #current-site .el-dialog__body { padding-top: 0; }
  #current-site .el-form-item { margin-bottom: 0; }
</style>
<script>
import { listSite, getCurrentSite, setCurrentSite } from "@/api/contentcore/site";
import { getConfigKey } from "@/api/system/config";

export default {
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中行站点ID
      selectedRow: undefined,
      // 总条数
      total: 0,
      // 表格数据
      siteList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      currentSite: this.$cache.local.get("CurrentSite"),
      currentSiteName: undefined,
      showCurrentSite: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        siteName: undefined
      },
    }
  },
  created () {
    getConfigKey("CMSModuleEnable").then(res => {
      this.showCurrentSite = res.data == "true";
      if (this.showCurrentSite) {
        this.loadCurrentSite();
      }
    });
  },
  methods: {
    // 当前站点，如果未设置，后台会默认取数据库第一个站点数据作为当前站点
    loadCurrentSite() {
      getCurrentSite().then(response => {
          this.currentSite = response.data.siteId;
          this.currentSiteName = response.data.siteName;
          this.$ELEMENT.currentSite = this.currentSite;
          this.$cache.local.set("CurrentSite", this.currentSite);
          this.showCurrentSite = true;
      });
    },
    resetCurrentSite(changed) {
      setCurrentSite(this.currentSite).then(response => {
          if (response.code == 200 && response.data != undefined) {
            this.currentSite = response.data.siteId;
            this.currentSiteName = response.data.siteName;
            this.$ELEMENT.currentSite = this.currentSite;
            this.$cache.local.set("CurrentSite", this.currentSite);
            this.showCurrentSite = true;
            if (changed) {
              this.open = false;
              this.refreshView();
            }
          } else {
            this.showCurrentSite = false;
          }
        });
    },
    handleChangeCurrentSite() {
      if (this.selectedRow == undefined) {
        this.$modal.msgError(this.$t('Common.SelectFirst'));
        return;
      }
      if (this.currentSite == this.selectedRow.siteId) {
        this.$modal.msgError(this.$t('CMS.Site.AlreayCurrentSite'));
        return;
      }
      this.currentSite = this.selectedRow.siteId;
      this.resetCurrentSite(true);
    },
    loadSiteList () {
      this.loading = true;
      listSite(this.queryParams).then(response => {
        this.siteList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    /** 搜索按钮操作 */
    handleQuery () {
      this.queryParams.pageNum = 1;
      this.loadSiteList();
    },
    /** 重置按钮操作 */
    resetQuery () {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 选中数据
    handleSelectionChange (currentRow) {
      this.selectedRow = currentRow;
    },
    handleRowDblclick(row) {
      this.selectedRow = row
      this.handleChangeCurrentSite()
    },
    handleOpenSiteList() {
      this.open = true;
      this.loadSiteList();
    },
    // 取消按钮
    cancel () {
      this.open = false;
    },
    refreshView() {
      // In order to make the cached page re-rendered
      this.$store.dispatch('tagsView/delAllCachedViews', this.$route)

      const { fullPath } = this.$route

      this.$nextTick(() => {
        this.$router.replace({
          path: '/redirect' + fullPath
        })
      })
    }
  }

}
</script>
