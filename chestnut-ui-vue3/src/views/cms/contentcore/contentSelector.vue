<template>
  <div class="app-container" style="padding: 0px;">
    <el-dialog 
      :title="$t('CMS.ContentCore.SelectContent')"
      v-model="visible"
      width="1100px"
      :close-on-click-modal="false"
      custom-class="content-selector-dialog"
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
            <el-form-item label="" prop="title">
              <el-input 
                v-model="queryParams.title" 
                :placeholder="$t('CMS.Content.Placeholder.Title')"
                @keyup.enter.native="handleQuery">
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
            ref="tableContentListRef"
            :data="contentList"
            highlight-current-row
            @row-click="handleRowClick"
            @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="50" align="center" />
            <el-table-column :label="$t('CMS.Content.Title')" align="left" prop="title">
              <template #default="scope">
                <span><i v-if="scope.row.topFlag>0" class="el-icon-top top-icon" :title="$t('CMS.Content.SetTop')"></i> {{ scope.row.title }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('CMS.Content.PublishDate')" align="center" prop="publishDate" width="160">
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
            @pagination="loadContentList" />
        </el-main>
      </el-container>
      <template #footer>
        <el-button type="primary" @click="handleOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleClose">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CmsContentSelector">
import { getContentList } from "@/api/contentcore/content";
import CmsCatalogTree from '@/views/cms/contentcore/catalogTree';

const { proxy } = getCurrentInstance();

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
    required: true
  },
  contentType: {
    type: String,
    default: '',
    required: false
  }
});

const emit = defineEmits(['ok', 'close']);

const visible = ref(props.open);
const loading = ref(false);
const selectedContents = ref([]);
const contentList = ref([]);
const total = ref(0);
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  status: 30,
  catalogId: '',
  contentType: '',
  title: ''
});

watch(() => props.open, (newVal) => {
  visible.value = newVal;
});

watch(visible, (newVal) => {
  if (!newVal) {
    handleClose();
  } else {
    loadContentList();
  }
});

watch(() => props.contentType, (newVal) => {
  queryParams.contentType = newVal;
});

function handleTreeNodeClick(data) {
  queryParams.catalogId = data && data != null ? data.id : ''; 
  loadContentList();
}

function loadContentList() {
  if (!props.open) {
    return;
  }
  loading.value = true;
  getContentList(queryParams).then(response => {
    contentList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    selectedContents.value = [];
    loading.value = false;
  });
}

function handleSelectionChange(selection) {
  selectedContents.value = selection;
}

function handleRowClick (row) {
  selectedContents.value.forEach(row => {
    proxy.$refs.tableContentListRef.toggleRowSelection(row, false);
  });
  selectedContents.value = [];
  proxy.$refs.tableContentListRef.toggleRowSelection(row);
}

function handleOk () {
  emit("ok", selectedContents.value);
}

function handleClose () {
  emit("close");
  proxy.resetForm("queryFormRef");
  queryParams.contentType = ''
  selectedContents.value = [];
  contentList.value = [];
}

function handleQuery () {
  loadContentList();
}

function resetQuery () {
  proxy.resetForm("queryFormRef");
  handleQuery();
}
</script>
<style scoped>
.content-selector-dialog .el-dialog__body {
  padding-top: 10px;
  padding-bottom: 10px;
}
.content-selector-dialog .el-aside {
  padding: 10px;
  background-color: #fff;
}
.content-selector-dialog .el-main {
  padding: 10px;
}
</style>