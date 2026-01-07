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
        <el-col :span="4" :xs="24">
          <cms-tag-word-group-tree 
            ref="groupTreeRef"
            :show-add="false"
            @node-click="handleTreeNodeClick">
          </cms-tag-word-group-tree>
        </el-col>
        <el-col :span="20" :xs="24">
          <el-row :gutter="24">
            <el-col :span="16" :xs="24">
              <span>{{ $t('WordMgr.TAG.TagList') }}</span>
              <el-divider />
              <el-tag 
                :key="tag.wordId"
                v-for="tag in tagList"
                @click="handleSelectTag(tag)">
                {{ tag.word }} 
              </el-tag>
            </el-col>
            <el-col :span="8" :xs="24">
              <span>{{ $t('WordMgr.TAG.SelectedTagList') }}</span>
              <el-divider />
              <el-tag 
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
<script setup name="TagSelector">
import { getTagWordList } from "@/api/word/tagWord";
import CmsTagWordGroupTree from '@/views/word/tagWordGroupTree';

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
    required: true
  }
});

watch(() => props.open, (newVal) => {
  if (!newVal) {
    handleClose();
  } else {
    loadWordList();
  }
}, { immediate: true })

const loading = ref(false);
const selectedTags = ref([]);
const tagList = ref([]);
const total = ref(0);
const objects = reactive({
  queryParams: {
    query: undefined,
    groupId: 0,
    pageSize: 100,
    pageNum: 1
  }
});
const { queryParams } = toRefs(objects);

function handleTreeNodeClick(data) {
  const groupId = data && data != null ? data.id : 0;
  if (groupId.value !== queryParams.value.groupId) {
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
}
function handleRemoveTag (tagWord) {
  selectedTags.value.splice(selectedTags.value.indexOf(tagWord), 1);
}
function handleOk () {
  emit("ok", selectedTags.value);
}
function handleClose () {
  emit("close");
  queryParams.value.groupId = 0;
  selectedTags.value = [];
  tagList.value = [];
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