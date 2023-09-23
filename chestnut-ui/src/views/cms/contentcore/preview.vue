<template>
  <div class="app-container">
    <el-container>
      <el-header class="header-bar" v-if="type!=='pagewidget'" style="padding-top: 15px">
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <span class="mr10" style="font-size: 14px;">{{ $t('CMS.ContentCore.PublishPipe') }}</span>
            <el-select 
              v-model="selectedPublishPipe"
              type="primary"
              size="small" 
              style="width:180px"
              @change="handlePublishPipeChange">
              <el-option 
                v-for="pp of publishPipes"
                :key="pp.pipeCode"
                :label="pp.pipeName"
                :value="pp.pipeCode" />
            </el-select>
          </el-col>
          <el-col :span="1.5">
            <el-button 
              plain
              type="primary"
              size="small"
              icon="el-icon-refresh"
              @click="handleRefresh">{{ $t('Common.Refresh') }}</el-button>
          </el-col>
        </el-row>
      </el-header>
      <el-main style="padding:0;">
        <el-scrollbar style="width:100%;height:100%;">
          <iframe id="iframePreview" :src="previewUrl" 
              frameborder="0" 
              style="width:100%;height:calc(100vh - 59px);"></iframe>
        </el-scrollbar>
      </el-main>
    </el-container>
  </div>
</template>
<script>
import { getToken } from '@/utils/auth'
import { getPublishPipeSelectData } from "@/api/contentcore/publishpipe";

export default {
  name: "ContentCorePreview",
  data() {
    return {
      type: this.$route.query.type,
      dataId: this.$route.query.dataId,
      publishPipes: [],
      selectedPublishPipe: undefined,
      previewUrl: undefined
    };
  },
  created() {
    this.loadPublishPipes();
  },
  methods: {
    loadPublishPipes() {
      getPublishPipeSelectData().then(response => {
        this.publishPipes = response.data.rows;
        this.selectedPublishPipe = response.data.rows[0].pipeCode;
        this.handlePublishPipeChange();
      });
    },
    handlePublishPipeChange () {
      this.previewUrl = process.env.VUE_APP_BASE_API + "/cms/preview/" + this.type + "/" + this.dataId 
        + "?pp=" + this.selectedPublishPipe+ "&Authorization=Bearer " + getToken();
    },
    handleRefresh () {
      document.querySelector('iframe').contentWindow.location.reload();
    }
  }
};
</script>
<style>
body {
  overflow-y: hidden;
}
.app-container { 
  padding:0; 
}
.header-bar {
  height:40px;
  padding-top: 15px;
  background-color: #f7f7f7;
  border-bottom: solid 1px #ddd;
  position:relative;
  z-index: 999;
}
.el-main {
  padding: 0;
}
.el-button {
  margin-left: 10px;
}
</style>