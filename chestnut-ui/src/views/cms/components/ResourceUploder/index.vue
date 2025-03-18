<template>
  <div class="file-uploder">
    <div class="upload-wrap">
      <div class="file-wrap" v-for="(file, index) in fileList" :key="index">
        <el-image 
           v-if="isImageResource(index)"
          :src="file.src"  
          :style="{'width':imgWidth,'height':imgHeight}"
          @click="handleEdit(index)"
          fit="scale-down">
        </el-image>
        <svg-icon v-else :icon-class="getResourceFileIconClass(index)" :style="{'width':imgWidth,'height':imgHeight}"  @click="handleEdit(index)" />
        <div class="title" :title="file.name">
          {{ file.name }}
        </div>
        <div class="toolbar">
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
    <cms-resource-dialog 
      :open.sync="openResourceDialog"
      :rtype="type"
      :upload-limit="1" 
      :single="true"
      @ok="handleResourceDialogOk">
    </cms-resource-dialog>
  </div>
</template>
<script>
import { isImage, getFileSvgIconClass, setUrlParameter } from "@/utils/chestnut";
import CMSResourceDialog from "@/views/cms/contentcore/resourceDialog";

export default {
  name: "CMSResourceUploder",
  components: {
    "cms-resource-dialog": CMSResourceDialog,
  },
  model: {
    prop: 'files',
    event: 'change'
  },
  props: {
    files: {
      type: Array,
      default: () => [],
      required: true,
    },
    type: {
      type: String,
      default: "file",
      required: false
    },
    width: {
      type: Number,
      default: 120,
      required: false,
    },
    height: {
      type: Number,
      default: 80,
      required: false,
    },
    limit: {
      type: Number,
      default: 5,
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
      return this.fileList == undefined || this.fileList.length < this.limit;
    }
  },
  watch: {
    files(newVal) {
      this.fileList = newVal;
    },
    fileList(newVal) {
      this.$emit("change", newVal);
    }
  },
  data () {
    return {
      fileList: this.path,
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
            if (this.fileList.length < this.limit) {
              this.fileList.push({
                name: results[i].name,
                path: results[i].path,
                src: results[i].src
              });
            }
          }
        } else {
          this.fileList[this.curIndex] = {
              name: results[0].name,
              path: results[0].path,
              src: results[0].src
            };
        }
      }
    },
    handleEdit (index) {
      this.curIndex = index;
      this.openResourceDialog = true;
    },
    handleRemove (index) {
      this.fileList.splice(index, 1);
    }, 
    handleUpload () {
      this.curIndex = -1;
      this.openResourceDialog = true;
    },
    getResourceFileIconClass(index) {
      return getFileSvgIconClass(this.fileList[index].src)
    },
    isImageResource (index) {
      return isImage(this.fileList[index].src);
    },
  }
};
</script>
<style scoped>
.file-uploder {
  line-height: 0;
}
.file-uploder .file-wrap {
  position: relative;
  overflow: hidden;
  background-color: #fff;
  box-sizing: border-box;
  display: block;
  float: left;
}
.file-uploder .file-wrap:hover .toolbar {
  display: block;
}
.file-uploder .el-image {
  background-color: #E7E7E7;
}
.file-uploder .file-wrap .title {
  position: absolute;
  text-align: left;
  width: 100%;
  height: 28px;
  top: 0;
  font-size: 12px;
  line-height: 20px;
  z-index: 100;
  color: #eee;
  padding: 5px;
  background-color: #666;
  opacity: 0.5;
  text-overflow: ellipsis;
  white-space: nowrap;
  overflow: hidden;
}
.file-uploder .toolbar {
  position: absolute;
  text-align: center;
  width: 100%;
  bottom: 0;
  z-index: 100;
  color: #eee;
  background-color: #000;
  transition: opacity .3s;
  opacity: 0.8;
  display: none;
}
.file-uploder .toolbar i {
  font-size: 16px;
  padding: 7px;
  cursor: pointer;
}
.file-uploder .toolbar i:hover {
  color: #409EFF;
}
.file-uploder .no-picture {
  color: #777;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.file-uploder .no-picture:hover {
  color: #409EFF;
}
.file-uploder .file-wrap, .no-picture {
  border: 1px dashed #a7a7a7;
  margin-right: .5rem;
  margin-bottom: .5rem;
}
</style>