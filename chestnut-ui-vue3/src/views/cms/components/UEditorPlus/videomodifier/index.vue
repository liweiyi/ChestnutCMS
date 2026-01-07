<template>
  <div style="padding: 0;">
    <el-dialog 
      class="video-modifier"
      :title="$t('CMS.UEditor.VideoModifier.DialogTitle')"
      v-model="visible"
      width="700px"
      :close-on-click-modal="false"
      append-to-body>
      <el-form ref="formVideoRef" :model="formData" label-position="top" :loading="loading">
        <el-row :gutter="24">
          <el-col :span="16">
            <el-form-item :label="$t('CMS.UEditor.VideoModifier.Cover')" prop="poster">
              <cms-logo-view v-model="formData.poster" @update="handleUploadCoverChange" :width="435" :height="260"></cms-logo-view>
              <div class="mt10 display-flex align-items-center">
                <div class="mr5">{{ $t('CMS.UEditor.VideoModifier.FrameCoverSeconds') }}</div>
                <el-input-number v-model="screenshotTime" :min="0" style="width:180px" class="mr5"></el-input-number>
                <el-button type="primary" @click="handleScreenshot">{{ $t('CMS.UEditor.VideoModifier.Screenshot') }}</el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item :label="$t('CMS.UEditor.VideoModifier.Width')" prop="width">
              <el-input v-model="formData.width" style="width:180px"></el-input>
            </el-form-item>
            <el-form-item :label="$t('CMS.UEditor.VideoModifier.Height')" prop="height">
              <el-input v-model="formData.height" style="width:180px"></el-input>
            </el-form-item>
            <el-form-item prop="controls">
              <el-checkbox v-model="formData.controls">{{ $t('CMS.UEditor.VideoModifier.Controls') }}</el-checkbox>
            </el-form-item>
            <el-form-item prop="loop">
              <el-checkbox v-model="formData.loop">{{ $t('CMS.UEditor.VideoModifier.Loop') }}</el-checkbox>
            </el-form-item>
            <el-form-item prop="muted">
              <el-checkbox v-model="formData.muted">{{ $t('CMS.UEditor.VideoModifier.Muted') }}</el-checkbox>
            </el-form-item>
            <el-form-item prop="autoplay">
              <el-checkbox v-model="formData.autoplay">{{ $t('CMS.UEditor.VideoModifier.AutoPlay') }}</el-checkbox>
            </el-form-item>
            <!-- <el-form-item v-if="!formData.autoplay" :label="$t('CMS.UEditor.VideoModifier.Preload')" prop="preload">
              <el-select v-model="formData.preload">
                <el-option value="none">{{ $t('CMS.UEditor.VideoModifier.PreloadNone') }}</el-option>
                <el-option value="meta">{{ $t('CMS.UEditor.VideoModifier.PreloadMeta') }}</el-option>
                <el-option value="auto">{{ $t('CMS.UEditor.VideoModifier.PreloadAuto') }}</el-option>
              </el-select>
            </el-form-item> -->
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleCancel">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CMSUeditorVideoModifier">
import CmsLogoView from '@/views/cms/components/LogoView';
import { videoScreenshot } from "@/api/contentcore/video";

const { proxy } = getCurrentInstance()

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
    required: true
  },
  data: {
    type: Object,
    default: () => {},
    required: true,
  }
})

const emit = defineEmits(['update:open', 'ok', 'close']);

const loading = ref(false);
const screenshotTime = ref(0);
const visible = ref(false);
const objects = reactive({
  formData: {},
})
const { formData } = toRefs(objects);

watch(() => props.data, (newVal) => {
  formData.value = newVal;
}, { immediate: true })
watch(() => props.open, (newVal) => {
  console.log('props.open', newVal);
  visible.value = newVal;
})
watch(visible, (newVal) => {
  if (!newVal) {
    noticeClose();
  }
})

function handleScreenshot() {
  loading.value = true;
  const data = { path: formData.value.videoIUrl, timestamp: screenshotTime.value }
  videoScreenshot(data).then(res => {
    formData.value.poster = res.data.internalUrl;
    formData.value.posterSrc = res.data.src;
    loading.value = false;
  })
}

function handleUploadCoverChange(data) {
  formData.value.poster = data.path;
  formData.value.posterSrc = data.src;
}

function handleOk () {
  noticeOk();
}

function handleCancel () {
  visible.value = false;
}

function noticeOk () {
  if (visible.value) {
    proxy.$refs.formVideoRef.validate(valid => {
      if (valid) {
        emit("ok", formData.value);
        visible.value = false;
      }
    });
  }
}

function noticeClose () {
  if (!visible.value) {
    emit("update:open", false);
    emit("close");
    formData.value = {}
  }
}
</script>
<style scoped>
:deep(.el-dialog .el-form-item) {
  margin-bottom: 0;
}
:deep(.el-dialog .el-form-item__label) {
  padding-bottom: 5px;
}
:deep(.el-dialog .el-form--label-top .el-form-item__label) {
  padding-bottom: 0;
}
</style>