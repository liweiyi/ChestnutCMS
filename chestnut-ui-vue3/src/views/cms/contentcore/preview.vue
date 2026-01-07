<template>
  <div class="app-container preview-page">
    <el-container>
      <el-header class="header-bar" style="padding-top: 15px">
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <span class="mr10" style="font-size: 14px;">{{ $t('CMS.ContentCore.PublishPipe') }}</span>
            <el-select 
              v-model="selectedPublishPipe"
              type="primary"
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
            <el-radio-group v-model="clientType" @change="handleClientTypeChange">
              <el-radio-button v-for="ct in clientTypes" :key="ct.id" :label="ct.id">{{ ct.name }}</el-radio-button>
            </el-radio-group>
          </el-col>
          <el-col :span="1.5">
            <el-button 
              plain
              type="primary"
              icon="Refresh"
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
<script setup name="ContentCorePreview">
import { getPublishPipeSelectData } from "@/api/contentcore/publishpipe";

const { proxy } = getCurrentInstance()

const type = ref(proxy.$route.query.type);
const dataId = ref(proxy.$route.query.dataId);
const publishPipes = ref([]);
const selectedPublishPipe = ref(undefined);
const previewUrl = ref(undefined);
const clientTypes = ref([ 
  { id: "pc", name: proxy.$t("CMS.ContentCore.ClientType.PC"), className: "preview preview_pc" },
  { id: "phone", name: proxy.$t("CMS.ContentCore.ClientType.Phone"), className: "preview preview_mobile preview_phone" },
  { id: "pad", name: proxy.$t("CMS.ContentCore.ClientType.Pad"), className: "preview preview_mobile" }
]);
const clientType = ref("pc");
const previewClass = ref("preview preview_pc");

onMounted(() => {
  document.body.style['overflow-y'] = 'hidden';
  loadPublishPipes();
});

function loadPublishPipes() {
  getPublishPipeSelectData().then(response => {
    publishPipes.value = response.data.rows;
    selectedPublishPipe.value = response.data.rows[0].pipeCode;
    handlePublishPipeChange();
  });
}

function handlePublishPipeChange() {
  previewUrl.value = import.meta.env.VITE_APP_BASE_API + "/cms/preview/" + type.value + "/" + dataId.value 
    + "?pp=" + selectedPublishPipe.value+ "&" + proxy.$auth.getTokenQuery();
}

function handleClientTypeChange() {
  previewClass.value = clientTypes.value.find(item => item.id === clientType.value).className
}

function handleRefresh() {
  document.querySelector('#iframePreview').contentWindow.location.reload();
}
</script>
<style scoped>
.preview-page { 
  padding:0; 
}
.preview-page .header-bar {
  height:64px;
  padding-top: 15px;
  background-color: #f7f7f7;
  border-bottom: solid 1px #ddd;
  position:relative;
  z-index: 999;
}
.preview-page .el-main {
  padding: 0;
}
.preview-page .el-button {
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