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
      taskCount: 0,
      interval: undefined,
      taskZeroTimes: 0
    }
  },
  created() {
    setInterval(this.checkPublishFlag, 2000); 
  },
  methods: {
    checkPublishFlag() {
      if (this.$cache.local.get('publish_flag') === 'true') {
        if (!this.interval) {
          this.interval = setInterval(this.loadPublishTaskCount, 5000); 
          this.taskZeroTimes = 0;
        }
      } else {
        if (this.interval) {
          clearInterval(this.interval)
          this.interval = undefined;
        }
      }
    },
    loadPublishTaskCount() {
      getPublishTaskCount().then(res => {
        this.taskCount = res.data;
        if (this.taskCount == 0) {
          this.taskZeroTimes++;
          if (this.taskZeroTimes == 3) {
            this.$cache.local.set('publish_flag', '')
          }
        }
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
