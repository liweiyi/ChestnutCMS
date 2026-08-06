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

    <div class="resource-grid" v-loading="loading">
      <el-card
        shadow="hover"
        class="resource-card"
        :body-class="['resource-card-body', r.selected ? 'selected' : '']"
        v-for="(r, index) in resourceList"
        :key="r.resourceId"
        @click="handleCardClick(index)">
        <div class="card-preview">
          <el-image v-if="isImageResource(r.src)" class="item-img" fit="scale-down" :src="r.src" hide-on-click-modal></el-image>
          <svg-icon v-else :icon-class="r.iconClass" class="item-svg" />
        </div>
        <div class="card-info">
          <div class="r-name" :title="r.name">{{ r.name }}</div>
          <div class="r-meta">
            <span class="r-type">{{ r.resourceTypeName }}</span>
            <span class="r-size">{{ r.fileSizeName }}</span>
          </div>
        </div>
        <div class="card-actions" @click.stop>
          <el-button link type="primary" icon="View" :title="$t('Common.Preview')" @click="handlePreview(r)" />
          <el-button link type="primary" icon="Edit" :title="$t('Common.Edit')" @click="handleUpdate(r)" />
          <el-button link type="danger" icon="Delete" :title="$t('Common.Delete')" @click="handleDelete(r)" />
        </div>
      </el-card>
    </div>
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
import { isImage, getFileSvgIconClass } from "@/utils/chestnut";
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
    pageSize: 24,
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
    resourceList.value.forEach(r => {
      r.iconClass = getFileSvgIconClass(r.name)
      r.selected = false
    })
    selectedIds.value = []
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
const isImageResource = (src) => {
  return isImage(src);
}
const handlePreview = (row) => {
  // 在独立浏览器标签页打开预览（不在后台 tab 组件中新增 tab）
  const routeData = proxy.$router.resolve({
    path: '/cms/resource/preview',
    query: { iurl: row.internalUrl }
  });
  window.open(routeData.href, '_blank');
}
const handleCardClick = (index) => {
  const r = resourceList.value[index];
  r.selected = !r.selected;
  selectedIds.value = resourceList.value.filter(item => item.selected).map(item => item.resourceId);
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
.resource-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  min-height: 300px;
  margin-top: 12px;
  padding: 4px;

  .resource-card {
    width: calc((100% - 84px) / 8);
    cursor: pointer;
    border: none;
    transition: all 0.2s;

    :deep(.resource-card-body) {
      padding: 10px;
      border: 2px solid transparent;
      border-radius: var(--el-card-border-radius);
      position: relative;

      &.selected {
        border-color: #319766 !important;
      }
      &.selected:before {
        position: absolute;
        right: -2px;
        top: -2px;
        content: '';
        width: 0;
        height: 0;
        border: 14px solid #fff;
        border-top-color: #319766;
        border-right-color: #319766;
        z-index: 1;
      }
      &.selected:after {
        content: "";
        position: absolute;
        right: 2px;
        top: 2px;
        width: 14px;
        height: 14px;
        background-image: var(--cc-icon-check);
        background-size: contain;
        background-repeat: no-repeat;
        z-index: 2;
      }

      .card-preview {
        display: flex;
        align-items: center;
        justify-content: center;
        height: 90px;
        background-color: #f7f7f7;
        border-radius: 4px;
        overflow: hidden;

        .item-img {
          width: 100%;
          height: 100%;
        }

        .item-svg {
          width: 56px;
          height: 56px;
          padding: 6px;
        }
      }

      .card-info {
        padding: 8px 2px 4px;

        .r-name {
          font-size: 13px;
          line-height: 20px;
          height: 20px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          text-align: center;
          color: #333;
        }

        .r-meta {
          display: flex;
          justify-content: center;
          gap: 8px;
          margin-top: 4px;
          font-size: 12px;
          color: #999;

          .r-type {
            color: var(--el-color-primary);
          }
        }
      }

      .card-actions {
        display: flex;
        justify-content: center;
        border-top: 1px solid #f0f0f0;
        padding-top: 6px;
        margin-top: 4px;
      }
    }
  }
}

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