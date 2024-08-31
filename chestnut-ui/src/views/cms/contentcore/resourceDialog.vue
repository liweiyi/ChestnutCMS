<template>
  <div class="app-container" style="padding: 0;">
    <el-dialog 
      class="resource-dialog"
      :title="$t('CMS.Resource.SelectorTitle')"
      :visible.sync="visible"
      width="1010px"
      :close-on-click-modal="false"
      append-to-body>
      <el-tabs v-model="activeName" @tab-click="handleTabClick">
        <el-tab-pane :label="$t('CMS.Resource.LocalUpload')" name="local">
          <el-form 
            ref="formUpload"
            :model="form_upload"
            v-loading="upload.isUploading"
            label-width="130px">
            <el-form-item :label="$t('CMS.Resource.Source')" prop="source">
               <el-radio-group v-model="form_upload.source" size="small">
                 <el-radio-button label="local">{{ $t('CMS.Resource.LocalUpload') }}</el-radio-button>
                 <el-radio-button label="net">{{ $t('CMS.Resource.RemoteLink') }}</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-show="showLocal" :label="$t('CMS.Resource.Upload')" prop="path">
              <el-upload
                ref="upload"
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
                <i slot="default" class="el-icon-plus"></i>
                <div slot="file" slot-scope="{file}">
                  <img v-if="isImageResource(file.name)" class="el-upload-list__item-thumbnail" :src="file.url" />
                  <svg-icon v-else :icon-class="getResourceFileIconClass(file.name)" />
                  <span class="el-upload-list__item-actions">
                    <span class="el-upload-list__item-delete" @click="handleRemoveFile(file)">
                      <i class="el-icon-delete"></i>
                    </span>
                  </span>
                </div>
                <div slot="tip" class="el-upload__tip">{{ $t('CMS.Resource.UPloadTip', [ upload.accept, fileSizeName ]) }}</div>
              </el-upload>
            </el-form-item>
            <el-form-item v-show="showNet" :label="$t('CMS.Resource.RemoteLink')" prop="path">
              <el-input v-model="form_upload.path" size="small" placeholder="http(s)://"></el-input>
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
                ref="tagInput"
                size="small"
                @keyup.enter.native="handleTagInputConfirm"
                @blur="handleTagInputConfirm">
              </el-input>
              <el-button v-else class="button-new-tag" size="small" @click="showTagInput">+ New Tag</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane :label="$t('CMS.Resource.MaterialLibrary')" name="resources">
          <el-container style="height: 500px; border: 1px solid #eee">
            <el-aside width="200px">
              <el-row>
                <el-link icon="el-icon-user" @click="loadMyResources">{{ $t('CMS.Resource.MyMaterial') }}</el-link>
              </el-row>
            </el-aside>
            <el-container>
              <el-header height="50px">
                <el-form :model="filterQuery"
                ref="queryForm"
                :inline="true"
                size="small"
                label-width="68px"
                class="el-form-search">
                  <el-form-item :label="$t('CMS.Resource.Name')" prop="name">
                    <el-input v-model="filterQuery.name" style="width: 170px;"></el-input>
                  </el-form-item>
                  <el-form-item :label="$t('Common.CreateTime')" style="margin-top:1px;">
                    <el-date-picker 
                      v-model="dateRange"
                      style="width: 240px"
                      value-format="yyyy-MM-dd"
                      type="daterange"
                      range-separator="-"
                      :start-placeholder="$t('Common.BeginDate')"
                      :end-placeholder="$t('Common.EndDate')"></el-date-picker>
                  </el-form-item>
                  <el-form-item>
                    <el-button 
                      type="primary"
                      icon="el-icon-search"
                      plain
                      @click="handleQuery">{{ $t("Common.Search") }}</el-button>
                    <el-button 
                      plain
                      icon="el-icon-refresh"
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
                  :page.sync="filterQuery.pageNum"
                  :limit.sync="filterQuery.pageSize"
                  @pagination="loadResources" />
              </el-footer>
            </el-container>
          </el-container>
        </el-tab-pane>
      </el-tabs>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :loading="upload.isUploading" @click="handleOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleCancel">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { isImage, getFileSvgIconClass } from "@/utils/chestnut";
import { getToken } from "@/utils/auth";
import { getResrouceList, getResourceTypes } from "@/api/contentcore/resource";
import { getConfigKey } from "@/api/system/config";
export default {
  name: "CMSResourceDialog",
  props: {
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
    }
  },
  data () {
    return {
      visible: false,
      activeName: 'local',
      siteId: this.$cache.local.get("CurrentSite"),
      form_upload: {
        source: 'local',
        tags: []
      },
      tagInputVisible: false,
      tagInputValue: '',
      // 上传参数
      upload: {
        isUploading: false, // 上传按钮loading
        accept: "", // 文件类型限制
        acceptSize: 0,
        limit: this.uploadLimit, // 文件数限制
        headers: { Authorization: "Bearer " + getToken(), CurrentSite: this.$cache.local.get("CurrentSite") },
        url: process.env.VUE_APP_BASE_API + "/cms/resource/upload", // 上传的地址
        fileList: [], // 上传的文件列表
        data : {} // 附带参数
      },
      uploadedCount: 0, // 已上传文件数量
      results: [], // 返回的上传/选择的文件结果

      loadingList: false,
      resourcesLoaded: false,
      resourceList: [],
      resourceTotal: 0,
      filterQuery: {
        pageNum: 1,
        pageSize: 10,
        resourceType: this.rtype,
        owner: false,
        name: undefined
      },
      dateRange: [],
    };
  },
  computed: {
    showLocal () {
      return this.form_upload.source=='local';
    },
    showNet () {
      return this.form_upload.source=='net'
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
    open (newVal) {
      this.visible = newVal;
    },
    visible (newVal) {
      if (!newVal) {
        this.noticeClose();
      } else {
        this.upload.isUploading = false;
        this.uploadedCount = 0;
      }
    },
    rtype (newVal) {
      this.loadResourceTypes();
    }
  },
  created() {
    this.loadResourceTypes();
    getConfigKey("ResourceUploadAcceptSize").then(res => {
      this.upload.acceptSize = parseInt(res.data);
    });
  },
  methods: {
    isImageResource(src) {
      return isImage(src);
    },
    getResourceFileIconClass(path) {
      return getFileSvgIconClass(path)
    },
    handleTabClick (tab, event) {
      if (this.activeName == "resources" && !this.resourcesLoaded) {
        this.loadResources();
      }
    },
    handleDeleteTag(tag) {
      this.form_upload.tags.splice(this.form_upload.tags.indexOf(tag), 1);
    },
    showTagInput() {
      this.tagInputVisible = true;
      this.$nextTick(_ => {
        this.$refs.tagInput.$refs.input.focus();
      });
    },
    handleTagInputConfirm() {
      let tagValue = this.tagInputValue;
      if (tagValue) {
        this.form_upload.tags.push(tagValue);
      }
      this.tagInputVisible = false;
      this.tagInputValue = '';
    },
    loadResourceTypes() {
      getResourceTypes().then(response => {
        response.data.forEach((item) => {
          if (item.id == this.rtype) {
            this.upload.accept = "." + item.accepts.replaceAll(",", ",.")
          }
        })
      });
    },
    loadMyResources () {
      this.filterQuery.owner = true;
      this.loadResources();
    },
    loadResources () {
      this.loadingList = true;
      if (this.dateRange && this.dateRange.length == 2) {
        this.filterQuery.beginTime = this.dateRange[0];
        this.filterQuery.endTime = this.dateRange[1];
      }
      this.filterQuery.resourceType = this.rtype || ''
      getResrouceList(this.filterQuery).then(response => {
        this.resourceList = response.data.rows;
        this.resourceList.forEach(r => this.$set(r,'selected',false));
        this.resourceTotal = parseInt(response.data.total);
        this.loadingList = false;
      });
    },
    handleResourceChecked(index) {
      this.$set(this.resourceList[index],'selected',!this.resourceList[index].selected)
    },
    /** 搜索按钮操作 */
    handleQuery () {
      this.filterQuery.pageNum = 1;
      this.loadResources();
    },
    /** 重置按钮操作 */
    resetQuery () {
      this.resetForm("queryForm");
      this.dateRange = [];
      this.filterQuery.owner = false;
      this.handleQuery();
    },
    handleRemoveFile (file) {
      for (var i = 0; i < this.upload.fileList.length; i++) {
        if (this.upload.fileList[i].uid == file.uid) {
          this.upload.fileList.splice(i, 1)
          break;
        }
      }
    },
    handleFileChange (file, fileList) {
      this.upload.fileList = fileList
    },
    handleFileBeforeUpload (file) {
      if (this.upload.acceptSize > 0 && file.size > this.upload.acceptSize) {
        this.$message.error(this.$t('CMS.Resource.UploadFileSizeLimit', [ this.fileSizeName ]));
        return false;
      }
      return true;
    },
    handleFileUloadExceed (files, fileList) {
      this.$modal.msgWarning(this.$t('CMS.Resource.UploadLimit', [ this.upload.limit ]));
    },
    handleFileUploadProgress (event, file, fileList) {
      this.upload.isUploading = true;
    },
    handleFileUploadSuccess (response, file, fileList) {
      this.onFileUploaded(response.code == 200, fileList, response.code == 200 ? response.data : response.msg);
    },
    handleFileUploadError (err, file, fileList) {
      this.onFileUploaded(false, fileList, "Upload failed.");
    },
    onFileUploaded(isSuccess, fileList, result) {
      if (isSuccess) {
        this.uploadedCount++;
        this.results.push({ 
          path: result.internalUrl, 
          name: result.name, 
          src: result.src, 
          width: result.width, 
          height: result.height, 
          fileSize: result.fileSize,
          fileSizeName: result.fileSizeName,
          resourceType: result.resourceType
        });
      } else {
        this.$modal.msgError(result);
      }
      if (this.uploadedCount == fileList.length) {
        this.noticeOk();
        this.upload.isUploading = false;
      }
    },
    handleOk () {
      if (this.activeName === "local") {
        if (this.form_upload.source === 'local') {
          Object.keys(this.form_upload).forEach(key => this.upload.data[key] = this.form_upload[key]);
          this.$refs.upload.submit();
        } else {
          const url = this.form_upload.path;
          if (!url || (!url.startsWith("http://") && !url.startsWith("https://"))) {
            this.$modal.msgError(this.$t('CMS.Resource.RemoteLinkErr'));
            return;
          }
          const name = url.substring(url.lastIndexOf("/") + 1);
          this.results.push({ 
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
          this.noticeOk();
        }
      } else {
        this.resourceList.forEach((item)=>{
          if (item.selected) {
            this.results.push({ 
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
        if (this.results.length == 0) {
          this.$modal.msgError(this.$t('Common.SelectFirst'));
          return;
        }
        this.noticeOk();
      }
    },
    handleCancel () {
      this.visible = false;
    },
    noticeOk () {
      if (this.visible) {
        this.$emit("ok", this.results);
        this.visible = false;
      }
    },
    noticeClose () {
      if (!this.visible) {
        this.$emit('update:open', false);
        this.$emit("close");
        this.reset();
      }
    },
    reset () {
        this.activeName = "local";
        this.form_upload = { source: 'local', tags: [] };
        this.tagInputVisible = false,
        this.tagInputValue = '',
        this.upload.fileList = [];
        this.upload.data = {};
        this.uploadedCount = 0;
        this.results = [];
        this.resourcesLoaded = false;
        this.resourceList = [];
        this.resourceTotal = 0;
        this.filterQuery.pageNum = 1;
        this.filterQuery.owner = false;
        this.filterQuery.name = undefined;
        this.dateRange = [];
    }
  }
};
</script>
<style>
.resource-dialog .el-upload-list__item .svg-icon {
  width: 66px;
  height: 66px;
}
.resource-dialog .el-aside {
  height: 500px;
}
.resource-dialog .el-header {
  padding: 5px;
  background-color: #f7f7f7;
}
.resource-dialog .el-main {
  overflow-y: hidden;
  background-color: #fff;
  padding: 0 4px;
}
.resource-dialog .el-card {
  width: 148px;
  text-align: center;
  float: left;
  border: none;
  padding: 0;
}
.resource-dialog .el-card__body {
  padding: 15px;
}
.resource-dialog .el-card .r-name {
  height: 28px;
  line-height: 28px;
  overflow: hidden;
}
.resource-dialog .el-card .item-img {
  width: 120px;
  height: 120px;
  background-color: #f7f7f7;
  cursor: pointer;
}
.resource-dialog .el-card .item-svg {
  width: 120px;
  height: 120px;
  background-color: #f7f7f7;
  cursor: pointer;
  padding: 20px;
}
.resource-dialog .el-tag {
  margin-right: 10px;
}
.resource-dialog .button-new-tag {
  height: 32px;
  line-height: 30px;
  padding-top: 0;
  padding-bottom: 0;
}
.resource-dialog .input-new-tag {
  width: 120px;
  vertical-align: bottom;
}
.resource-dialog .el-upload-list--picture-card .el-upload-list__item {
  width: 68px;
  height: 68px;
}
.resource-dialog .el-upload--picture-card {
  line-height: 78px;
  width: 68px;
  height: 68px;
}
</style>