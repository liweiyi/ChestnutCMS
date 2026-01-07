<template>
  <div>
    <el-dialog 
      :title="$t('WordMgr.TAG.SelectorTitle')"
      v-model="visible"
      width="1100px"
      :close-on-click-modal="false"
      custom-class="tag-selector-dialog"
      append-to-body>
      <el-row :gutter="24">
        <el-col :span="6" :xs="24">
          <cms-tag-word-group-tree 
            ref="groupTreeRef"
            :show-add="false"
            @node-click="handleTreeNodeClick">
          </cms-tag-word-group-tree>
        </el-col>
        <el-col :span="18" :xs="24">
          <el-row :gutter="24">
            <el-col :span="16" :xs="24">
              <span>{{ $t('WordMgr.TAG.TagList') }}</span>
              <el-divider></el-divider>
              <el-tag 
                style="cursor: pointer;margin: 0 5px 5px 0;"
                :key="tag.wordId"
                v-for="tag in tagList"
                :type="tagType(tag)"
                @click="handleSelectTag(tag)"
              >{{ tag.word }}</el-tag>
            </el-col>
            <el-col :span="8" :xs="24">
              <span>{{ $t('WordMgr.TAG.SelectedTagList') }}</span>
              <el-divider></el-divider>
              <el-tag 
                style="cursor: pointer;margin: 0 5px 5px 0;"
                closable
                :key="tag"
                type="success"
                v-for="tag in selectedTags"
                @close="handleRemoveTag(tag)">
                {{ tag }} 
              </el-tag>
            </el-col>
          </el-row>
        </el-col>
      </el-row>
      <template #footer>
        <el-button type="primary" @click="handleOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleClose">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CmsTagSelector">
import { getTagWordList } from "@/api/word/tagWord";
import CmsTagWordGroupTree from '@/views/cms/word/tagWordGroupTree';

const tags = defineModel('tags', {
  type: Array,
  default: () => [],
  required: false
});

const open = defineModel('open', {
  type: Boolean,
  default: false,
  required: true
});

const emit = defineEmits(['update:open', 'update:tags']);

const visible = ref(false);

const loading = ref(false);
const selectedTags = ref([]);
const tagList = ref([]);
const total = ref(0);
const objects = reactive({
  queryParams: {
    query: undefined,
    groupId: 0,
    pageSize: 100,
  }
});
const { queryParams } = toRefs(objects);

watch(open, (newVal) => {
  visible.value = newVal;
  selectedTags.value = [...tags.value];
}, { immediate: true })

watch(visible, (newVal) => {
  if (!newVal) {
    notifyClose();
  } else {
    loadWordList();
  }
})

function tagType(tag) {
  return selectedTags.value.indexOf(tag.word) > -1 ? "info" : "success" 
}
function handleTreeNodeClick(data) {
  const groupId = data && data != null ? data.id : 0;
  if (groupId !== queryParams.value.groupId) {
    queryParams.value.groupId = groupId;
    loadWordList();
  }
}
function loadWordList () {
  if (queryParams.value.groupId > 0) {
    loading.value = true;
    getTagWordList(queryParams.value).then(response => {
      tagList.value = response.data.rows;
      total.value = parseInt(response.data.total);
      loading.value = false;
    });
  }
}
function handleSelectTag (tag) {
  if (selectedTags.value.indexOf(tag.word) > -1) {
    return;
  }
  selectedTags.value.push(tag.word);
  console.log(selectedTags.value);
}
function handleRemoveTag (tagWord) {
  selectedTags.value.splice(selectedTags.value.indexOf(tagWord), 1);
}
function handleOk () {
  emit('update:tags', selectedTags.value);
  visible.value = false;
}
function handleClose () {
  visible.value = false;
}
function notifyClose () {
  selectedTags.value = [];
  emit('update:open', false);
}
</script>
<style rel="stylesheet/scss" lang="scss">
.tag-selector-dialog .el-dialog__body {
  padding-top: 10px;
  padding-bottom: 10px;
}
.tag-selector-dialog .el-divider {
  margin: 15px 0;
}
.tag-selector-dialog .el-tag {
  margin-bottom: 5px;
  margin-right: 5px;
}
.tag-selector-dialog .el-tag.el-tag--success:hover {
  cursor: pointer;
}
.tag-selector-dialog .el-tag.el-tag--info:hover {
  cursor: not-allowed;
}
</style>