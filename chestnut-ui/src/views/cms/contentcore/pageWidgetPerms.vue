<template>
  <div class="menu-perms-container" v-loading="loading">
    <el-row :gutter="10" class="mb10">
      <el-col :span="1.5">
        <el-select v-model="currentSiteId" size="mini" @change="handleSiteChange">
          <el-option
            v-for="item in siteOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-col>
      <el-col :span="1.5">
        <el-button plain type="success" icon="el-icon-edit" size="mini" @click="handleSave">{{ $t("Common.Save") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button plain type="primary" icon="el-icon-check" size="mini" @click="handleSelectAll">{{ this.selectAll ? $t('Common.CheckInverse') : $t('Common.CheckAll') }}</el-button>
      </el-col>
    </el-row>
    <el-row>
      <el-col>
        <el-table 
          v-if="siteOptions.length > 0"
          v-loading="loading"
          :data="pageWidgetPrivs"
          style="width:100%;line-height: normal;">
          <el-table-column type="index" :label="$t('Common.RowNo')" width="50">
          </el-table-column>
          <el-table-column :label="$t('CMS.PageWidget.Name')" width="200">
            <template slot-scope="scope">
                <el-checkbox @change="handleRowSelectAll($event, scope.row.pageWidgetId)" v-model="scope.row.perms['View'].granted" :disabled="scope.row.perms['View'].inherited">{{ scope.row.name }}</el-checkbox>
            </template>
          </el-table-column>
          <template v-for="(item, index) in privItems">
            <el-table-column :key="index" :label="item.name" v-if="item.id!='View'">
              <template slot="header">
                <el-checkbox @change="handleColumnSelectAll(item.id)">{{ item.name }}</el-checkbox>
              </template>
              <template slot-scope="scope">
                <el-checkbox v-model="scope.row.perms[item.id].granted" :disabled="scope.row.perms[item.id].inherited" @change="handleRowColumnChange($event, scope.row)"></el-checkbox>
              </template>
            </el-table-column>
          </template>
        </el-table>
        <div v-else style="background-color: #f4f4f5;color: #909399;font-size:12px;line-height: 30px;padding-left:10px;">
          <i class="el-icon-info mr5"></i>{{ $t("CMS.Catalog.NoSitePermissions") }}
        </div>
      </el-col>
    </el-row>
  </div>
</template>
<script>
import { getPageWidgetPermissions, savePageWidgetPermissions, getSiteOptions } from "@/api/contentcore/perms"

export default {
  name: "PageWidgetPermission",
  props: {
    ownerType: {
      type: String,
      require: false,
      default: ""
    },
    owner: {
      type: String,
      require: false,
      default: ""
    }
  },
  watch: {
    ownerType: {
      handler(newV, oldV) {
        if (newV && newV != '') {
          this.form.ownerType = newV;
          this.loadData();
        }
      }
    },
    owner: {
      handler(newV, oldV) {
        if (newV && newV != '') {
          this.form.owner = newV;
          this.loadData();
        }
      }
    },
  },
  data() {
    return {
      loading: false,
      selectAll: false,
      selectColumnAll: {},
      siteOptions: [],
      currentSiteId: "",
      pageWidgetPrivs: [],
      privItems: []
    };
  }, 
  created() {
    this.loadSiteOptions();
  },
  methods: {
    loadSiteOptions() {
      const params = { ownerType: this.ownerType, owner: this.owner }
      getSiteOptions(params).then(response => {
        this.siteOptions = response.data.rows;
        if (this.siteOptions.length > 0) {
          this.currentSiteId = this.siteOptions[0].id;
          this.loadData();
        }
      })
    },
    loadData() {
      this.loading = true;
      const params = { ownerType: this.ownerType, owner: this.owner, siteId: this.currentSiteId }
      getPageWidgetPermissions(params).then(response => {
        this.privItems = response.data.privItems;
        response.data.pageWidgetPrivs.forEach(item => {
          this.privItems.forEach(privItem => {
            if (!item.perms.hasOwnProperty(privItem.id)) {
              item.perms[privItem.id] = { granted: false, inherited: false };
            }
          });
        });
        this.pageWidgetPrivs = response.data.pageWidgetPrivs;
        this.loading = false;
      });
    },
    handleSiteChange() {
      this.loadData();
    },
    handleSelectAll() {
      this.selectAll = !this.selectAll;
      this.pageWidgetPrivs.forEach(row => {
        Object.keys(row.perms).forEach(key => row.perms[key].granted = this.selectAll)
      })
    },
    handleRowSelectAll(value, pageWidgetId) {
      this.pageWidgetPrivs.some(row => {
        if (row.pageWidgetId == pageWidgetId) {
          Object.keys(row.perms).forEach(key => {
            row.perms[key].granted = value;
          })
          return true;
        }
        return false;
      })
    },
    handleColumnSelectAll(column) {
      this.selectColumnAll[column] = !this.selectColumnAll[column];
      this.pageWidgetPrivs.forEach(row => {
        Object.keys(row.perms).forEach(key => {
          if (key == column) {
            row.perms[key].granted = this.selectColumnAll[column];
            if (this.selectColumnAll[column]) {
              row.perms['View'].granted = this.selectColumnAll[column]
            }
          }
        })
      })
    },
    handleRowColumnChange(value, row) {
      if (value) {
        row.perms['View'].granted = true;
      }
    },
    handleSave() {
      const data = {
        ownerType: this.ownerType,
        owner: this.owner,
        perms: this.pageWidgetPrivs
      };
      savePageWidgetPermissions(data).then(response => {
        this.$modal.notifySuccess(this.$t('Common.SaveSuccess'));
      });
    }
  }
};
</script>
