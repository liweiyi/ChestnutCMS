<template>
  <el-dropdown trigger="click" @command="handleCommand" v-if="taskCount > 0">
    <div style="font-size: 12px">
      <el-tag type="success">{{ $t('Component.Layout.Navbar.PublishTask') }} [ {{ taskCount }} ]</el-tag>
    </div>
    <el-dropdown-menu slot="dropdown">
      <el-dropdown-item command="clear">
        {{ $t('Component.Layout.Navbar.ClearPublishTask') }}
      </el-dropdown-item>
    </el-dropdown-menu>
  </el-dropdown>
</template>

<script>
import { getPublishTaskCount, clearPublishTask } from "@/api/contentcore/publish";

export default {
  name: "CMSPublishTask",
  data() {
    return {
      taskCount: 0
    }
  },
  created() {
    // 每5秒更新一次数据
    setInterval(this.loadPublishTaskCount, 5000); 
  },
  methods: {
    loadPublishTaskCount() {
      getPublishTaskCount().then(res => {
        this.taskCount = res.data;
      })
    },
    handleCommand(command) {
      if (command === 'clear') {
        clearPublishTask().then(res => {
          this.loadPublishTaskCount();
        })
      }
    }
  }
}
</script>
