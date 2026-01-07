<template>
  <div class="menu-perms-container" v-loading="loading">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button 
          plain 
          type="success" 
          icon="Edit" 
          @click="handleSave"
        >{{ $t("Common.Save") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain 
          type="primary" 
          icon="Check" 
          @click="handleSelectAll"
        >{{ selectAll ? $t('Common.CheckInverse') : $t('Common.CheckAll') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-select v-model="currentSiteId" @change="handleSiteChange" style="width: 260px;">
          <el-option
            v-for="item in siteOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-col>
    </el-row>
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
        <template #default="scope">
            <el-checkbox @change="handleRowSelectAll($event, scope.row.catalogId)" v-model="scope.row.perms['View'].granted" :disabled="scope.row.perms['View'].inherited">{{ scope.row.name }}</el-checkbox>
        </template>
      </el-table-column>
      <template v-for="(item, index) in catalogPrivItems">
        <el-table-column :key="index" :label="item.name" v-if="item.id!='View'" width="100">
          <template #header>
            <el-checkbox @change="handleColumnSelectAll(item.id)">{{ item.name }}</el-checkbox>
          </template>
          <template #default="scope">
            <el-checkbox v-model="scope.row.perms[item.id].granted" :disabled="scope.row.perms[item.id].inherited" @change="handleRowColumnChange($event, scope.row)"></el-checkbox>
          </template>
        </el-table-column>
      </template>
    </el-table>
    <div v-else style="background-color: #f4f4f5;color: #909399;font-size:12px;line-height: 30px;padding-left:10px;">
      <el-icon class="mr5"><InfoFilled /></el-icon>{{ $t("CMS.Catalog.NoSitePermissions") }}
    </div>
  </div>
</template>
<script setup name="CatalogPermission">
import { getCatalogPermissions, saveCatalogPermissions, getSiteOptions } from "@/api/contentcore/perms"

const props = defineProps({
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
});

watch(() => props.ownerType, (newV, oldV) => {
  if (newV && newV != '') {
    loadData();
  }
});

watch(() => props.owner, (newV, oldV) => {
  if (newV && newV != '') {
    loadData();
  }
}); 

const { proxy } = getCurrentInstance();

const loading = ref(false);
const tableHeight = ref(600);
const tableMaxHeight = ref(900);
const selectAll = ref(false);
const catalogPrivs = ref([]);
const catalogPrivItems = ref([]);
const currentSiteId = ref("");
const siteOptions = ref([]);

const data = reactive({
  selectColumnAll: {},
});

const { selectColumnAll } = toRefs(data);

function changeTableHeight () {
  let height = document.body.offsetHeight // 网页可视区域高度
  tableHeight.value = height - 140;
  tableMaxHeight.value = tableHeight.value;
}
function loadSiteOptions() {
  const params = { ownerType: props.ownerType, owner: props.owner }
  getSiteOptions(params).then(response => {
    siteOptions.value = response.data.rows;
    if (siteOptions.value.length > 0) {
      currentSiteId.value = siteOptions.value[0].id;
      loadData();
    }
  })
}
function loadData() {
  loading.value = true;
  const params = { ownerType: props.ownerType, owner: props.owner, siteId: currentSiteId.value }
  getCatalogPermissions(params).then(response => {
    catalogPrivItems.value = response.data.catalogPrivItems;
    catalogPrivs.value = initCatalogPrivs(response.data.catalogPrivs);
    loading.value = false;
  });
}
function initCatalogPrivs(catalogPrivs) {
  catalogPrivs.forEach(item => {
    catalogPrivItems.value.forEach(privItem => {
      if (!item.perms.hasOwnProperty(privItem.id)) {
        item.perms[privItem.id] = { granted: false, inherited: false };
      }
    });
    if (item.children && item.children.length > 0) {
      initCatalogPrivs(item.children);
    }
  });
  return catalogPrivs;
}
function handleSiteChange() {
  loadData();
}
function handleSelectAll() {
  selectAll.value = !selectAll.value;
  selectCatalogPrivs(catalogPrivs.value, selectAll.value)
}
function selectCatalogPrivs(arr, checked) {
  arr.forEach(row => {
    Object.keys(row.perms).forEach(key => {
      if (!row.perms[key].inherited) {
        row.perms[key].granted = checked
      }
    })
    if (row.children && row.children.length > 0) {
      selectCatalogPrivs(row.children, checked)
    }
  });
}
function handleRowSelectAll(value, catalogId) {
  selectRowAll(catalogPrivs.value, value, catalogId)
}
function selectRowAll(arr, checked, catalogId) {
  proxy.$nextTick(() => {
    arr.some(row => {
      if (row.catalogId == catalogId) {
        const permItems = Object.keys(row.perms)
        if (checked) {
          permItems.forEach(key => {
            if (!row.perms[key].inherited) {
              row.perms[key].granted = checked;
            }
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
                if (!row.perms[key].inherited) {
                  row.perms[key].granted = checked;
                }
              }
            })
            row.perms['View'].granted = hasCheckedPerm
          }
        }
        return true;
      }
      if (row.children && row.children.length > 0) {
        selectRowAll(row.children, checked, catalogId)
      }
      return false
    })
  })
}
function handleColumnSelectAll(column) {
  selectColumnAll.value[column] = !selectColumnAll.value[column];
  selectColumn(catalogPrivs.value, column, selectColumnAll.value[column]);
}
function selectColumn(arr, column, checked) {
  arr.forEach(row => {
    Object.keys(row.perms).forEach(key => {
      if (key == column) {
        if (!row.perms[key].inherited) {
          row.perms[key].granted = checked;
          if (checked) {
            row.perms['View'].granted = checked
          }
        }
      }
    })
    if (row.children && row.children.length > 0) {
      selectColumn(row.children, column, checked)
    }
  })
}
function handleRowColumnChange(value, row) {
  if (value) {
    row.perms['View'].granted = true;
  }
}
function handleSave() {
  const data = {
    ownerType: props.ownerType,
    owner: props.owner,
    siteId: currentSiteId.value,
    perms: catalogPrivs.value
  };
  saveCatalogPermissions(data).then(response => {
    proxy.$modal.notifySuccess(proxy.$t('Common.SaveSuccess'));
  });
}

function getPermissionKeys(arr, permissions) {
  arr.forEach(row => {
    Object.keys(row.perms).forEach(key => {
      if (row.perms[key].granted) {
        permissions.push(key + ":" + row.catalogId);
      }
    })
    if (row.children && row.children.length > 0) {
      getPermissionKeys(row.children, permissions)
    }
  })
}

changeTableHeight();
loadSiteOptions();
</script>
