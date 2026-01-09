<template>
  <el-form-item :label="$t('System.CloudConfig.Volc.AccessKey')" prop="accessKey">
    <el-input v-model="configProps.accessKey" />
  </el-form-item>
  <el-form-item :label="$t('System.CloudConfig.Volc.SecretKey')" prop="secretKey">
    <el-input v-model="configProps.secretKey" />
  </el-form-item>
  <el-form-item :label="$t('System.CloudConfig.Volc.Region')" prop="region">
    <el-input v-model="configProps.region" />
  </el-form-item>
</template>
<script setup name="SysCloudConfigVolc">

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
  console.log('modelValue', newVal);
  configProps.value = newVal || {}
}, { immediate: true });

watch(configProps, (newVal) => {
  console.log('configProps', newVal);
  modelValue.value = newVal || {}
}, { deep: true });
</script>