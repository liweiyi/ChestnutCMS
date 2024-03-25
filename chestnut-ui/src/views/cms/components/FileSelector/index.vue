<template>
  <div class="app-container" style="padding: 0px;">
    <el-dialog 
      title="选择文件"
      :visible.sync="visible"
      width="1100px"
      :close-on-click-modal="false"
      custom-class="file-selector-dialog"
      append-to-body>
      <el-row style="margin-bottom:10px;">
        <el-input placeholder="请输入文件名" v-model="queryParams.fileName" class="input-path" @input="handleFilterFile">
          <div slot="prepend">
            <el-link icon="el-icon-folder" @click="handlePathClick(-1)"></el-link>
            <div class="path-spliter">/</div>
            <span class="span-path" v-for="(item, index) in pathArray" :key="item" >
              <el-link @click="handlePathClick(index)" v-if="index < pathArray.length - 1">{{item}}</el-link>
              <div class="path-spliter" v-if="index < pathArray.length - 1">/</div>
            </span>
          </div>
        </el-input>
      </el-row>
      <el-table v-loading="loading" 
        ref="tableFileList"
        :data="fileList"
        highlight-current-row
        @row-click="handleRowClick"
        @selection-change="handleSelectionChange"
      >
        <el-table-column v-if="multi" type="selection" width="50" align="center" />
        <el-table-column :label="$t('CMS.File.FileName')" align="left" prop="fileName">
          <template slot-scope="scope">
            <i v-if="scope.row.isDirectory" class="el-icon-folder"></i> <el-button v-if="scope.row.isDirectory" type="text" @click="handleDirectoryClick(scope.row)">{{ scope.row.fileName }}</el-button>
            <span v-else><svg-icon :icon-class="scope.row.iconClass" /> {{ scope.row.fileName }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('CMS.File.FileSize')" align="center" width="160" prop="fileSize"/>
        <el-table-column :label="$t('CMS.File.ModifyTime')" align="center" width="200" prop="modifyTime"/>
      </el-table>
      
      <div slot="footer"
            class="dialog-footer">
        <el-button type="primary" 
                    @click="handleOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleClose">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getFileList } from "@/api/contentcore/file";
import { isImage, getFileSvgIconClass } from "@/utils/chestnut";

export default {
  name: "CMSFileSelector",
  props: {
    open: {
      type: Boolean,
      default: false,
      required: true
    },
    multi: {
      type: Boolean,
      default: false,
      required: false
    },
    path: {
      type: String,
      default: '',
      required: false
    },
    suffix: {
      type: String,
      default: '',
      required: false
    }
  },
  watch: {
    path (newVal) {
      console.log("path", newVal)
      this.queryParams.filePath = newVal
    },
    open () {
      this.visible = this.open;
    },
    suffix (newVal) {
      console.log("suffix", newVal)
      this.queryParams.fileName = "." + newVal
    },
    visible (newVal) {
      if (!newVal) {
        this.handleClose();
      } else {
        this.loadFileList();
      }
    }
  },
  computed: {
    pathArray() {
      console.log(this.queryParams.filePath)
      if (this.queryParams.filePath == "/") {
        return [];
      }
      return this.queryParams.filePath.split("/");
    },
  },
  data () {
    return {
      loading: false,
      visible: this.open,
      selectedFiles: [],
      fileList: [],
      sourceFileList: [],
      queryParams: {
        filePath: '/',
        fileName: ''
      }
    };
  },
  methods: {
    loadFileList () {
      if (!this.visible) {
        return;
      }
      console.log("suffix", this.suffix)
      this.loading = true;
      getFileList(this.queryParams).then(response => {
        this.sourceFileList = response.data;
        this.sourceFileList.forEach(f => {
            f.isImage = isImage(f.fileName);
            f.iconClass = getFileSvgIconClass(f.fileName);
        });
        this.fileList = this.sourceFileList;
        this.loading = false;
      });
    },
    handleDirectoryClick(row) {
      this.queryParams.filePath = row.filePath + "/";
      this.loadFileList();
    },
    handlePathClick (index) {
      if (index == -1) {
        this.queryParams.filePath = "/";
      } else {
        this.queryParams.filePath = this.pathArray.slice(0, index + 1).join("/") + "/";
      }
      this.loadFileList();
    },
    handleRowClick (row) {
      this.selectedFiles.forEach(row => {
          this.$refs.tableFileList.toggleRowSelection(row, false);
      });
      this.selectedFiles = [];
      this.$refs.tableFileList.toggleRowSelection(row);
    },
    handleSelectionChange (selection) {
      this.selectedFiles = selection;
    },
    handleOk () {
      this.$emit("ok", this.selectedFiles);
    },
    handleClose () {
      this.$emit("close");
      this.resetForm("queryForm");
      this.queryParams.filePath = '/'
      this.selectedFiles = [];
      this.fileList = [];
    },
    handleFilterFile() {
      this.fileList = this.sourceFileList.filter(data => {
        if (!this.queryParams.fileName || this.queryParams.fileName.length == 0) {
          return true;
        }
        if (!data.fileName.toLowerCase().includes(this.queryParams.fileName.toLowerCase())) {
          return false;
        }
        return true;
      });
    },
    handleQuery () {
      this.loadFileList();
    },
    handleReset () {
      this.resetForm("queryForm");
      this.handleQuery();
    }
  }
};
</script>
<style>
.file-selector-dialog .el-dialog__body {
  padding: 0px 20px;
}
.file-selector-dialog .input-path .el-input-group__prepend {
  width: 100%;
}
.file-selector-dialog .input-path .el-input__inner {
  width: 160px;
}
.file-selector-dialog .path-spliter {
  display: inline-block;
  margin: 0 8px;
  vertical-align: middle;
  position: relative;
}
</style>