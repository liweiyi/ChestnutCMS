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
<script setup name="CommonImageView">
import { ElImageViewer } from "element-plus"

const model = defineModel();

const { proxy } = getCurrentInstance();

const props = defineProps({
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
});

const imageSrc = ref(import.meta.env.VITE_APP_BASE_API + props.src);
const showImageViewer = ref(false);
const imageViewerList = ref([]);
const objects = ref({
  upload: {
    accept: ".jpg,.jpeg,.png",
    limit: 1,
    filesize: props.sizelimit * 1024,
    headers: { ...proxy.$auth.getTokenHeader() },
    url: import.meta.env.VITE_APP_BASE_API + props.action, //"/cms/site/upload_watermarkimage",
    fileList: [],
    data : props.data
  }
});
const { upload } = toRefs(objects)

const imgWidth = computed(() => {
  return props.width + 'px';
});
const imgHeight = computed(() => {
  return props.height + 'px';
});
const viewerHeight = computed(() => {
  return (props.height + 10) + 'px';
});
const noWidth = computed(() => {
  return (props.width) + 'px';
});
const noHeight = computed(() => {
  return (props.height) + 'px';
});
const svgSize = computed(() => {
  return (props.height - 5) + 'px';
});
const showImage = computed(() => {
  return model.value != undefined && model.value != null && model.value.length > 0;
});
const showText = computed(() => {
  return model.value == undefined || model.value == null || model.value.length == 0;
});

watch(() => props.site, (newVal) => {
  upload.value.data.siteId = newVal;
});
watch(() => props.src, (newVal) => {
  imageSrc.value = import.meta.env.VITE_APP_BASE_API + newVal;
});
watch(imageSrc, (newVal) => {
  if (newVal && newVal.length > 0) {
    imageViewerList.value = [ newVal ];
  } else {
    imageViewerList.value.splice(0);
  }
});

function handleBeforeUpload(file) {
  if (upload.value.accept) {
    const fileName = file.name.split('.');
    const fileExt = "." + fileName[fileName.length - 1];
    const isTypeOk = upload.value.accept.split(',').indexOf(fileExt) >= 0;
    if (!isTypeOk) {
      proxy.$modal.msgError(proxy.$t('Common.InvalidFileSuffix', [ upload.value.accept ]));
      return false;
    }
  }
  if (file.size > upload.value.filesize) {
    var s = upload.value.filesize / 1024;
    if (s < 1024) {
      proxy.$modal.msgError(proxy.$t('Component.CommonImageViewer.UploadSizeErr', [ s + " KB" ]));
    } else {
      proxy.$modal.msgError(proxy.$t('Component.CommonImageViewer.UploadSizeErr', [ (s / 1024) + " MB" ]));
    }
    return false;
  }
  proxy.$modal.loading(proxy.$t('Component.CommonImageViewer.Uploading'));
  return true;
}

function handleFileUploadSuccess (response, file, fileList) {
  onFileUploaded(response.code == 200, fileList, response.code == 200 ? response.data : response.msg);
}
function handleFileUploadError (err, file, fileList) {
  onFileUploaded(false, fileList, err);
  proxy.$modal.closeLoading()
}
function onFileUploaded(isSuccess, fileList, result) {
  proxy.$modal.closeLoading()
  if (isSuccess) {
    imagePath.value = result.path;
    imageSrc.value = import.meta.env.VITE_APP_BASE_API + result.src + "?" + Date.now();
    upload.value.fileList = [];
  } else {
    proxy.$modal.msgError(result);
  }
}

function handleView (event) {
  event.stopPropagation();
  showImageViewer.value = true;
}

function handleImageViewerClose() {
  showImageViewer.value = false;
}

function handleRemove (event) {
  event.stopPropagation();
  imagePath.value = "";
  upload.value.fileList = [];
}
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