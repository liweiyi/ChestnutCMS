<template>
   <!-- 此组件上传图片需指定action，上传的图片不作为网站素材库资源记录，一般用来上传固定路径图片，比如用户头像等 -->
  <div class="simple-image-viewer" :style="{'width':imgWidth,'height':viewerHeight}">
    <el-upload
      ref="upload"
      :show-file-list="false"
      :accept="upload.accept"
      :limit="upload.limit"
      :multiple="false"
      :action="upload.url"
      :headers="upload.headers"
      :data="upload.data"
      :file-list="upload.fileList"
      :before-upload="handleBeforeUpload"
      :on-success="handleFileUploadSuccess"
      :on-error="handleFileUploadError"
      auto-upload>
      <div class="picture"
          v-show="showImage">
        <el-image :src="imageSrc"  
                  :style="{'width':imgWidth,'height':imgHeight}"
                  fit="scale-down">
        </el-image>
        <el-image-viewer v-if="showImageViewer" 
                        :on-close="handleImageViewerClose"
                        :url-list="imageViewerList"
                        style="z-index:9999">
        </el-image-viewer>
        <div class="toolbar">
          <el-tooltip class="item" effect="dark" :content="$t('Common.View')" placement="top">
            <i class="el-icon-search" @click="handleView" />
          </el-tooltip>
          <el-tooltip class="item" effect="dark" :content="$t('Common.Remove')" placement="top">
            <i class="el-icon-delete" @click="handleRemove" />
          </el-tooltip>
        </div>
      </div>
      <div v-show="showText"
          class="no-picture"
          :style="{'width':noWidth,'height':noHeight,'font-size':svgSize}">
        <svg-icon icon-class="upload"></svg-icon>
      </div>
      <div slot="tip" class="el-upload__tip" style="margin:10px 0">{{ $t('Component.CommonImageViewer.Tips') }}</div>
    </el-upload>
  </div>
</template>
<script>
import { getToken } from "@/utils/auth";
import ElImageViewer from "element-ui/packages/image/src/image-viewer"

export default {
  name: "CommonImageView",
  components: {
    "el-image-viewer": ElImageViewer
  },
  model: {
    prop: 'path',
    event: 'change'
  },
  props: {
    path: {
      type: String,
      default: undefined,
      required: false,
    },
    src: {
      type: String,
      default: "",
      required: false,
    },
    action: {
      type: String,
      default: "",
      required: false,
    },
    data: {
      type: Object,
      default: function() {
        return {};
      },
      required: false,
    },
    sizelimit: {
      type: Number,
      default: 2048,
      required: false,
    },
    width: {
      type: Number,
      default: 218,
      required: false,
    },
    height: {
      type: Number,
      default: 150,
      required: false,
    }
  },
  computed: {
    imgWidth () {
      return this.width + 'px';
    },
    imgHeight () {
      return this.height + 'px';
    },
    viewerHeight () {
      return (this.height + 10) + 'px';
    },
    noWidth () {
      return (this.width) + 'px';
    },
    noHeight () {
      return (this.height) + 'px';
    },
    svgSize () {
      return (this.height - 5) + 'px';
    },
    showImage () {
      return this.imagePath != undefined && this.imagePath != null && this.imagePath.length > 0;
    },
    showText() {
      return this.imagePath == undefined || this.imagePath == null || this.imagePath.length == 0;
    }
  },
  watch: {
    site(newVal) {
      this.upload.data.siteId = newVal;
    },
    path(newVal) {
      this.imagePath = newVal;
    },
    src(newVal) {
      this.imageSrc = process.env.VUE_APP_BASE_API + newVal;
    },
    imagePath(newVal) {
      this.$emit("change", newVal);
    },
    imageSrc(newVal) {
      if (newVal && newVal.length > 0) {
        this.imageViewerList = [ newVal ];
      } else {
        this.imageViewerList.splice(0);
      }
    }
  },
  data () {
    return {
      imagePath: this.path,
      imageSrc: process.env.VUE_APP_BASE_API + this.src,
      showImageViewer: false,
      imageViewerList: [],
      upload: {
        accept: ".jpg,.jpeg,.png",
        limit: 1,
        filesize: this.sizelimit * 1024,
        headers: { Authorization: "Bearer " + getToken() },
        url: process.env.VUE_APP_BASE_API + this.action, //"/cms/site/upload_watermarkimage",
        fileList: [],
        data : this.data
      },
    };
  },
  methods: {
    handleBeforeUpload(file) {
      if (this.upload.accept) {
        const fileName = file.name.split('.');
        const fileExt = "." + fileName[fileName.length - 1];
        const isTypeOk = this.upload.accept.split(',').indexOf(fileExt) >= 0;
        if (!isTypeOk) {
          this.$modal.msgError(this.$t('Common.InvalidFileSuffix', [ this.upload.accept ]));
          return false;
        }
      }
      if (file.size > this.upload.filesize) {
        var s = this.upload.filesize / 1024;
        if (s < 1024) {
          this.$modal.msgError(this.$t('Component.CommonImageViewer.UploadSizeErr', [ s + " KB" ]));
        } else {
          this.$modal.msgError(this.$t('Component.CommonImageViewer.UploadSizeErr', [ (s / 1024) + " MB" ]));
        }
        return false;
      }
      this.$modal.loading(this.$t('Component.CommonImageViewer.Uploading'));
      return true;
    },
    handleFileUploadSuccess (response, file, fileList) {
      this.onFileUploaded(response.code == 200, fileList, response.code == 200 ? response.data : response.msg);
    },
    handleFileUploadError (err, file, fileList) {
      this.onFileUploaded(false, fileList, err);
      this.$modal.closeLoading()
    },
    onFileUploaded(isSuccess, fileList, result) {
      this.$modal.closeLoading()
      if (isSuccess) {
        this.imagePath = result.path;
        this.imageSrc = process.env.VUE_APP_BASE_API + result.src + "?" + Date.now();
        this.upload.fileList = [];
      } else {
        this.$modal.msgError(result);
      }
    },
    handleView (event) {
      event.stopPropagation();
      this.showImageViewer = true;
    },
    handleImageViewerClose() {
      this.showImageViewer = false;
    },
    handleRemove (event) {
      event.stopPropagation();
      this.imagePath = "";
      this.upload.fileList = [];
    }
  }
};
</script>
<style scoped>
.simple-image-viewer {
  line-height: 0;
}
.simple-image-viewer .picture {
  position: relative;
  overflow: hidden;
  background-color: #fff;
  box-sizing: border-box;
  display: block;
}
.simple-image-viewer .picture:hover .toolbar {
  opacity: 80;
}
.simple-image-viewer .el-image {
  background-color: #E7E7E7;
}
.simple-image-viewer .toolbar {
  position: absolute;
  text-align: center;
  width: 100%;
  height: 30px;
  top: 120px;
  z-index: 100;
  color: #eee;
  background-color: rgba(0,0,0,.5);
  transition: opacity .3s;
  opacity: 0;
}
.simple-image-viewer .toolbar i {
  font-size: 16px;
  padding: 7px;
  cursor: pointer;
}
.simple-image-viewer .toolbar i:hover {
  color: #409EFF;
}
.simple-image-viewer .no-picture {
  border: 3px dashed #a7a7a7;
  text-align: center;
  color: #777;
  cursor: pointer;
}
.simple-image-viewer .no-picture:hover {
  color: #409EFF;
}
</style>