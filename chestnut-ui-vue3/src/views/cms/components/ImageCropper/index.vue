<template>
  <div id="cms-cropper-container">
    <el-dialog id="appBox" v-model="visible" width="1000px" :close-on-click-modal="false" append-to-body>
      <el-tabs v-model="activeName" @tab-click="handleTabClick">
        <el-tab-pane :label="$t('CMS.ImageCropper.Crop')" name="crop"></el-tab-pane>
        <el-tab-pane :label="$t('CMS.ImageCropper.Rotate')" name="rotate"></el-tab-pane>
        <!-- <el-tab-pane label="文字水印" name="textWatermark"></el-tab-pane>
        <el-tab-pane label="图片水印" name="imgWatermark"></el-tab-pane>
        <el-tab-pane label="马赛克" name="mosaic"></el-tab-pane> -->
      </el-tabs>
      <el-row :gutter="20">
        <el-col :span="16">
          <div class="cropper-wrap">
            <img ref="cropperImage" id="cropper-image" :src="imageUrl" />
          </div>
        </el-col>
        <el-col :span="8">
          <el-row :gutter="10" class="px10" v-if="activeName=='crop'">
            <el-col :span="12">
              {{ $t('CMS.ImageCropper.Width') }} <el-input v-model="cropWidth" @change="handleCropWidthChange" style="width: 80px" />
            </el-col>
            <el-col :span="12">
              {{ $t('CMS.ImageCropper.Height') }} <el-input v-model="cropHeight" @change="handleCropHeightChange" style="width: 80px" />
            </el-col>
          </el-row>
          <el-row class="px10" v-if="activeName=='crop'">
            <el-checkbox v-model="fixed" @change="handleFixedAspectRatioChange">{{ $t('CMS.ImageCropper.FixedAspectRatio') }}</el-checkbox>
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
            <b class="px10">{{ $t('CMS.ImageCropper.PresetSize') }}</b>
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
                <el-button icon="el-icon-refresh-left" @click="handleRotate(-90)">{{ $t('CMS.ImageCropper.RotateLeft') }}</el-button>
              </el-col>
              <el-col :span="12">
                <el-button icon="el-icon-refresh-right" @click="handleRotate(90)">{{ $t('CMS.ImageCropper.RotateRight') }}</el-button>
              </el-col>
            </el-row>
            <el-row class="px10" :gutter="10">
              <el-col :span="12">
                <el-button @click="handleFlipX"><svg-icon icon-class="arrow-left-right" />{{ $t('CMS.ImageCropper.FlipX') }}</el-button>
              </el-col>
              <el-col :span="12">
                <el-button @click="handleFlipY"><svg-icon icon-class="arrow-down-up" />{{ $t('CMS.ImageCropper.FlipY') }}</el-button>
              </el-col>
            </el-row>
            <el-row class="px10" :gutter="10">
              <el-col :span="12">
                <el-button icon="el-icon-zoom-in" @click="handleZoom(0.1)">{{ $t('CMS.ImageCropper.ZoomIn') }}</el-button>
              </el-col>
              <el-col :span="12">
                <el-button icon="el-icon-zoom-out" @click="handleZoom(-0.1)">{{ $t('CMS.ImageCropper.ZoomOut') }}</el-button>
              </el-col>
            </el-row>
              <el-slider
                v-model="zoomRatio"
                :step="0.01"
                :min="0.01"
                :max="1"
                :format-tooltip="showZoomRatio">
              </el-slider>
          </el-row>
          <el-divider></el-divider>
          <el-row class="px10 align-center">
            <el-button type="success" @click="handleSaveCrop">{{ $t('CMS.ImageCropper.Apply') }}</el-button>
          </el-row>
        </el-col>
      </el-row>

      <template #footer> 
        <el-button type="primary" @click="handleClose">{{ $t("Common.Close") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CMSImageCropper">
import { cropImage, rotateImage } from "@/api/contentcore/resource"

import Cropper from 'cropperjs';
import 'cropperjs/dist/cropper.css';

const { proxy } = getCurrentInstance();

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  resourceId: {
    type: String,
    default: '',
  },
  src: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['update:open', 'update:src', 'close']);

const visible = ref(false);
const activeName = ref("crop");
const aspectRatioOptions = ref([
  { label: proxy.$t('CMS.ImageCropper.OriginalRatio'), value: 0 },
  { label: "1:1", value: 1.0 },
  { label: "1:2", value: 1.0 / 2.0 },
  { label: "2:1", value: 2.0 / 1.0 },
  { label: "2:3", value: 2.0 / 3.0 },
  { label: "3:2", value: 3.0 / 2.0 },
  { label: "3:4", value: 3.0 / 4.0 },
  { label: "4:3", value: 4.0 / 3.0 },
  { label: "16:9", value: 16.0 / 9.0 },
]);
const cropSizeOptions = ref([
  { label: "90x90", value: "90x90", disabled: false }, // 缩略图
  { label: "120x120", value: "120x120", disabled: false }, // 缩略图
  { label: "300x300", value: "300x300", disabled: false }, // 缩略图
  { label: "360x120", value: "360x120", disabled: false }, // 网站横幅
  { label: "1200x400", value: "1200x400", disabled: false }, // 网站横幅
  { label: "360x200", value: "360x200", disabled: false }, // 主页横幅（h5）
  { label: "1280X720", value: "1280X720", disabled: false }, // 主页横幅（pc）
  { label: "360x240", value: "360x240", disabled: false }, // 文章插图
  { label: "1200x800", value: "1200x800", disabled: false }, // 文章插图
]);

const imageResourceId = ref(props.resourceId);
const imgCropper = ref(undefined);
const imageUrl = ref(props.src);
const cropX = ref(0);
const cropY = ref(0);
const cropWidth = ref(0);
const cropHeight = ref(0);
const fixed = ref(true); // 是否开启截图框宽高固定比例
const aspectRatio = ref(1); // 截图框比例
const cropSize = ref(null); // 预设尺寸
const zoomRatio = ref(1.0); // 缩放比例
const rotate = ref(0); // 旋转角度
const options = ref({
  viewMode: 1,
  dragMode: 'none',
  aspectRatio: 1,
  // autoCropArea: 0.6,
  background: false,
  // center: false,
  movable: false,
  zoomable: true,
  zoomOnWheel: false,
  zoomOnTouch: false,
  checkCrossOrigin: false,
  crop: handleCropEvent,
  ready: handleCropperReady,
});
const cropData = ref({});

watch(() => props.open, (newVal) => {
  visible.value = newVal;
})

watch(() => props.resourceId, (newVal) => {
  imageResourceId.value = newVal || '';
})

watch(visible, (newVal) => {
  if (!newVal) {
    noticeClose();
  } else {
    initCropper();
  }
})

watch(() => props.src, (newVal) => {
  imageUrl.value = newVal || '';
  initCropper();
})

watch(zoomRatio, (newVal) => {
  if (imgCropper.value) {
    imgCropper.value.zoomTo(newVal);
  }
})

function handleTabClick(tab, event) {
  if (activeName.value == 'crop') {
    options.value.aspectRatio = 1;
  } else if (activeName.value == 'rotate') {
    options.value.aspectRatio = NaN;
    options.value.zoomable = true;
  }
  initCropper();
}
function initCropper() {
  if (visible.value) {
    reset();
    nextTick(() => {
      if (imgCropper.value) {
        imgCropper.value.destroy();
      }
      let elImage = proxy.$refs.cropperImage;
      imgCropper.value = new Cropper(elImage, options.value)
    })
  }
}
function handleCropperReady() {
  if (activeName.value == 'crop') {
    let imageData = imgCropper.value.getImageData();
    cropSizeOptions.value.forEach(item => {
      const [width, height] = item.value.split('x').map(Number);
      item.disabled = imageData.naturalWidth < width || imageData.naturalHeight < height;
    })
  } else if (activeName.value == 'rotate') {
    imgCropper.value.clear();
    imgCropper.value.zoomTo(1);
  }
}
function handleCropEvent(e) {
  var data = e.detail;
  cropData.value = data
  cropX.value = Math.round(data.x);
  cropY.value = Math.round(data.y);
  cropWidth.value = Math.round(data.width);
  cropHeight.value = Math.round(data.height);
  rotate.value = typeof data.rotate !== 'undefined' ? data.rotate : 0;
}
function handleCropWidthChange() {
  let imageData = imgCropper.value.getImageData();
  let ratio = imageData.width / imageData.naturalWidth;
  let cropBoxData = imgCropper.value.getCropBoxData();
  cropBoxData.width = cropWidth.value * ratio;
  if (imgCropper.value.aspectRatio && imgCropper.value.aspectRatio > 0) {
    cropBoxData.height = cropBoxData.width / imgCropper.value.aspectRatio;
  }
  imgCropper.value.setCropBoxData(cropBoxData);
}
function handleCropHeightChange() {
  let imageData = imgCropper.value.getImageData();
  let ratio = imageData.width / imageData.naturalWidth;
  let cropBoxData = imgCropper.value.getCropBoxData();
  cropBoxData.height = cropHeight.value * ratio;
  if (imgCropper.value.aspectRatio && imgCropper.value.aspectRatio > 0) {
    cropBoxData.width = cropBoxData.height / imgCropper.value.aspectRatio;
  }
  imgCropper.value.setCropBoxData(cropBoxData);
}
function handleAspectRatioChange() {
  imgCropper.value.setAspectRatio(aspectRatio.value);
  if (fixed.value) {
    let data = imgCropper.value.getCropBoxData();
    imgCropper.value.setAspectRatio(data.width / data.height);
  } else {
    let data = imgCropper.value.getCropBoxData();
    imgCropper.value.setAspectRatio(NaN);
    imgCropper.value.setCropBoxData(data);
  }
}
function handleFixedAspectRatioChange() {
  let data = imgCropper.value.getCropBoxData();
  if (fixed.value) {
    imgCropper.value.setAspectRatio(data.width / data.height);
  } else {
    imgCropper.value.setAspectRatio(NaN);
  }
  imgCropper.value.setCropBoxData(data);
}
function handleCropSizeChange() {
  let imageData = imgCropper.value.getImageData();
  let ratio = imageData.width / imageData.naturalWidth;
  let data = imgCropper.value.getCropBoxData();
  const [width, height] = cropSize.value.split('x').map(Number);
  data.width = width * ratio;
  data.height = height * ratio;
  if (fixed.value) {
    imgCropper.value.setAspectRatio(data.width / data.height);
  }
  imgCropper.value.setCropBoxData(data);
}
function handleSaveCrop() {
  if (activeName.value == 'crop') {
    let data = {
      resourceId: imageResourceId.value, x: cropX.value, y: cropY.value, width: cropWidth.value, height: cropHeight.value
    }
    cropImage(data).then(response => {
      proxy.$modal.notifySuccess(proxy.$t('Common.SaveSuccess'));
      initCropper();
    });
  } else if (activeName.value == 'rotate') {
    let imageData = imgCropper.value.getImageData();
    let data = {
      resourceId: imageResourceId.value,
      rotate: cropData.value.rotate,
      flipX: cropData.value.scaleX == -1,
      flipY: cropData.value.scaleY == -1,
      width: zoomRatio.value * imageData.naturalWidth,
      height: zoomRatio.value * imageData.naturalHeight
    }
    rotateImage(data).then(response => {
      proxy.$modal.notifySuccess(proxy.$t('Common.SaveSuccess'));
      initCropper();
    });
  }
}
function handleZoom(ratio) {
  if (zoomRatio.value + ratio > 1 || zoomRatio.value + ratio < 0.1) {
    return;
  }
  zoomRatio.value += ratio;
}
function showZoomRatio(ratio) {
      return Math.round(ratio*100) + "%";
}
function handleRotate(degree) {
  imgCropper.value.rotate(degree);
}
function handleFlipX() {
  imgCropper.value.scaleX(cropData.value.scaleX == 1 ? -1 : 1);
}
function handleFlipY() {
  imgCropper.value.scaleY(cropData.value.scaleY == 1 ? -1 : 1);
}
function handleClose() {
  visible.value = false;
}
function noticeClose () {
  if (!visible.value) {
    emit('update:open', false);
    emit("close");
  }
}
function reset() {
  cropX.value = 0;
  cropY.value = 0;
  cropWidth.value = 0;
  cropHeight.value = 0;
  fixed.value = true;
  aspectRatio.value = 1;
  cropSize.value = null;
  zoomRatio.value = 1.0;
  rotate.value = 0;
}
</script>
<style lang="scss" scoped>
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
