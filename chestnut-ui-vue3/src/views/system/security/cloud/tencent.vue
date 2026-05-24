<template>
  <el-form-item :label="$t('System.CloudConfig.Tencent.SecretId')" prop="secretId">
    <el-input v-model="configProps.secretId" />
  </el-form-item>
  <el-form-item :label="$t('System.CloudConfig.Tencent.SecretKey')" prop="secretKey">
    <el-input v-model="configProps.secretKey" />
  </el-form-item>
</template>
<script setup name="SysCloudConfigTencent">

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
