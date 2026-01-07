<template>
  <teleport to="body">
    <div
      v-show="visible"
      ref="contextMenuRef"
      class="cc-context-menu"
      :style="menuStyle"
      @contextmenu.prevent
      @click="handleMenuClick">
      <slot :data="data"></slot>
    </div>
  </teleport>
</template>

<script setup name="ContextMenu">
const props = defineProps({
  open: {
    type: Boolean,
    default: false
  },
  data: {
    type: Object,
    default: () => ({})
  }
});

const emit = defineEmits(['update:open']);

const contextMenuRef = ref(null);
const visible = ref(false);
const menuX = ref(0);
const menuY = ref(0);

const menuStyle = computed(() => ({
  left: menuX.value + 'px',
  top: menuY.value + 'px'
}));

// 显示菜单
function show(event) {
  if (event) {
    event.preventDefault();
    menuX.value = event.clientX;
    menuY.value = event.clientY;
  }
  visible.value = true;
  emit('update:open', true);
  
  nextTick(() => {
    // 调整位置，确保菜单不超出视口
    adjustPosition();
    // 添加关闭事件监听
    document.addEventListener('click', handleClickOutside);
    document.addEventListener('scroll', close, true);
    window.addEventListener('resize', close);
  });
}

// 关闭菜单
function close() {
  visible.value = false;
  emit('update:open', false);
  removeListeners();
}

// 调整菜单位置，防止超出视口
function adjustPosition() {
  if (!contextMenuRef.value) return;
  
  const menu = contextMenuRef.value;
  const menuRect = menu.getBoundingClientRect();
  const viewportWidth = window.innerWidth;
  const viewportHeight = window.innerHeight;
  
  // 如果菜单超出右边界，向左偏移
  if (menuX.value + menuRect.width > viewportWidth) {
    menuX.value = viewportWidth - menuRect.width - 5;
  }
  
  // 如果菜单超出下边界，向上偏移
  if (menuY.value + menuRect.height > viewportHeight) {
    menuY.value = viewportHeight - menuRect.height - 5;
  }
  
  // 确保不会出现负值
  if (menuX.value < 0) menuX.value = 5;
  if (menuY.value < 0) menuY.value = 5;
}

// 点击外部关闭
function handleClickOutside(event) {
  if (contextMenuRef.value && !contextMenuRef.value.contains(event.target)) {
    close();
  }
}

// 点击菜单项关闭菜单（排除分隔线）
function handleMenuClick(event) {
  const target = event.target.closest('li');
  if (target && !target.classList.contains('menu-divider')) {
    close();
  }
}

// 移除事件监听
function removeListeners() {
  document.removeEventListener('click', handleClickOutside);
  document.removeEventListener('scroll', close, true);
  window.removeEventListener('resize', close);
}

// 监听 open prop 变化
watch(() => props.open, (val) => {
  if (!val && visible.value) {
    close();
  }
});

// 组件卸载时清理
onUnmounted(() => {
  removeListeners();
});

// 暴露方法给父组件
defineExpose({
  show,
  close
});
</script>

<style lang="scss" scoped>
.cc-context-menu {
  position: fixed;
  z-index: 3000;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 5px 0;
  min-width: 150px;

  :deep(ul) {
    list-style: none;
    margin: 0;
    padding: 0;
  }

  :deep(li) {
    padding: 8px 16px;
    cursor: pointer;
    font-size: 14px;
    color: var(--el-text-color-regular);
    display: flex;
    align-items: center;
    transition: background-color 0.2s;

    &:hover {
      background-color: var(--el-fill-color-light);
      color: var(--el-color-primary);
    }

    .el-icon, .svg-icon {
      margin-right: 8px;
      font-size: 16px;
    }

    &.menu-divider {
      height: 1px;
      padding: 0;
      margin: 5px 10px;
      background-color: var(--el-border-color-lighter);
      cursor: default;

      &:hover {
        background-color: var(--el-border-color-lighter);
      }
    }
  }
}
</style>

