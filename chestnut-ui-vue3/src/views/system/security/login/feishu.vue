<template>
  <el-form-item :label="$t('System.Security.LoginConfig.Feishu.AppId')" prop="appId">
    <el-input v-model="configProps.appId" />
  </el-form-item>
  <el-form-item :label="$t('System.Security.LoginConfig.Feishu.AppSecret')" prop="appSecret">
    <el-input v-model="configProps.appSecret" />
  </el-form-item>
  <el-form-item :label="$t('System.Security.LoginConfig.Feishu.Scope')" prop="scope">
    <el-input v-model="configProps.scope" />
  </el-form-item>
  <el-form-item :label="$t('System.Security.LoginConfig.Feishu.RedirectUri')" prop="redirectUri">
    <el-input v-model="configProps.redirectUri" />
  </el-form-item>
</template>
<script setup name="SysLoginConfigFeishu">

const modelValue = defineModel('modelValue', {
  type: Object,
  required: false,
  default: () => ({})
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