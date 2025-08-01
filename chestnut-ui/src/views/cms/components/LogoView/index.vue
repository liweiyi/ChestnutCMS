<template>
  <div class="logo-viewer" :style="{'width':imgWidth,'height':imgHeight}">
    <div class="picture"
         v-show="showImage">
      <el-image :src="imageSrc"  
                :style="{'width':imgWidth,'height':imgHeight}"
                @click="handleEdit"
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
        <el-tooltip class="item" effect="dark" :content="$t('Common.Edit')" placement="top">
          <i class="el-icon-edit" @click="handleEdit" />
        </el-tooltip>
        <el-tooltip class="item" effect="dark" :content="$t('Common.Remove')" placement="top">
          <i class="el-icon-delete" @click="handleRemove" />
        </el-tooltip>
      </div>
    </div>
    <div v-show="showText" class="no-picture" :style="{'width':noWidth,'height':noHeight,'font-size':svgSize}">
      <svg-icon icon-class="upload" @click="handleEdit"></svg-icon>
    </div>
    <cms-resource-dialog 
      :open.sync="openResourceDialog"
      rtype="image"
      :upload-limit="1" 
      :single="true"
      :siteId="currentSiteId"
      @ok="handleResourceDialogOk">
    </cms-resource-dialog>
  </div>
</template>
<script>
import * as utils from "@/utils/chestnut";
import CMSResourceDialog from "@/views/cms/contentcore/resourceDialog";
import ElImageViewer from "element-ui/packages/image/src/image-viewer"

export default {
  name: "CMSLogoView",
  components: {
    "cms-resource-dialog": CMSResourceDialog,
    "el-image-viewer": ElImageViewer,
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
    width: {
      type: Number,
      default: 218,
      required: false,
    },
    height: {
      type: Number,
      default: 150,
      required: false,
    },
    siteId: {
      type: String,
      default: "0",
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
    noWidth () {
      return (this.width - 3) + 'px';
    },
    noHeight () {
      return (this.height - 3) + 'px';
    },
    svgSize () {
      return (this.height - 5) + 'px';
    },
    showImage () {
      return this.imagePath != undefined && this.imagePath != null && this.imagePath.length > 0;
    },
    showText() {
      return this.imagePath == undefined || this.imagePath == null || this.imagePath.length == 0;
    },
    isResourceImage() {
      return this.path && this.path.startsWith("iurl://");
    },
    resourceId() {
      if (this.path && this.path.startsWith("iurl://")) {
        let queryString = this.path.substring(this.path.indexOf('?') + 1)
        let idParam = queryString.split("&").find(item => item.startsWith("id="));
        return idParam.substring(idParam.indexOf('=') + 1);
      }
      return '';
    }
  },
  watch: {
    path(newVal) {
      this.imagePath = newVal;
    },
    src(newVal) {
      this.imageSrc = newVal;
      this.originalSrc = this.getOriginalSrc(newVal);
      console.log("originalSrc", this.originalSrc)
    },
    imagePath(newVal) {
      this.$emit("change", newVal);
    },
    imageSrc(newVal) {
      if (newVal && newVal.length > 0) {
        this.originalSrc = this.getOriginalSrc(newVal);
        this.imageViewerList = [ this.originalSrc ];
      } else {
        this.imageViewerList.splice(0);
      }
    },
    siteId(newVal) {
      this.currentSiteId = newVal;
    },
  },
  data () {
    return {
      imagePath: this.path,
      imageSrc: this.src,
      originalSrc: this.src,
      openResourceDialog: false,
      showImageViewer: false,
      imageViewerList: [],
      currentSiteId: '0',
    };
  },
  methods: {
    handleResourceDialogOk (results) {
      if (results && results.length > 0) {
        const r = results[0];
        this.imagePath = r.path;
        this.imageSrc = r.src;
        this.$emit("update", {
          path: r.path,
          src: r.src
        });
      }
    },
    handleView () {
      this.showImageViewer = true;
    },
    handleImageViewerClose() {
      this.showImageViewer = false;
    },
    handleEdit () {
      this.openResourceDialog = true;
    },
    handleRemove () {
      this.imagePath = "";
      this.$emit("update", {
        path: "",
        src: ""
      });
    }, 
    getOriginalSrc(src) {
      if (src.indexOf("/preview/") > -1) {
        let fileName = utils.substringAfterLast(src, "/");
        if (fileName.indexOf('_') > -1) {
          let name = utils.substringBeforeLast(fileName, "_") + "." + utils.substringAfterLast(fileName, ".");
          let prefix = utils.substringBeforeLast(src, "/");
          return utils.setUrlParameter(prefix + "/" + name, "t", new Date().getTime());
        }
      }
      return utils.setUrlParameter(src, "t", new Date().getTime());
    },
  }
};
</script>
<style scoped>
.logo-viewer {
  line-height: 0;
}
.logo-viewer .picture {
  position: relative;
  overflow: hidden;
  background-color: #fff;
  box-sizing: border-box;
  display: block;
}
.logo-viewer .picture:hover .toolbar {
  opacity: 80;
}
.logo-viewer .el-image {
  background-color: #E7E7E7;
}
.logo-viewer .toolbar {
  position: absolute;
  text-align: center;
  width: 100%;
  height: 30px;
  bottom: 0;
  z-index: 100;
  color: #eee;
  background-color: rgba(0,0,0,.5);
  transition: opacity .3s;
  opacity: 0;
}
.logo-viewer .toolbar i {
  font-size: 16px;
  padding: 7px;
  cursor: pointer;
}
.logo-viewer .toolbar i:hover {
  color: #409EFF;
}
.logo-viewer .no-picture {
  border: 3px dashed #a7a7a7;
  text-align: center;
  color: #777;
  cursor: pointer;
}
.logo-viewer .no-picture:hover {
  color: #409EFF;
}
</style>