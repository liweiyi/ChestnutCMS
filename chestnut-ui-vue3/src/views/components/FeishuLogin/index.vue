<template>
  <div class="feishu-login-container">
    <el-button v-show="showLoginButton" :loading="loading" link @click="handleLogin">
      <svg-icon icon-class="feishu" :style="{ width: props.iconSize, height: props.iconSize }" class="mr5" />
      <el-text>{{ props.name }}</el-text>
    </el-button>
  </div> 
</template>
<script setup name="FeiShuLogin">
import { getLoginUrl } from '@/api/system/feishu'

const { proxy } = getCurrentInstance()

const props = defineProps({
  configId: {
    type: String,
    required: true,
  },
  iconSize: {
    type: String,
    required: false,
    default: '1rem',
  },
  name: {
    type: String,
    required: false,
    default: '',
  }
});

const loading = ref(false)
const showLoginButton = ref(false);

watch(() => props.configId, (newVal) => {
  showLoginButton.value = proxy.$tools.isNotEmpty(newVal); 
}, { immediate: true });

function handleLogin() {
  loading.value = true;
  getLoginUrl(props.configId).then(res => {
    const url = res.data?.url
    if (typeof url !== 'string' || !url.trim()) {
      throw new Error('Missing Feishu authorization URL')
    }
    window.location.assign(url)
  }).catch(() => {
    loading.value = false;
    proxy.$modal.msgError(proxy.$t('Login.Feishu.GetLoginUrlFail'))
  });
}
</script>
<style lang='scss' scoped>
.feishu-login-container {
  padding: 5px 10px;
}
</style>