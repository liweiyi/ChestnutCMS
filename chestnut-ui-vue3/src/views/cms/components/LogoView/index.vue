<template>
  <div class="logo-viewer" :style="{'width':imgWidth,'height':imgHeight}">
    <div v-show="showImage" class="picture">
      <el-image 
        ref="imageRef"
        :src="imageSrc"  
        :style="{'width':imgWidth,'height':imgHeight}"
        @click="handleEdit"
        fit="scale-down">
      </el-image>
      <div class="toolbar">
         <el-tooltip class="item" effect="dark" :content="$t('Common.View')" placement="top">
          <el-icon @click="handleView"><Search /></el-icon>
         </el-tooltip>
        <el-tooltip class="item" effect="dark" :content="$t('Common.Edit')" placement="top">
          <el-icon @click="handleEdit"><Edit /></el-icon>
        </el-tooltip>
        <el-tooltip class="item" effect="dark" :content="$t('Common.Remove')" placement="top">
          <el-icon @click="handleRemove"><Delete /></el-icon>
        </el-tooltip>
      </div>
    </div>
    <div v-show="showImageViewer" class="image-viewer">
      <el-image-viewer
        v-if="showImageViewer"
        :url-list="imageViewerList"
        show-progress
        @close="showImageViewer = false"
      />
    </div>
    <div v-show="showText" class="no-picture" :style="{'width':noWidth,'height':noHeight,'font-size':svgSize}">
      <svg-icon icon-class="upload" @click="handleEdit"></svg-icon>
    </div>
    <cms-resource-dialog 
      v-if="appStore.isFree"
      v-model:open="openResourceDialog"
      rtype="image"
      :upload-limit="1" 
      :single="true"
      :siteId="currentSiteId"
      @ok="handleResourceDialogOk">
    </cms-resource-dialog>
  </div>
</template>
<script setup name="CmsLogoView">
import useAppStore from "@/store/modules/app"
const appStore = useAppStore()
import * as utils from "@/utils/chestnut";
import { getResourceDetail } from "@/api/contentcore/resource";
import CmsResourceDialog from "@/views/cms/contentcore/resourceDialog";

const model = defineModel();

const props = defineProps({
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
});

const imageSrc = ref(undefined);
const originalSrc = ref(undefined);
const openResourceDialog = ref(false);
const imageViewerList = ref([]);
const showImageViewer = ref(false);
const currentSiteId = ref('0');
const internalUpdate = ref(false);

const imgWidth = computed(() => {
  return props.width + 'px';
});

const imgHeight = computed(() => {
  return props.height + 'px';
});

const noWidth = computed(() => {
  return (props.width - 3) + 'px';
});

const noHeight = computed(() => {
  return (props.height - 3) + 'px';
});

const svgSize = computed(() => {
  return (props.height - 5) + 'px';
});

const showImage = computed(() => {
  return model.value && model.value.length > 0;
});

const showText = computed(() => {
  return !model.value || model.value.length == 0;
});

const isInternalImage = computed(() => {
  return model.value && model.value.startsWith("iurl://");
});

const resourceId = computed(() => {
  if (model.value && model.value.startsWith("iurl://")) {
    let queryString = model.value.substring(model.value.indexOf('?') + 1)
    let idParam = queryString.split("&").find(item => item.startsWith("id="));
    return idParam.substring(idParam.indexOf('=') + 1);
  }
  return '';
});

watch(model, (newVal) => {
  if (internalUpdate.value) {
    internalUpdate.value = false;
    return;
  }
  loadResourceDetail(newVal);
}, { immediate: true });

watch(() => props.siteId, (newVal) => {
  currentSiteId.value = newVal;
});

watch(imageSrc, (newVal) => {
  updateOriginalSrc();
  updateImageViewerList();
});

function loadResourceDetail(path) {
  if (!path || path.length == 0) {
    updateOriginalSrc();
    updateImageViewerList();
    return;
  }
  if (isInternalImage.value) {
    let queryString = path.substring(path.indexOf('?') + 1);
    let idParam = queryString.split("&").find(item => item.startsWith("id="));
    let resourceId = idParam ? idParam.substring(idParam.indexOf('=') + 1) : "";
    if (resourceId && resourceId.length > 0) {
      getResourceDetail(resourceId).then(res => imageSrc.value = res.data.src);
    }
  } else {
    imageSrc.value = path;
  }
}

function updateImageViewerList() {
  if (imageSrc.value && imageSrc.value.length > 0) {
    imageViewerList.value = [ originalSrc.value ];
  } else {
    imageViewerList.value.splice(0);
  }
}

function updateOriginalSrc() {
  if (!imageSrc.value || imageSrc.value.length == 0) {
    return;
  }
  let fileName = utils.substringAfterLast(imageSrc.value, "/");
  if (isInternalImage.value && fileName.indexOf('_') > -1) {
    let name = utils.substringBeforeLast(fileName, "_") + "." + utils.substringAfterLast(fileName, ".");
    let prefix = utils.substringBeforeLast(imageSrc.value, "/");
    originalSrc.value = utils.setUrlParameter(prefix + "/" + name, "t", new Date().getTime());
  } else {
    originalSrc.value = utils.setUrlParameter(imageSrc.value, "t", new Date().getTime());
  }
}

function handleResourceDialogOk(results) {
  if (results && results.length > 0) {
    const r = results[0];
    model.value = r.path;
    imageSrc.value = r.src;
    internalUpdate.value = true;
  }
}

function handleView() {
  showImageViewer.value = true;
}

function handleEdit() {
  openResourceDialog.value = true;
}

function handleRemove() {
  model.value = "";
}

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
  width: 100%;
  height: 2rem;
  bottom: 0;
  z-index: 100;
  color: #eee;
  background-color: rgba(0,0,0,.5);
  transition: opacity .3s;
  opacity: 0;
  display: flex;
  align-items: center;
  justify-content: space-around;
}
.logo-viewer .toolbar .el-icon {
  font-size: 1rem;
  cursor: pointer;
}
.logo-viewer .toolbar .el-icon:hover {
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