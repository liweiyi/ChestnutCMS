<template>
  <div class="app-container-content-sort">
    <el-dialog 
      :title="$t('CMS.Content.SortDialogTitle')"
      v-model="props.open"
      width="800px"
      :close-on-click-modal="false"
      custom-class="content-selector-dialog"
      append-to-body>
      <div style="background-color: #f4f4f5;color: #909399;font-size:12px;line-height: 30px;padding-left:10px;">
        <i icon="InfoFilled" class="mr5"></i>{{ $t("CMS.Content.SortDialogTip") }}
      </div>
      <el-form 
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        class="el-form-search mt10 mb10"
        style="text-align:left">
        <el-form-item prop="title">
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
        :data="contentList"
        highlight-current-row
        @current-change="handleSelectionChange">
        <el-table-column type="index" :label="$t('Common.RowNo')" align="center" width="50" />
        <el-table-column :label="$t('CMS.Content.Title')" align="left" prop="title">
          <template #default="scope">
            <span><i v-if="scope.row.topFlag>0" class="el-icon-top top-icon" :title="$t('CMS.Content.SetTop')"></i> {{ scope.row.title }}</span>
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
        @pagination="loadContentList" />
      <template #footer>
        <el-button type="primary"  @click="handleOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleClose">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CMSContentSortDialog">
import { getContentList } from "@/api/contentcore/content";

const { proxy } = getCurrentInstance();
const { CMSContentStatus } = proxy.useDict('CMSContentStatus');

const emit = defineEmits(['ok', 'close']);
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

const loading = ref(false);
const selectedContents = ref([]);
const contentList = ref([]);
const total = ref(0);
const objects = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    catalogId: props.cid,
    title: undefined
  }
});
const { queryParams } = toRefs(objects);

watch(() => props.open, (newVal) => {
  if (!newVal) {
    handleClose();
  } else {
    loadContentList();
  }
});

watch(() => props.cid, (newVal, oldVal) => {
  if (newVal && newVal != oldVal) {
    queryParams.value.catalogId = newVal;
  }
});

function loadContentList () {
  if (!props.open) {
    return;
  }
  loading.value = true;
  getContentList(queryParams.value).then(response => {
    contentList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    selectedContents.value = [];
    loading.value = false;
  });
}

function handleSelectionChange (selection) {
  if (selection && selection.contentId) {
    selectedContents.value = [ selection ];
  }
}

function handleOk () {
  emit("ok", selectedContents.value);
}

function handleClose () {
  emit("close");
  proxy.resetForm("queryFormRef");
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
.content-selector-dialog .top-icon {
  font-weight: bold;
  font-size: 12px;
  color:green;
}
</style>