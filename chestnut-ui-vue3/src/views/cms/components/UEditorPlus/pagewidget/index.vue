<template>
  <div class="app-container" style="padding: 0;">
    <el-dialog 
      class="pagewidget-dialog"
      :title="$t('CMS.UEditor.PageWidget.DialogTitle')"
      v-model="visible"
      width="980px"
      :close-on-click-modal="false"
      append-to-body>
      <el-container>
        <el-aside>
          <cms-catalog-tree 
            ref="catalogTreeRef"
            @node-click="handleTreeNodeClick">
          </cms-catalog-tree>
        </el-aside>
        <el-main>
          <el-form :model="queryParams"
              ref="queryFormRef"
              :inline="true"
              class="el-form-search">
            <el-form-item label="" prop="query">
              <el-input v-model="queryParams.query" :placeholder="$t('CMS.UEditor.PageWidget.Name')">
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-button-group>
                <el-button type="primary" icon="Search" @click="handleQuery">{{ $t("Common.Search") }}</el-button>
                <el-button icon="Refresh" @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
              </el-button-group>
            </el-form-item>
          </el-form>
          <el-table 
            v-loading="loading"
            ref="tablePageWidgetListRef"
            :data="pageWidgetList"
            highlight-current-row
            @row-click="handleRowClick"
            @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="50" align="center" />
            <el-table-column :label="$t('CMS.UEditor.PageWidget.Name')" align="left" prop="title">
              <template #default="scope">
                <span><i v-if="scope.row.topFlag>0" class="el-icon-top top-icon" :title="$t('CMS.Content.SetTop')"></i> {{ scope.row.title }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('CMS.UEditor.PageWidget.Code')" align="center" prop="publishDate" width="160">
              <template #default="scope">
                <span>{{ parseTime(scope.row.publishDate) }}</span>
              </template>
            </el-table-column>
          </el-table>
          <pagination 
            v-show="total>0"
            :total="total"
            v-model:page="queryParams.pageNum"
            v-model:limit="queryParams.pageSize"
            @pagination="loadPageWidgetList" />
        </el-main>
      </el-container>
      <template #footer>
        <el-button type="primary" @click="handleOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleCancel">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CMSUeditorPageWidget">
import { listPageWidgets } from "@/api/contentcore/pagewidget";
import CmsCatalogTree from '@/views/cms/contentcore/catalogTree';

const { proxy } = getCurrentInstance()

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
    required: true
  },
  type: {
    type: String,
    default: "",
    required: false
  }
});
const emit = defineEmits(['update:open', 'ok', 'close']);

const loading = ref(false);
const selectedRows = ref(undefined);
const pageWidgetList = ref([]);
const total = ref(0);
const visible = ref(false);
const objects = ref({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    status: 30,
    catalogId: '',
    query: '',
    type: props.type
  }
});
const { queryParams } = toRefs(objects);

watch(() => props.open, (newVal) => {
  visible.value = newVal;
})
watch(visible, (newVal) => {
  if (!newVal) {
    noticeClose();
  }
})

function handleTreeNodeClick(data) {
  queryParams.value.catalogId = data && data != null ? data.id : ''; 
  loadPageWidgetList();
}

function loadPageWidgetList () {
  if (!visible.value) {
    return;
  }
  loading.value = true;
  listPageWidgets(queryParams.value).then(response => {
    pageWidgetList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    selectedRows.value = [];
    loading.value = false;
  });
}

function handleRowClick (row) {
  selectedRows.value.forEach(row => {
    proxy.$refs.tablePageWidgetListRef.toggleRowSelection(row, false);
  });
  selectedRows.value = [];
  proxy.$refs.tablePageWidgetListRef.toggleRowSelection(row);
}

function handleSelectionChange (selection) {
  selectedRows.value = selection;
}

function handleQuery () {
  loadPageWidgetList();
}

function handleOk () {
  noticeOk();
}

function handleCancel () {
  visible.value = false;
}

function noticeOk () {
  if (visible.value && selectedRows.value) {
    var html = '<p class="text-align:center;"><img src="/UEditorPlus/themes/default/images/spacer.gif" pw_code="'
      + selectedRows.value.code + '" title="' + selectedRows.value.name + '" class="pw_placeholder" /></p>' // 引用页面部件占位符

    emit("ok", html);
    visible.value = false;
  }
}

function noticeClose () {
  if (!visible.value) {
    emit("update:open", false);
    emit("close");
    
    selectedRows.value = [];
    pageWidgetList.value = [];
    
    queryParams.value = {
      pageNum: 1,
      pageSize: 10,
      status: 30,
      catalogId: '',
      query: '',
      type: props.type
    }
  }
}

function resetQuery() {
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    status: 30,
    catalogId: '',
    query: '',
    type: props.type
  }
  loadPageWidgetList()
}
</script>