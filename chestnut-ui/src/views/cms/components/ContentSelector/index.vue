<template>
  <div :style="{'width':width}">
    <el-input v-model="selectedContent.title" disabled>
      <el-button 
        slot="append"
        type="primary"
        @click="handleSelectContent()"
      >{{ $t('Common.Select') }}</el-button>
      <el-divider slot="append" direction="vertical"></el-divider>
      <el-button 
        slot="append"
        type="primary"
        @click="handleRemove()"
      >{{ $t('Common.Clean') }}</el-button>
    </el-input>
    <!-- 内容选择组件 -->
    <cms-content-selector
      :open="openContentSelector"
      @ok="handleContentSelectorOk"
      @close="handleContentSelectorClose"></cms-content-selector>
  </div>
</template>
<script>
import CMSContentSelector from "@/views/cms/contentcore/contentSelector";

export default {
  name: "XModelContentSelector",
  components: {
    'cms-content-selector': CMSContentSelector,
  },
  model: {
    prop: 'value',
    event: 'change'
  },
  props: {
    value: {
      type: String,
      default: "",
      required: true,
    },
    selected: {
      type: [ String, Object ],
      default: {},
      required: true,
    },
    width: {
      type: String,
      default: "100%",
      required: false,
    }
  },
  watch: {  
    selected(newVal) {  
      if (typeof newVal === String) {
        this.selectedContent = newVal == '' ? {} : JSON.parse(newVal)
      }
      this.selectedContent = newVal;  
    },
    selectedContent(newVal) {
      this.$emit("change", this.selectedContent.contentId);
    }
  },
  data () {
    return {
      selectedContent: {},
      openContentSelector: false,
    };
  },
  methods: {
    handleSelectContent () {
      this.openContentSelector = true;
    },
    handleContentSelectorClose() {
      this.openContentSelector = false;
    },
    handleContentSelectorOk(contents) {
      if (contents && contents.length > 0) {
        this.selectedContent = { contentId: contents[0].contentId, title: contents[0].title };
      }
      this.openContentSelector = false;
    },
    handleRemove () {
      this.selectedContent = {};
    }
  }
};
</script>