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
            <el-icon @click="handleView(index)"><Search /></el-icon>
          </el-tooltip>
          <el-tooltip class="item" effect="dark" :content="$t('Common.Edit')" placement="top">
            <el-icon @click="handleEdit(index)"><Edit /></el-icon>
          </el-tooltip>
          <el-tooltip class="item" effect="dark" :content="$t('Common.Remove')" placement="top">
            <el-icon @click="handleRemove(index)"><Delete /></el-icon>
          </el-tooltip>
        </div>
      </div>
    </div>
    <div v-if="showAdd" class="no-picture" @click="handleUpload" :style="{'width':noWidth,'height':noHeight,'font-size':svgSize}">
      <el-icon><Plus /></el-icon>
    </div>
    <el-image-viewer 
      v-if="showImageViewer" 
      @close="handleImageViewerClose"
      :initial-index="curIndex" 
      :url-list="originalSrcList">
    </el-image-viewer>
    <cms-resource-dialog 
      v-if="appStore.isFree"
      v-model:open="openResourceDialog"
      rtype="image"
      :upload-limit="1" 
      :single="true"
      @ok="handleResourceDialogOk">
    </cms-resource-dialog>
  </div>
</template>
<script setup name="CmsImageGroup">
import { defineAsyncComponent } from "vue";
import useAppStore from "@/store/modules/app"
const appStore = useAppStore()
import CmsResourceDialog from "@/views/cms/contentcore/resourceDialog";
import { ElImageViewer } from "element-plus"
import { getOriginalImageSource } from "@/utils/imageOptimizer/resource.js";

const model = defineModel();
const emit = defineEmits(['update:src']);

const props = defineProps({
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
})

const imgWidth = computed(() => {
  return props.width + 'px';
})

const imgHeight = computed(() => {
  return props.height + 'px';
})

const noWidth = computed(() => {
  return (props.width + 2) + 'px';
})

const noHeight = computed(() => {
  return (props.height + 2) + 'px';
})

const svgSize = computed(() => {
  return (props.height / 2) + 'px';
})

const imageSrcList = ref([]);
const originalSrcList = ref([]);
const openResourceDialog = ref(false);
const  showImageViewer = ref(false);
const curIndex = ref(-1);
const pendingSources = new Map();

const showAdd = computed(() => {
  let count = 0;
  if (model.value) {
    count =  model.value.length;
  }
  return count < props.limit;
})

watch([() => model.value, () => props.src], ([paths, sources]) => {
  const currentPaths = paths || [];
  for (const [index, pending] of pendingSources) {
    if (currentPaths[index] !== pending.path) pendingSources.delete(index);
  }
  imageSrcList.value = currentPaths.map((path, index) => {
    const incoming = sources?.[index];
    const pending = pendingSources.get(index);
    if (pending && incoming === pending.previousSrc) return pending.src;
    pendingSources.delete(index);
    return incoming || (typeof path === 'string' && !path.startsWith('iurl://') ? path : '');
  });
  originalSrcList.value = imageSrcList.value.map(getOriginalSrc);
}, { immediate: true, deep: true })

function updateImages(paths, sources) {
  pendingSources.clear();
  paths.forEach((path, index) => {
    if (sources[index] !== props.src?.[index]) pendingSources.set(index, {
      path, src: sources[index], previousSrc: props.src?.[index]
    });
  });
  imageSrcList.value = [...sources];
  originalSrcList.value = sources.map(getOriginalSrc);
  model.value = [...paths];
  emit('update:src', [...sources]);
}

function getOriginalSrc(src) {
  if (!src || src.indexOf("/preview/") === -1) {
    return src;
  }
  return getOriginalImageSource(src);
}

function handleResourceDialogOk (results) {
  if (results && results.length > 0) {
    const paths = [...(model.value || [])];
    const sources = [...imageSrcList.value];
    if (curIndex.value < 0) {
      for (let i = 0; i < results.length; i++) {
        if (paths.length < props.limit) {
          paths.push(results[i].path);
          sources.push(results[i].src);
        }
      }
    } else {
      if (curIndex.value >= paths.length) return;
      const r = results[0];
      paths[curIndex.value] = r.path;
      sources[curIndex.value] = r.src;
    }
    updateImages(paths, sources);
  }
}

function handleView (index) {
  curIndex.value = index;
  showImageViewer.value = true;
}

function handleImageViewerClose() {
  showImageViewer.value = false;
}

function handleEdit (index) {
  curIndex.value = index;
  openResourceDialog.value = true;
}

function handleRemove (index) {
  const paths = [...(model.value || [])];
  const sources = [...imageSrcList.value];
  paths.splice(index, 1);
  sources.splice(index, 1);
  updateImages(paths, sources);
}

function handleUpload () {
  curIndex.value = -1;
  openResourceDialog.value = true;
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
  float: left;
  border: 1px dashed #a7a7a7;
  margin-bottom: 5px;
  margin-right: 5px;
}
.logo-viewer .el-image {
  background-color: #E7E7E7;
}
.logo-viewer .toolbar {
  position: absolute;
  text-align: center;
  width: 100%;
  height: 2rem;
  bottom: 0;
  z-index: 100;
  color: #eee;
  background-color: rgba(0,0,0,.5);
  transition: opacity 0.3s;
  opacity: 80;
  display: none;
  align-items: center;
  justify-content: space-around;
}
.logo-viewer .toolbar .el-icon {
  font-size: 1rem;
  width: 1rem;
  height: 1rem;
  cursor: pointer;
}
.logo-viewer .toolbar .el-icon:hover {
  color: #409EFF;
}
.logo-viewer .picture:hover .toolbar {
  display: flex;
}
.logo-viewer .no-picture {
  color: #777;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #a7a7a7;
  margin-bottom: 5px;
  margin-right: 5px;
}
.logo-viewer .no-picture:hover {
  color: #409EFF;
}
</style>