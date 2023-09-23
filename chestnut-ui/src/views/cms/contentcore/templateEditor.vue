<template>
  <div class="app-container" v-loading="loading">
    <el-card class="btn-card" style="margin-bottom:10px;">
      <el-row>
        <el-col :span="4" style="text-algin:right;">
          <div class="grid-btn-bar bg-purple-white">
            <el-button plain type="primary" size="mini" icon="el-icon-edit" @click="handleSave">{{ $t('Common.Save') }}</el-button>
          </div>
        </el-col>
        <el-col :span="20">
          <div class="template-info">
            <el-tag>{{ $t('CMS.Template.Name') }} : {{ this.form.path }}</el-tag>
            <el-divider direction="vertical"></el-divider>
            <el-tag type="success">{{ $t('CMS.ContentCore.PublishPipe') }} : {{ this.form.publishPipeCode }}</el-tag>
          </div>
        </el-col>
      </el-row>
    </el-card>
    <el-row>
      <codemirror 
        class="template-editor"
        ref="cmEditor"
        v-model="form.content" 
        :options="cmOptions"
        @ready="handleCMReady"
        @input="handleCMChange"></codemirror>
    </el-row>
  </div>
</template>
<style scoped>
.btn-card .el-card__body {
  padding: 5px 10px 5px 10px;
}
.template-info {
  line-height: 28px;
  text-align: right;
  color: #777;
}
</style>
<script>
import { codemirror } from 'vue-codemirror'
import 'codemirror/mode/javascript/javascript.js'
import 'codemirror/lib/codemirror.css'
import 'codemirror/keymap/sublime' //sublime编辑器效果
import "codemirror/theme/dracula.css"// 配置里面也需要theme设置为monokai
import 'codemirror/addon/selection/active-line' //光标行背景高亮
import "codemirror/mode/vue/vue.js" // 配置里面也需要mode设置为vue
// 搜索需要引入
import 'codemirror/addon/scroll/annotatescrollbar.js'
import 'codemirror/addon/search/matchesonscrollbar.js'
import 'codemirror/addon/search/match-highlighter.js'
import 'codemirror/addon/search/jump-to-line.js'
import 'codemirror/addon/dialog/dialog.js'
import 'codemirror/addon/dialog/dialog.css'
import 'codemirror/addon/search/searchcursor.js'
import 'codemirror/addon/search/search.js'

import { getTemplateDetail, editTemplate } from "@/api/contentcore/template";

export default {
  name: "CmsTemplateEditor",
  components: { codemirror },
  data () {
    return {
      loading: false,
      // codemirro编辑器配置
      cmOptions: {
        tabSize: 2,// tab的空格个数
        theme: 'dracula',//主题样式
        lineNumbers: true,//是否显示行数
        lineWrapping: true, //是否自动换行
        styleActiveLine: true,//line选择是是否加亮
        matchBrackets: true,//括号匹配
        mode: "vue",
        keyMap: "sublime", // 快捷键方案：sublime、emacs、vim
        readOnly: false//只读
      },
      templateId: this.$route.query.id,
      form: {},
      editor: undefined
    };
  },
  mounted() {
    let that = this;
    that.clientHeight = `${document.documentElement.clientHeight}`;//获取浏览器可视区域高度
    // // 设置codemirror高度
    that.$refs['cmEditor'].codemirror.setSize('auto', this.clientHeight - 220);
    
    // 监听屏幕
    window.onresize = function () {
      that.clientHeight = `${document.documentElement.clientHeight}`;
      // 设置代码区域高度
      if (that.$refs['cmEditor'] && that.$refs['cmEditor'].codemirror) {
        that.$refs['cmEditor'].codemirror.setSize('auto', parseFloat(that.clientHeight) - 220);
      }
    }
  },
  created() {
    this.loadTemplateDetail();
  },
  methods: {
    loadTemplateDetail () {
      this.loading = true;
      getTemplateDetail(this.templateId).then(response => {
        this.form = response.data;
        this.loading = false;
      });
    },
    handleSave () {
      editTemplate(this.form).then(response => {
        this.$modal.msgSuccess(response.msg);
      });
    },
    handleCMReady () {
      
    },
    handleCMChange () {
      
    }
  }
};
</script>
<style scoped>
</style>
<style >
.CodeMirror {
    height: 100%;
}
</style>