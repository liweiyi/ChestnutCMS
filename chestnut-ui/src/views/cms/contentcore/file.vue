<template>
  <div class="app-container cms-file">
    <el-container>
      <el-aside width="220">
        <el-scrollbar :style="treeSideStyle">
          <div class="treeRoot">
            {{ $t("CMS.File.ResourceRoot") }}
            <el-tooltip class="item" effect="dark" :content="resourceRoot" placement="top-start">
              <svg-icon icon-class="question" />
            </el-tooltip>
          </div>
          <div class="divider"></div>
          <el-tree :data="directoryTreeData" 
                  :props="defaultProps" 
                  :expand-on-click-node="false"
                  :filter-node-method="filterNode"
                  v-loading="treeLoading"
                  node-key="id"
                  ref="tree"
                  highlight-current
                  @node-click="handleNodeClick">
          </el-tree>
        </el-scrollbar>
      </el-aside>
      <el-container>
        <el-header style="height: 70px;">
          <div class="btn-toolbar">
            <el-button 
              type="primary"
              size="mini"
              plain
              icon="el-icon-plus"
              :disabled="disableAdd"
              @click="handleAdd">{{ $t("Common.Add") }}</el-button>
            <el-button 
              type="primary"
              size="mini"
              plain
              icon="el-icon-upload2"
              :disabled="disableAdd"
              @click="handleUpload">{{ $t('CMS.File.Upload') }}</el-button>
            <el-button 
              type="danger"
              size="mini"
              plain
              icon="el-icon-delete"
              :disabled="disableAdd||multiple"
              @click="handleDelete()">{{ $t("Common.Delete") }}</el-button>
          </div>
          <el-card shadow='hover' class="directory-toolbar" style="padding: 0, 10px">
            <span class="span-path">
              <el-button type="text" icon="el-icon-folder" @click="handlePathClick(-1)"></el-button>
              <span class="path-spliter">/</span>
            </span>
            <span class="span-path" v-for="(item, index) in pathArray" :key="item" >
              <span v-if="index" class="path-spliter">/</span>
              <el-button type="text" @click="handlePathClick(index)" style="margin-left:0;">{{item}}</el-button>
            </span>
          </el-card>
        </el-header>
        <el-main>
          <el-table 
            v-loading="loading" 
            :data="fileList" 
            @row-dblclick="handleEdit"
            @selection-change="handleSelectionChange" 
            size="mini">
            <el-table-column type="selection" width="50" align="center" />
            <el-table-column :label="$t('CMS.File.FileName')" align="left" prop="fileName">
              <template slot-scope="scope">
                <i v-if="scope.row.isDirectory" class="el-icon-folder"></i> <el-button v-if="scope.row.isDirectory" type="text" @click="handleDirectoryClick(scope.row)">{{ scope.row.fileName }}</el-button>
                <span v-else><svg-icon :icon-class="scope.row.iconClass" /> {{ scope.row.fileName }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('CMS.File.FileSize')" align="center" width="160" prop="fileSize"/>
            <el-table-column :label="$t('CMS.File.ModifyTime')" align="center" width="200" prop="modifyTime"/>
            <el-table-column :label="$t('Common.Operation')" align="center" width="180" class-name="small-padding fixed-width">
              <template slot-scope="scope">
                <el-popover style="margin-right:10px;"
                  v-if="!disableAdd"
                  placement="top"
                  width="200"
                  v-model="scope.row.showRename">
                  <el-input v-model="scope.row.rename" size="mini" :placeholder="$t('CMS.File.InputFileName')" />
                  <div style="text-align: right; margin-top: 5px;">
                    <el-button size="mini" type="text" @click="scope.row.showRename = false">{{ $t('Common.Cancel') }}</el-button>
                    <el-button type="primary" size="mini" @click="handleRename(scope.row)">{{ $t('Common.Confirm') }}</el-button>
                  </div>
                  <el-button 
                    slot="reference" 
                    type="text"
                    icon="el-icon-edit"
                    size="small"
                  >{{ $t('CMS.File.Rename') }}</el-button>
                </el-popover>
                <el-button
                  v-if="scope.row.canEdit"
                  type="text"
                  icon="el-icon-edit"
                  size="small"
                  @click="handleEdit(scope.row)">{{ $t('Common.Edit') }}</el-button>
                <!-- <el-button
                          v-if="scope.row.isImage"
                          type="text"
                          icon="el-icon-crop"
                          size="small"
                          @click="handleCrop(scope.row)">{{ $t('CMS.Resource.Cut') }}</el-button> -->
                <el-button
                  v-if="!disableAdd"
                  type="text"
                  icon="el-icon-delete"
                  size="small"
                  @click="handleDelete(scope.row)">{{ $t("Common.Delete") }}</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-main>
      </el-container>
    </el-container>
    <!-- 添加文件或目录 -->
    <el-dialog 
      :title="$t('CMS.File.AddTitle')"
      :visible.sync="openAddDialog"
      width="500px"
      append-to-body>
      <el-form  
        ref="addForm"
        :model="addForm"
        :rules="rules"
        label-width="80px">
        <el-form-item :label="$t('CMS.File.Type')" prop="isDirectory">
          <el-radio-group v-model="addForm.isDirectory" size="medium">
            <el-radio-button :label="false">{{ $t('CMS.File.File') }}</el-radio-button>
            <el-radio-button :label="true">{{ $t('CMS.File.Directory') }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('CMS.File.Name')" prop="name">
          <el-input v-model="addForm.fileName" size="medium" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleAddSubmit">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleAddClose">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
    <!-- 上传文件对话框 -->
    <el-dialog 
      :title="$t('CMS.File.UploadTitle')"
      :visible.sync="openUploadDialog"
      width="400px"
      append-to-body>
      <el-form ref="uploadForm" :model="uploadForm">
        <el-form-item>
          <el-upload 
            ref="upload"
            drag
            :data="uploadForm"
            :action="upload.url"
            :headers="upload.headers"
            :file-list="upload.fileList"
            :on-progress="handleFileUploadProgress"
            :on-success="handleFileSuccess"
            :auto-upload="false"
            :limit="1">
            <i class="el-icon-upload"></i>
            <div class="el-upload__text">{{ $t('CMS.File.UploadTip') }}</div>
            <div slot="tip" class="el-upload__tip">{{ $t('CMS.Resource.UPloadTip', [ upload.accept, fileSizeName ]) }}</div>
          </el-upload>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :loading="upload.isUploading" @click="handleUploadSubmit">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleUploadClose">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getToken } from "@/utils/auth";
import { isImage, getFileSvgIconClass } from "@/utils/chestnut";
import { getDirectoryTreeData, getFileList, renameFile, addFile, deleteFile } from "@/api/contentcore/file";
import { getConfigKey } from "@/api/system/config";

export default {
  name: "CmsContentcoreFile",
  data () {
    return {
      treeSideHeight: 600,
      // 遮罩层
      treeLoading: false,
      loading: false,
      // 选中数组
      selectedRows: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      defaultProps: {
        children: "children",
        label: "label"
      },
      // 目录树
      resourceRoot: "",
      directoryTreeData: [],
      selectedDirectory: "/",
      pathArr: [],
      // 文件列表
      fileList: [],
      // 搜索文件名
      fileName: "",
      // 新建文件/目录弹窗
      openAddDialog: false,
      addForm: {},
      // 表单校验
      rules: {
        fileName: [
          { required: true, pattern: "^[A-Za-z0-9_\.]+$", message: this.$t('CMS.File.RuleTips.FileName'), trigger: "blur" }
        ]
      },
      openUploadDialog: false,
      uploadForm: {},
      // 上传参数
      upload: {
        acceptSize: 0,
        accept: "*",
        // 是否禁用上传
        isUploading: false,
        // 设置上传的请求头部
        headers: { 
          Authorization: "Bearer " + getToken(),
          CurrentSite: this.$cache.local.get("CurrentSite")
        },
        // 上传的地址
        url: process.env.VUE_APP_BASE_API + "/cms/file/upload",
        // 上传的文件列表
        fileList: []
      },
    };
  },
  computed: {
    treeSideStyle() {
      return { height: this.treeSideHeight + 'px' };
    },
    pathArray() {
      if (this.selectedDirectory == "/") {
        return [];
      }
      return this.selectedDirectory.split("/");
    },
    disableAdd() {
      return this.selectedDirectory == "" || this.selectedDirectory == "/"
    },
    fileSizeName() {
      if (this.upload.acceptSize > 0) {
        return this.upload.acceptSize / 1024 / 1024 + " MB"
      } else {
        return "∞";
      }
    }
  },
  watch: {
    selectedDirectory(newVal) {
      this.pathArr = newVal.split("/");
    }
  },
  created () {
    let height = document.body.offsetHeight;
    this.treeSideHeight = height - 150;
    this.loadDirectory();
    this.loadFileList();
    getConfigKey("ResourceUploadAcceptSize").then(res => {
      this.upload.acceptSize = parseInt(res.data);
    });
    getConfigKey("AllowUploadFileType").then(res => {
      if (res.data && res.data.length > 0) {
        this.upload.accept = res.data;
      }
    });
  },
  methods: {
    filterNode (value, data) {
      if (!value) return true;
      return data.label.indexOf(value) > -1;
    },
    handleNodeClick (data) {
      this.selectedDirectory = data.id;
      this.loadFileList();
    },
    handleDirectoryClick(row) {
      this.selectedDirectory = row.filePath + "/";
      this.loadFileList();
    },
    handlePathClick (index) {
      if (index == -1) {
        this.selectedDirectory = "/";
      } else {
        this.selectedDirectory = this.pathArray.slice(0, index + 1).join("/") + "/";
      }
      this.loadFileList();
    },
    loadDirectory() {
      this.treeLoading = true;
      getDirectoryTreeData().then(response => {
        this.directoryTreeData = response.data.tree;
        this.resourceRoot = response.data.resourceRoot
        this.treeLoading = false;
      });
    },
    loadFileList() {
      if (this.selectedDirectory === '') {
        this.$modal.msgError(this.$t('Common.SelectFirst'));
        return;
      }
      this.loading = true;
      getFileList({ filePath: this.selectedDirectory, fileName: this.fileName }).then(response => {
        this.fileList = response.data;
        this.fileList.forEach(f => {
            f.isImage = isImage(f.fileName);
            f.iconClass = getFileSvgIconClass(f.fileName);
        });
        this.loading = false;
      });
    },
    handleQuery () {
      this.loadFileList();
    },
    // 多选框选中数据
    handleSelectionChange (selection) {
      this.selectedRows = selection.map(item => item);
      this.single = selection.length != 1;
      this.multiple = !selection.length;
    },
    handleAdd () {
      this.addForm = { isDirectory: false, filePath: "" };
      this.openAddDialog = true;
    },
    handleAddSubmit () {
      const params = {
        dir: this.selectedDirectory,
        fileName: this.addForm.fileName,
        isDirectory: this.addForm.isDirectory
      };
      addFile(params).then(response => {
        this.$modal.msgSuccess(this.$t('Common.OpSuccess'));
        this.openAddDialog = false;
        this.loadFileList();
      });
    },
    handleAddClose () {
      this.openAddDialog = false;
    },
    handleRename (row) {
      const params = {
        filePath: row.filePath,
        rename: row.rename,
        isDirectory: row.isDirectory
      };
      renameFile(params).then(response => {
        this.$modal.msgSuccess(this.$t('Common.SaveSuccess'));
        row.showRename = false;
        this.loadFileList();
      });
    },
    handleEdit (row) {
      if (row.canEdit) {
        this.$router.push({ 
          path: "/cms/file/editor", 
          query: { 
            filePath: row.filePath 
          } 
        });
      }
    },
    // TODO 裁剪图片
    handleCrop (row) {

    },
    handleDelete (row) {
      const rows = row ? [ row ] : this.selectedRows
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function () {
        return deleteFile(rows);
      }).then(() => {
        this.$modal.msgSuccess(this.$t('Common.DeleteSuccess'));
        this.loadFileList();
      }).catch(function () { });
    },
    handleUpload () {
      this.openUploadDialog = true;
    },
    handleUploadClose () {
      this.openUploadDialog = false;
    },
    handleFileUploadProgress(event, file, fileList) {
      this.upload.isUploading = true;
    },
    handleFileSuccess(response, file, fileList) {
      if (response.code == 200) {
        this.$modal.msgSuccess(this.$t('Common.SaveSuccess'));
        this.open = false;
        this.loadFileList();
      } else {
        this.$modal.msgError(response.msg)
      }
      this.upload.isUploading = false;
      this.$refs.upload.clearFiles();
      this.resetForm("uploadForm");
    },
    handleUploadSubmit () {
      this.$refs["uploadForm"].validate(valid => {
        if (valid) {
          this.uploadForm.dir = this.selectedDirectory;
          this.$refs.upload.submit();
        }
      });
    }
  }
};
</script>
<style>
.cms-file .el-card__body {
  padding: 0 10px;
}
.cms-file .el-aside {
  width: 220px;
  padding: 0;
  border-radius: 4px;
  border: 1px solid #e6ebf5;
  background-color: #fff;
}
.cms-file .btn-toolbar {
  margin-bottom: 10px;
}
.cms-file .btn-toolbar .el-button {
  margin-right: 10px;
  margin-left: 0;
}
.cms-file .el-scrollbar__wrap {
  overflow-x: hidden;
}
.cms-file .span-path {
  line-height: 30px;
}
.cms-file .path-spliter {
  display: inline-block;
  padding: 0 5px;
}
.cms-file .divider {
  background-color: #DCDFE6;
  position: relative;
  display: block;
  height: 1px;
  width: 100%;
  margin: 5px 0;
}
.cms-file .treeRoot {
  padding: 0 10px;
  color: #515a6e;
  font-size: 14px;
}
</style>