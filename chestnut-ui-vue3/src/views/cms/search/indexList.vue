<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button 
          type="danger"
          plain
          icon="Delete"
          :disabled="selectedIds.length == 0"
          @click="handleDelete"
        >{{ $t("Common.Delete") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          type="success"
          icon="Refresh"
          plain
          @click="handleRebuildIndex"
        >{{ $t('CMS.ESIndex.RebuildAll') }}</el-button>
      </el-col>
    </el-row>
    <el-form 
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      class="el-form-search">
      <el-form-item prop="contentType">
        <el-select v-model="queryParams.contentType" :placeholder="$t('CMS.ContentCore.ContentType')" clearable style="width: 140px">
          <el-option 
            v-for="ct in contentTypeOptions"
            :key="ct.id"
            :label="ct.name"
            :value="ct.id" />
        </el-select>
      </el-form-item>
      <el-form-item prop="query">
        <el-input v-model="queryParams.query" :placeholder="$t('CMS.ESIndex.Query')" clearable style="width: 500px">
        </el-input>
      </el-form-item>
      <el-form-item prop="onlyTitle">
        <el-checkbox v-model="queryParams.onlyTitle" :label="$t('CMS.ESIndex.OnlyTitle')" border></el-checkbox>
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

    <el-table v-loading="loading" :data="contentList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="contentId" width="180" />
      <el-table-column :label="$t('CMS.Content.Catalog')" align="left" width="180" prop="catalogName" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('CMS.Content.Title')" align="left" prop="title">
        <template #default="scope">
          <span v-html="scope.row.title"></span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('CMS.Content.ContentType')" width="110" align="center" prop="contentType" :formatter="contentTypeFormat" />
      <el-table-column :label="$t('CMS.Content.Status')" width="110" align="center">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusFormat(scope.row, 'status') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('CMS.Content.PublishDate')" align="center" prop="publishDateInstance" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.publishDateInstance) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.CreateTime')" align="center" prop="createTimeInstance" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTimeInstance) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="_score" align="left" prop="hitScore" width="70" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Common.Operation')" align="center" width="220">
        <template #default="scope">
          <el-button 
            type="text"
            icon="Search"
            @click="handleShowDetail(scope.row)">{{ $t("Common.Details") }}</el-button>
          <el-button 
            type="text"
            icon="Edit"
            @click="handleEdit(scope.row)">{{ $t("Common.Edit") }}</el-button>
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
      @pagination="getList" />

    <!-- 添加或修改资源对话框 -->
    <el-dialog 
      :title="$t('CMS.ESIndex.IndexDetails')"
      v-model="open"
      width="960px"
      append-to-body>
      <el-row :gutter="15" class="data_row" v-for="(value, key, index) in showData" :key="index">
        <el-col :span="4" class="data_row_left">
          {{ key }}
        </el-col>
        <el-col :span="20">
          <span v-if="isHtmlField(key)" v-html="value"></span>
          <span v-else>{{ value }}</span>
        </el-col>
      </el-row>
      <template #footer>
        <el-button @click="cancel">{{ $t('Common.Close') }}</el-button>
      </template>
    </el-dialog>
    <!-- 进度条 -->
    <cms-progress :title="$t('CMS.ESIndex.ProgressTitle')" v-model:open="openProgress" :taskId="taskId" @close="handleProgressClose"></cms-progress>
  </div>
</template>
<script setup name="CmsSearchIndexList">
import CmsProgress from '@/views/components/Progress';
import { getUserPreference } from "@/api/system/user";
import { getContentTypes } from "@/api/contentcore/catalog";
import { getContentIndexList, deleteContentIndex, rebuildIndex } from "@/api/contentcore/search";
const { proxy } = getCurrentInstance();
const { CMSContentStatus } = proxy.useDict('CMSContentStatus');

const loading = ref(true);
const selectedIds = ref([]);
const taskId = ref(undefined);
const openProgress = ref(false);
const total = ref(0);
const contentTypeOptions = ref([]);
const contentList = ref([]);
const showData = ref({});
const open = ref(false);
const queryParams = reactive({
  contentType: "",
  query: "",
  onlyTitle: false,
  pageNum: 1,
  pageSize: 10,
});
const openEditorW = ref(false);

onMounted(() => {
  getContentTypes().then(response => {
    contentTypeOptions.value = response.data;
  });
  getUserPreference('OpenContentEditorW').then(response => {
    openEditorW.value = response.data == 'Y'
  })
  getList();
});

function getList () {
  loading.value = true;
  getContentIndexList(queryParams).then(response => {
    contentList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
}

function isHtmlField (key) {
  return key == 'title' || key == 'fullText';
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

function handleQuery () {
  queryParams.pageNum = 1;
  getList();
}

function resetQuery () {
  proxy.resetForm("queryFormRef");
  handleQuery();
}

function statusFormat (row, column) {
  return proxy.selectDictLabel(CMSContentStatus.value, row[column]);
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

function handleSelectionChange (selection) {
  selectedIds.value = selection.map(item => item.contentId)
}

function handleShowDetail(row) {
  showData.value = row;
  open.value = true;
}

function cancel () {
  open.value = false;
}

function handleEdit (row) {
  openEditor(row.catalogId, row.contentId, row.contentType);
}

function openEditor(catalogId, contentId, contentType) {
  if (openEditorW.value) {
    let routeData = proxy.$router.resolve({
      path: "/cms/content/editorW",
      query: { type: contentType, catalogId: catalogId, id: contentId },
    });
    window.open(routeData.href, '_blank');
  } else {
    proxy.$router.push({ path: "/cms/content/editor", query: { type: contentType, catalogId: catalogId, id: contentId, w: openEditorW.value } });
  }
}

function handleDelete (row) {
  const contentIds = row.contentId ? [ row.contentId ] : selectedIds.value;
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function () {
    return deleteContentIndex(contentIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
  }).catch(function () { });
}

function handleRebuildIndex() {
  rebuildIndex().then(response => {
    if (response.data && response.data != "") {
      taskId.value = response.data;
      openProgress.value = true;
    }
  });
}

function handleProgressClose(result) {
  if (result.status == 'SUCCESS' || result.status == 'INTERRUPTED') {
    getList();
  }
}
</script>
<style scoped>
.data_row {
  line-height: 28px;
}
.data_row_left {
  font-weight: 600;
  text-align: right;
}
</style>