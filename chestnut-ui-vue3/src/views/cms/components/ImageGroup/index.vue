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
      :on-close="handleImageViewerClose"
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
import useAppStore from "@/store/modules/app"
const appStore = useAppStore()
import CmsResourceDialog from "@/views/cms/contentcore/resourceDialog";
import { ElImageViewer } from "element-plus"

const { proxy } = getCurrentInstance()

const model = defineModel();

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

const showAdd = computed(() => {
  let count = 0;
  if (model.value) {
    count =  model.value.length;
  }
  return count < props.limit;
})

watch(() => props.src, (newVal) => {
  imageSrcList.value = newVal;
  originalSrcList.value = []
  for (let i = 0; i < newVal.length; i++) {
    if (newVal[i].indexOf("/preview/") > -1) {
      let fileName = proxy.$tools.substringAfterLast(newVal[i], "/");
      if (fileName.indexOf('_') > -1) {
        let name = utils.substringBeforeLast(fileName, "_") + "." + utils.substringAfterLast(fileName, ".");
        let prefix = utils.substringBeforeLast(newVal[i], "/");
        originalSrcList.value.push(prefix + "/" + name)
      }
    } else {
      originalSrcList.value.push(newVal[i]);
    }
  }
})

function handleResourceDialogOk (results) {
  if (results && results.length > 0) {
    if (curIndex.value < 0) {
      for (let i = 0; i < results.length; i++) {
        if (model.value.length < props.limit) {
          model.value.push(results[i].path);
          imageSrcList.value.push(results[i].src);
        }
      }
    } else {
      const r = results[0];
      model.value[curIndex.value] = r.path;
      imageSrcList.value[curIndex.value] = r.src
    }
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
  model.value.splice(index, 1);
  imageSrcList.value.splice(index, 1);
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