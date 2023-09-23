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
        @keyup.enter.native="handleInputConfirm"
        @blur="handleInputConfirm"
      >
      </el-input>
    </div>
    <el-button-group v-if="!inputVisible">
      <el-button class="button-new-tag" icon="el-icon-plus" :size="size" @click="handleNewTag">{{ btnName }}</el-button>
      <el-button v-if="select" class="button-new-tag" icon="el-icon-search" :size="size" @click="handleSelectTag">{{ $t('Common.Select') }}</el-button>
    </el-button-group>
    
    <tag-selector
      :open="showTagSelector"
      @ok="handleTagSelectorOk"
      @close="handleTagSelectorClose"></tag-selector>
  </div>
</template>
<script>
import TagSelector from '@/views/word/tagSelector';

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
      default: [],
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
    }
  },
  data () {
    return {
      tagList: this.tags,
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
    handleInputConfirm() {
      if (this.tagList.indexOf(this.inputValue) > -1) {
        this.$modal.msgWarning("Repeated!");
        return;
      }
      let inputValue = this.inputValue;
      if (inputValue) {
        this.tagList.push(inputValue.trim());
      }
      this.inputVisible = false;
      this.inputValue = '';
    },
    handleSelectTag() {
      this.showTagSelector = true;
    },
    handleTagSelectorOk(tags) {
      this.showTagSelector = false;
      if (tags) {
        tags.forEach(tag => this.tagList.push(tag));
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
  margin-left: 10px;
  vertical-align: bottom;
}
</style>