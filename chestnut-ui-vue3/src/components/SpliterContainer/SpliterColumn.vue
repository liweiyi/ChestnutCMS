<template>
  <div 
    class="spliter-column" 
    :style="columnStyle"
    ref="columnRef"
  >
    <slot></slot>
  </div>
</template>

<script setup>
import { inject, ref, computed, onMounted, onBeforeUnmount, getCurrentInstance } from 'vue'

defineOptions({
  name: 'SpliterColumn'
})

const props = defineProps({
  // 宽度，支持固定值(如 '200px')或百分比(如 '30%')
  width: {
    type: String,
    default: ''
  },
  // 最小宽度
  minWidth: {
    type: String,
    default: '50px'
  }
})

const columnRef = ref(null)
const instance = getCurrentInstance()

// 注入父组件提供的方法
const registerColumn = inject('registerColumn', null)
const unregisterColumn = inject('unregisterColumn', null)
const getColumnWidth = inject('getColumnWidth', null)

// 计算列样式
const columnStyle = computed(() => {
  const width = getColumnWidth ? getColumnWidth(instance.uid) : props.width
  return {
    width: width || 'auto',
    minWidth: props.minWidth,
    flexShrink: props.width ? 0 : 1,
    flexGrow: props.width ? 0 : 1
  }
})

onMounted(() => {
  if (registerColumn) {
    registerColumn({
      uid: instance.uid,
      width: props.width,
      minWidth: props.minWidth,
      el: columnRef.value
    })
  }
})

onBeforeUnmount(() => {
  if (unregisterColumn) {
    unregisterColumn(instance.uid)
  }
})

// 暴露给父组件
defineExpose({
  uid: instance.uid,
  width: props.width,
  minWidth: props.minWidth
})
</script>

<style scoped>
.spliter-column {
  height: 100%;
  overflow: auto;
  box-sizing: border-box;
}
</style>

