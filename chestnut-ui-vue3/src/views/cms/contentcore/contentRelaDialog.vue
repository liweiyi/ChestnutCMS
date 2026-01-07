<template>
  <div class="app-container-content-sort">
    <el-dialog 
      :title="$t('CMS.Content.RelaContent')"
      v-model="props.open"
      width="800px"
      :close-on-click-modal="false"
      append-to-body>
      <div style="display: flext; justify-content: space-between;">
        <el-row :gutter="10">
          <el-col :span="1.5">
            <el-button 
              plain
              type="primary"
              icon="Plus"
              v-hasPermi="[ $p('Catalog:EditContent:{0}', [ props.cid ]) ]"
              @click="handleAdd">{{ $t("Common.Add") }}
            </el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button 
              plain
              type="danger"
              icon="Delete"
              :disabled="selectedContents.length == 0"
              v-hasPermi="[ $p('Catalog:EditContent:{0}', [ props.cid ]) ]"
              @click="handleDelete">{{ $t("Common.Delete") }}
            </el-button>
          </el-col>
        </el-row>
        <el-form 
          :model="queryParams"
          ref="queryFormRef"
          :inline="true"
          class="el-form-search mt10 mb10"
          style="text-align:left">
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
      </div>
      
      <el-table 
        v-loading="loading"
        :data="relaContentList"
        highlight-current-row
        @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column type="index" :label="$t('Common.RowNo')" align="center" width="50" />
        <el-table-column :label="$t('CMS.Content.Title')" align="left" prop="title">
          <template #default="scope">
            <span><i v-if="scope.row.topFlag>0" icon="Top" :title="$t('CMS.Content.SetTop')"></i> {{ scope.row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('CMS.Content.Status')" align="center" prop="status">
          <template #default="scope">
            <dict-tag :options="CMSContentStatus" :value="scope.row.status"/>
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
        @pagination="loadRelaContentList" />
      <template #footer>
        <el-button @click="handleClose">{{ $t("Common.Close") }}</el-button>
      </template>
    </el-dialog>
    <!-- 内容选择组件 -->
    <cms-content-selector
      v-model:open="openContentSelector"
      @ok="handleContentSelectorOk"
      @close="handleContentSelectorClose"></cms-content-selector>
  </div>
</template>
<script setup name="CMSContentRelaDialog">
import { getRelaContentList, addRelaContents, delRelaContents } from "@/api/contentcore/rela";
import CmsContentSelector from "@/views/cms/contentcore/contentSelector";

const { proxy } = getCurrentInstance();

const { CMSContentStatus } = proxy.useDict('CMSContentStatus');

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
    required: true
  },
  cid: {
    type: String,
    default: undefined,
    required: false
  }
});
const emit = defineEmits(['close']);

watch(() => props.open, (newVal) => {
  if (!newVal) {
    handleClose();
  } else {
    loadRelaContentList();
  }
});

watch(() => props.cid, (newVal, oldVal) => {
  if (newVal && newVal != oldVal) {
    queryParams.value.contentId = newVal;
  }
});

const loading = ref(false);
const selectedContents = ref([]);
const relaContentList = ref([]);
const total = ref(0);
const openContentSelector = ref(false);

const objects = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    contentId: props.cid,
    title: undefined
  }
});
const { queryParams } = toRefs(objects);

function loadRelaContentList () {
  if (!props.open) {
    return;
  }
  loading.value = true;
  getRelaContentList(queryParams.value).then(response => {
    relaContentList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    selectedContents.value = [];
    loading.value = false;
  });
}

function handleSelectionChange (selection) {
  selectedContents.value = selection;
}

function handleAdd () {
  openContentSelector.value = true;
}

function handleContentSelectorOk(contents) {
  if (contents && contents.length > 0) {
    const data = { contentId: props.cid, relaContentIds: contents.map(c => c.contentId) }
    addRelaContents(data).then(response => {
      openContentSelector.value = false;
      proxy.$modal.msgSuccess(proxy.$t('Common.AddSuccess'));
      handleQuery();
    });
  } else {
    proxy.$modal.msgWarning(proxy.$t('Common.SelectFirst'));
  }
}

function handleContentSelectorClose() {
  openContentSelector.value = false;
}

function handleDelete () {
  if (selectedContents.value.length == 0) {
    proxy.$modal.msgWarning(proxy.$t('Common.SelectFirst'));
    return;
  }
  const data = { contentId: props.cid, relaContentIds: selectedContents.value.map(c => c.contentId) }
  delRelaContents(data).then(response => {
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
    handleQuery();
  });
}

function handleClose () {
  emit("close");
  proxy.resetForm("queryForm");
  selectedContents.value = [];
  relaContentList.value = [];
}

function handleQuery () {
  loadRelaContentList();
}

function resetQuery () {
  proxy.resetForm("queryForm");
  handleQuery();
}
</script>