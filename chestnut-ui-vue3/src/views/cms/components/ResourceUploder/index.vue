<template>
  <div class="file-uploder">
    <div class="upload-wrap">
      <div class="file-wrap" v-for="(file, index) in model" :key="index">
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
    <cms-resource-dialog 
      v-if="appStore.isFree"
      v-model:open="openResourceDialog"
      :rtype="type"
      :upload-limit="1" 
      :single="true"
      @ok="handleResourceDialogOk">
    </cms-resource-dialog>
  </div>
</template>
<script setup name="CmsResourceUploder">
import { isImage, getFileSvgIconClass, setUrlParameter } from "@/utils/chestnut";
import CmsResourceDialog from "@/views/cms/contentcore/resourceDialog";

import useAppStore from "@/store/modules/app"
const appStore = useAppStore()
const { proxy } = getCurrentInstance()

const model = defineModel({
  type: Array,
  default: () => [],
});

const props = defineProps({
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
});

const imgWidth = computed(() => {
  return props.width + 'px';
});
const imgHeight = computed(() => {
  return props.height + 'px';
});
const noWidth = computed(() => {
  return (props.width) + 'px';
});
const noHeight = computed(() => {
  return (props.height) + 'px';
});
const svgSize = computed(() => {
  return (props.height / 2) + 'px';
});
const showAdd = computed(() => {
  return model.value.length < props.limit;
});

const openResourceDialog = ref(false);
const curIndex = ref(-1);

function handleResourceDialogOk (results) {
  if (results && results.length > 0) {
    if (curIndex.value < 0) {
      for (let i = 0; i < results.length; i++) {
        if (model.value.length < props.limit) {
          model.value.push({
            name: results[i].name,
            path: results[i].path,
            src: results[i].src
          });
        }
      }
    } else {
      model.value[curIndex.value] = {
        name: results[0].name,
        path: results[0].path,
        src: results[0].src
      };
    }
  }
}

function handleEdit (index) {
  curIndex.value = index;
  openResourceDialog.value = true;
}

function handleRemove (index) {
  model.value.splice(index, 1);
}

function handleUpload () {
  curIndex.value = -1;
  openResourceDialog.value = true;
}

function getResourceFileIconClass(index) {
  return getFileSvgIconClass(model.value[index].src)
}

function isImageResource (index) {
  return isImage(model.value[index].src);
}

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
  display: flex;
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
  width: 100%;
  height: 1.5rem;
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
.file-uploder .toolbar .el-icon {
  font-size: 1rem;
  width: 1rem;
  height: 1rem;
  cursor: pointer;
}
.file-uploder .toolbar .el-icon:hover {
  color: #409EFF;
}
.file-uploder .no-picture {
  color: #777;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #a7a7a7;
  margin-bottom: 5px;
  margin-right: 5px;
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