<template>
  <div class="wechat-login-container">
    <el-button v-show="showLoginButton" :loading="loading" link @click="handleLogin">
      <svg-icon icon-class="wechat" :style="{ width: props.iconSize, height: props.iconSize }" class="mr5" />
      <el-text>{{ props.name }}</el-text>
    </el-button>
    <el-dialog
      v-model="open"
      width="500px"
      append-to-body
    >
      <iframe 
        ref="iframeRef" 
        :src="loginData.url"
        width="100%"
        height="400px"
        frameborder="0"
        scrolling="no"
        sandbox="allow-scripts allow-top-navigation allow-same-origin"
      ></iframe>
    </el-dialog>
  </div> 
</template>
<script setup name="WechatLogin">
import { getLoginUrl } from '@/api/system/wechat'

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

const showLoginButton = ref(false);
const loading = ref(false);
const open = ref(false);
const loginData = ref({});

watch(() => props.configId, (newVal) => {
  showLoginButton.value = proxy.$tools.isNotEmpty(newVal); 
}, { immediate: true });

function handleLogin() {
  loading.value = true;
  getLoginUrl(props.configId).then(res => {
    loginData.value = res.data;
    loading.value = false;
  }).catch(() => {
    loading.value = false;
    proxy.$modal.msgError(proxy.$t('Login.Wechat.GetLoginUrlFail'))
  });
  open.value = true;
}
</script>
<style lang='scss' scoped>
.wechat-login-container {
  padding: 5px 10px;
}
</style>