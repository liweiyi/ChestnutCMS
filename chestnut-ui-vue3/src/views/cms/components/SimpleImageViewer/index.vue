<template>
  <!-- 此组件上传图片需指定action，上传的图片不作为网站素材库资源记录，一般用来上传固定路径图片，比如站点LOGO,会员头像等 -->
  <div
    class="simple-image-viewer"
    :style="{ width: imgWidth, height: viewerHeight }"
  >
    <el-upload
      ref="uploadRef"
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
      auto-upload
    >
      <div class="picture" v-show="showImage">
        <el-image
          ref="imageRef"
          :src="imageSrc"
          :style="{ width: imgWidth, height: imgHeight }"
          fit="scale-down"
          :preview-src-list="imageViewerList"
        >
        </el-image>
        <div class="toolbar">
          <el-tooltip
            class="item"
            effect="dark"
            :content="$t('Common.View')"
            placement="top"
          >
            <el-icon @click="handleView" style="width: var(--font-size);"><Search /></el-icon><
          </el-tooltip>
          <el-tooltip
            class="item"
            effect="dark"
            :content="$t('Common.Remove')"
            placement="top"
          >
            <el-icon @click="handleRemove" style="width: var(--font-size);"><Delete /></el-icon>
          </el-tooltip>
        </div>
      </div>
      <div
        v-show="showText"
        class="no-picture"
        :style="{ width: noWidth, height: noHeight, 'font-size': svgSize }"
      >
        <svg-icon icon-class="upload"></svg-icon>
      </div>
      <template #tip>
        <div class="el-upload__tip" style="margin: 10px 0">
          只能上传jpg/png文件，且不超过200kb
        </div>
      </template>
    </el-upload>
  </div>
</template>
<script setup name="CMSSimpleImageView">
const { proxy } = getCurrentInstance();

const model = defineModel();

const props = defineProps({
  src: {
    type: String,
  },
  action: {
    type: String,
    required: true,
  },
  site: {
    type: String,
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
});

const emit = defineEmits(["change"]);

const imageSrc = ref(props.src);
const imageViewerList = ref([]);
const objects = reactive({
  upload: {
    isUploading: false,
    accept: ".jpg,.jpeg,.png",
    limit: 1,
    headers: { ...proxy.$auth.getTokenHeader() },
    url: import.meta.env.VITE_APP_BASE_API + props.action, //"/cms/site/upload_watermarkimage",
    fileList: [],
    data: { siteId: props.site },
  }
});
const { upload } = toRefs(objects);

const imgWidth = computed(() => {
  return props.width + "px";
});
const imgHeight = computed(() => {
  return props.height + "px";
});
const viewerHeight = computed(() => {
  return props.height + 10 + "px";
});
const noWidth = computed(() => {
  return props.width + "px";
});
const noHeight = computed(() => {
  return props.height + "px";
});
const svgSize = computed(() => {
  return props.height - 5 + "px";
});

const showImage = computed(() => {
  return proxy.$tools.isNotEmpty(model.value);
});
const showText = computed(() => {
  return proxy.$tools.isEmpty(model.value);
});

watch(() => props.site, (newVal, oldVal) => {
  upload.value.data.siteId = newVal;
});
watch(() => props.src, (newVal) => {
  imageSrc.value = newVal;
});
watch(imageSrc, (newVal) => {
  if (proxy.$tools.isNotEmpty(newVal)) {
    imageViewerList.value = [ newVal ];
  } else {
    imageViewerList.value.splice(0);
  }
});

const handleFileUploadProgress = (event, file, fileList) => {
  upload.value.isUploading = true;
};

const handleFileUploadSuccess = (response, file, fileList) => {
  onFileUploaded(response.code == 200, fileList, response.code == 200 ? response.data : response.msg);
};

const handleFileUploadError = (err, file, fileList) => {
  onFileUploaded(false, fileList, err);
}

const onFileUploaded = (isSuccess, fileList, result) => {
  upload.value.isUploading = false;
  if (isSuccess) {
    model.value = result.path;
    imageSrc.value = result.src + "?" + Date.now();
    upload.value.fileList = [];
    emit("update:src", imageSrc.value);
  } else {
    proxy.$modal.msgError(result);
  }
};
    
const handleRemove = (event) => {
  event.stopPropagation();
  model.value = "";
  imageSrc.value = "";
  upload.value.fileList = [];
  emit("update:src", imageSrc.value);
};

const handleView = (event) => {
  event.stopPropagation();
  proxy.$refs.imageRef.showPreview();
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
  background-color: #e7e7e7;
}
.simple-image-viewer .toolbar {
  position: absolute;
  text-align: center;
  width: 100%;
  height: 30px;
  top: 120px;
  z-index: 100;
  color: #eee;
  background-color: rgba(0, 0, 0, 0.5);
  transition: opacity 0.3s;
  opacity: 0;
  display: flex;
  align-items: center;
  justify-content: space-around;
}
.simple-image-viewer .toolbar i {
  font-size: 16px;
  padding: 7px;
  cursor: pointer;
}
.simple-image-viewer .toolbar i:hover {
  color: #409eff;
}
.simple-image-viewer .no-picture {
  border: 3px dashed #a7a7a7;
  text-align: center;
  color: #777;
  cursor: pointer;
}
.simple-image-viewer .no-picture:hover {
  color: #409eff;
}
</style>
