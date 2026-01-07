<template>
  <div class="menu-perms-container" v-loading="loading">
    <el-row class="mb8">
      <el-col>
        <el-button 
          plain 
          type="success" 
          icon="Edit" 
          @click="handleSave"
        >{{ $t("Common.Save") }}</el-button>
        <el-button 
          plain 
          type="primary" 
          icon="Check" 
          @click="handleSelectAll"
        >{{ selectAll ? $t('Common.CheckInverse') : $t('Common.CheckAll')  }}</el-button>
      </el-col>
    </el-row>
    <el-table 
      v-loading="loading"
      :data="sitePrivs"
      style="width:100%;line-height: normal;">
      <el-table-column type="index" :label="$t('Common.RowNo')" width="50" />
      <el-table-column :label="$t('CMS.Site.Name')" width="200">
        <template #default="scope">
          <el-checkbox 
            @change="handleRowSelectAll($event, scope.row.siteId)"
            v-model="scope.row.perms['View'].granted" 
            :disabled="scope.row.perms['View'].inherited"
          >{{ scope.row.name }}</el-checkbox>
        </template>
      </el-table-column>
      <template v-for="(item, index) in sitePrivItems">
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
  </div>
</template>
<script setup name="SitePermission">
import { getSitePermissions, saveSitePermissions } from "@/api/contentcore/perms"

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
    form.value.ownerType = newV;
    loadData();
  }
});

watch(() => props.owner, (newV, oldV) => {
  if (newV && newV != '') {
    form.value.owner = newV;
    loadData();
  }
});

const { proxy } = getCurrentInstance();

const loading = ref(false);
const selectAll = ref(false);
const sitePrivs = ref([]);
const sitePrivItems = ref([]);

const data = reactive({
  selectColumnAll: {},
  form: {}
});

const { form, selectColumnAll } = toRefs(data);

function loadData() {
  loading.value = true;
  const params = { ownerType: props.ownerType, owner: props.owner }
  getSitePermissions(params).then(response => {
    sitePrivItems.value = response.data.sitePrivItems;
    response.data.sitePrivs.forEach(item => {
      sitePrivItems.value.forEach(privItem => {
        if (!item.perms.hasOwnProperty(privItem.id)) {
          item.perms[privItem.id] = { granted: false, inherited: false };
        }
      });
    });
    sitePrivs.value = response.data.sitePrivs;
    loading.value = false;
  });
}

function handleSelectAll() {
  selectAll.value = !selectAll.value;
  sitePrivs.value.forEach(row => {
    Object.keys(row.perms).forEach(key => {
      if (!row.perms[key].inherited) {
        row.perms[key].granted = selectAll.value
      }
    })
  })
}

function handleRowSelectAll(value, siteId) {
  sitePrivs.value.some(row => {
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
}

function handleColumnSelectAll(column) {
  selectColumnAll.value[column] = !selectColumnAll.value[column];
  sitePrivs.value.forEach(row => {
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
    perms: sitePrivs.value
  };
  saveSitePermissions(data).then(response => {
    proxy.$modal.notifySuccess(proxy.$t('Common.SaveSuccess'));
  });
}

loadData();
</script>
