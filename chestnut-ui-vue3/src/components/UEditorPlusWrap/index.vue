<template>
  <div class="ueditor-container">
    <div ref="editorRef" :name="props.name" class="ueditor-wrap">
      
    </div>
  </div>
</template>
<script setup name="UEditorPlusWrap">
import { LoadEvent } from './utils/LoadEvent'
import { asyncSeries } from './utils/async-series'

const { proxy } = getCurrentInstance()

const model = defineModel({
  type: String,
  default: ''
});

const props = defineProps({
  // 编辑器容器id
  editorId: {
    type: String,
    required: true,
  },
  // 编辑器名称,用于表单绑定
  name: {
    type: String,
    required: false,
    default: ''
  },
  // 编辑器配置
  config: {
    type: Object,
    required: true
  },
  // 是否在组件销毁时销毁编辑器
  destroy: {
    type: Boolean,
    required: false,
    default: true
  },
  forceInit: {
    type: Boolean,
    required: false,
    default: false
  },
  // 编辑器依赖的文件
  editorDependencies: {
    type: Array,
    required: false,
    default: [
      'ueditor.config.js', 'ueditor.all.min.js'
    ]
  },
  // 编辑器依赖的文件检查函数
  editorDependenciesChecker: {
    type: Function,
    required: false,
    default: () => {}
  }
})

const emit = defineEmits(['before-init', 'ready', 'destroy'])

const STATUS_MAP = {
  UNREADY: 'unready',
  PENDING: 'pending',
  READY: 'ready',
  DESTROYED: 'destroyed',
}

const status = ref(STATUS_MAP.UNREADY);
const editor = ref(null);
const contentChangeFlag = ref(false);

const defaultEditorDependencies = ['ueditor.config.js', 'ueditor.all.min.js'];

if (!window.$loadEventBus) {
  window.$loadEventBus = new LoadEvent();
}

function loadScript(src) {
  console.log('loadScript', src);
  return new Promise((resolve, reject) => {
    window.$loadEventBus.on(src, resolve);
    if (window.$loadEventBus.getListener(src).requested === false) {
      window.$loadEventBus.getListener(src).requested = true;
      // 如果这个资源从未被请求过，就手动创建脚本去加载
      const script = document.createElement('script');
      script.src = src;
      script.onload = () => {
        window.$loadEventBus.emit(src);
      };
      script.onerror = reject;
      document.getElementsByTagName('head')[0].appendChild(script);
    }
  });
}

function loadCss(link) {
  console.log('loadCss', link);
  return new Promise((resolve, reject) => {
    window.$loadEventBus.on(link, resolve);
    if (window.$loadEventBus.getListener(link).requested === false) {
      window.$loadEventBus.getListener(link).requested = true;
      const css = document.createElement('link');
      css.type = 'text/css';
      css.rel = 'stylesheet';
      css.href = link;
      css.onload = () => {
        window.$loadEventBus.emit(link);
      };
      css.onerror = reject;
      document.getElementsByTagName('head')[0].appendChild(css);
    }
  });
}

const defaultEditorDependenciesChecker = () => {
  return window.UE && window.UE.getEditor && window.UEDITOR_CONFIG && Object.keys(window.UEDITOR_CONFIG).length !== 0
}

// 加载 UEditor 相关的静态资源
const loadEditorDependencies = () => {
  return new Promise((resolve, reject) => {
    if (props.editorDependencies && props.editorDependenciesChecker && props.editorDependenciesChecker()) {
      resolve();
      return;
    }

    if (!props.editorDependencies && defaultEditorDependenciesChecker()) {
      resolve();
      return;
    }

    // 把 js 和 css 分组
    const { jsLinks, cssLinks } = (props.editorDependencies || defaultEditorDependencies).reduce(
      (res, link) => {
        // 如果不是完整的 URL 就在前面补上 UEDITOR_HOME_URL, 完整的 URL 形如：
        // 1. http://www.example.com/xxx.js
        // 2. https://www.example.com/xxx.js
        // 3. //www.example.com/xxx.js
        // 4. www.example.com/xxx.js
        const isFullUrl = /^((https?:)?\/\/)?[-a-zA-Z0-9]+(\.[-a-zA-Z0-9]+)+\//.test(link);
        if (!isFullUrl) {
          link = (props.config?.UEDITOR_HOME_URL || import.meta.env.VITE_APP_PATH) + link;
        }
        if (link.slice(-3) === '.js') {
          res.jsLinks.push(link);
        } else if (link.slice(-4) === '.css') {
          res.cssLinks.push(link);
        }
        return res;
      },
      {
        jsLinks: [],
        cssLinks: [],
      }
    );

    Promise.all([
      Promise.all(cssLinks.map((link) => loadCss(link))),
      // 依次加载依赖的 JS 文件，JS 执行是有顺序要求的，比如 ueditor.all.js 就要晚于 ueditor.config.js 执行
      // 动态创建 script 是先加载完的先执行，所以不可以一次性创建所有资源的引入脚本
      asyncSeries(jsLinks.map((link) => () => loadScript(link))),
    ]).then(() => resolve()).catch(reject);
  });
};

onDeactivated(() => {
  editor.value && editor.value.removeListener('contentChange', handleContentChange);

})

onBeforeUnmount(() => {
  destroyEditor();
})

// 监听 v-model 值的变化
watch(() => model.value, (newVal) => {
  if (status.value === STATUS_MAP.UNREADY) {
    status.value = STATUS_MAP.PENDING;
    (props.forceInit || typeof window !== 'undefined') &&
      loadEditorDependencies()
        .then(() => {
          proxy.$refs.editorRef ? initEditor() : nextTick(() => initEditor());
        })
        .catch((err) => {
          console.error(err);
          throw new Error(
            '[vue-ueditor-wrap] UEditor 资源加载失败！请检查资源是否存在，UEDITOR_HOME_URL 是否配置正确！'
          );
        });
  } else if (status.value === STATUS_MAP.READY) {
    if (contentChangeFlag.value) {
      contentChangeFlag.value = false;
      return;
    }
    const currentContent = editor.value.getContent();
    if (newVal !== currentContent) {
      editor.value.setContent(newVal || '');
    }
  }
}, { immediate: true })

const initEditor = () => {
  console.log('initEditor', model.value);
  try {
    const editorId = props.editorId || 'ueditor_' + randomString(8);
    emit('before-init', editorId);
    proxy.$refs.editorRef.id = editorId;
    editor.value = window.UE.getEditor(editorId, props.config);
    editor.value.addListener('ready', () => {
      if (status.value === STATUS_MAP.READY) {
        // 使用 keep-alive 组件会出现这种情况
        editor.value.setContent(model.value || '');
      } else {
        status.value = STATUS_MAP.READY;
        emit('ready', editor.value);

        editor.value.setContent(model.value || '');
      }
      editor.value.addListener('contentChange', handleContentChange);
    });
  } catch (error) {
    console.error('UEditor init failed:', error);
    status.value = STATUS_MAP.UNREADY;
  }
}

// 处理内容变化
const handleContentChange = () => {
  if (editor.value && status.value === STATUS_MAP.READY) {
    const contentHtml = editor.value.getContent();
    contentChangeFlag.value = true;
    model.value = contentHtml;
  }
}

// 销毁编辑器
const destroyEditor = () => {
  if (status.value === STATUS_MAP.DESTROYED) return;
  if (props.destroy && editor.value && editor.value.destroy) {
    try {
      editor.value.destroy();
      editor.value = null;
  
      status.value = STATUS_MAP.DESTROYED;
      emit('destroy');
    } catch (error) {
      console.error('UEditor destroy failed:', error);
    }
  }
}

function randomString(length) {
  const alphabet = 'abcdefghijklmnopqrstuvwxyz';
  let str = '';
  for (let i = 0; i < length; i++) {
    str += alphabet.charAt(Math.floor(Math.random() * alphabet.length));
  }
  return str;
}

// 获取编辑器实例
const getEditor = () => {
  return editor.value;
}

// 获取编辑器内容
const getContent = () => {
  if (editor.value && status.value === STATUS_MAP.READY) {
    return editor.value.getContent();
  }
  return '';
}

// 设置编辑器内容
const setContent = (content) => {
  if (editor.value && status.value === STATUS_MAP.READY) {
    editor.value.setContent(content || '');
  }
}

// 获取纯文本内容
const getContentTxt = () => {
  if (editor.value && status.value === STATUS_MAP.READY) {
    return editor.value.getContentTxt();
  }
  return '';
}

// 获取有本地路径的内容
const getLocalContent = () => {
  if (editor.value && status.value === STATUS_MAP.READY) {
    return editor.value.getAllHtml();
  }
  return '';
}

// 设置焦点
const focus = () => {
  if (editor.value && status.value === STATUS_MAP.READY) {
    editor.value.focus();
  }
}

// 获取编辑器状态
const getStatus = () => {
  return status.value;
}

// 暴露方法给父组件
defineExpose({
  getEditor,
  getContent,
  setContent,
  getContentTxt,
  getLocalContent,
  focus,
  getStatus
})
</script>

<style scoped>
.ueditor-container {
  width: 100%;
  line-height: normal;
}

.ueditor-wrap {
  width: 100%;
}
</style>