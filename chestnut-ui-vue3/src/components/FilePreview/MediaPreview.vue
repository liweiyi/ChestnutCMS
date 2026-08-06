<template>
  <div class="media-preview">
    <div v-if="type === 'image'" class="preview-wrap image-wrap">
      <el-image
        :src="src"
        :preview-src-list="[src]"
        fit="contain"
        hide-on-click-modal
        class="preview-image" />
    </div>
    <div v-else-if="type === 'audio'" class="preview-wrap audio-wrap">
      <div class="audio-icon">
        <svg-icon icon-class="file-audio" class-name="audio-svg" />
      </div>
      <audio :src="src" controls class="preview-audio"></audio>
    </div>
    <div v-else-if="type === 'video'" class="preview-wrap video-wrap">
      <video :src="src" controls class="preview-video"></video>
    </div>
    <div v-else class="preview-wrap unsupported">
      <el-empty :description="$t('Common.FilePreview.Unsupported')" />
    </div>
  </div>
</template>

<script setup name="FilePreviewMedia">
/**
 * 通用媒体预览组件
 * 使用 HTML5 原生标签处理图片（el-image）、音频（audio）、视频（video）
 */
import { getPreviewType } from "@/utils/chestnut";

const props = defineProps({
  src: {
    type: String,
    default: ""
  },
  // 指定类型，未指定时根据 src 自动识别
  mediaType: {
    type: String,
    default: ""
  }
});

const type = computed(() => {
  if (props.mediaType) return props.mediaType;
  return getPreviewType(props.src);
});
</script>

<style lang="scss" scoped>
.media-preview {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;

  .preview-wrap {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-direction: column;
  }

  .image-wrap {
    .preview-image {
      max-width: 100%;
      max-height: 100%;

      :deep(img) {
        width: 100%;
        height: 100%;
        object-fit: contain;
      }
    }
  }

  .audio-wrap {
    gap: 24px;

    .audio-icon {
      .audio-svg {
        width: 120px;
        height: 120px;
        opacity: 0.7;
      }
    }

    .preview-audio {
      width: 80%;
      max-width: 600px;
      outline: none;
    }
  }

  .video-wrap {
    .preview-video {
      max-width: 100%;
      max-height: 100%;
      background-color: #000;
    }
  }

  .unsupported {
    color: var(--el-text-color-secondary);
  }
}
</style>
