<template>
  <div :style="{'width':width}">
    <el-input v-model="model.title" disabled>
      <template #append>
        <el-button 
          type="primary"
          @click="handleSelectContent()"
        >{{ $t('Common.Select') }}</el-button>
        <el-divider direction="vertical"></el-divider>
        <el-button 
          type="primary"
          @click="handleRemove()"
        >{{ $t('Common.Clean') }}</el-button>
      </template>
    </el-input>
    <!-- 内容选择组件 -->
    <cms-content-selector
      v-model:open="openContentSelector"
      @ok="handleContentSelectorOk"
      @close="handleContentSelectorClose"></cms-content-selector>
  </div>
</template>
<script setup name="CmsContentSelector">
import CmsContentSelector from "@/views/cms/contentcore/contentSelector";

const model = defineModel({
  type: [ String, Object ],
  default: {},
  required: true,
});

const props = defineProps({
  selected: {
    default: {
      contentId: '',
      title: '',
    },
  },
  width: {
    type: String,
    default: "100%",
    required: false,
  }
});

const openContentSelector = ref(false)

function handleSelectContent () {
  openContentSelector.value = true;
}

function handleContentSelectorClose() {
  openContentSelector.value = false;
}

function handleContentSelectorOk(contents) {
  if (contents && contents.length > 0) {
    model.value = { contentId: contents[0].contentId, title: contents[0].title };
  }
  openContentSelector.value = false;
}

function handleRemove () {
  model.value = {};
}
</script>