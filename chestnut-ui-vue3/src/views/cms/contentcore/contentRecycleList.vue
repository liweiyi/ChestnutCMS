<template>
  <div class="cms-content-list">
    <el-row :gutter="24">
      <el-col :span="12">
        <el-row :gutter="10">
          <el-col :span="1.5">
            <el-button 
              plain
              type="primary"
              icon="RefreshLeft"
              :disabled="selectedRows.length == 0"
              v-hasPermi="[ $p('Catalog:AddContent:{0}', [ catalogId ]) ]"
              @click="handleRecover">{{ $t('CMS.Content.Restore') }}
            </el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button 
              plain
              type="danger"
              icon="Delete"
              :disabled="selectedRows.length == 0"
              v-hasPermi="[ $p('Catalog:DeleteContent:{0}', [ catalogId ]) ]"
              @click="handleDelete">{{ $t("Common.Delete") }}
            </el-button>
          </el-col>
        </el-row>
      </el-col>
      <el-col :span="12" style="text-align:right">
        <el-form 
          :model="queryParams"
          ref="queryFormRef"
          :inline="true"
          class="el-form-search">
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
                v-for="dict in CMSContentStatus"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button-group>
              <el-button 
                type="primary"
                icon="Search"
                @click="handleQuery">{{ $t("Common.Search") }}</el-button>
              <el-button 
                icon="Refresh"
                @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
            </el-button-group>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>

    <el-table 
      v-loading="loading"
      ref="tableRef"
      :data="contentRecycleList"
      :height="tableHeight"
      :max-height="tableMaxHeight"
      @row-click="handleRowClick"
      @selection-change="handleSelectionChange">
      <el-table-column 
        type="selection"
        width="50"
        align="center" />
      <el-table-column :label="$t('CMS.Content.Title')" :show-overflow-tooltip="true" prop="title" />
      <el-table-column 
        :label="$t('CMS.Content.ContentType')" 
        width="110"
        align="center"
        prop="contentType"
        :formatter="contentTypeFormat" />
      <el-table-column 
        :label="$t('CMS.Content.StatusBefore')"
        width="110"
        align="center">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusFormat(scope.row, 'status') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column 
        :label="$t('CMS.Content.DeleteTime')"
        align="center"
        prop="updateTime"
        width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('CMS.Content.DeleteUser')" align="center" :show-overflow-tooltip="true" prop="updateBy" width="140" />
      <el-table-column 
        :label="$t('Common.Operation')"
        align="center"
        width="100"
       >
        <template #default="scope">
          <el-button 
            type="text"
            icon="Delete"
            @click="handleDelete(scope.row)">{{ $t("Common.Delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination 
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="loadRecyclecontentRecycleList" />
  </div>
</template>
<script setup name="CMSContentRecycleList">
import { getContentTypes } from "@/api/contentcore/catalog";
import { getRecycleContentList, recoverRecycleContent, deleteRecycleContents } from "@/api/contentcore/recycle";

const { proxy } = getCurrentInstance();
const { CMSContentStatus } = proxy.useDict('CMSContentStatus');

const props = defineProps({
  cid: {
    type: String,
    default: undefined,
    required: false,
  }
});

const loading = ref(false);
const contentTypeOptions = ref([]);
const catalogId = ref(props.cid);
const contentRecycleList = ref(null);
const total = ref(0);
const tableHeight = ref(600);
const tableMaxHeight = ref(600);
const selectedRows = ref([]);
const objects = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    title: undefined,
    contentType: undefined,
    status: undefined,
    catalogId: undefined,
  }
});
const { queryParams } = toRefs(objects);

watch(() => props.cid, (newVal, oldVal) => {
  if (newVal && newVal != oldVal) {
    loadRecyclecontentRecycleList();
  }
});

onMounted(() => {
  changeTableHeight();
  getContentTypes().then(response => {
    contentTypeOptions.value = response.data;
  });
  if (catalogId.value && catalogId.value > 0) {
    loadRecyclecontentRecycleList();
  }
});

function loadRecyclecontentRecycleList () {
  loading.value = true;
  queryParams.value.catalogId = catalogId.value;
  getRecycleContentList(queryParams.value).then(response => {
    contentRecycleList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
}
  
function statusFormat (row, column) {
  return proxy.selectDictLabel(CMSContentStatus.value, row[column]);
}

function contentTypeFormat (row, column) {
  var hitValue = [];
  contentTypeOptions.value.forEach(ct => {
    if (ct.id == ('' + row.contentType)) {
      hitValue = ct.name;
      return;
    }
  });
  return hitValue;
}

function handleSelectionChange (selection) {
  selectedRows.value = selection;
}

function handleRowClick (currentRow) {
  toggleAllCheckedRows();
  tableRef.value.toggleRowSelection(currentRow);
  selectedRows.value.push(currentRow);
}

function toggleAllCheckedRows() {
  selectedRows.value.forEach(row => {
    tableRef.value.toggleRowSelection(row, false);
  });
  selectedRows.value = [];
}

function statusTagType(status) {
  if (status == 40) {
    return "warning";
  } else if (status == 20 || status == 30) {
    return "success";
  } else if (status == 0) {
    return "info";
  }
  return "";
}

function handleQuery () {
  queryParams.value.pageNum = 1;
  loadRecyclecontentRecycleList();
}

function resetQuery () {
  proxy.resetForm("queryFormRef");
  handleQuery();
}

function handleDelete (row) {
  const backupIds = row.backupId ? [ row.backupId ] : selectedRows.value.map(row => row.backupId);
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function () {
    return deleteRecycleContents(backupIds);
  }).then(() => {
    loadRecyclecontentRecycleList();
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
  }).catch(function () { });
}

function changeTableHeight () {
  let height = document.body.offsetHeight // 网页可视区域高度
  tableHeight.value = height - 330;
  tableMaxHeight.value = tableHeight.value;
}

function handleRecover(row) {
  const backupIds = row.backupId ? [ row.backupId ] : selectedRows.value.map(row => row.backupId);
  recoverRecycleContent(backupIds).then(response => {
    loadRecyclecontentRecycleList();
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
  });
}
</script>
<style scoped>
.head-container .el-select .el-input {
  width: 110px;
}
.el-divider {
  margin-top: 10px;
}
.el-tabs__header {
  margin-bottom: 10px;
}
.pagination-container {
  height: 30px;
}
.row-more-btn {
  padding-left: 10px;
}
.top-icon {
  font-weight: bold;
  font-size: 12px;
  color:green;
}
.content_attr {
  margin-left: 2px;
}
</style>