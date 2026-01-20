<template>
  <div class="publish-task" style="display: flex;align-items: center;">
    <el-dropdown trigger="click" @command="handleCommand" v-if="taskCount > 0">
      <div style="font-size: 12px">
        <el-tag type="success">{{ $t('Component.Layout.Navbar.PublishTask') }} [ {{ taskCount }} ]</el-tag>
      </div>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="clear">
            {{ $t('Component.Layout.Navbar.ClearPublishTask') }}
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup name="CMSPublishTask">
import { getPublishTaskCount, clearPublishTask } from "@/api/contentcore/publish"

const { proxy } = getCurrentInstance();

// Reactive data
const taskCount = ref(0)
const interval = ref(undefined)
const taskZeroTimes = ref(0)
const checkInterval = ref(undefined)

// Methods
const checkPublishFlag = () => {
  if (proxy.$cms.getPublishFlag() === 'true') {
    if (!interval.value) {
      interval.value = setInterval(loadPublishTaskCount, 5000)
      taskZeroTimes.value = 0
    }
  } else {
    if (interval.value) {
      clearInterval(interval.value)
      interval.value = undefined
    }
  }
}

const loadPublishTaskCount = () => {
  getPublishTaskCount().then(res => {
    taskCount.value = res.data
    if (taskCount.value == 0) {
      taskZeroTimes.value++
      if (taskZeroTimes.value == 3) {
        proxy.$cms.setPublishFlag('')
      }
    }
  })
}

const handleCommand = (command) => {
  if (command === 'clear') {
    clearPublishTask().then(res => {
      loadPublishTaskCount()
    })
  }
}

// Lifecycle hooks
onMounted(() => {
  checkInterval.value = setInterval(checkPublishFlag, 2000)
})

onUnmounted(() => {
  if (checkInterval.value) {
    clearInterval(checkInterval.value)
  }
  if (interval.value) {
    clearInterval(interval.value)
  }
})
</script>
