<template>
  <div class="menu-perms-container" v-loading="loading">
    <el-row class="mb12">
      <el-col>
        <el-button plain type="success" icon="el-icon-edit" size="mini" @click="handleSave">{{ $t("Common.Save") }}</el-button>
        <el-button plain type="primary" icon="el-icon-check" size="mini" @click="handleSelectAll">{{ this.selectAll ? $t('Common.CheckInverse') : $t('Common.CheckAll')  }}</el-button>
      </el-col>
    </el-row>
    <el-row>
      <el-col>
        <el-table 
          v-loading="loading"
          :data="sitePrivs"
          style="width:100%;line-height: normal;">
          <el-table-column type="index" :label="$t('Common.RowNo')" width="50">
          </el-table-column>
          <el-table-column :label="$t('CMS.Site.Name')" width="200">
            <template slot-scope="scope">
                <el-checkbox @change="handleRowSelectAll($event, scope.row.siteId)" v-model="scope.row.perms['View'].granted" :disabled="scope.row.perms['View'].inherited">{{ scope.row.name }}</el-checkbox>
            </template>
          </el-table-column>
          <template v-for="(item, index) in sitePrivItems">
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
      </el-col>
    </el-row>
  </div>
</template>
<script>
import { getSitePermissions, saveSitePermissions } from "@/api/contentcore/perms"

export default {
  name: "SitePermission",
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
  mounted() {
    this.loadData();
  },
  data() {
    return {
      loading: true,
      selectAll: false,
      selectColumnAll: {},
      sitePrivs: [],
      sitePrivItems: []
    };
  }, 
  methods: {
    loadData() {
      this.loading = true;
      const params = { ownerType: this.ownerType, owner: this.owner }
      getSitePermissions(params).then(response => {
        this.sitePrivItems = response.data.sitePrivItems;
        response.data.sitePrivs.forEach(item => {
          this.sitePrivItems.forEach(privItem => {
            if (!item.perms.hasOwnProperty(privItem.id)) {
              item.perms[privItem.id] = { granted: false, inherited: false };
            }
          });
        });
        this.sitePrivs = response.data.sitePrivs;
        this.loading = false;
      });
    },
    handleSelectAll() {
      this.selectAll = !this.selectAll;
      this.sitePrivs.forEach(row => {
        Object.keys(row.perms).forEach(key => {
          if (!row.perms[key].inherited) {
            row.perms[key].granted = this.selectAll
          }
        })
      })
    },
    handleRowSelectAll(value, siteId) {
      this.sitePrivs.some(row => {
        if (row.siteId == siteId) {
          Object.keys(row.perms).forEach(key => {
            if (!row.perms[key].inherited) {
              row.perms[key].granted = value;
            }
          })
          return true;
        }
        return false;
      })
    },
    handleColumnSelectAll(column) {
      this.selectColumnAll[column] = !this.selectColumnAll[column];
      this.sitePrivs.forEach(row => {
        Object.keys(row.perms).forEach(key => {
          if (key == column) {
            if (!row.perms[key].inherited) {
              row.perms[key].granted = this.selectColumnAll[column];
              if (this.selectColumnAll[column]) {
                row.perms['View'].granted = this.selectColumnAll[column]
              }
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
        perms: this.sitePrivs
      };
      saveSitePermissions(data).then(response => {
        this.$modal.notifySuccess(this.$t('Common.SaveSuccess'));
      });
    }
  }
};
</script>
