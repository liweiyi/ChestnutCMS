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
import { defineAsyncComponent } from "vue";
import useAppStore from "@/store/modules/app"
const appStore = useAppStore()
import * as utils from "@/utils/chestnut";
import { getResourceDetail } from "@/api/contentcore/resource";
import CmsResourceDialog from "@/views/cms/contentcore/resourceDialog";
import { getOriginalImageSource, withImageVersion } from "@/utils/imageOptimizer/resource.js";

const model = defineModel();
const emit = defineEmits(['update']);

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
const currentSiteId = ref(props.siteId);
const internalUpdate = ref(null);
let detailVersion = 0;

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

watch(() => props.siteId, (newVal, oldVal) => {
  currentSiteId.value = newVal;
  if (oldVal !== undefined && newVal !== oldVal) loadResourceDetail(model.value);
}, { immediate: true });

watch(model, (newVal) => {
  const updatedLocally = internalUpdate.value === newVal;
  internalUpdate.value = null;
  if (updatedLocally) return;
  loadResourceDetail(newVal);
}, { immediate: true });

watch(imageSrc, (newVal) => {
  updateOriginalSrc();
  updateImageViewerList();
});

function loadResourceDetail(path) {
  const version = ++detailVersion;
  if (!path || path.length == 0) {
    imageSrc.value = undefined;
    originalSrc.value = undefined;
    updateOriginalSrc();
    updateImageViewerList();
    return;
  }
  if (utils.isInternalUrl(path)) {
    const resourceId = utils.getInternalUrlId(path);
    const queryAt = path.indexOf('?');
    const referenceSiteId = new URLSearchParams(queryAt < 0 ? '' : path.slice(queryAt + 1).split('#')[0]).get('sid');
    const siteId = /^[1-9]\d*$/.test(referenceSiteId || '') ? referenceSiteId : currentSiteId.value;
    if (resourceId) {
      getResourceDetail(resourceId, { siteId }).then(res => {
        if (version === detailVersion && model.value === path) imageSrc.value = res.data.src;
      });
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
    originalSrc.value = undefined;
    return;
  }
  originalSrc.value = isInternalImage.value
    ? withImageVersion(getOriginalImageSource(imageSrc.value))
    : imageSrc.value;
}

function handleResourceDialogOk(results) {
  if (results && results.length > 0) {
    applyResource(results[0]);
  }
}

function applyResource(resource) {
  const samePath = model.value === resource.path;
  detailVersion++;
  internalUpdate.value = resource.path;
  model.value = resource.path;
  if (samePath) emit('update:modelValue', resource.path);
  imageSrc.value = resource.src;
  updateOriginalSrc();
  updateImageViewerList();
  // Video cover editors also keep a resolved posterSrc beside the bound iurl.
  emit('update', { ...resource, path: resource.path, src: resource.src });
}

function handleView() {
  showImageViewer.value = true;
}

function handleEdit() {
  openResourceDialog.value = true;
}

function handleRemove() {
  detailVersion++;
  internalUpdate.value = null;
  model.value = "";
  imageSrc.value = undefined;
  updateOriginalSrc();
  updateImageViewerList();
  emit('update', { path: '', src: '' });
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