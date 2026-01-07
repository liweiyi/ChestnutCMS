<template>
  <div class="menu-perms-container" v-loading="loading">
    <el-row :gutter="10" class="mb8">
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
      <el-col :span="1.5">
        <el-button 
          plain type="success" 
          icon="Edit" 
          @click="handleSave"
        >{{ $t("Common.Save") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain type="primary" 
          icon="Check" 
          @click="handleSelectAll"
        >{{ selectAll ? $t('Common.CheckInverse') : $t('Common.CheckAll') }}</el-button>
      </el-col>
    </el-row>
    <el-table 
      v-if="siteOptions.length > 0"
      v-loading="loading"
      :data="pageWidgetPrivs"
      style="width:100%;line-height: normal;">
      <el-table-column type="index" :label="$t('Common.RowNo')" width="50" />
      <el-table-column :label="$t('CMS.PageWidget.Name')" width="200">
        <template #default="scope">
          <el-checkbox 
            @change="handleRowSelectAll($event, scope.row.pageWidgetId)" 
            v-model="scope.row.perms['View'].granted" 
            :disabled="scope.row.perms['View'].inherited"
          >{{ scope.row.name }}</el-checkbox>
        </template>
      </el-table-column>
      <template v-for="(item, index) in privItems">
        <el-table-column :key="index" :label="item.name" v-if="item.id!='View'">
          <template #header>
            <el-checkbox 
              @change="handleColumnSelectAll(item.id)"
              v-model="selectColumnAll[item.id]"
            >{{ item.name }}</el-checkbox>
          </template>
          <template #default="scope">
            <el-checkbox 
              v-model="scope.row.perms[item.id].granted" 
              :disabled="scope.row.perms[item.id].inherited" 
              @change="handleRowColumnChange($event, scope.row)"
            ></el-checkbox>
          </template>
        </el-table-column>
      </template>
    </el-table>
    <div v-else style="background-color: #f4f4f5;color: #909399;font-size:12px;line-height: 30px;padding-left:10px;">
      <el-icon class="mr5"><InfoFilled /></el-icon>{{ $t("CMS.Catalog.NoSitePermissions") }}
    </div>
  </div>
</template>
<script setup name="PageWidgetPermission">
import { getPageWidgetPermissions, savePageWidgetPermissions, getSiteOptions } from "@/api/contentcore/perms"

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
const selectAll = ref(false);
const siteOptions = ref([]);
const currentSiteId = ref("");
const pageWidgetPrivs = ref([]);
const privItems = ref([]);

const data = reactive({
  selectColumnAll: {},
});

const { selectColumnAll } = toRefs(data);

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
  getPageWidgetPermissions(params).then(response => {
    privItems.value = response.data.privItems;
    response.data.pageWidgetPrivs.forEach(item => {
      privItems.value.forEach(privItem => {
        if (!item.perms.hasOwnProperty(privItem.id)) {
          item.perms[privItem.id] = { granted: false, inherited: false };
        }
      });
    });
    pageWidgetPrivs.value = response.data.pageWidgetPrivs;
    loading.value = false;
  });
}
function handleSiteChange() {
  loadData();
}
function handleSelectAll() {
  selectAll.value = !selectAll.value;
  pageWidgetPrivs.value.forEach(row => {
    Object.keys(row.perms).forEach(key => {
      if (!row.perms[key].inherited) {
        row.perms[key].granted = selectAll.value
      }
    })
  })
}
function handleRowSelectAll(value, pageWidgetId) {
  pageWidgetPrivs.value.some(row => {
    if (row.pageWidgetId == pageWidgetId) {
      Object.keys(row.perms).forEach(key => {
        if (!row.perms[key].inherited) {
          row.perms[key].granted = value;
        }
      })
      return true;
    }
    return false;
  })
}
function handleColumnSelectAll(column) {
  selectColumnAll.value[column] = !selectColumnAll.value[column];
  pageWidgetPrivs.value.forEach(row => {
    Object.keys(row.perms).forEach(key => {
      if (key == column) {
        if (!row.perms[key].inherited) {
          row.perms[key].granted = selectColumnAll.value[column];
          if (selectColumnAll.value[column]) {
            row.perms['View'].granted = selectColumnAll.value[column]
          }
        }
      }
    })
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
    perms: pageWidgetPrivs.value
  };
  savePageWidgetPermissions(data).then(response => {
    proxy.$modal.notifySuccess(proxy.$t('Common.SaveSuccess'));
  });
}

loadSiteOptions();
</script>
