<template>
  <div class="spliter-container" ref="containerRef">
    <template v-for="(column, index) in columns" :key="column.uid">
      <!-- 分栏内容插槽 -->
      <div 
        class="spliter-column-wrapper"
        :style="getColumnStyle(column, index)"
      >
        <component :is="column.vnode" />
      </div>
      <!-- 分隔线 -->
      <div 
        v-if="index < columns.length - 1"
        class="spliter-divider"
        :class="{ 
          'is-draggable': !fixed,
          'is-dragging': draggingIndex === index 
        }"
        @mousedown="(e) => startDrag(e, index)"
      >
        <div class="spliter-divider-line"></div>
      </div>
    </template>
    <!-- 隐藏的默认插槽，仅用于收集子组件信息，不实际渲染 -->
    <template v-if="false">
      <slot></slot>
    </template>
  </div>
</template>

<script setup>
import { ref, provide, computed, onMounted, onBeforeUnmount, useSlots, watch, nextTick, getCurrentInstance } from 'vue'

defineOptions({
  name: 'SpliterContainer'
})

const props = defineProps({
  // 是否固定，不可拖动改变宽度
  fixed: {
    type: Boolean,
    default: false
  },
  // localStorage 存储的 key，如果指定则会持久化分栏宽度
  storageKey: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['resize'])

const containerRef = ref(null)
const slots = useSlots()
const { proxy } = getCurrentInstance()

// 存储列信息
const columnRegistry = ref([])
// 存储计算后的宽度
const columnWidths = ref({})
// 当前拖动的分隔线索引
const draggingIndex = ref(-1)
// 拖动起始位置
const dragStartX = ref(0)
// 拖动起始时的宽度
const dragStartWidths = ref([])

// 获取完整的 localStorage key
const getStorageKey = () => {
  return props.storageKey && props.storageKey.trim() !== '' ? `cc-spliter-${props.storageKey}` : undefined
}

// 从 localStorage 读取保存的宽度数据
const loadFromStorage = () => {
  const key = getStorageKey()
  if (!key) return null
  
  return proxy.$cache.local.getJSON(key)
}

// 保存宽度数据到 localStorage
const saveToStorage = () => {
  const key = getStorageKey()
  if (!key) return
  
  const widthsArray = columns.value.map(col => columnWidths.value[col.uid])
  proxy.$cache.local.setJSON(key, {
    count: columns.value.length,
    widths: widthsArray
  })
}

// 清除 localStorage 中的数据
const clearStorage = () => {
  const key = getStorageKey()
  if (!key) return
  
  proxy.$cache.local.remove(key)
}

// 从插槽中解析列组件
const columns = computed(() => {
  const defaultSlot = slots.default?.()
  if (!defaultSlot) return []
  
  const result = []
  const flatten = (vnodes) => {
    vnodes.forEach(vnode => {
      if (vnode.type?.__name === 'SpliterColumn' || vnode.type?.name === 'SpliterColumn') {
        result.push({
          uid: vnode.key || result.length,
          vnode: vnode,
          width: vnode.props?.width || '',
          minWidth: vnode.props?.minWidth || '50px'
        })
      } else if (Array.isArray(vnode.children)) {
        flatten(vnode.children)
      }
    })
  }
  flatten(defaultSlot)
  return result
})

// 根据容器宽度计算可用宽度（分隔线实际占用 = width + margin*2 = 8 + 5 + 5 = 18px）
const DIVIDER_TOTAL_WIDTH = 18

const getAvailableWidth = (containerWidth) => {
  const dividerCount = columns.value.length - 1
  return containerWidth - dividerCount * DIVIDER_TOTAL_WIDTH
}

// 初始化列宽度
const initColumnWidths = () => {
  if (!containerRef.value || columns.value.length === 0) return

  const containerWidth = containerRef.value.offsetWidth
  if (containerWidth === 0) return

  const availableWidth = getAvailableWidth(containerWidth)

  // 尝试从 localStorage 读取保存的宽度
  const savedData = loadFromStorage()
  if (savedData) {
    // 检查分栏数是否匹配
    if (savedData.count === columns.value.length && savedData.widths?.length === columns.value.length) {
      // 按比例缩放到当前容器宽度，而非直接使用保存的绝对像素值
      const savedTotal = savedData.widths.reduce((sum, w) => sum + parseFloat(w), 0)
      if (savedTotal > 0) {
        columns.value.forEach((col, index) => {
          const ratio = parseFloat(savedData.widths[index]) / savedTotal
          const minWidth = parseFloat(col.minWidth || '50')
          columnWidths.value[col.uid] = `${Math.max(availableWidth * ratio, minWidth)}px`
        })
        return
      }
    }
    // 分栏数不匹配或数据异常，清除 localStorage 数据
    clearStorage()
  }

  // 计算固定宽度和剩余列
  let fixedWidth = 0
  let flexColumns = []

  columns.value.forEach((col) => {
    if (col.width) {
      if (col.width.endsWith('%')) {
        // 百分比宽度
        const percent = parseFloat(col.width) / 100
        const width = availableWidth * percent
        columnWidths.value[col.uid] = `${width}px`
        fixedWidth += width
      } else {
        // 固定宽度
        columnWidths.value[col.uid] = col.width
        fixedWidth += parseFloat(col.width)
      }
    } else {
      flexColumns.push(col)
    }
  })

  // 平均分配剩余宽度
  if (flexColumns.length > 0) {
    const flexWidth = (availableWidth - fixedWidth) / flexColumns.length
    flexColumns.forEach(col => {
      columnWidths.value[col.uid] = `${Math.max(flexWidth, 50)}px`
    })
  }
}

// 响应容器尺寸变化：按比例缩放当前列宽，不重新读取 localStorage
const recalculateOnResize = () => {
  if (!containerRef.value || columns.value.length === 0) return

  const containerWidth = containerRef.value.offsetWidth
  if (containerWidth === 0) return

  const newAvailable = getAvailableWidth(containerWidth)

  const currentWidths = columns.value.map(col => parseFloat(columnWidths.value[col.uid] || '0'))
  const currentTotal = currentWidths.reduce((sum, w) => sum + w, 0)

  if (currentTotal === 0) {
    initColumnWidths()
    return
  }

  columns.value.forEach((col, index) => {
    const ratio = currentWidths[index] / currentTotal
    const minWidth = parseFloat(col.minWidth || '50')
    columnWidths.value[col.uid] = `${Math.max(newAvailable * ratio, minWidth)}px`
  })
}

// 获取列样式
const getColumnStyle = (column, index) => {
  const width = columnWidths.value[column.uid] || 'auto'
  return {
    width,
    minWidth: column.minWidth,
    flexShrink: 0,
    flexGrow: 0
  }
}

// 获取列宽度（提供给子组件）
const getColumnWidth = (uid) => {
  return columnWidths.value[uid]
}

// 开始拖动
const startDrag = (e, index) => {
  if (props.fixed) return
  
  e.preventDefault()
  draggingIndex.value = index
  dragStartX.value = e.clientX
  
  // 记录拖动开始时的宽度
  dragStartWidths.value = columns.value.map(col => {
    const width = columnWidths.value[col.uid]
    return parseFloat(width) || 0
  })
  
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
}

// 拖动中
const onDrag = (e) => {
  if (draggingIndex.value === -1) return
  
  const deltaX = e.clientX - dragStartX.value
  const index = draggingIndex.value
  
  // 获取左右两列的最小宽度
  const leftMinWidth = parseFloat(columns.value[index].minWidth) || 50
  const rightMinWidth = parseFloat(columns.value[index + 1].minWidth) || 50
  
  // 计算新宽度
  let leftWidth = dragStartWidths.value[index] + deltaX
  let rightWidth = dragStartWidths.value[index + 1] - deltaX
  
  // 应用最小宽度限制
  if (leftWidth < leftMinWidth) {
    leftWidth = leftMinWidth
    rightWidth = dragStartWidths.value[index] + dragStartWidths.value[index + 1] - leftMinWidth
  }
  if (rightWidth < rightMinWidth) {
    rightWidth = rightMinWidth
    leftWidth = dragStartWidths.value[index] + dragStartWidths.value[index + 1] - rightMinWidth
  }
  
  // 更新宽度
  columnWidths.value[columns.value[index].uid] = `${leftWidth}px`
  columnWidths.value[columns.value[index + 1].uid] = `${rightWidth}px`
  
  emit('resize', {
    index,
    leftWidth,
    rightWidth
  })
}

// 停止拖动
const stopDrag = () => {
  draggingIndex.value = -1
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.body.style.cursor = ''
  document.body.style.userSelect = ''
  
  // 保存宽度到 localStorage
  saveToStorage()
}

// 提供给子组件的方法
provide('registerColumn', (column) => {
  columnRegistry.value.push(column)
})

provide('unregisterColumn', (uid) => {
  const index = columnRegistry.value.findIndex(c => c.uid === uid)
  if (index > -1) {
    columnRegistry.value.splice(index, 1)
  }
})

provide('getColumnWidth', getColumnWidth)

// 监听列变化，重新计算宽度
watch(columns, () => {
  nextTick(() => {
    initColumnWidths()
  })
}, { immediate: true })

// 监听容器大小变化
let resizeObserver = null
onMounted(() => {
  nextTick(() => {
    initColumnWidths()
  })
  
  if (typeof ResizeObserver !== 'undefined') {
    resizeObserver = new ResizeObserver(() => {
      // 仅当没有拖动时重新计算，按比例缩放而非重新读取 localStorage
      if (draggingIndex.value === -1) {
        recalculateOnResize()
      }
    })
    if (containerRef.value) {
      resizeObserver.observe(containerRef.value)
    }
  }
})

onBeforeUnmount(() => {
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  if (resizeObserver) {
    resizeObserver.disconnect()
  }
})

// 暴露方法
defineExpose({
  initColumnWidths,
  clearStorage
})
</script>

<style scoped>
.spliter-container {
  display: flex;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.spliter-column-wrapper {
  height: 100%;
  overflow: auto;
  box-sizing: border-box;
}

.spliter-divider {
  flex-shrink: 0;
  width: 8px;
  height: 100%;
  margin: 0 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--el-border-color-lighter, #ebeef5);
  transition: background-color 0.2s;
}

.spliter-divider.is-draggable {
  cursor: col-resize;
}

.spliter-divider.is-draggable:hover,
.spliter-divider.is-dragging {
  background-color: var(--el-color-primary-light-7, #c6e2ff);
}

.spliter-divider-line {
  width: 2px;
  height: 2rem;
  background-color: var(--el-border-color, #dcdfe6);
  border-radius: 1px;
  transition: background-color 0.2s, height 0.2s;
}

.spliter-divider.is-draggable:hover .spliter-divider-line,
.spliter-divider.is-dragging .spliter-divider-line {
  background-color: var(--el-color-primary, #409eff);
  height: 50px;
}
</style>

