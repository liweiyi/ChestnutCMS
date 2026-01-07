<template>
  <!-- 自定义表单管理页 -->
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          plain
          type="primary"
          icon="Plus"
          @click="handleAdd">{{ $t("Common.Add") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="success"
          icon="Edit"
          :disabled="ids.length != 1"
          @click="handleEdit">{{ $t("Common.Edit") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="danger"
          icon="Delete"
          :disabled="ids.length == 0"
          @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="warning"
          icon="Close"
          @click="handleClose">{{ $t("Common.Close") }}</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="loadCustomFormDataList"></right-toolbar>
    </el-row>
    <el-row v-show="showSearch">
      <el-form :model="queryParams" ref="queryFormRef" class="el-form-search" :inline="true">
        <el-form-item prop="ip">
          <el-input
            v-model="queryParams.ip"
            clearable
            placeholder="IP"
            style="width: 160px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button-group>
            <el-button type="primary" icon="Search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
            <el-button icon="Refresh" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
          </el-button-group>
        </el-form-item>
      </el-form>
    </el-row>

    <el-table 
      v-loading="loading"
      :data="dataList"
      @selection-change="handleSelectionChange">
      <el-table-column 
        type="selection"
        width="55"
        align="center" />
      <el-table-column 
        v-for="field in fields"
        :key="field.code"
        :label="field.name"
        :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <el-image v-if="field.controlType=='CMSImage'" :src="scope.row[field.code + '_src']" style="max-width:300px;">
              <div slot="error" class="image-slot">
                <el-icon><PictureOutline /></el-icon>
              </div>
          </el-image>
          <span v-else>{{ scope.row[field.code] }}</span>
        </template>
      </el-table-column>
      <el-table-column  
        :label="$t('Common.Operation')"
        align="center"
        width="320" 
       >
        <template slot-scope="scope">
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
      @pagination="loadCustomFormDataList" />

    <el-dialog 
      :title="title" 
      v-model:open="open"
      :close-on-click-modal="false" 
      width="600px" 
      append-to-body>
      <el-form ref="formRef" :model="form" label-width="130px">
        <div v-for="field in fields" :key="field.code">
          <el-form-item
            v-if="field.editable"
            :label="field.name">
            <cms-logo-view v-if="field.controlType=='CMSImage'" v-model="form[field.code]" :height="150"></cms-logo-view>
            <el-input v-else v-model="form[field.code]" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleSubmitForm">{{ $t('Common.Submit') }}</el-button>
        <el-button @click="handleCancel">{{ $t('Common.Cancel') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CmsCustomformData">
import { listModelAllField } from "@/api/meta/model";
import { listCustomFormDatas, getCustomFormData, addCustomFormData, editCustomFormData, deleteCustomFormDatas } from "@/api/customform/customform";
import CmsLogoView from '@/views/cms/components/LogoView';

const { proxy } = getCurrentInstance()

const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const total = ref(0)
const dataList = ref([])
const fields = ref([])
const title = ref("")
const open = ref(false)
const objects = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    formId: proxy.$route.query.formId
  },
  form: {
  }
})

const { queryParams, form } = toRefs(objects)

onMounted(() => {
  loadCustomFormFields();
  loadCustomFormDataList();
});

function loadCustomFormFields() {
  listModelAllField(queryParams.value.formId, false).then(response => {
    fields.value = response.data.rows;
    loading.value = false;
  });
}

function loadCustomFormDataList() {
  loading.value = true;
  listCustomFormDatas(queryParams.value).then(response => {
    dataList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  loadCustomFormDataList();
}

function resetQuery() {
  proxy.$refs.queryFormRef.resetFields();
  handleQuery();
}

function handleSelectionChange(selection) { 
  ids.value = selection.map(item => item.dataId)
}

function reset() {
  proxy.$refs.formRef.resetFields();
  form.value = {};
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = proxy.$t('CMS.CustomForm.AddTitle');
}

function handleEdit(row) {
  reset();
  const dataId = row.dataId ? row.dataId : ids.value[0];
  getCustomFormData(queryParams.value.formId, dataId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = proxy.$t('CMS.CustomForm.EditDataTitle');
  });
}

function handleCancel() {
  open.value = false;
  reset();
}

function handleSubmitForm() {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      if (form.value.dataId) {
        editCustomFormData(queryParams.value.formId, form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.SaveSuccess'));
          open.value = false;
          loadCustomFormDataList();
        });
      } else {
        addCustomFormData(queryParams.value.formId, form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.AddSuccess'));
          open.value = false;
          loadCustomFormDataList();
        });
      }
    }
  });
}

function handleDelete(row) {
  const dataIds = row.dataId ? [ row.dataId ] : ids.value;
  const formId = queryParams.value.formId;
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function () {
    return deleteCustomFormDatas(formId, dataIds);
  }).then(() => {
    loadCustomFormDataList();
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
  }).catch(function () { });
}

function handleClose() {
  const obj = { path: "/operations/customform" };
  proxy.$tab.closeOpenPage(obj);
}
</script>