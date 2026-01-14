<template>
  <div class="app-container" style="padding: 0;">
    <el-dialog 
      class="resource-dialog"
      :title="$t('CMS.Resource.SelectorTitle')"
      v-model="visible"
      width="1010px"
      :close-on-click-modal="false"
      append-to-body>
      <el-tabs v-model="activeName" @tab-click="handleTabClick">
        <el-tab-pane :label="$t('CMS.Resource.LocalUpload')" name="local">
          <el-form 
            ref="formUploadRef"
            :model="form_upload"
            v-loading="upload.isUploading"
            label-width="130px">
            <el-form-item :label="$t('CMS.Resource.Source')" prop="source">
               <el-radio-group v-model="form_upload.source">
                 <el-radio-button label="local">{{ $t('CMS.Resource.LocalUpload') }}</el-radio-button>
                 <el-radio-button label="net" v-if="enableNet">{{ $t('CMS.Resource.RemoteLink') }}</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-show="showLocal" :label="$t('CMS.Resource.Upload')" prop="path">
              <el-upload
                ref="uploadRef"
                list-type="picture-card"
                :accept="upload.accept"
                :limit="upload.limit"
                :multiple="upload.limit>1"
                :action="upload.url"
                :headers="upload.headers"
                :data="upload.data"
                :file-list="upload.fileList"
                :before-upload="handleFileBeforeUpload"
                :on-progress="handleFileUploadProgress"
                :on-success="handleFileUploadSuccess"
                :on-error="handleFileUploadError"
                :on-exceed="handleFileUloadExceed"
                :on-change="handleFileChange"
                :auto-upload="false">
                <template #default>
                  <el-icon><Plus /></el-icon>
                </template>
                <template #file="{file}" style="width: 68px;height: 68px;">
                  <div class="preview-wrap">
                  <div class="image-wrap">
                    <img v-if="isImageResource(file.name)" :src="file.url" style="max-width: 100%;max-height: 100%;" />
                    <svg-icon v-else :icon-class="getResourceFileIconClass(file.name)" />
                  </div>
                  <span class="el-upload-list__item-actions">
                    <span class="el-upload-list__item-delete" @click="handleRemoveFile(file)">
                      <el-icon><Delete /></el-icon>
                    </span>
                  </span>
                  </div>
                </template>
                <template #tip>
                  <div class="el-upload__tip">{{ $t('CMS.Resource.UPloadTip', [ upload.accept, $tools.formatSize(upload.acceptSize) ]) }}</div>
                </template>
              </el-upload>
            </el-form-item>
            <el-form-item v-show="showNet" :label="$t('CMS.Resource.RemoteLink')" prop="path">
              <el-input v-model="form_upload.path" placeholder="http(s)://"></el-input>
            </el-form-item>
            <el-form-item :label="$t('CMS.Resource.Tag')" prop="tags">
              <el-tag
                :key="tag"
                v-for="tag in form_upload.tags"
                closable
                :disable-transitions="false"
                @close="handleDeleteTag(tag)">
                {{tag}}
              </el-tag>
              <el-input
                class="input-new-tag"
                v-if="tagInputVisible"
                v-model="tagInputValue"
                ref="tagInputRef"
                size="small"
                @keyup.enter="handleTagInputConfirm"
                @blur="handleTagInputConfirm">
              </el-input>
              <el-button v-else class="button-new-tag" @click="showTagInput">+ New Tag</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane :label="$t('CMS.Resource.MaterialLibrary')" name="resources">
          <el-container style="height: 500px; border: 1px solid #eee">
            <el-aside width="200px">
              <el-row>
                <el-link icon="User" @click="loadMyResources">{{ $t('CMS.Resource.MyMaterial') }}</el-link>
              </el-row>
            </el-aside>
            <el-container>
              <el-header height="50px">
                <el-form :model="filterQuery"
                ref="queryFormRef"
                :inline="true"
                label-width="68px"
                class="el-form-search">
                  <el-form-item :label="$t('CMS.Resource.Name')" prop="name">
                    <el-input v-model="filterQuery.name" style="width: 170px;"></el-input>
                  </el-form-item>
                  <el-form-item :label="$t('Common.CreateTime')" style="margin-top:1px;">
                    <el-date-picker 
                      v-model="dateRange"
                      style="width: 240px"
                      value-format="YYYY-MM-DD"
                      type="daterange"
                      range-separator="-"
                      :start-placeholder="$t('Common.BeginDate')"
                      :end-placeholder="$t('Common.EndDate')"></el-date-picker>
                  </el-form-item>
                  <el-form-item>
                    <el-button 
                      type="primary"
                      icon="Search"
                      plain
                      @click="handleQuery">{{ $t("Common.Search") }}</el-button>
                    <el-button 
                      plain
                      icon="Refresh"
                      @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
                    <!-- <el-button 
                      plain
                      icon="el-icon-scissors"
                      @click="alert('没空做')">{{ $t('CMS.Resource.Cut') }}{{</el-button> -->
                  </el-form-item>
                </el-form>
              </el-header>
              <el-main v-loading="loadingList" style="overflow-y: scroll;">
                <el-card shadow="never" v-for="(r, index) in resourceList" :key="r.resourceId">
                  <el-image v-if="isImageResource(r.src)" class="item-img" fit="scale-down" :src="r.src" @click="handleResourceChecked(index)"></el-image>
                  <svg-icon v-else :icon-class="getResourceFileIconClass(r.path)" class="item-svg" />
                  <div class="r-name" :title="r.name"><el-checkbox v-model="r.selected">{{ r.name }}</el-checkbox></div>
                </el-card>
              </el-main>
              <el-footer>
                <pagination 
                  v-show="resourceTotal>0"
                  :total="resourceTotal"
                  v-model:page="filterQuery.pageNum"
                  v-model:limit="filterQuery.pageSize"
                  @pagination="loadResources" />
              </el-footer>
            </el-container>
          </el-container>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="upload.isUploading" @click="handleOk">{{ $t("Common.Confirm") }}</el-button>
          <el-button @click="handleCancel">{{ $t("Common.Cancel") }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CMSResourceDialog">
import { isImage, getFileSvgIconClass } from "@/utils/chestnut";
import { getResourceSelectList, getResourceTypes } from "@/api/contentcore/resource";
import { getConfigKey } from "@/api/system/config";

const { proxy, emit } = getCurrentInstance();

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
    required: true
  },
  rtype: {
    type: String,
    required: false,
    default: "file"
  },
  uploadLimit: {
    type: Number,
    default: 1,
    required: false
  },
  siteId: {
    type: String,
    default: "0",
    required: false,
  },
  enableNet: {
    type: Boolean,
    default: true,
    required: false
  }
});

const visible = ref(false);
const activeName = ref('local');
const form_upload = reactive({
  source: 'local',
  tags: []
});

const tagInputVisible = ref(false);
const tagInputValue = ref('');
const currentSiteId = ref(proxy.$cms.getCurrentSite());

// 上传参数
const upload = reactive({
  isUploading: false, // 上传按钮loading
  accept: "", // 文件类型限制
  acceptSize: 0,
  limit: props.uploadLimit, // 文件数限制
  headers: { ...proxy.$auth.getTokenHeader(), ...proxy.$cms.currentSiteHeader() },
  url: import.meta.env.VITE_APP_BASE_API + "/cms/resource/upload", // 上传的地址
  fileList: [], // 上传的文件列表
  data : {} // 附带参数
});

const uploadedCount = ref(0); // 已上传文件数量
const results = ref([]); // 返回的上传/选择的文件结果

const loadingList = ref(false);
const resourcesLoaded = ref(false);
const resourceList = ref([]);
const resourceTotal = ref(0);
const filterQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  resourceType: props.rtype,
  owner: false,
  name: undefined,
  siteId: '0'
});
const dateRange = ref([]);

const showLocal = computed(() => {
  return form_upload.source == 'local';
});

const showNet = computed(() => {
  return form_upload.source == 'net'
});

function isImageResource(src) {
  return isImage(src);
}

function getResourceFileIconClass(path) {
  return getFileSvgIconClass(path)
}

function handleTabClick(tab, event) {
  if (tab.paneName == "resources" && !resourcesLoaded.value) {
    loadResources();
  }
}

function handleDeleteTag(tag) {
  form_upload.tags.splice(form_upload.tags.indexOf(tag), 1);
}

function showTagInput() {
  tagInputVisible.value = true;
  nextTick(_ => {
    proxy.$refs.tagInputRef.$refs.input.focus();
  });
}

function handleTagInputConfirm() {
  let tagValue = tagInputValue.value;
  if (tagValue) {
    form_upload.tags.push(tagValue);
  }
  tagInputVisible.value = false;
  tagInputValue.value = '';
}

function loadResourceTypes() {
  getResourceTypes().then(response => {
    response.data.forEach((item) => {
      if (item.id == props.rtype) {
        upload.accept = "." + item.accepts.replaceAll(",", ",.")
      }
    })
  });
}

function loadMyResources() {
  filterQuery.owner = true;
  loadResources();
}

function loadResources() {

  loadingList.value = true;
  if (dateRange.value && dateRange.value.length == 2) {
    filterQuery.beginTime = dateRange.value[0];
    filterQuery.endTime = dateRange.value[1];
  }
  filterQuery.resourceType = props.rtype || ''
  getResourceSelectList(filterQuery).then(response => {
    resourceList.value = response.data.rows;
    resourceList.value.forEach(r => r.selected = false);
    resourceTotal.value = parseInt(response.data.total);
    loadingList.value = false;
    resourcesLoaded.value = true;
  });
}

function handleResourceChecked(index) {
  resourceList.value[index].selected = !resourceList.value[index].selected;
}

function handleQuery() {
  filterQuery.pageNum = 1;
  loadResources();
}

function resetQuery() {
  proxy.resetForm("queryFormRef");
  dateRange.value = [];
  filterQuery.owner = false;
  handleQuery();
}

function handleRemoveFile(file) {
  for (var i = 0; i < upload.fileList.length; i++) {
    if (upload.fileList[i].uid == file.uid) {
      upload.fileList.splice(i, 1)
      break;
    }
  }
}

function handleFileChange(file, fileList) {
  upload.fileList = fileList
}

function handleFileBeforeUpload(file) {
  if (upload.acceptSize > 0 && file.size > upload.acceptSize) {
    proxy.$message.error(proxy.$t('CMS.Resource.UploadFileSizeLimit', [ fileSizeName.value ]));
    return false;
  }
  return true;
}

function handleFileUloadExceed(files, fileList) {
  proxy.$modal.msgWarning(proxy.$t('CMS.Resource.UploadLimit', [ upload.limit ]));
}

function handleFileUploadProgress(event, file, fileList) {
  upload.isUploading = true;
}

function handleFileUploadSuccess(response, file, fileList) {
  onFileUploaded(response.code == 200, fileList, response.code == 200 ? response.data : response.msg);
}

function handleFileUploadError(err, file, fileList) {
  onFileUploaded(false, fileList, "Upload failed.");
}

function onFileUploaded(isSuccess, fileList, result) {
  if (!isSuccess) {
    proxy.$modal.msgError(result);
    upload.isUploading = false;
    return;
  }
  uploadedCount.value++;
  if (uploadedCount.value == fileList.length) {
    results.value = upload.fileList.map(f => {
      return { 
        path: f.response.data.internalUrl, 
        name: f.response.data.name, 
        src: f.response.data.src, 
        width: f.response.data.width, 
        height: f.response.data.height, 
        fileSize: f.response.data.fileSize,
        fileSizeName: f.response.data.fileSizeName,
        resourceType: f.response.data.resourceType
      }
    })
    noticeOk();
    upload.isUploading = false;
  }
}

function handleOk() {
  if (activeName.value === "local") {
    if (form_upload.source === 'local') {
      Object.keys(form_upload).forEach(key => upload.data[key] = form_upload[key]);
      proxy.$refs.uploadRef.submit();
    } else {
      const url = form_upload.path;
      if (!url || (!url.startsWith("http://") && !url.startsWith("https://"))) {
        proxy.$modal.msgError(proxy.$t('CMS.Resource.RemoteLinkErr'));
        return;
      }
      const name = url.substring(url.lastIndexOf("/") + 1);
      results.value.push({ 
        path: url, 
        name: name, 
        src: url, 
        width: 0, 
        height: 0, 
        fileSize: 0,
        fileSizeName: "",
        resourceType: "unknown",
        net: true
      });
      noticeOk();
    }
  } else {
    resourceList.value.forEach((item)=>{
      if (item.selected) {
        results.value.push({ 
          path: item.internalUrl, 
          name: item.name, 
          src: item.src, 
          width: item.width, 
          height: item.height, 
          fileSize: item.fileSize,
          fileSizeName: item.fileSizeName,
          resourceType: item.resourceType
        });
      }
    });
    if (results.value.length == 0) {
      proxy.$modal.msgError(proxy.$t('Common.SelectFirst'));
      return;
    }
    noticeOk();
  }
}

function handleCancel() {
  visible.value = false;
}

function noticeOk() {
  if (visible.value) {
    emit("ok", results.value);
    visible.value = false;
  }
}

function noticeClose() {
  if (!visible.value) {
    emit('update:open', false);
    emit("close");
    reset();
  }
}

function reset() {
  activeName.value = "local";
  Object.assign(form_upload, { source: 'local', tags: [] });
  tagInputVisible.value = false;
  tagInputValue.value = '';
  upload.fileList = [];
  upload.data = {};
  uploadedCount.value = 0;
  results.value = [];
  resourcesLoaded.value = false;
  resourceList.value = [];
  resourceTotal.value = 0;
  filterQuery.pageNum = 1;
  filterQuery.owner = false;
  filterQuery.name = undefined;
  dateRange.value = [];
}

watch(() => props.open, (newVal) => {
  visible.value = newVal;
});

watch(visible, (newVal) => {
  if (!newVal) {
    noticeClose();
  } else {
    upload.isUploading = false;
    uploadedCount.value = 0;
  }
});

watch(() => props.rtype, (newVal) => {
  loadResourceTypes();
});

watch(() => props.siteId, (newVal) => {
  filterQuery.siteId = newVal;
  if (newVal != '0') {
    currentSiteId.value = newVal;
  }
});

onMounted(() => {
  loadResourceTypes();
  getConfigKey("ResourceUploadAcceptSize").then(res => {
    upload.acceptSize = parseInt(res.data);
  });
});
</script>
<style scoped lang="scss">
.resource-dialog {

  .el-header {
    padding: 5px;
    background-color: #f7f7f7;
  }

  .el-aside {
    height: 500px;
  }

  .el-main {
    overflow-y: hidden;
    background-color: #fff;
    padding: 0 4px;
  }

  .el-card {
    width: 148px;
    text-align: center;
    float: left;
    border: none;
    padding: 0;

    .el-card__body {
      padding: 15px;

      .r-name {
        height: 28px;
        line-height: 28px;
        overflow: hidden;
      }

      .item-img {
        width: 120px;
        height: 120px;
        background-color: #f7f7f7;
        cursor: pointer;
      }

      .item-svg {
        width: 120px;
        height: 120px;
        background-color: #f7f7f7;
        cursor: pointer;
        padding: 20px;
      }
    }
  }

  .el-upload-list__item  {
    .svg-icon {
      width: 66px;
      height: 66px;
    }
    .preview-wrap {
      .image-wrap {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 66px;
        height: 66px;
      }
    }
  }

  .el-tag {
    margin-right: 10px;
  }

  .button-new-tag {
    height: 32px;
    line-height: 30px;
    padding-top: 0;
    padding-bottom: 0;
  }

  .input-new-tag {
    width: 120px;
    vertical-align: bottom;
  }
}
:deep(.el-upload-list__item) {
  width: 68px;
  height: 68px;
}
:deep(.el-upload.el-upload--picture-card) {
  line-height: 78px;
  width: 68px;
  height: 68px;
}
</style>