<template>
  <div class="office-preview">
    <!-- .docx 预览 -->
    <VueOfficeDocx
      v-if="shouldRender && type === 'word'"
      :src="src"
      class="office-component"
      @rendered="handleRendered"
      @error="handleError" />

    <!-- .xlsx / .xls 预览 -->
    <VueOfficeExcel
      v-else-if="shouldRender && type === 'excel'"
      :src="src"
      class="office-component"
      @rendered="handleRendered"
      @error="handleError" />

    <!-- .pptx 预览 -->
    <VueOfficePptx
      v-else-if="shouldRender && type === 'ppt'"
      :src="src"
      class="office-component"
      @rendered="handleRendered"
      @error="handleError" />

    <!-- 老版本 .doc/.ppt 不支持在线预览 -->
    <div v-else-if="unsupported" class="unsupported-overlay">
      <el-empty :description="$t('Common.FilePreview.OfficeUnsupported')">
        <el-button type="primary" @click="handleDownload">
          {{ $t('Common.Download') }}
        </el-button>
      </el-empty>
    </div>

    <el-empty v-else :description="$t('Common.FilePreview.NoSrc')" />
  </div>
</template>

<script setup name="FilePreviewOffice">
/**
 * Office 文件预览组件
 * 基于 vue-office 库，支持 .docx / .xlsx / .xls / .pptx 文件预览
 * 注意：vue-office 不支持老版本 .doc / .ppt，需通过 download 兜底
 */
import { defineAsyncComponent } from 'vue';
import { getPreviewType, isWord, isExcel, isPpt } from "@/utils/chestnut";

// 三个 vue-office 组件按需异步加载，避免一次性引入所有包增大首屏体积
const VueOfficeDocx = defineAsyncComponent(() => import('@vue-office/docx'));
const VueOfficeExcel = defineAsyncComponent(() => import('@vue-office/excel'));
const VueOfficePptx = defineAsyncComponent(() => import('@vue-office/pptx'));

// 样式需要静态引入（CSS 不能异步加载）
import '@vue-office/docx/lib/index.css';
import '@vue-office/excel/lib/index.css';

const props = defineProps({
  src: {
    type: String,
    default: ""
  },
  // 指定类型，未指定时根据 src 自动识别；可选值：word | excel | ppt
  officeType: {
    type: String,
    default: ""
  }
});

// 判断是否为 vue-office 支持的格式（.docx/.xlsx/.xls/.pptx）
const isSupported = (src, type) => {
  if (!src) return false;
  const lower = src.toLowerCase();
  if (type === 'word') return lower.endsWith('.docx');
  if (type === 'excel') return lower.endsWith('.xlsx') || lower.endsWith('.xls');
  if (type === 'ppt') return lower.endsWith('.pptx');
  return false;
};

const type = computed(() => {
  if (props.officeType) return props.officeType;
  return getPreviewType(props.src);
});

// 老格式 .doc/.ppt 无法预览
const unsupported = computed(() => {
  if (!props.src) return false;
  if (type.value === 'word' && props.src.toLowerCase().endsWith('.doc')) return true;
  if (type.value === 'ppt' && props.src.toLowerCase().endsWith('.ppt')) return true;
  return false;
});

// 同时满足 src 存在 + 类型是 word/excel/ppt + 格式受 vue-office 支持
const shouldRender = computed(() => {
  return !!props.src && isSupported(props.src, type.value);
});

const emit = defineEmits(['rendered', 'error']);

const handleRendered = () => {
  emit('rendered');
};

const handleError = (err) => {
  emit('error', err);
};

const handleDownload = () => {
  if (props.src) {
    window.open(props.src, '_blank');
  }
};
</script>

<style lang="scss" scoped>
.office-preview {
  width: 100%;
  height: 100%;
  position: relative;
  background-color: #fff;
  overflow: auto;

  .office-component {
    width: 100%;
    min-height: 100%;
  }

  .unsupported-overlay {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #fff;
  }
}
</style>
