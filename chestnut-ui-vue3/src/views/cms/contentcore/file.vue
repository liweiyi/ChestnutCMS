<template>
  <div class="app-container cms-file-container">
    <el-container>
      <el-aside width="220">
        <el-scrollbar :style="treeSideStyle">
          <div class="treeRoot">
            {{ $t("CMS.File.ResourceRoot") }}
            <el-tooltip
              class="item"
              effect="dark"
              :content="resourceRoot"
              placement="top-start"
            >
              <svg-icon icon-class="question" />
            </el-tooltip>
          </div>
          <div class="divider"></div>
          <el-tree
            :data="directoryTreeData"
            :props="defaultProps"
            :expand-on-click-node="false"
            :filter-node-method="filterNode"
            v-loading="treeLoading"
            node-key="id"
            ref="treeRef"
            highlight-current
            @node-click="handleNodeClick"
          >
          </el-tree>
        </el-scrollbar>
      </el-aside>
      <el-container>
        <el-header style="height: 70px">
          <div class="btn-toolbar">
            <el-button
              type="primary"
              plain
              icon="Plus"
              :disabled="disableAdd"
              @click="handleAdd"
              >{{ $t("Common.Add") }}</el-button
            >
            <el-button
              type="primary"
              plain
              icon="Upload"
              :disabled="disableAdd"
              @click="handleUpload"
              >{{ $t("CMS.File.Upload") }}</el-button
            >
            <el-button
              type="danger"
              plain
              icon="Delete"
              :disabled="disableAdd || selectedRows.length==0"
              @click="handleDelete()"
              >{{ $t("Common.Delete") }}</el-button
            >
          </div>
          <el-card
            shadow="hover"
            class="directory-toolbar"
            style="padding: 0, 10px"
          >
            <span class="span-path">
              <el-button
                type="text"
                icon="Folder"
                @click="handlePathClick(-1)"
              ></el-button>
              <span class="path-spliter">/</span>
            </span>
            <span
              class="span-path"
              v-for="(item, index) in pathArray"
              :key="item"
            >
              <span v-if="index" class="path-spliter">/</span>
              <el-button
                type="text"
                @click="handlePathClick(index)"
                style="margin-left: 0"
                >{{ item }}</el-button
              >
            </span>
          </el-card>
        </el-header>
        <el-main>
          <el-table
            v-loading="loading"
            :data="fileList"
            @row-dblclick="handleEdit"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="50" align="center" />
            <el-table-column
              :label="$t('CMS.File.FileName')"
              align="left"
              prop="fileName"
            >
              <template #default="scope">
                <i v-if="scope.row.isDirectory" class="el-icon-folder"></i>
                <el-button
                  v-if="scope.row.isDirectory"
                  type="text"
                  @click="handleDirectoryClick(scope.row)"
                  >{{ scope.row.fileName }}</el-button
                >
                <span v-else
                  ><svg-icon :icon-class="scope.row.iconClass" />
                  {{ scope.row.fileName }}</span
                >
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('CMS.File.FileSize')"
              align="center"
              width="160"
              prop="fileSize"
            />
            <el-table-column
              :label="$t('CMS.File.ModifyTime')"
              align="center"
              width="200"
              prop="modifyTime"
            />
            <el-table-column
              :label="$t('Common.Operation')"
              align="center"
              width="180"
             
            >
              <template #default="scope">
                <el-popover
                  style="margin-right: 10px"
                  v-if="!disableAdd"
                  placement="top"
                  width="200"
                  v-model="scope.row.showRename"
                >
                  <el-input
                    v-model="scope.row.rename"
                    size="small"
                    :placeholder="$t('CMS.File.InputFileName')"
                  />
                  <div style="text-align: right; margin-top: 5px">
                    <el-button
                      type="text"
                      @click="scope.row.showRename = false"
                      >{{ $t("Common.Cancel") }}</el-button
                    >
                    <el-button
                      type="primary"
                      @click="handleRename(scope.row)"
                      >{{ $t("Common.Confirm") }}</el-button
                    >
                  </div>
                  <el-button
                    slot="reference"
                    type="text"
                    icon="Edit"
                    >{{ $t("CMS.File.Rename") }}</el-button
                  >
                </el-popover>
                <el-button
                  v-if="scope.row.canEdit"
                  type="text"
                  icon="Edit"
                  @click="handleEdit(scope.row)"
                  >{{ $t("Common.Edit") }}</el-button
                >
                <el-button
                  v-if="!disableAdd"
                  type="text"
                  icon="Delete"
                  @click="handleDelete(scope.row)"
                  >{{ $t("Common.Delete") }}</el-button
                >
              </template>
            </el-table-column>
          </el-table>
        </el-main>
      </el-container>
    </el-container>
    <!-- 添加文件或目录 -->
    <el-dialog
      :title="$t('CMS.File.AddTitle')"
      v-model="openAddDialog"
      width="500px"
      append-to-body
    >
      <el-form ref="formAddRef" :model="formAdd" :rules="rules" label-width="80px">
        <el-form-item :label="$t('CMS.File.Type')" prop="isDirectory">
          <el-radio-group v-model="formAdd.isDirectory">
            <el-radio-button :label="false">{{
              $t("CMS.File.File")
            }}</el-radio-button>
            <el-radio-button :label="true">{{
              $t("CMS.File.Directory")
            }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('CMS.File.Name')" prop="name">
          <el-input v-model="formAdd.fileName" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleAddSubmit">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleAddClose">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
    <!-- 上传文件对话框 -->
    <el-dialog
      :title="$t('CMS.File.UploadTitle')"
      v-model="openUploadDialog"
      width="400px"
      append-to-body
    >
      <el-form ref="uploadFormRef" :model="uploadForm">
        <el-upload
          ref="uploadRef"
          drag
          :data="uploadForm"
          :action="upload.url"
          :headers="upload.headers"
          :file-list="upload.fileList"
          :on-progress="handleFileUploadProgress"
          :on-success="handleFileSuccess"
          :auto-upload="false"
          :limit="1"
        >
          <template #default>
            <el-icon :size="36"><Upload /></el-icon>
            <div class="upload-tip">{{ $t("CMS.File.UploadTip") }}</div>
          </template>
          <template #tip>
            <div class="upload-limit-tip">{{ $t("CMS.Resource.UPloadTip", [upload.accept, fileSizeName]) }}</div>
          </template>
        </el-upload>
      </el-form>
      <template #footer>
        <el-button
          type="primary"
          :loading="upload.isUploading"
          @click="handleUploadSubmit"
          >{{ $t("Common.Confirm") }}</el-button
        >
        <el-button @click="handleUploadClose">{{
          $t("Common.Cancel")
        }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CmsFile">
import { fileNameValidator } from "@/utils/validate";
import { isImage, getFileSvgIconClass } from "@/utils/chestnut";
import {
  getDirectoryTreeData,
  getFileList,
  renameFile,
  addFile,
  deleteFile,
} from "@/api/contentcore/file";
import { getConfigKey } from "@/api/system/config";

const { proxy } = getCurrentInstance();

const treeSideHeight = ref(600);
const treeLoading = ref(false);
const loading = ref(false);
const selectedRows = ref([]);
const defaultProps = {
  children: "children",
  label: "label",
}
const resourceRoot = ref("");
const directoryTreeData = ref([]);
const selectedDirectory = ref("/");
const pathArr = ref([]);
const fileList = ref([]);
const fileName = ref("");
const openAddDialog = ref(false);
const rules = {
  fileName: [
    {
      required: true,
      message: proxy.$t("Common.RuleTips.NotEmpty"),
      trigger: "blur",
    },
    { validator: fileNameValidator, trigger: "change" },
  ],
};
const openUploadDialog = ref(false);

const objects = reactive({
  formAdd: {},
  uploadForm: {},
  upload: {
    acceptSize: 0,
    accept: "*",
    isUploading: false,
    headers: {
      ...proxy.$auth.getTokenHeader(),
      ...proxy.$cms.currentSiteHeader(),
    },
    url: import.meta.env.VITE_APP_BASE_API + "/cms/file/upload",
    fileList: [],
  },
});

const { formAdd, uploadForm, upload } = toRefs(objects);

const treeSideStyle = computed(() => {
  return { height: treeSideHeight.value + "px" };
});
const pathArray = computed(() => {
  if (selectedDirectory.value == "/") {
    return [];
  }
  return selectedDirectory.value.split("/");
});

const disableAdd = computed(() => {
  return selectedDirectory.value == "" || selectedDirectory.value == "/";
});

const fileSizeName = computed(() => {
  if (upload.value.acceptSize > 0) {
    return upload.value.acceptSize / 1024 / 1024 + " MB";
  } else {
    return "∞";
  }
});

watch(selectedDirectory, (newVal) => {
  pathArr.value = newVal.split("/");
});

onMounted(() => {
  let height = document.body.offsetHeight;
  treeSideHeight.value = height - 150;
  loadDirectory();
  loadFileList();
  getConfigKey("ResourceUploadAcceptSize").then((res) => {
    upload.value.acceptSize = parseInt(res.data);
  });
  getConfigKey("AllowUploadFileType").then((res) => {
    if (res.data && res.data.length > 0) {
      upload.value.accept = res.data;
    }
  });
});

const filterNode = (value, data) => {
  if (!value) return true;
  return data.label.indexOf(value) > -1;
}
const handleNodeClick = (data) => {
  selectedDirectory.value = data.id;
  loadFileList();
}
const handleDirectoryClick = (row) => {
  selectedDirectory.value = row.filePath + "/";
  loadFileList();
}
const handlePathClick = (index) => {
  if (index == -1) {
    selectedDirectory.value = "/";
  } else {
    selectedDirectory.value =
      pathArray.value.slice(0, index + 1).join("/") + "/";
  }
  loadFileList();
}
const loadDirectory = () => {
  treeLoading.value = true;
  getDirectoryTreeData().then((response) => {
    directoryTreeData.value = response.data.tree;
    resourceRoot.value = response.data.resourceRoot;
    treeLoading.value = false;
  });
}
const loadFileList = () => {
  if (selectedDirectory.value === "") {
    proxy.$modal.msgError(proxy.$t("Common.SelectFirst"));
    return;
  }
  loading.value = true;
  getFileList({
    filePath: selectedDirectory.value,
    fileName: fileName.value,
  }).then((response) => {
    fileList.value = response.data;
    fileList.value.forEach((f) => {
      f.isImage = isImage(f.fileName);
      f.iconClass = getFileSvgIconClass(f.fileName);
    });
  loading.value = false;
});
}
const handleQuery = () => {
  loadFileList();
}
const handleSelectionChange = (selection) => {
  selectedRows.value = selection.map((item) => item);
}
const handleAdd = () => {
  formAdd.value = { isDirectory: false, filePath: "" };
  openAddDialog.value = true;
}
const handleAddSubmit = () => {
  const params = {
    dir: selectedDirectory.value,
    fileName: formAdd.value.fileName,
    isDirectory: formAdd.value.isDirectory,
  };
  addFile(params).then((response) => {
    proxy.$modal.msgSuccess(proxy.$t("Common.OpSuccess"));
    openAddDialog.value = false;
    loadFileList();
  });
}
const handleAddClose = () => {
  openAddDialog.value = false;
}
const handleRename = (row) => {
  const params = {
    filePath: row.filePath,
    rename: row.rename,
    isDirectory: row.isDirectory,
  };
  renameFile(params).then((response) => {
    proxy.$modal.msgSuccess(proxy.$t("Common.SaveSuccess"));
    row.showRename = false;
    loadFileList();
  });
}
const handleEdit = (row) => {
  if (row.canEdit) {
    proxy.$router.push({
      path: "/cms/file/editor",
      query: {
        filePath: row.filePath,
      },
    });
  }
}
// TODO 裁剪图片
const handleCrop = (row) => {
  // TODO
}
const handleDelete = (row) => {
  const rows = row ? [row] : selectedRows.value;
  proxy.$modal.confirm(proxy.$t("Common.ConfirmDelete")).then(() => {
    deleteFile(rows).then(() => {
      proxy.$modal.msgSuccess(proxy.$t("Common.DeleteSuccess"));
      loadFileList();
    });
  });
}
const handleUpload = () => {
  openUploadDialog.value = true;
}
const handleUploadClose = () => {
  openUploadDialog.value = false;
}
const handleFileUploadProgress = (event, file, fileList) => {
  upload.value.isUploading = true;
}
const handleFileSuccess = (response, file, fileList) => {
  if (response.code == 200) {
    proxy.$modal.msgSuccess(proxy.$t("Common.SaveSuccess"));
    openUploadDialog.value = false;
    loadFileList();
  } else {
    proxy.$modal.msgError(response.msg);
  }
  upload.value.isUploading = false;
  proxy.$refs.uploadRef.clearFiles();
}
const handleUploadSubmit = () => {
  uploadForm.value.dir = selectedDirectory.value;
  proxy.$refs.uploadRef.submit();
}
</script>
<style lang="scss" scoped>
.cms-file-container {

  :deep(.el-card__body) {
    padding: 0 10px!important;
  }

  .el-aside {
    width: 220px;
    padding: 0;
    border-radius: 4px;
    border: 1px solid #e6ebf5;
    background-color: #fff;
  }

  .btn-toolbar {
    margin-bottom: 10px;

    .el-button {
      margin-right: 10px;
      margin-left: 0;
    }
  }

  .el-scrollbar__wrap {
    overflow-x: hidden;
  }

  .span-path {
    line-height: 30px;
  }

  .path-spliter {
    display: inline-block;
    padding: 0 5px;
  }

  .divider {
    background-color: #dcdfe6;
    position: relative;
    display: block;
    height: 1px;
    width: 100%;
    margin: 5px 0;
  }

  .treeRoot {
    padding: 0 10px;
    color: #515a6e;
    font-size: 14px;
  }

  .upload-form-item {

    :deep(.el-form-item__content) {
      display: block!important;
    }
  }
}

:deep(.upload-tip) {
  font-size: 14px;
  color: #515a6e;
  margin-top: 10px;
}

:deep(.upload-limit-tip) {
  font-size: 12px;
  color: #999;
  margin-top: 10px;
}
</style>
