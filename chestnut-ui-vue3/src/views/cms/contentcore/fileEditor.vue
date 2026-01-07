<template>
  <div class="app-container" v-loading="loading">
    <el-card class="btn-card mb8">
      <el-row justify="space-between">
        <div class="grid-btn-bar bg-purple-white">
          <el-button plain type="info" icon="Back" @click="handleGoBack">{{ $t('Common.GoBack') }}</el-button>
          <el-button plain type="primary" icon="Edit" @click="handleSave">{{ $t('Common.Save') }}</el-button>
        </div>
        <div class="template-info">
          <el-tag type="success">{{ $t('CMS.File.File') }} : {{ form.filePath }}</el-tag>
        </div>
      </el-row>
    </el-card>
    <el-row>
      <code-mirror 
        class="file-editor"
        ref="cmEditorRef"
        v-model="form.fileContent" 
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
<script setup name="CmsFileEditor">
import CodeMirror from 'vue-codemirror6';
import { readFile, editFile } from "@/api/contentcore/file";
import { html } from '@codemirror/lang-html';
import { javascript } from '@codemirror/lang-javascript';
import { css } from '@codemirror/lang-css';

const { proxy } = getCurrentInstance()

const loading = ref(false)
const clientHeight = ref(0)
const objects = reactive({
  cmOptions: {
    dark: false,
    readonly: false, // 只读
    disabled: false, // 禁用
    tab: true, // 启用制表符缩进
    tabSize: 2,// tab的空格个数
    javascript: html(),
    lineWrapping: true, // 是否自动换行
  },
  form: {
    filePath: proxy.$route.query.filePath,
    fileContent: ""
  },
});

const { cmOptions, form } = toRefs(objects);

onMounted(() => {
  //获取浏览器可视区域高度
  clientHeight.value = `${document.documentElement.clientHeight}`;
  // 监听屏幕
  window.onresize = function () {
    clientHeight.value = `${document.documentElement.clientHeight}`;
  }
  if (form.value.filePath.endsWith('.js')) {
    cmOptions.value.lang = javascript();
  } else if (form.value.filePath.endsWith('.css')) {
    cmOptions.value.lang = css();
  } else {
    cmOptions.value.lang = html();
  }
  loadFileContent();
});

watch(clientHeight, (newVal) => {
  // 设置代码区域高度
  resizeCMEditor();
});

const loadFileContent = () => {
  loading.value = true;
  readFile(form.value).then(response => {
    form.value.fileContent = response.data;
    loading.value = false;
  });
}
const handleSave = () => {
  editFile(form.value).then(response => {
    proxy.$modal.msgSuccess(proxy.$t('Common.SaveSuccess'));
  });
}
const handleGoBack = () => {
  const obj = { path: "/cms/file/editor", query: { filePath: form.value.filePath } };
  proxy.$tab.closeOpenPage(obj).then(() => {
    proxy.$router.push({ path: "/configs/file" });
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