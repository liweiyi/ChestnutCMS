<template>
  <div class="logo-viewer">
    <div class="picture-wrap">
      <div class="picture" v-for="(imgSrc, index) in imageSrcList" :key="index">
        <el-image 
          :src="imgSrc"  
          :style="{'width':imgWidth,'height':imgHeight}"
          @click="handleEdit(index)"
          fit="scale-down">
        </el-image>
        <div class="toolbar">
          <el-tooltip class="item" effect="dark" :content="$t('Common.View')" placement="top">
            <i class="el-icon-search" @click="handleView(index)"></i>
          </el-tooltip>
          <el-tooltip class="item" effect="dark" :content="$t('Common.Edit')" placement="top">
            <i class="el-icon-edit" @click="handleEdit(index)"></i>
          </el-tooltip>
          <el-tooltip class="item" effect="dark" :content="$t('Common.Remove')" placement="top">
            <i class="el-icon-delete" @click="handleRemove(index)"></i>
          </el-tooltip>
        </div>
      </div>
    </div>
    <div v-if="showAdd" class="no-picture" @click="handleUpload" :style="{'width':noWidth,'height':noHeight,'font-size':svgSize}">
      <i class="el-icon-plus"></i>
    </div>
    <el-image-viewer 
      v-if="showImageViewer" 
      :on-close="handleImageViewerClose"
      :initial-index="curIndex" 
      :url-list="imageSrcList">
    </el-image-viewer>
    <cms-resource-dialog 
      :open.sync="openResourceDialog"
      rtype="image"
      :upload-limit="1" 
      @ok="handleResourceDialogOk">
    </cms-resource-dialog>
  </div>
</template>
<script>
import { setUrlParameter } from "@/utils/chestnut";
import CMSResourceDialog from "@/views/cms/contentcore/resourceDialog";
import ElImageViewer from "element-ui/packages/image/src/image-viewer"

export default {
  name: "CMSImageGroup",
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
      type: Array,
      default: () => [],
      required: true,
    },
    src: {
      type: Array,
      default: () => [],
      required: true,
    },
    width: {
      type: Number,
      default: 160,
      required: false,
    },
    height: {
      type: Number,
      default: 120,
      required: false,
    },
    limit: {
      type: Number,
      default: 1,
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
      return (this.width) + 'px';
    },
    noHeight () {
      return (this.height) + 'px';
    },
    svgSize () {
      return (this.height / 2) + 'px';
    },
    showAdd () {
      return this.imagePathList.length < this.limit;
    }
  },
  watch: {
    path(newVal) {
      this.imagePathList = newVal;
    },
    src(newVal) {
      this.imageSrcList = newVal;
    },
    imagePathList(newVal) {
      this.$emit("change", newVal);
    }
  },
  data () {
    return {
      imagePathList: this.path,
      imageSrcList: this.src,
      openResourceDialog: false,
      showImageViewer: false,
      curIndex: -1,
    };
  },
  methods: {
    handleResourceDialogOk (results) {
      if (results && results.length > 0) {
        if (this.curIndex < 0) {
          for (let i = 0; i < results.length; i++) {
            if (this.imagePathList.length < this.limit) {
              this.imagePathList.push(results[i].path);
              this.imageSrcList.push(results[i].src);
            }
          }
        } else {
          const r = results[0];
          this.imagePathList[this.curIndex] = r.path;
          this.imageSrcList[this.curIndex] = r.src
        }
      }
    },
    handleView (index) {
      this.curIndex = index;
      this.showImageViewer = true;
    },
    handleImageViewerClose() {
      this.showImageViewer = false;
    },
    handleEdit (index) {
      this.curIndex = index;
      this.openResourceDialog = true;
    },
    handleRemove (index) {
      this.imagePathList.splice(index, 1);
      this.imageSrcList.splice(index, 1);
    }, 
    handleUpload () {
      this.curIndex = -1;
      this.openResourceDialog = true;
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
  float: left;
}
.logo-viewer .picture:hover .toolbar {
  display: block;
}
.logo-viewer .el-image {
  background-color: #E7E7E7;
}
.logo-viewer .toolbar {
  position: absolute;
  text-align: center;
  width: 100%;
  bottom: 0;
  z-index: 100;
  color: #eee;
  background-color: rgba(0,0,0,.5);
  transition: opacity .3s;
  opacity: 80;
  display: none;
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
  color: #777;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.logo-viewer .no-picture:hover {
  color: #409EFF;
}
.logo-viewer .picture, .no-picture {
  border: 1px dashed #a7a7a7;
  margin-right: .5rem;
  margin-bottom: .5rem;
}
</style>