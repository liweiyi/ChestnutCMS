<template>
  <div class="home-shortcut-container">
    <el-card shadow="hover" class="mb10">
      <div slot="header" class="clearfix">
        <span>{{ $t('System.Menu.Shortcut') }}</span>
      </div>
      <div class="body">
        <el-card v-for="shortcut in shortcuts" :key="shortcut.router" shadow="hover" :body-style="{ padding: '0 10px' }" style="float: left;margin-right:10px;margin-bottom:10px;">
          <el-link class="shortcut-link" @click="handleShortcutRedirect(shortcut.router)">
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
<script>
import { getHomeShortcuts } from "@/api/system/user";

export default {
  name: "ShortcutDashboard",
  data () {
    return {
      shortcuts: []
    };
  },
  created() {
    this.loadShortcuts();
  },
  methods: {
    loadShortcuts() {
      getHomeShortcuts().then(response => {
        this.shortcuts = response.data;
      })
    },
    handleShortcutRedirect(router) {
      this.$router.push({ name: router })
    }
  }
};
</script>
<style>
.home-shortcut-container .shortcut-link {
  display: block;
  padding: 10px 10px;
  text-align: center;
  width: 165px;
}
.home-shortcut-container .card-panel-icon {
  width: 36px;
  height: 36px;
  padding: 5px;
}
.home-shortcut-container .shortcut-text {
  padding-top: 5px;
  text-align: center;
  font-size: 14px;
}
</style>