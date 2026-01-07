<template>
  <div class="tag-editor" style="padding: 0;">
    <div>
      <el-tag 
        closable
        :key="tag"
        v-for="tag in selectedTags"
        :disable-transitions="false"
        @close="handleRemoveTag(tag)"
      >{{tag}}</el-tag>
    </div>
    <div>
      <el-input
        class="input-new-tag"
        v-if="inputVisible"
        ref="tagInputRef"
        v-model="inputValue"
        @keyup="handleEnterInputConfirm"
        @blur="handleInputConfirm"
      >
      </el-input>
    </div>
    <el-button-group v-if="!inputVisible">
      <el-button ref="tagAddBtnRef" class="button-new-tag" icon="Plus" @click="handleNewTag">{{ btnName }}</el-button>
      <el-button v-if="select" class="button-new-tag" icon="Search" @click="handleSelectTag">{{ $t('Common.Select') }}</el-button>
    </el-button-group>
    
    <tag-selector
      v-model:open="showTagSelector"
      v-model:tags="selectedTags"
      @ok="handleTagSelectorOk"></tag-selector>
  </div>
</template>
<script setup name="CmsTagEditor">
import TagSelector from '@/views/cms/word/tagSelector';

const { proxy } = getCurrentInstance()

const selectedTags = defineModel({
  type: Array,
  default: () => [],
  required: false
});

const props = defineProps({
  btnName: {
    type: String,
    default: 'New',
    required: false,
  },
  select: {
    type: Boolean,
    default: false,
    required: false,
  }
});

const showTagSelector = ref(false);
const inputValue = ref('');
const inputVisible = ref(false);

function handleRemoveTag(tag) {
  console.log("handleRemoveTag", tag);
  const arr = selectedTags.value.slice(0);
  arr.splice(arr.indexOf(tag), 1);
  selectedTags.value = arr;
}

function handleNewTag() {
  inputVisible.value = true;
  nextTick(() => {
    proxy.$refs.tagInputRef.focus();
  });
}

function handleEnterInputConfirm(e) {
  if (e.keyCode == 13) { // ENTER
    handleInputConfirm()
    handleNewTag()
  } else if (e.keyCode == 27) { // ESC
    inputValue.value = '';
    inputVisible.value = false;
  }
}

function handleInputConfirm() {
  if (selectedTags.value.indexOf(inputValue.value) > -1) {
    proxy.$modal.msgWarning("Repeated!");
    return;
  }
  if (proxy.$tools.isNotEmpty(inputValue.value)) {
    selectedTags.value.push(inputValue.value.trim());
  }
  inputValue.value = '';
  inputVisible.value = false;
}

function handleSelectTag() {
  showTagSelector.value = true;
}

function handleTagSelectorOk(tags) {
  if (tags) {
    tags.forEach(tag => {
      if (selectedTags.value.indexOf(tag) < 0) {
        selectedTags.value.push(tag)
      }
    });
  }
}
</script>
<style scoped>
.el-tag {
  margin-right: 5px;
}
.button-new-tag {
  margin-left: 0px;
}
.input-new-tag {
  width: 90px;
  vertical-align: bottom;
}
</style>