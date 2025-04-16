<template>
  <div id="cms-cropper-container">
    <el-dialog id="appBox" :visible.sync="visible" width="1000px" :close-on-click-modal="false" append-to-body>
      <el-tabs v-model="activeName" @tab-click="handleTabClick">
        <el-tab-pane label="图片裁剪" name="crop"></el-tab-pane>
        <el-tab-pane label="旋转缩放" name="rotate"></el-tab-pane>
        <!-- <el-tab-pane label="文字水印" name="textWatermark"></el-tab-pane>
        <el-tab-pane label="图片水印" name="imgWatermark"></el-tab-pane>
        <el-tab-pane label="马赛克" name="mosaic"></el-tab-pane> -->
      </el-tabs>
        <el-row :gutter="20">
          <el-col :span="16">
            <div class="cropper-wrap">
              <img ref="cropperImage" id="cropper-image" :src="imageUrl"></img>
            </div>
          </el-col>
          <el-col :span="8">
            <el-row :gutter="10" class="px10" v-if="activeName=='crop'">
              <el-col :span="12">
                宽 <el-input v-model="cropWidth" @change="handleCropWidthChange" size="mini" style="width: 80px"></el-input>
              </el-col>
              <el-col :span="12">
                高 <el-input v-model="cropHeight" @change="handleCropHeightChange" size="mini" style="width: 80px"></el-input>
              </el-col>
            </el-row>
            <el-row class="px10" v-if="activeName=='crop'">
              <el-checkbox v-model="fixed" @change="handleFixedAspectRatioChange">固定宽高</el-checkbox>
            </el-row>
            <el-row v-if="activeName=='crop'">
              <el-radio-group v-model="aspectRatio" @change="handleAspectRatioChange">
                <el-row>
                  <el-col :span="8" v-for="(item, index) in aspectRatioOptions" :key="index" class="px10">
                    <el-radio :label="item.value">{{ item.label }}</el-radio>
                  </el-col>
                </el-row>
              </el-radio-group>
            </el-row>
            <el-row v-if="activeName=='crop'">
              <b class="px10">预设尺寸</b>
              <el-radio-group v-model="cropSize" @change="handleCropSizeChange">
                <el-row>
                  <el-col :span="8" v-for="(item, index) in cropSizeOptions" :key="index" class="px10">
                    <el-radio :label="item.value" :disabled="item.disabled">{{ item.label }}</el-radio>
                  </el-col>
                </el-row>
              </el-radio-group>
            </el-row>
            <el-row v-if="activeName=='rotate'" class="rotate-wrap">
              <el-row class="px10" :gutter="10">
                <el-col :span="12">
                  <el-button icon="el-icon-refresh-left" @click="handleRotate(-90)">左转90°</el-button>
                </el-col>
                <el-col :span="12">
                  <el-button icon="el-icon-refresh-right" @click="handleRotate(90)">右转90°</el-button>
                </el-col>
              </el-row>
              <el-row class="px10" :gutter="10">
                <el-col :span="12">
                  <el-button @click="handleFlipX"><svg-icon icon-class="arrow-left-right" />水平翻转</el-button>
                </el-col>
                <el-col :span="12">
                  <el-button @click="handleFlipY"><svg-icon icon-class="arrow-down-up" />垂直翻转</el-button>
                </el-col>
              </el-row>
              <el-row class="px10" :gutter="10">
                <el-col :span="12">
                  <el-button icon="el-icon-zoom-in" @click="handleZoom(0.1)">放大10%</el-button>
                </el-col>
                <el-col :span="12">
                  <el-button icon="el-icon-zoom-out" @click="handleZoom(-0.1)">缩小10%</el-button>
                </el-col>
              </el-row>
              <el-row :gutter="10" style="padding: 0 20px">
                <el-slider
                  v-model="zoomRatio"
                  :step="0.01"
                  :min="0.01"
                  :max="1"
                  :format-tooltip="showZoomRatio">
                </el-slider>
              </el-row>
            </el-row>
            <el-divider></el-divider>
            <el-row class="px10 align-center">
              <el-button type="success" @click="handleSaveCrop">应用</el-button>
            </el-row>
          </el-col>
        </el-row>

      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleClose">{{ $t("Common.Close") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { cropImage, rotateImage } from "@/api/contentcore/resource"

import Cropper from 'cropperjs';
import 'cropperjs/dist/cropper.css';

export default {
  name: "CMSImageCropper",
  props: {
    open: {
      type: Boolean,
      default: false,
      required: true
    },
    resourceId: {
      type: String,
      required: true,
    },
    src: {
      type: String,
      required: true,
    },
  },
  watch: {
    open (newVal) {
      this.visible = newVal;
    },
    visible (newVal) {
      if (!newVal) {
        this.noticeClose();
      } else {
        this.initCropper();
      }
    },
    src(newVal) {
      this.imageUrl = newVal;
      this.initCropper();
    },
    zoomRatio(newVal) {
      this.imgCropper.zoomTo(newVal);
    }
  },
  data() {
    return {
      visible: false,
      activeName: "crop",
      aspectRatioOptions: [
        { label: "原比例", value: 0 },
        { label: "1:1", value: 1.0 },
        { label: "1:2", value: 1.0 / 2.0 },
        { label: "2:1", value: 2.0 / 1.0 },
        { label: "2:3", value: 2.0 / 3.0 },
        { label: "3:2", value: 3.0 / 2.0 },
        { label: "3:4", value: 3.0 / 4.0 },
        { label: "4:3", value: 4.0 / 3.0 },
        { label: "16:9", value: 16.0 / 9.0 },
      ],
      cropSizeOptions: [
        { label: "90x90", value: [ 90, 90 ], disabled: false }, // 缩略图
        { label: "120x120", value: [ 120, 120 ], disabled: false }, // 缩略图
        { label: "300x300", value: [ 300, 300 ], disabled: false }, // 缩略图
        { label: "360x120", value: [ 360, 120 ], disabled: false }, // 网站横幅
        { label: "1200x400", value: [ 1200, 400 ], disabled: false }, // 网站横幅
        { label: "360x200", value: [ 360, 200 ], disabled: false }, // 主页横幅（h5）
        { label: "1280X720", value: [ 1280, 720 ], disabled: false }, // 主页横幅（pc）
        { label: "360x240", value: [ 360, 240 ], disabled: false }, // 文章插图
        { label: "1200x800", value: [ 1200, 800 ], disabled: false }, // 文章插图
      ],
      imgCropper: undefined,
      imageUrl: this.src,  // 图片地址
      cropX: 0,
      cropY: 0,
      cropWidth: 0,
      cropHeight: 0,
      fixed: true, // 是否开启截图框宽高固定比例
      aspectRatio: 1, // 截图框比例
      cropSize: undefined, // 预设尺寸
      zoomRatio: 1.0, // 缩放比例
      options: {
        viewMode: 1,
        dragMode: 'none',
        aspectRatio: 1,
        // autoCropArea: 0.6,
        background: false,
        // center: false,
        movable: false,
        zoomable: false,
        // zoomOnWheel: false,
        // zoomOnTouch: false,
        checkCrossOrigin: false,
        crop: this.handleCropEvent,
        ready: this.handleCropperReady,
      },
      cropData: {}
    }
  },
  methods: {
    handleTabClick(tab, event) {
      if (this.activeName == 'crop') {
        this.options.aspectRatio = 1;
      } else if (this.activeName == 'rotate') {
        this.options.aspectRatio = NaN;
        this.options.zoomable = true;
      }
      this.initCropper();
    },
    initCropper() {
      if (this.visible) {
        this.reset();
        this.$nextTick(() => {
          if (this.imgCropper) {
            this.imgCropper.destroy();
          }
          let elImage = this.$refs.cropperImage;
          this.imgCropper = new Cropper(elImage, this.options)
        })
      }
    },
    handleCropperReady() {
      if (this.activeName == 'crop') {
        let imageData = this.imgCropper.getImageData();
        this.cropSizeOptions.forEach(item => {
          item.disabled = imageData.naturalWidth < item.value[0] || imageData.naturalHeight < item.value[1];
        })
      } else if (this.activeName == 'rotate') {
        this.imgCropper.clear();
        this.imgCropper.zoomTo(1);
      }
    },
    handleCropEvent(e) {
      var data = e.detail;
      this.cropData = data
      this.cropX = Math.round(data.x);
      this.cropY = Math.round(data.y);
      this.cropWidth = Math.round(data.width);
      this.cropHeight = Math.round(data.height);
      this.rotate = typeof data.rotate !== 'undefined' ? data.rotate : 0;
    },
    handleCropWidthChange() {
      let imageData = this.imgCropper.getImageData();
      let ratio = imageData.width / imageData.naturalWidth;
      let cropBoxData = this.imgCropper.getCropBoxData();
      cropBoxData.width = this.cropWidth * ratio;
      if (this.imgCropper.aspectRatio && this.imgCropper.aspectRatio > 0) {
        cropBoxData.height = cropBoxData.width / this.imgCropper.aspectRatio;
      }
      this.imgCropper.setCropBoxData(cropBoxData);
    },
    handleCropHeightChange() {
      let imageData = this.imgCropper.getImageData();
      let ratio = imageData.width / imageData.naturalWidth;
      let cropBoxData = this.imgCropper.getCropBoxData();
      cropBoxData.height = this.cropHeight * ratio;
      if (this.imgCropper.aspectRatio && this.imgCropper.aspectRatio > 0) {
        cropBoxData.width = cropBoxData.height / this.imgCropper.aspectRatio;
      }
      this.imgCropper.setCropBoxData(cropBoxData);
    },
    handleAspectRatioChange() {
      this.imgCropper.setAspectRatio(this.aspectRatio);
      if (this.fixed) {
        let data = this.imgCropper.getCropBoxData();
        this.imgCropper.setAspectRatio(data.width / data.height);
      } else {
        let data = this.imgCropper.getCropBoxData();
        this.imgCropper.setAspectRatio(NaN);
        this.imgCropper.setCropBoxData(data);
      }
    },
    handleFixedAspectRatioChange() {
      let data = this.imgCropper.getCropBoxData();
      if (this.fixed) {
        this.imgCropper.setAspectRatio(data.width / data.height)
      } else {
        this.imgCropper.setAspectRatio(NaN)
      }
      this.imgCropper.setCropBoxData(data)
    },
    handleCropSizeChange() {
      // console.log('handleCropSizeChange', this.cropSize[0], this.cropSize[1])
      let imageData = this.imgCropper.getImageData();
      let ratio = imageData.width / imageData.naturalWidth;
      let data = this.imgCropper.getCropBoxData();
      data.width = this.cropSize[0] * ratio;
      data.height = this.cropSize[1] * ratio;
      if (this.fixed) {
        this.imgCropper.setAspectRatio(data.width / data.height)
      }
      this.imgCropper.setCropBoxData(data)
    },
    handleSaveCrop() {
      if (this.activeName == 'crop') {
        let data = {
          resourceId: this.resourceId, x: this.cropX, y: this.cropY, width: this.cropWidth, height: this.cropHeight
        }
        cropImage(data).then(response => {
          this.$modal.notifySuccess(this.$t('Common.SaveSuccess'));
          this.initCropper();
        });
      } else if (this.activeName == 'rotate') {
        let imageData = this.imgCropper.getImageData();
        let data = {
          resourceId: this.resourceId,
          rotate: this.cropData.rotate,
          flipX: this.cropData.scaleX == -1,
          flipY: this.cropData.scaleY == -1,
          width: this.zoomRatio * imageData.naturalWidth,
          height: this.zoomRatio * imageData.naturalHeight
        }
        rotateImage(data).then(response => {
          this.$modal.notifySuccess(this.$t('Common.SaveSuccess'));
          this.initCropper();
        });
      }
    },
    handleZoom(ratio) {
      if (this.zoomRatio + ratio > 1 || this.zoomRatio + ratio < 0.1) {
        return;
      }
      this.zoomRatio += ratio;
    },
    showZoomRatio(ratio) {
      return Math.round(ratio*100) + "%";
    },
    handleRotate(degree) {
      this.imgCropper.rotate(degree);
    },
    handleFlipX() {
      this.imgCropper.scaleX(this.cropData.scaleX == 1 ? -1 : 1);
    },
    handleFlipY() {
      this.imgCropper.scaleY(this.cropData.scaleY == 1 ? -1 : 1);
    },
    handleClose() {
      this.visible = false;
    },
    noticeClose () {
      if (!this.visible) {
        this.$emit('update:open', false);
        this.$emit("close");
      }
    },
    reset() {
      this.cropX = 0;
      this.cropY = 0;
      this.cropWidth = 0;
      this.cropHeight = 0;
      this.fixed = true;
      this.aspectRatio = 1;
      this.cropSize = undefined;
      this.zoomRatio = 1.0;
      this.rotate = 0;
    },
  },
}
</script>
<style scoped>
.cropper-wrap {
  width: 100%;
  height: 450px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e5e5e5;
}
h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
}
.px10 {
  padding-top: 10px;
  padding-bottom: 10px;
}
.align-center {
  text-align: center;
}
#cropper-image {
  max-width: 633px;
}
.rotate-wrap {
  text-align: center;
}
.rotate-wrap .el-button {
  width: 120px;
}
</style>
