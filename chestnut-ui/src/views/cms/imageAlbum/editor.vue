<template>
  <div class="cms-image-editor bg-purple-white">
    <transition-group name="flip-image-list" tag="div">
      <el-card shadow="always"
              v-for="(item, index) in imageList"
              :key="item.path"
              class="r-card mt10">
        <el-container class="r-container">
          <el-aside width="240px" class="r-left">
            <el-image :src="item.src"
                      fit="scale-down"></el-image>
            <div class="r-name">{{ item.title }}</div>
            <div class="r-info">
              [ {{ $t('CMS.Image.FileSize') }}: {{ item.fileSizeName }} ]
              <span :v-if="item.resourceType=='image'"> [ {{ $t('CMS.Image.WidthHeight') }}: {{ item.width }} x {{ item.height }} ] </span>
            </div>
          </el-aside>
          <el-container>
            <el-main>
              <el-row>
                <el-form-item :label="$t('CMS.Image.Title')">
                  <el-input v-model="item.title"></el-input>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item :label="$t('CMS.Image.Summary')">
                  <el-input type="textarea" :rows="2" v-model="item.description"></el-input>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item :label="$t('CMS.Image.RedirectUrl')">
                  <el-input v-model="item.redirectUrl"></el-input>
                </el-form-item>
              </el-row>
              <el-row class="r-opr-row">
                <el-link icon="el-icon-top"
                        v-if="index > 0" 
                        @click="upImage(index)">{{ $t('CMS.Image.MoveUp') }}</el-link>
                <el-link icon="el-icon-bottom"
                              v-if="imageList.length > 1 && index < imageList.length - 1" 
                        @click="downImage(index)">{{ $t('CMS.Image.MoveDown') }}</el-link>
                <el-link icon="el-icon-search"
                        @click="showImage(index)">{{ $t('Common.View') }}</el-link>
                <el-link icon="el-icon-picture"
                        @click="chooseImage(index)">{{ $t('CMS.Image.SetLogo') }}</el-link>
                <el-link icon="el-icon-edit"
                        @click="editImage(index)">{{ $t('Common.Edit') }}</el-link>
                <el-link icon="el-icon-delete"
                        @click="deleteImage(index)">{{ $t('Common.Delete') }}</el-link>
              </el-row>
            </el-main>
          </el-container>
        </el-container>
      </el-card>
    </transition-group>
    <el-card shadow="always" class="mt10">
        <div class="btn-add-image bg-purple-white" @click="addImage">
          <svg-icon icon-class="image" style="width:60px;height:60px;"></svg-icon>
          <div>{{ $t('CMS.Image.Add') }}</div>
        </div>
    </el-card>
    <cms-resource-dialog 
      :open.sync="openResourceDialog"
      rtype="image"
      :upload-limit="uploadLimit"
      @ok="handleResourceDialogOk">
    </cms-resource-dialog>
    <el-image-viewer 
      v-if="showImageViewer" 
      :initialIndex="imageViewerIndex"
      :on-close="handleImageViewerClose"
      :url-list="imageList.map(img=>img.src)">
    </el-image-viewer>
  </div>
</template>
<script>
import ElImageViewer from "element-ui/packages/image/src/image-viewer"
import CMSResourceDialog from "@/views/cms/contentcore/resourceDialog";

export default {
  name: "CMSImageList",
  components: {
    "el-image-viewer": ElImageViewer,
    "cms-resource-dialog": CMSResourceDialog
  },
  model: {
    prop: 'imageList',
    event: 'change'
  },
  props: {
    imageList: {
      type: Array,
      default: () => [],
      required: false,
    }
  },
  data () {
    return {
      openResourceDialog: false,
      uploadLimit: 10,
      editIndex: -1,
      showImageViewer: false,
      imageViewerIndex: 0
    };
  },
  watch: {
    imageList(newVal) {
      this.$emit("change", newVal);
    }
  },
  methods: {
    handleImageViewerClose() {
      this.showImageViewer = false;
    },
    handleResourceDialogOk (results) {
      if (this.editIndex > -1) {
        const r = results[0];
        this.imageList[this.editIndex] = { 
            path: r.path, 
            src: r.src, 
            title: r.name, 
            fileName: r.name, 
            width: r.width, 
            height: r.height, 
            fileSize: r.fileSize,
            fileSizeName: r.fileSizeName,
            resourceType: r.resourceType
          };
        this.editIndex = -1;
      } else {
        results.forEach(r => {
          this.imageList.push({ 
            path: r.path, 
            src: r.src, 
            title: r.name, 
            fileName: r.name, 
            width: r.width, 
            height: r.height, 
            fileSize: r.fileSize,
            fileSizeName: r.fileSizeName,
            resourceType: r.resourceType
          });
        });
      }
    },
    addImage () {
      this.editIndex = -1;
      this.uploadLimit = 10;
      this.openResourceDialog = true;
    },
    editImage (index) {
      this.uploadLimit = 1;
      this.editIndex = index;
      this.openResourceDialog = true;
    },
    deleteImage (index) {
      this.imageList.splice(index, 1);
    }, 
    showImage (index) {
      this.imageViewerIndex = index;
      this.showImageViewer = true;
    },
    upImage (index) {
        let temp = this.imageList[index];
        // this.imageList[index] = this.imageList[index - 1];
        this.$set(this.imageList, index, this.imageList[index - 1]);
        this.$set(this.imageList, index - 1, temp);
    },
    downImage (index) {
        let temp = this.imageList[index];
        // this.imageList[index] = this.imageList[index + 1];
        this.$set(this.imageList, index, this.imageList[index + 1]);
        this.$set(this.imageList, index + 1, temp);
    },
    chooseImage (index) {
        this.$emit("choose", this.imageList[index].path, this.imageList[index].src);
    }
  }
};
</script>
<style>
.cms-image-editor .flip-image-list-move {
  transition: transform 1s;
}
.cms-image-editor .flip-image-list-item {
  transition: all 1s;
  display: inline-block;
}
.cms-image-editor .flip-image-list-enter, .flip-image-list-leave-to
/* .list-complete-leave-active for below version 2.1.8 */ {
  opacity: 0;
  transform: translateY(30px);
}
.cms-image-editor .flip-image-list-leave-active {
  position: absolute;
}
.cms-image-editor .btn-add-image {
  width: 100%;
  text-align: center;
  display: inline-block;
  cursor: pointer;
  fill: #5a5e66;
}
.cms-image-editor .btn-add-image:hover {
  color:#1890ff;
}
.cms-image-editor .r-card .el-card__body {
  padding: 15px;
}
.cms-image-editor .r-container {
  height: 200px;
  overflow: hidden;
}
.cms-image-editor .r-container .r-left {
  padding: 0;
  background-color: #fff;
  text-align: center;
  overflow: hidden;
  height: 200px;
  font-size: 12px;
  color: #777;
}
.cms-image-editor .r-container .r-left .el-image {
  width: 240px;
  height: 150px;
  background-color: #f7f7f7;
}
.cms-image-editor .r-container .r-name, .r-info {
  line-height: 20px;
  height: 20px;
  overflow: hidden;
  text-align: left;
}
.cms-image-editor .r-container .r-name {
  color: #5587F0;
}
.cms-image-editor .r-container .el-main {
  padding: 0px;
}
.cms-image-editor .r-container .el-main .el-form-item {
  margin-bottom: 12px;
}
.cms-image-editor .r-container .el-main .el-link {
  text-decoration: none;
  margin-left: 20px;
}
.cms-image-editor .r-container .r-opr-row {
  text-align: right;
  margin-top: 10px;
}
</style>