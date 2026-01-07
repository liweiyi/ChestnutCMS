<template>
  <el-form-item :label="$t('System.Security.LoginConfig.Wechat.AppId')" prop="appId">
    <el-input v-model="configProps.appId" />
  </el-form-item>
  <el-form-item :label="$t('System.Security.LoginConfig.Wechat.AppSecret')" prop="appSecret">
    <el-input v-model="configProps.appSecret" />
  </el-form-item>
  <el-form-item :label="$t('System.Security.LoginConfig.Wechat.RedirectUri')" prop="redirectUri">
    <el-input v-model="configProps.redirectUri" />
  </el-form-item>
  <el-form-item :label="$t('System.Security.LoginConfig.Wechat.Style')" prop="style">
    <el-radio-group v-model="configProps.style">
      <el-radio label="black">black</el-radio>
      <el-radio label="white">white</el-radio>
    </el-radio-group>
  </el-form-item>
  <el-form-item :label="$t('System.Security.LoginConfig.Wechat.Href')" prop="href">
    <el-input v-model="configProps.href" />
  </el-form-item>
  <el-form-item :label="$t('System.Security.LoginConfig.Wechat.Stylelite')" prop="stylelite">
    <el-radio-group v-model="configProps.stylelite">
      <el-radio label="0">旧UI</el-radio>
      <el-radio label="1">新UI</el-radio>
    </el-radio-group>
  </el-form-item>
  <el-form-item :label="$t('System.Security.LoginConfig.Wechat.FastLogin')" prop="fastLogin">
    <el-radio-group v-model="configProps.fastLogin">
      <el-radio label="0">关闭</el-radio>
      <el-radio label="1">开启</el-radio>
    </el-radio-group>
  </el-form-item>
  <el-form-item :label="$t('System.Security.LoginConfig.Wechat.ColorScheme')" prop="colorScheme">
    <el-radio-group v-model="configProps.colorScheme">
      <el-radio label="light">light</el-radio>
      <el-radio label="dark">dark</el-radio>
      <el-radio label="auto">跟随系统主题</el-radio>
      <el-radio label="">跟随浏览器主题</el-radio>
    </el-radio-group>
  </el-form-item>
</template>
<script setup name="SysLoginConfigWechat">

const modelValue = defineModel('modelValue', {
  type: Object,
  required: false,
  default: () => ({
    style: 'black',
    stylelite: '1',
    fastLogin: '1',
    colorScheme: '',
  })
})

const objects = reactive({
  configProps: {}
});
const { configProps } = toRefs(objects);

watch(modelValue, (newVal) => {
  configProps.value = newVal || {}
}, { immediate: true });

watch(configProps, (newVal) => {
  modelValue.value = newVal || {}
}, { deep: true });
</script>