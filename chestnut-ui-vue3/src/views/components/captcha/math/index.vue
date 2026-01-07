<template>
  <div class="math-captcha-container">
    <el-input
      v-model="authCode"
      type="text"
      size="large"
      auto-complete="off"
      :placeholder="$t('Login.Captcha')"
      class="captcha-code"
      @keyup="handleCodeChanged"
    >
      <template #prefix>
        <svg-icon icon-class="validCode" class="el-input__icon input-icon" />
      </template>
      <template #append>
        <img v-if="codeImage!=''" :src="codeImage" @click="reloadCaptcha" class="captcha-img" :style="{ width: `${imgWidth}`, height: `${imgHeight}` }" />
        <el-link v-else icon="Refresh" :underline="false" @click="reloadCaptcha" :style="{ width: `${imgWidth}` }">刷新</el-link>
      </template>
    </el-input>
  </div>
</template>
<script setup>
import { getCaptcha } from "@/api/system/captcha"

const { proxy } = getCurrentInstance()

const model = defineModel();

const props = defineProps({
  width: {
    type: Number,
    default: 100,
    required: false,
  },
  height: {
    type: Number,
    default: 38,
    required: false,
  },
  token: {
    type: String,
    default: '',
    required: false,
  }
})

const loading = ref(false)
const captchaType = ref("Math")
const authCode = ref('')
const codeImage = ref("")

const imgWidth = computed(() => {
  return props.width > 0 ? props.width + 'px' : 'auto';
})
const imgHeight = computed(() => {
  return props.height > 0 ? props.height + 'px' : 'auto';
})

onMounted(() => {
  reloadCaptcha()
})

const reloadCaptcha = () => {
  if (proxy.$tools.isEmpty(props.token)) {
    console.warn('token is empty');
    return;
  }
  model.value = {};
  loading.value = true;
  authCode.value = "";
  getCaptcha({ token: props.token }).then(res => {
    codeImage.value = "data:image/gif;base64," + res.data.image;
    loading.value = false;
  }).catch(err => {
    codeImage.value = "";
    loading.value = false;
  })
}

const handleCodeChanged = () => {
  model.value = {
    type: captchaType.value,
    token: props.token,
    data: authCode.value
  };
}

defineExpose({
  reloadCaptcha
});
</script>
<style lang="scss" scoped>
.math-captcha-container {
  width: 100%;

  .input-icon {
    height: 1rem;
    width: 1rem;
  }

  .captcha-code {
    :deep(.el-input-group__append) {
      padding: 0;
      overflow: hidden;
    }
    .captcha-img {
      cursor: pointer;
      margin-right: 1px;
      border-bottom-right-radius: var(--el-input-border-radius);
      border-top-right-radius: var(--el-input-border-radius);
    }
  }
}
</style>