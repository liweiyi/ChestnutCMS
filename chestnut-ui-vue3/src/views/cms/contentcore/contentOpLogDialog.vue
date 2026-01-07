<template>
  <div class="app-container-content-sort">
    <el-dialog 
      :title="$t('CMS.Content.OpLog')"
      v-model="props.open"
      width="1000px"
      :close-on-click-modal="false"
      append-to-body>
      <el-row>
        <el-col>
          <el-form 
            :model="queryParams"
            ref="queryFormRef"
            :inline="true"
            @submit.native.prevent
            class="el-form-search mt10 mb10"
            style="text-align:left">
            <el-form-item label="" prop="type">
              <el-select 
                v-model="queryParams.type"
                :placeholder="$t('CMS.ContentOpLog.Type')"
                clearable
                style="width: 110px">
                <el-option 
                  v-for="dict in CMSContentOpType"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value" />
              </el-select>
            </el-form-item>
            <el-form-item prop="operator">
              <el-input v-model="queryParams.operator" :placeholder="$t('CMS.ContentOpLog.Operator')" @keyup.enter="handleQuery"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button-group>
                <el-button type="primary" icon="Search" @click="handleQuery">{{ $t("Common.Search") }}</el-button>
                <el-button icon="Refresh" @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
              </el-button-group>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
      
      <el-table 
        v-loading="loading"
        :data="dataList">
        <el-table-column type="index" :label="$t('Common.RowNo')" align="center" width="50" />
        <el-table-column :label="$t('CMS.ContentOpLog.Type')" align="center" prop="type">
          <template #default="scope">
            <dict-tag :options="CMSContentOpType" :value="scope.row.type"/>
          </template>
        </el-table-column>
        <el-table-column :label="$t('CMS.ContentOpLog.OperatorType')" align="center" prop="operatorType"></el-table-column>
        <el-table-column :label="$t('CMS.ContentOpLog.Operator')" align="center" prop="operator" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column :label="$t('CMS.ContentOpLog.LogTime')" align="center" prop="logTime" width="160">
          <template #default="scope">
            <span>{{ parseTime(scope.row.logTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('CMS.ContentOpLog.Details')" align="center" prop="details" :show-overflow-tooltip="true"></el-table-column>
      </el-table>
      <pagination 
        v-show="total>0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="loadContentOpLogList" />
      <template #footer>
        <el-button @click="handleClose">{{ $t("Common.Close") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CMSContentOpLogDialog">
import { getContentOpLogList } from "@/api/contentcore/content";

const { proxy } = getCurrentInstance();
const { CMSContentOpType } = proxy.useDict('CMSContentOpType');

const emit = defineEmits(['close']);
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

watch(() => props.open, (newVal) => {
  if (!newVal) {
    handleClose();
  } else {
    loadContentOpLogList();
  }
});

watch(() => props.cid, (newVal) => {
  queryParams.value.contentId = newVal;
});

const loading = ref(false);
const dataList = ref([]);
const total = ref(0);
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  contentId: props.cid,
  type: undefined,
  operator: undefined
});

function loadContentOpLogList () {
  if (!props.open) {
    return;
  }
  loading.value = true;
  getContentOpLogList(queryParams.value).then(response => {
    dataList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
}

function handleClose () {
  emit("close");
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    contentId: props.cid,
    type: undefined,
    operator: undefined
  };
  dataList.value = [];
  total.value = 0;
}

function handleQuery () {
  loadContentOpLogList();
}

function resetQuery () {
  proxy.resetForm("queryFormRef");
  handleQuery();
}
</script>