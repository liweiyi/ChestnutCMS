<template>
  <div class="home-shortcut-container">
    <el-card shadow="hover" class="mb10" v-if="shortcuts.length > 0"> 
      <div class="body">
        <el-card class="shortcut-card" v-for="shortcut in shortcuts" :key="shortcut.router" shadow="hover">
          <el-link :underline="false" class="shortcut-link" @click="handleShortcutRedirect(shortcut.router)">
            <svg-icon :icon-class="shortcut.icon" class-name="card-panel-icon"></svg-icon>
            <div class="shortcut-text">
              <span>{{ shortcut.name }}</span>
            </div>
          </el-link>
        </el-card>
      </div>
    </el-card>

  </div>
</template>
<script setup>
import useAppStore from '@/store/modules/app'
const appStore = useAppStore()
const size = computed(() => appStore.size)

import { useRouter } from 'vue-router'
import { getHomeShortcuts } from "@/api/system/user";

// 路由
const router = useRouter()

// 响应式数据
const shortcuts = ref([])

// 方法
const loadShortcuts = () => {
  getHomeShortcuts().then(response => {
    shortcuts.value = response.data;
  })
}

const handleShortcutRedirect = (routerPath) => {
  router.push({ path: routerPath })
}

// 生命周期
onMounted(() => {
  loadShortcuts();
})
</script>
<style lang="scss" scoped>
.home-shortcut-container {

  :deep(.el-card__body) {
    padding: 5px!important;
    display: inline-flex;
  }

  .body {
    display: inline-block;
  }

  .shortcut-card {
    float: left;
    margin: 5px;

    :deep(.el-card__body) {
      padding: 0!important;
    }
  }

  .shortcut-link {
    display: block;
    padding: 10px 20px;
    text-align: center;
  }

  .card-panel-icon {
    width: 1rem;
    height: 1rem;
    margin-right: 0.5rem;
  }

  .shortcut-text {
    text-align: center;
  }
}
</style>
