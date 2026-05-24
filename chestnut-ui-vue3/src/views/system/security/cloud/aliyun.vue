<template>
  <el-form-item :label="$t('System.CloudConfig.Aliyun.AccessKey')" prop="accessKey">
    <el-input v-model="configProps.accessKey" />
  </el-form-item>
  <el-form-item :label="$t('System.CloudConfig.Aliyun.SecretKey')" prop="secretKey">
    <el-input v-model="configProps.secretKey" />
  </el-form-item>
</template>
<script setup name="SysCloudConfigAliyun">

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
