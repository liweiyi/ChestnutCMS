<template>
  <div class="app-container">
    <el-row justify="space-between">
      <div>
        <el-row :gutter="10">
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
              :disabled="selectedIds.length != 1"
              @click="handleUpdate">{{ $t("Common.Edit") }}</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button 
              plain
              type="danger"
              icon="Delete"
              :disabled="selectedIds.length == 0"
              @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
          </el-col>
        </el-row>
      </div>
      <div>
        <el-form 
          :model="queryParams"
          ref="queryFormRef"
          :inline="true"
          class="el-form-search">
          <el-form-item prop="name">
            <el-input :placeholder="$t('CMS.Resource.Name')" v-model="queryParams.name">
              <template #prepend>
                <el-select v-model="queryParams.resourceType" :placeholder="$t('CMS.Resource.Type')" clearable style="width:100px;">
                  <el-option
                    v-for="rt in resourceTypes"
                    :key="rt.id"
                    :label="rt.name"
                    :value="rt.id"
                  />
                </el-select>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item :label="$t('Common.CreateTime')">
            <el-date-picker 
              v-model="dateRange"
              style="width: 240px"
              value-format="YYYY-MM-DD"
              type="daterange"
              range-separator="-"
              :start-placeholder="$t('Common.BeginDate')"
              :end-placeholder="$t('Common.EndDate')"
            ></el-date-picker>
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
      </div>
    </el-row>

    <el-table v-loading="loading" :data="resourceList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" prop="resourceId" align="center" width="180" />
      <el-table-column :label="$t('CMS.Resource.Type')" prop="resourceTypeName" width="80" />
      <el-table-column :label="$t('CMS.Resource.Name')" prop="name" align="left">
        <template #default="scope">
          <svg-icon :icon-class="scope.row.iconClass" /> <el-link type="primary" target="_blank" :href="scope.row.src">{{ scope.row.name }}</el-link>
        </template>
      </el-table-column>
      <el-table-column :label="$t('CMS.Resource.StorageType')" prop="storageType" align="center" width="120" />
      <el-table-column :label="$t('CMS.Resource.FileSize')" prop="fileSizeName" align="center" width="120" />
      <el-table-column :label="$t('Common.CreateTime')" prop="createTime" align="center" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column 
        :label="$t('Common.Operation')"
        align="center"
        width="180" 
       >
        <template #default="scope">
          <el-button 
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)">{{ $t("Common.Edit") }}</el-button>
          <el-button 
            link
            type="danger"
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
      :title="title"
      v-model="open"
      width="500px"
      append-to-body>
      <el-form 
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px">
        <el-form-item :label="$t('CMS.Resource.UploadResource')" class="upload-form-item">
          <el-upload 
            ref="uploadRef"
            drag
            :data="form"
            :action="upload.url"
            :headers="upload.headers"
            :file-list="upload.fileList"
            :on-progress="handleFileUploadProgress"
            :on-success="handleFileSuccess"
            :auto-upload="false"
            :before-upload="handleBeforeUpload"
            :on-change="handleUploadChange"
            :limit="1">
              <template #default>
                <el-icon :size="36"><Upload /></el-icon>
                <div class="upload-tip">{{ $t("CMS.Resource.UploadTip1") }}</div>
              </template>
              <template #tip>
                <el-popover
                  :content="$t('CMS.Resource.Accept', [ upload.accept ])"
                  placement="top-start"
                  width="500"
                >
                  <template #reference>
                    <div class="upload-limit-tip">{{ $t("CMS.Resource.Accept", [ upload.accept ]) }}</div>
                  </template>
                </el-popover>
                {{ $t("CMS.Resource.AcceptSize", [ $tools.formatSize(upload.acceptSize) ]) }}
              </template>
            </el-upload>
        </el-form-item>
        <el-form-item :label="$t('CMS.Resource.Name')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" :loading="upload.isUploading" @click="submitForm">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CmsContentcoreResource">
import { getFileSvgIconClass } from "@/utils/chestnut";
import { getResourceTypes, getResourceList, getResourceDetail, delResource } from "@/api/contentcore/resource";
import { getConfigKey } from "@/api/system/config";

const { proxy } = getCurrentInstance()

const loading = ref(true)
const selectedIds = ref([])
const resourceList = ref([])
const total = ref(0)
const open = ref(false)
const title = ref("")
const dateRange = ref([])
const objects = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 15,
    resourceType: undefined,
    name: undefined,
    beginTime: undefined,
    endTime: undefined
  },
  form: {},
})
const { queryParams, form } = toRefs(objects)
const resourceTypes = ref([])
const rules = reactive({
  name: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ]
})
const upload = reactive({
  isUploading: false,
  accept: "",
  acceptSize: 0,
  headers: { ...proxy.$auth.getTokenHeader(), ...proxy.$cms.currentSiteHeader() },
  url: import.meta.env.VITE_APP_BASE_API + "/cms/resource",
  fileList: [],
  data: {}
})

onMounted(() => {
  getConfigKey("ResourceUploadAcceptSize").then(res => {
    upload.acceptSize = parseInt(res.data);
  });
  loadResourceTypes();
  getList();
})
const loadResourceTypes = () => {
  getResourceTypes().then(response => {
    resourceTypes.value = response.data;
    resourceTypes.value.forEach((item) => {
      upload.accept += "." + item.accepts.replaceAll(",", ",.")
    })
  });
}
const getList = () => {
  loading.value = true;
  if (dateRange.value && dateRange.value.length == 2) {
    queryParams.value.beginTime = dateRange.value[0];
    queryParams.value.endTime = dateRange.value[1];
  }
  getResourceList(queryParams.value).then(response => {
    resourceList.value = response.data.rows;
    resourceList.value.forEach(r => r.iconClass = getFileSvgIconClass(r.name))
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
}
const cancel = () => {
  open.value = false;
  reset();
}
const reset = () => {
  proxy.resetForm("formRef");
  form.value = {};
}
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
}
const resetQuery = () => {
  proxy.resetForm("queryFormRef");
  queryParams.value.beginTime = undefined;
  queryParams.value.endTime = undefined;
  dateRange.value = [];
  handleQuery();
}
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.resourceId)
}
const handleAdd = () => {
  reset();
  open.value = true;
  title.value = proxy.$t('CMS.Resource.AddDialogTitle');
}
const handleUpdate = (row) => {
  reset();
  const resourceId = row.resourceId || selectedIds.value
  getResourceDetail(resourceId).then(response => {
    form.value = response.data;
    title.value = proxy.$t('CMS.Resource.EditDialogTitle');
    open.value = true;
  });
}
const handleFileUploadProgress = (event, file, fileList) => {
  upload.isUploading = true;
}
const handleFileSuccess = (response, file, fileList) => {
  upload.isUploading = false;
  proxy.$modal.msgSuccess(response.msg);
  if (response.code == 200) {
    open.value = false;
    getList();
  }
  proxy.$refs.uploadRef.clearFiles();
  reset();
}
const handleUploadChange = (file) => {
  file.name = file.name.toLowerCase();
  form.value.name = file.name;
}
const handleBeforeUpload = (file) => {
  return true;
}
const submitForm = () => {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      proxy.$refs.uploadRef.submit();
    }
  });
}
const handleDelete = (row) => {
  const resourceIds = row.resourceId || selectedIds.value;
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(() => {
    return delResource(resourceIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
  });
}
</script>
<style lang="scss" scoped>
.upload-form-item {

  :deep(.el-form-item__content) {
    display: block!important;
  }
  .upload-limit-tip {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}
</style>