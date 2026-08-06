<template>
  <div class="app-container resource-preview" v-loading="loading">
    <el-container>
      <el-header class="preview-header" height="56px">
        <div class="header-left">
          <el-button icon="Close" plain @click="handleClose">{{ $t('Common.Close') }}</el-button>
        </div>
        <div class="header-title" :title="resource.name">{{ resource.name }}</div>
        <div class="header-right">
          <el-link v-if="resource.src" type="primary" :href="resource.src" target="_blank" :underline="false">
            <el-icon><Download /></el-icon>
            {{ $t('Common.Download') }}
          </el-link>
        </div>
      </el-header>
      <el-main class="preview-main">
        <el-empty v-if="!loading && !resource.src" :description="$t('Common.FilePreview.NoSrc')" />

        <!-- 外部 http(s) 链接：直接用 iframe 显示 -->
        <IframePreview
          v-else-if="isExternalLink"
          :src="resource.src" />

        <!-- 内部资源：根据文件类型选择预览方式 -->
        <!-- 图片 / 音频 / 视频：使用 HTML5 原生标签 -->
        <MediaPreview
          v-else-if="previewType === 'image' || previewType === 'audio' || previewType === 'video'"
          :src="resource.src"
          :media-type="previewType" />

        <!-- Word / Excel / PPT：使用 vue-office -->
        <OfficePreview
          v-else-if="previewType === 'word' || previewType === 'excel' || previewType === 'ppt'"
          :src="resource.src"
          :office-type="previewType" />

        <!-- PDF：使用 iframe -->
        <IframePreview
          v-else-if="previewType === 'pdf'"
          :src="resource.src" />

        <!-- 不支持的类型 -->
        <div v-else-if="!loading" class="unsupported-tip">
          <el-empty :description="$t('Common.FilePreview.Unsupported')">
            <el-button type="primary" @click="handleDownload">
              {{ $t('Common.Download') }}
            </el-button>
          </el-empty>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup name="CmsContentcoreResourcePreview">
import { Download } from '@element-plus/icons-vue';
import MediaPreview from "@/components/FilePreview/MediaPreview.vue";
import OfficePreview from "@/components/FilePreview/OfficePreview.vue";
import IframePreview from "@/components/FilePreview/IframePreview.vue";
import { getResourceDetail } from "@/api/contentcore/resource";
import { getPreviewType, isInternalUrl, getInternalUrlId } from "@/utils/chestnut";
const route = useRoute();
const router = useRouter();
const { proxy } = getCurrentInstance();

const loading = ref(false);
const resource = ref({});
const previewType = computed(() => getPreviewType(resource.value.src));
// 是否为外部 http(s) 链接（外部链接统一用 iframe 显示）
const isExternalLink = ref(false);

// 通过 iurl 解析 resourceId 并获取资源详情
const loadResourceByIurl = (iurl) => {
  const resourceId = getInternalUrlId(iurl);
  if (!resourceId) {
    proxy.$modal.msgError(proxy.$t('Common.FilePreview.NoSrc'));
    return;
  }
  loading.value = true;
  isExternalLink.value = false;
  getResourceDetail(resourceId).then(response => {
    resource.value = response.data || {};
    if (resource.value.name) {
      document.title = resource.value.name;
    }
    loading.value = false;
  }).catch(() => {
    loading.value = false;
  });
}

// 外部链接直接预览（用 iframe 显示）
const loadExternalSrc = (url, name) => {
  isExternalLink.value = true;
  let fileName = name || '';
  if (!fileName) {
    try {
      const u = new URL(url, window.location.origin);
      fileName = decodeURIComponent(u.pathname.substring(u.pathname.lastIndexOf('/') + 1));
    } catch (e) {
      fileName = url.substring(url.lastIndexOf('/') + 1);
    }
  }
  resource.value = { src: url, name: fileName };
  if (fileName) {
    document.title = fileName;
  }
}

const handleClose = () => {
  // 预览页是独立浏览器标签，直接关闭当前窗口
  window.close();
}

const handleDownload = () => {
  if (resource.value.src) {
    window.open(resource.value.src, '_blank');
  }
}

onMounted(() => {
  document.body.style['overflow-y'] = 'hidden';
  const iurl = route.query.iurl;
  const name = route.query.name;
  if (iurl) {
    if (isInternalUrl(iurl)) {
      // iurl:// 内部资源：解析 resourceId 获取详情
      loadResourceByIurl(iurl);
    } else {
      // http(s):// 外部链接：直接用 iframe 显示
      loadExternalSrc(iurl, name);
    }
  } else {
    proxy.$modal.msgError(proxy.$t('Common.FilePreview.NoSrc'));
  }
});
</script>

<style lang="scss" scoped>
.resource-preview {
  padding: 0;

  :deep(.el-container) {
    height: 100vh;
  }

  .preview-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 16px;
    background-color: #fff;
    border-bottom: 1px solid var(--el-border-color-lighter);

    .header-left,
    .header-right {
      flex: 0 0 auto;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .header-title {
      flex: 1;
      text-align: center;
      font-size: 15px;
      font-weight: 500;
      color: var(--el-text-color-primary);
      padding: 0 16px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .preview-main {
    padding: 0;
    background-color: #f5f5f5;
    overflow: auto;
  }

  .unsupported-tip {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

:deep(.el-empty) {
  background-color: #fff;
  border-radius: 4px;
}
</style>
