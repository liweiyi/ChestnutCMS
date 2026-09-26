<template>
  <div class="math-captcha-container">
    <el-input
      v-model="authCode"
      type="text"
      size="large"
      auto-complete="off"
      :placeholder="$t('Login.Captcha')"
      class="captcha-code"
      :disabled="!ready || loading"
    >
      <template #prefix>
        <svg-icon icon-class="validCode" class="el-input__icon input-icon" />
      </template>
      <template #append>
        <span v-if="codeImage!=''" class="captcha-image-wrap" :style="{ width: `${imgWidth}`, height: `${imgHeight}` }">
          <img :src="codeImage" class="captcha-img" :class="{ 'is-refresh-disabled': isRefreshDisabled }" @click="handleReloadCaptcha" />
          <span v-if="leftSeconds > 0" class="captcha-countdown">{{ leftSeconds }}s</span>
        </span>
        <el-link v-else icon="Refresh" underline="never" :disabled="isRefreshDisabled" @click="handleReloadCaptcha" :style="{ width: `${imgWidth}` }">
          {{ $t('Common.Refresh') }}<span v-if="leftSeconds > 0"> ({{ leftSeconds }}s)</span>
        </el-link>
      </template>
    </el-input>
    <div v-if="errorMessage" class="captcha-error">{{ errorMessage }}</div>
  </div>
</template>
<script setup>
import useCaptcha from '../useCaptcha'

const { proxy } = getCurrentInstance()
const model = defineModel()
const emit = defineEmits(['loading-change'])
const props = defineProps({
  width: { type: Number, default: 100 },
  height: { type: Number, default: 38 },
  token: { type: String, default: '' },
  expires: { type: Number, default: 0 }
})
const authCode = ref('')
const {
  loading, ready, images, errorMessage, leftSeconds, isRefreshDisabled,
  reloadCaptcha, handleReloadCaptcha, resetCaptcha, isValid
} = useCaptcha({
  props, model, emit, type: 'Math', t: proxy.$t,
  imageSources: data => ['data:image/gif;base64,' + data.image]
})
const codeImage = computed(() => images.value[0]?.src || '')
const imgWidth = computed(() => props.width > 0 ? props.width + 'px' : 'auto')
const imgHeight = computed(() => props.height > 0 ? props.height + 'px' : 'auto')

watch(ready, value => { if (!value) authCode.value = '' }, { flush: 'sync' })
watch(authCode, value => {
  model.value = ready.value && value.trim()
    ? { type: 'Math', token: props.token, data: value.trim() }
    : null
}, { flush: 'sync' })

defineExpose({ reloadCaptcha, resetCaptcha, isValid })
</script>
<style lang="scss" scoped>
.math-captcha-container {
  width: 100%;

  .captcha-error {
    color: var(--el-color-danger);
    font-size: 12px;
  }

  .input-icon {
    height: 1rem;
    width: 1rem;
  }

  .captcha-code {
    :deep(.el-input-group__append) {
      padding: 0;
      overflow: hidden;
    }
    .captcha-image-wrap {
      position: relative;
      display: inline-flex;
    }
    .captcha-img {
      width: 100%;
      height: 100%;
      cursor: pointer;
      margin-right: 1px;
      border-bottom-right-radius: var(--el-input-border-radius);
      border-top-right-radius: var(--el-input-border-radius);
    }
    .captcha-img.is-refresh-disabled {
      cursor: not-allowed;
    }
    .captcha-countdown {
      position: absolute;
      right: 3px;
      bottom: 3px;
      padding: 0 4px;
      border-radius: 8px;
      background: rgba(0, 0, 0, 0.65);
      color: #fff;
      font-size: 12px;
      line-height: 18px;
      pointer-events: none;
    }
  }
}
</style>
