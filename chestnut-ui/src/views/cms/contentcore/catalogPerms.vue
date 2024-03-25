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
          :data="catalogPrivs"
          :height="tableHeight"
          :max-height="tableMaxHeight"
          row-key="catalogId"
          default-expand-all
          style="width:100%;line-height: normal;">
          <el-table-column :label="$t('CMS.Catalog.Name')" width="200">
            <template slot-scope="scope">
                <el-checkbox @change="handleRowSelectAll($event, scope.row.catalogId)" v-model="scope.row.perms['View'].granted" :disabled="scope.row.perms['View'].inherited">{{ scope.row.name }}</el-checkbox>
            </template>
          </el-table-column>
          <template v-for="(item, index) in catalogPrivItems">
            <el-table-column :key="index" :label="item.name" v-if="item.id!='View'" width="100">
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
import { getCatalogPermissions, saveCatalogPermissions, getSiteOptions } from "@/api/contentcore/perms"

export default {
  name: "CatalogPermission",
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
      tableHeight: 600,
      tableMaxHeight: 900,
      selectAll: false,
      selectColumnAll: {},
      catalogPrivs: [],
      catalogPrivItems: [],
      currentSiteId: "",
      siteOptions: []
    };
  }, 
  created () {
    this.changeTableHeight();
    this.loadSiteOptions();
  },
  methods: {
    changeTableHeight () {
      let height = document.body.offsetHeight // 网页可视区域高度
      this.tableHeight = height - 140;
      this.tableMaxHeight = this.tableHeight;
    },
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
      getCatalogPermissions(params).then(response => {
        this.catalogPrivItems = response.data.catalogPrivItems;
        this.catalogPrivs = this.initCatalogPrivs(response.data.catalogPrivs);
        this.loading = false;
      });
    },
    initCatalogPrivs(catalogPrivs) {
      catalogPrivs.forEach(item => {
        this.catalogPrivItems.forEach(privItem => {
          if (!item.perms.hasOwnProperty(privItem.id)) {
            item.perms[privItem.id] = { granted: false, inherited: false };
          }
        });
        if (item.children && item.children.length > 0) {
          this.initCatalogPrivs(item.children);
        }
      });
      return catalogPrivs;
    },
    handleSiteChange() {
      this.loadData();
    },
    handleSelectAll() {
      this.selectAll = !this.selectAll;
      this.selectCatalogPrivs(this.catalogPrivs, this.selectAll)
    },
    selectCatalogPrivs(arr, checked) {
      arr.forEach(row => {
        Object.keys(row.perms).forEach(key => row.perms[key].granted = checked)
        if (row.children && row.children.length > 0) {
          this.selectCatalogPrivs(row.children, checked)
        }
      });
    },
    handleRowSelectAll(value, catalogId) {
      this.selectRowAll(this.catalogPrivs, value, catalogId)
    },
    selectRowAll(arr, checked, catalogId) {
      this.$nextTick(() => {
        arr.some(row => {
          if (row.catalogId == catalogId) {
            const permItems = Object.keys(row.perms)
            if (checked) {
              permItems.forEach(key => {
                row.perms[key].granted = checked;
              })
            } else {
              let hasCheckedPerm = false;
              for (let i = 0; i < permItems.length; i++) {
                if (permItems[i] != 'View' && row.perms[permItems[i]].granted) {
                  hasCheckedPerm = true;
                  break;
                }
              }
              if (hasCheckedPerm) {
                permItems.forEach(key => {
                  if (key != 'View') {
                    row.perms[key].granted = checked;
                  }
                })
                row.perms['View'].granted = hasCheckedPerm
              }
            }
            return true;
          }
          if (row.children && row.children.length > 0) {
            this.selectRowAll(row.children, checked, catalogId)
          }
          return false
        })
      })
    },
    handleColumnSelectAll(column) {
      this.selectColumnAll[column] = !this.selectColumnAll[column];
      this.selectColumn(this.catalogPrivs, column, this.selectColumnAll[column]);
    },
    selectColumn(arr, column, checked) {
      arr.forEach(row => {
        Object.keys(row.perms).forEach(key => {
          if (key == column) {
            row.perms[key].granted = checked;
            if (checked) {
              row.perms['View'].granted = checked
            }
          }
        })
        if (row.children && row.children.length > 0) {
          this.selectColumn(row.children, column, checked)
        }
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
        siteId: this.currentSiteId,
        perms: this.catalogPrivs
      };
      saveCatalogPermissions(data).then(response => {
        this.$modal.notifySuccess(this.$t('Common.SaveSuccess'));
      });
    },
    getPermissionKeys(arr, permissions) {
      arr.forEach(row => {
        Object.keys(row.perms).forEach(key => {
          if (row.perms[key].granted) {
            permissions.push(key + ":" + row.catalogId);
          }
        })
        if (row.children && row.children.length > 0) {
          this.getPermissionKeys(row.children, permissions)
        }
      })
    }
  }
};
</script>
