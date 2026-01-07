<template>
  <div class="app-container" v-loading="loading">
    <el-card class="btn-card" style="margin-bottom:10px;">
      <el-row justify="space-between">
        <div class="grid-btn-bar bg-purple-white">
          <el-button plain type="info" icon="Back" @click="handleGoBack">{{ $t('Common.GoBack') }}</el-button>
          <el-button plain type="primary" icon="Edit" @click="handleSave">{{ $t('Common.Save') }}</el-button>
        </div>
        <div class="template-info">
          <el-tag>{{ $t('CMS.Template.Name') }} : {{ form.path }}</el-tag>
          <el-divider direction="vertical"></el-divider>
          <el-tag type="success">{{ $t('CMS.ContentCore.PublishPipe') }} : {{ form.publishPipeCode }}</el-tag>
        </div>
      </el-row>
    </el-card>
    <el-row>
      <code-mirror 
        class="file-editor"
        ref="cmEditorRef"
        v-model="form.content" 
        basic
        :tab="cmOptions.tab"
        :tab-size="cmOptions.tabSize"
        :readonly="cmOptions.readonly"
        :disabled="cmOptions.disabled"
        :lang="cmOptions.lang"
        :line-wrapping="cmOptions.lineWrapping"
        :wrap="cmOptions.wrap"
        :dark="cmOptions.dark"
        @ready="handleCMReady"
        @focus="handleCMFocus"
        @change="handleCMChange"
        @update="handleCMUpdate"
      />
    </el-row>
  </div>
</template>
<script setup name="CmsTemplateEditor">
import CodeMirror from 'vue-codemirror6';
import { getTemplateDetail, editTemplate } from "@/api/contentcore/template";
import { html } from '@codemirror/lang-html';

const { proxy } = getCurrentInstance()

const loading = ref(false)
const clientHeight = ref(0)
const templateId = ref(proxy.$route.query.id)
const objects = reactive({
  cmOptions: {
    dark: false,
    readonly: false, // 只读
    disabled: false, // 禁用
    tab: true, // 启用制表符缩进
    tabSize: 2,// tab的空格个数
    lang: html(),
    lineWrapping: true, // 是否自动换行
  },
  form: {},
});

const { cmOptions, form } = toRefs(objects);

watch(clientHeight, (newVal) => {
  // 设置代码区域高度
  resizeCMEditor();
});

onMounted(() => {
  //获取浏览器可视区域高度
  clientHeight.value = `${document.documentElement.clientHeight}`;
  // 监听屏幕
  window.onresize = function () {
    clientHeight.value = `${document.documentElement.clientHeight}`;
  }
  loadTemplateDetail();
});
const loadTemplateDetail = () => {
  loading.value = true;
  getTemplateDetail(templateId.value).then(response => {
    form.value = response.data;
    loading.value = false;
  });
}
const handleSave = () => {
  editTemplate(form.value).then(response => {
    proxy.$modal.msgSuccess(response.msg);
  });
}
const route = useRoute()
const handleGoBack = () => {
  const obj = { path: "/cms/template/editor", query: { id: templateId.value } };
  proxy.$tab.closeOpenPage(obj).then(() => {
    proxy.$router.push({ path: "/configs/template" });
  });
}
const handleCMReady = () => {
  console.log("handleCMReady");
  resizeCMEditor();
}
const resizeCMEditor = () => {
  if (proxy.$refs.cmEditorRef && proxy.$refs.cmEditorRef.editor) {
    proxy.$refs.cmEditorRef.editor.style.height = `${parseFloat(clientHeight.value) - 205}px`;
  }
}
const handleCMFocus = () => {
}

const handleCMChange = (state) => {
}

const handleCMUpdate = (viewUpdate) => {
}
</script>
<style scoped>
.btn-card .el-card__body {
  padding: 5px 10px 5px 10px;
}
.template-info {
  line-height: 28px;
  text-align: right;
  color: #777;
}
.file-editor {
  width: 100%;
  overflow: hidden;
  overflow-y: auto;
}
</style>