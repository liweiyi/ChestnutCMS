<template>
  <div class="tag-editor" style="padding: 0;">
    <div>
      <el-tag 
        :size="tagSize"
        closable
        :key="tag"
        v-for="tag in tagList"
        :disable-transitions="false"
        @close="handleRemoveTag(tag)">
        {{tag}}
      </el-tag>
    </div>
    <div>
      <el-input
        class="input-new-tag"
        v-if="inputVisible"
        ref="tagInput"
        :size="size"
        v-model="inputValue"
        @keyup.enter.native="handleEnterInputConfirm"
        @blur="handleInputConfirm"
      >
      </el-input>
    </div>
    <el-button-group v-if="!inputVisible">
      <el-button ref="tagAddBtn" class="button-new-tag" icon="el-icon-plus" :size="size" @click="handleNewTag">{{ btnName }}</el-button>
      <el-button v-if="select" class="button-new-tag" icon="el-icon-search" :size="size" @click="handleSelectTag">{{ $t('Common.Select') }}</el-button>
    </el-button-group>
    
    <tag-selector
      :open="showTagSelector"
      :tags="selectedTags"
      @ok="handleTagSelectorOk"
      @close="handleTagSelectorClose"></tag-selector>
  </div>
</template>
<script>
import TagSelector from '@/views/cms/word/tagSelector';

export default {
  name: "CMSTagEditor",
  components: {
    "tag-selector": TagSelector
  },
  model: {
    prop: 'tags',
    event: 'change'
  },
  props: {
    tags: {
      type: Array,
      default: () => [],
      required: true,
    },
    tagSize: {
      type: String,
      default: 'medium',
      required: false,
    },
    size: {
      type: String,
      default: 'mini',
      required: false,
    },
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
  },
  watch: {
    tags(newVal) {
      this.tagList = newVal;
    },
    tagList(newVal) {
      this.selectedTags = [...newVal]
    }
  },
  data () {
    return {
      tagList: this.tags,
      selectedTags: [],
      showTagSelector: false,
      inputValue: '',
      inputVisible: false,
    };
  },
  methods: {
    handleRemoveTag(tag) {
      this.tagList.splice(this.tagList.indexOf(tag), 1);
    },
    handleNewTag() {
      this.inputVisible = true;
      this.$nextTick(_ => {
        this.$refs.tagInput.$refs.input.focus();
      });
    },
    handleEnterInputConfirm() {
      this.handleInputConfirm()
      this.handleNewTag()
    },
    handleInputConfirm() {
      if (this.tagList.indexOf(this.inputValue) > -1) {
        this.$modal.msgWarning("Repeated!");
        return;
      }
      let inputValue = this.inputValue;
      if (inputValue && inputValue.trim().length > 0) {
        this.tagList.push(inputValue.trim());
      }
      this.inputValue = '';
      this.inputVisible = false;
    },
    handleSelectTag() {
      this.showTagSelector = true;
    },
    handleTagSelectorOk(tags) {
      this.showTagSelector = false;
      if (tags) {
        tags.forEach(tag => {
          if (this.tagList.indexOf(tag) < 0) {
            this.tagList.push(tag)
          }
        });
      }
    },
    handleTagSelectorClose() {
      this.showTagSelector = false;
    }
  }
};
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