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
            <el-radio-group size="small" v-model="clientType" @input="handleClientTypeChange">
              <el-radio-button v-for="ct in clientTypes" :key="ct.id" :label="ct.id">{{ ct.name }}</el-radio-button>
            </el-radio-group>
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
      <el-main>
        <div :class="previewClass">
          <div class="preview_iframe_wrap">
            <div style="width:100%;height:100%;">
              <iframe id="iframePreview" :src="previewUrl" width="100%" height="100%" frameborder="0"></iframe>
            </div>
          </div>
        </div>
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
      previewUrl: undefined,
      clientTypes: [ 
        { id: "pc", name: this.$t("CMS.ContentCore.ClientType.PC"), className: "preview preview_pc" },
        { id: "phone", name: this.$t("CMS.ContentCore.ClientType.Phone"), className: "preview preview_mobile preview_phone" },
        { id: "pad", name: this.$t("CMS.ContentCore.ClientType.Pad"), className: "preview preview_mobile" }
      ],
      clientType: "pc",
      previewClass: "preview preview_pc",
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
    handleClientTypeChange () {
      this.previewClass = this.clientTypes.find(item => item.id === this.clientType).className
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
.preview {
    position: absolute;
    top: 60px;
    right: 0;
    bottom: 0;
    left: 0;
    background: #e0e0e1
}
.preview .preview_iframe_wrap iframe {
    background: #fff
}
.preview .preview_iframe_wrap {
    transition: all .3s;
    position: absolute;
    top: 0;
    bottom: 0;
    left: 0;
    right: 0;
    margin: 0 auto;
    width: 100%;
    height: 100%;
    box-sizing: border-box;
    background: #eeeff2
}
.preview.preview_mobile .preview_iframe_wrap {
    bottom: 76px;
    width: 768px;
    height: auto;
    margin-top: 44px;
    margin-bottom: 18px;
    padding-top: 40px;
    padding-bottom: 65px;
    box-shadow: 0 0 7px 0 rgba(53,53,53,.12);
    border-radius: 28px 28px 45px 45px
}
.preview.preview_mobile .preview_iframe_wrap:before {
    width: 45px;
    height: 8px;
    top: 15px;
    border-radius: 4px
}
.preview.preview_mobile .preview_iframe_wrap:after,.preview.preview_mobile .preview_iframe_wrap:before {
    content: "";
    display: block;
    position: absolute;
    left: 0;
    right: 0;
    margin: auto;
    background: #c8c9cc;
    transition: all .3s
}
.preview.preview_mobile .preview_iframe_wrap:after {
    width: 32px;
    height: 32px;
    bottom: 17px;
    border-radius: 50%
}
.preview.preview_phone .preview_iframe_wrap {
    width: 375px;
    margin: 53px auto;
    padding-bottom: 60px;
    border-radius: 33px
}
.preview.preview_phone .preview_iframe_wrap:after {
    width: 30px;
    height: 30px;
    bottom: 15px
}

</style>