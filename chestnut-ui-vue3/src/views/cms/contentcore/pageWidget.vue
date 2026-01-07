<template>
  <div class="pagewidget-container">
    <el-row :gutter="24" class="mb8">
      <el-col :span="8">
        <el-button 
          plain
          type="primary"
          icon="Plus"
          v-hasPermi="[ $p('Site:AddPageWidget:{0}', [ siteId ]) ]"
          @click="handleAdd">{{ $t("Common.Add") }}</el-button>
      </el-col>
      <el-col :span="16" style="text-align: right;">
        <el-select 
          v-model="queryParams.type"
          :placeholder="$t('CMS.PageWidget.Placeholder.Type')"
          clearable
          @change="loadPageWidgetList"
          style="width: 240px;">
          <el-option 
            v-for="item in pageWidgetTypes"
            :key="item.id"
            :label="item.name"
            :value="item.id" />
        </el-select>
      </el-col>
    </el-row>
    <el-table 
      v-loading="loading"
      ref="pageWidgetListRef"
      :data="pageWidgetList"
      :height="tableHeight"
      :max-height="tableMaxHeight"
      @row-click="handleRowClick"
      @cell-dblclick="handleEdit"
      @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column :label="$t('CMS.PageWidget.Name')" prop="name" :show-overflow-tooltip="true">
        <template #default="scope">
          {{ scope.row.name }}
        </template>
      </el-table-column>
      <el-table-column :label="$t('CMS.PageWidget.Code')" align="left" prop="code" />
      <el-table-column :label="$t('CMS.PageWidget.Type')" align="center" width="120">
        <template #default="scope">
          {{ pageWidgetTypeName(scope.row.type) }}
        </template>
      </el-table-column>
      <el-table-column :label="$t('CMS.Content.Status')" align="center" width="100">
        <template #default="scope">
          <dict-tag :options="CMSPageWidgetStatus" :value="scope.row.state"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" align="left" width="360" fixed="right">
        <template #default="scope">
          <el-button 
            link
            type="primary"
            icon="View"
            @click="handlePreview(scope.row)">{{ $t('CMS.ContentCore.Preview') }}</el-button>
          <el-button 
            link
            type="primary"
            icon="Promotion"
            v-hasPermi="[ $p('PageWidget:Publish:{0}', [ scope.row.pageWidgetId ]) ]"
            @click="handlePublish(scope.row)">{{ $t('CMS.ContentCore.Publish') }}</el-button>
          <el-button 
            link
            type="danger"
            icon="Download"
            v-hasPermi="[ $p('PageWidget:Edit:{0}', [ scope.row.pageWidgetId ]) ]"
            @click="handleOffline(scope.row)">{{ $t("CMS.Content.Offline") }}</el-button>
          <el-button 
            link
            type="success"
            icon="Edit"
            v-hasPermi="[ $p('PageWidget:Edit:{0}', [ scope.row.pageWidgetId ]) ]"
            @click="handleEdit(scope.row)">{{ $t('Common.Edit') }}</el-button>
          <el-button 
            link
            type="danger"
            icon="Delete"
            v-hasPermi="[ $p('PageWidget:Delete:{0}', [ scope.row.pageWidgetId ]) ]"
            @click="handleDelete(scope.row)">{{ $t("Common.Delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="loadPageWidgetList"
    />
    <!-- 添加对话框 -->
    <el-dialog 
      :title="$t('CMS.PageWidget.AddTitle')"
      v-model="dialogVisible"
      :close-on-click-modal="false"
      width="600px"
      append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item :label="$t('CMS.PageWidget.Type')" prop="type">
          <el-select v-model="form.type" clearable>
            <el-option  
              v-for="item in pageWidgetTypes"
              :key="item.id"
              :label="item.name"
              :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('CMS.PageWidget.Name')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="$t('CMS.PageWidget.Code')" prop="code">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item :label="$t('CMS.PageWidget.Path')" prop="path">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea" maxlength="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleAddDialogOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleAddDialogClose">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CMSPageWdiget">
import { codeValidator, pathValidator } from '@/utils/validate';
import { getPublishPipeSelectData } from "@/api/contentcore/publishpipe";
import * as pageWidgetApi from "@/api/contentcore/pagewidget";

const { proxy } = getCurrentInstance();
const { CMSPageWidgetStatus } = proxy.useDict('CMSPageWidgetStatus');

const props = defineProps({
  cid: {
    type: String,
    default: undefined,
    required: false,
  }
});

const loading = ref(false);
const tableHeight = ref(600);
const tableMaxHeight = ref(600);
const selectedRows = ref([]);
const siteId = ref(proxy.$cms.getCurrentSite());
const dialogVisible = ref(false);
const publishPipes = ref([]);
const pageWidgetTypes = ref([]);
const pageWidgetList = ref([]);
const total = ref(0);
const objects = reactive({
  queryParams: {
    type: undefined,
    pageSize: 20,
    pageNum: 1
  },
  form: {
    path: 'include/pagewidget/'
  },
  rules: {
    type: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
    ],
    name: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
    ],
    code: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
      { validator: codeValidator, trigger: "change" },
    ],
    publishPipeCode: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
    ],
    path: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
      { validator: pathValidator, trigger: "change" },
    ]
  }
});
const { queryParams, form, rules } = toRefs(objects);

watch(() => props.cid, (newVal, oldVal) => {
  if (newVal && newVal != oldVal) {
    loadPageWidgetList();
  }
});

onMounted(() => {
  changeTableHeight();
  loadPageWidgetList();
  loadPageWdigetTypes();
  loadPublishPipes();
});

const changeTableHeight = () => {
  let height = document.body.offsetHeight // 网页可视区域高度
  tableHeight.value = height - 330;
  tableMaxHeight.value = tableHeight.value;
};

function loadPageWdigetTypes() {
  pageWidgetApi.listPageWidgetTypes().then(response => {
    pageWidgetTypes.value = response.data.rows;
  });
}

function pageWidgetTypeName(type) {
  let pt = pageWidgetTypes.value.find(v => v.id == type);
  return pt ? pt.name : type;
}

function loadPublishPipes() {
  getPublishPipeSelectData().then(response => {
    publishPipes.value = response.data.rows;
  });
}

function publishPipeName(code) {
  let pp = publishPipes.value.find(v => v.pipeCode == code);
  return pp ? pp.pipeName : code
}

function loadPageWidgetList() {
  loading.value = true;
  queryParams.value.catalogId = props.cid;
  pageWidgetApi.listPageWidgets(queryParams.value).then(response => {
    pageWidgetList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
}

function handleSelectionChange(selection) {
  selectedRows.value = selection;
}

function handleRowClick(currentRow) {
  toggleAllCheckedRows();
  proxy.$refs.pageWidgetListRef.toggleRowSelection(currentRow);
  selectedRows.value.push(currentRow);
}

function toggleAllCheckedRows() {
  selectedRows.value.forEach(row => {
      proxy.$refs.pageWidgetListRef.toggleRowSelection(row, false);
  });
  selectedRows.value = [];
}

function handleAdd() {
  dialogVisible.value = true;
  form.value = { path: 'include/pagewidget/' };
}

function handleAddDialogClose() {
  dialogVisible.value = false;
}

function handleAddDialogOk() {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      form.value.catalogId = (props.cid && props.cid.length > 0) ? props.cid : 0;
      pageWidgetApi.addPageWidget(form.value).then(response => {
        proxy.$modal.msgSuccess(response.msg);
        loadPageWidgetList();
        handleAddDialogClose();
      });
    }
  });
}

function handleDelete(row) {
  const pageWidgetIds = [ row.pageWidgetId ];
  proxy.$modal.confirm(proxy.$t('Common.ConfigmDelete')).then(function() {
    return pageWidgetApi.deletePageWidget(pageWidgetIds);
  }).then(response => {
    proxy.$modal.msgSuccess(response.msg);
    loadPageWidgetList();
  }).catch(() => {});
}

function handleEdit(row) {
  proxy.$router.push({ path: row.route, query: { id: row.pageWidgetId, from: "pagewidget" } });
}

function handlePublish(row) {
  const pageWidgetIds = [ row.pageWidgetId ];
  pageWidgetApi.publishPageWidgets(pageWidgetIds).then(response => {
    proxy.$modal.msgSuccess(response.msg);
    loadPageWidgetList();
  });
}

function handleOffline(row) {
  const pageWidgetIds = [ row.pageWidgetId ];
  pageWidgetApi.offlinePageWidgets(pageWidgetIds).then(response => {
    proxy.$modal.msgSuccess(response.msg);
    loadPageWidgetList();
  });
}

function handlePreview(row) {
  let routeData = proxy.$router.resolve({
    path: "/cms/preview",
    query: { type: "pagewidget", dataId: row.pageWidgetId },
  });
  window.open(routeData.href, '_blank');
}
</script>
<style scoped lang="scss">
.pagewidget-container .el-card {
  margin-bottom: 10px;
}
.pagewidget-container .el-card__body {
  padding: 0;
}
.pagewidget-container .item-state {
  text-align: left;
}
.pagewidget-container .item-toolbar {
  text-align: right;
}
.pagewidget-container .item-catalog {
  margin-right: 5px;
}
.pagewidget-container .item-row1, .item-row2 {
  padding: 10px;
}
.pagewidget-container .item-row1 .item-name {
  font-size: 16px;
  font-weight: 400;
}
.pagewidget-container .pagination-container {
  height: 30px;
}
.pagewidget-container .item-row2 {
  color: #999;
}
.pagewidget-container .item-row3 {
  background-color: #f9f9f9;
  padding: 10px;
}
</style>