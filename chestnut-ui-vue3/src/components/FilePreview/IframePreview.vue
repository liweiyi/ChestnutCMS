<template>
  <div class="iframe-preview">
    <iframe
      v-if="src"
      :src="src"
      class="preview-iframe"
      frameborder="0"
      allowfullscreen></iframe>
    <el-empty v-else :description="$t('Common.FilePreview.NoSrc')" />
  </div>
</template>

<script setup name="FilePreviewIframe">
/**
 * Iframe 预览组件
 * 适用于 PDF、以及浏览器原生支持的文档预览场景
 */
defineProps({
  src: {
    type: String,
    default: ""
  }
});
</script>

<style lang="scss" scoped>
.iframe-preview {
  width: 100%;
  height: 100%;

  .preview-iframe {
    width: 100%;
    height: 100%;
    border: none;
    display: block;
  }
}
</style>
