<template>
   <!-- 此组件上传图片需指定action，上传的图片不作为网站素材库资源记录，一般用来上传固定路径图片，比如站点LOGO,会员头像等 -->
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
      :on-progress="handleFileUploadProgress"
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
                        :url-list="imageViewerList">
        </el-image-viewer>
        <div class="toolbar">
          <el-tooltip class="item" effect="dark" :content="$t('Common.View')" placement="top">
            <i class="el-icon-search" @click="handleView" />
          </el-tooltip>
          <el-tooltip class="item" effect="dark" :content="$t('Common.Remove')" placement="top">
            <i class="el-icon-delete" @click="handleRemove" />
          </el-tooltip>
          <!-- <el-tooltip class="item" effect="dark" :content="$t('CMS.Resource.Cut')" placement="top">
            <i class="el-icon-crop" @click="handleCut" />
          </el-tooltip> -->
        </div>
      </div>
      <div v-show="showText"
          class="no-picture"
          :style="{'width':noWidth,'height':noHeight,'font-size':svgSize}">
        <svg-icon icon-class="upload"></svg-icon>
      </div>
      <div slot="tip" class="el-upload__tip" style="margin:10px 0">只能上传jpg/png文件，且不超过200kb</div>
    </el-upload>
  </div>
</template>
<script>
import { getToken } from "@/utils/auth";
import ElImageViewer from "element-ui/packages/image/src/image-viewer"

export default {
  name: "CMSSimpleImageView",
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
    site: {
      type: String,
      default: "",
      required: true,
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
      this.imageSrc = newVal;
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
      imageSrc: this.src,
      showImageViewer: false,
      imageViewerList: [],
      upload: {
        isUploading: false,
        accept: ".jpg,.jpeg,.png",
        limit: 1,
        headers: { Authorization: "Bearer " + getToken() },
        url: process.env.VUE_APP_BASE_API + this.action, //"/cms/site/upload_watermarkimage",
        fileList: [],
        data : { siteId: this.site }
      },
    };
  },
  methods: {
    handleFileUploadProgress (event, file, fileList) {
      this.upload.isUploading = true;
    },
    handleFileUploadSuccess (response, file, fileList) {
      this.onFileUploaded(response.code == 200, fileList, response.code == 200 ? response.data : response.msg);
    },
    handleFileUploadError (err, file, fileList) {
      this.onFileUploaded(false, fileList, err);
    },
    onFileUploaded(isSuccess, fileList, result) {
      this.upload.isUploading = false;
      if (isSuccess) {
        this.imagePath = result.path;
        this.imageSrc = result.src + "?" + Date.now();
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
    }, 
    handleCut (event) {
      event.stopPropagation();
      this.$modal.alert("没整呢~");
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