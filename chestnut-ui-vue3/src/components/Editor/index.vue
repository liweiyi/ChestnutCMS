<template>
  <div>
    <el-upload
      :action="uploadUrl"
      :before-upload="handleBeforeUpload"
      :on-success="handleUploadSuccess"
      :on-error="handleUploadError"
      name="file"
      :show-file-list="false"
      :headers="headers"
      class="editor-img-uploader"
      v-if="type == 'url'"
    >
      <i ref="uploadRef" class="editor-img-uploader"></i>
    </el-upload>
  </div>
  <div class="editor">
    <quill-editor
      ref="quillEditorRef"
      v-model:content="content"
      contentType="html"
      @textChange="(e) => $emit('update:modelValue', content)"
      :options="options"
      :style="styles"
    />
  </div>
</template>

<script setup>
import axios from 'axios'
import { QuillEditor } from "@vueup/vue-quill"
import "@vueup/vue-quill/dist/vue-quill.snow.css"

const { proxy } = getCurrentInstance()

const quillEditorRef = ref()
const uploadUrl = ref(import.meta.env.VITE_APP_BASE_API + "/common/upload") // 上传的图片服务器地址
const headers = ref({
  ...proxy.$auth.getTokenHeader()
})

const props = defineProps({
  /* 编辑器的内容 */
  modelValue: {
    type: String,
  },
  /* 高度 */
  height: {
    type: Number,
    default: null,
  },
  /* 最小高度 */
  minHeight: {
    type: Number,
    default: null,
  },
  /* 只读 */
  readOnly: {
    type: Boolean,
    default: false,
  },
  /* 上传文件大小限制(MB) */
  fileSize: {
    type: Number,
    default: 5,
  },
  /* 类型（base64格式、url格式） */
  type: {
    type: String,
    default: "url",
  }
})

const options = ref({
  theme: "snow",
  bounds: document.body,
  debug: "warn",
  modules: {
    // 工具栏配置
    toolbar: [
      ["bold", "italic", "underline", "strike"],      // 加粗 斜体 下划线 删除线
      ["blockquote", "code-block"],                   // 引用  代码块
      [{ list: "ordered" }, { list: "bullet" }],      // 有序、无序列表
      [{ indent: "-1" }, { indent: "+1" }],           // 缩进
      [{ size: ["small", false, "large", "huge"] }],  // 字体大小
      [{ header: [1, 2, 3, 4, 5, 6, false] }],        // 标题
      [{ color: [] }, { background: [] }],            // 字体颜色、字体背景颜色
      [{ align: [] }],                                // 对齐方式
      ["clean"],                                      // 清除文本格式
      ["link", "image", "video"]                      // 链接、图片、视频
    ],
  },
  placeholder: "Enter content",
  readOnly: props.readOnly
})

const styles = computed(() => {
  let style = {}
  if (props.minHeight) {
    style.minHeight = `${props.minHeight}px`
  }
  if (props.height) {
    style.height = `${props.height}px`
  }
  return style
})

const content = ref("")
watch(() => props.modelValue, (v) => {
  if (v !== content.value) {
    content.value = v == undefined ? "<p></p>" : v
  }
}, { immediate: true })

// 如果设置了上传地址则自定义图片上传事件
onMounted(() => {
  if (props.type == 'url') {
    let quill = quillEditorRef.value.getQuill()
    let toolbar = quill.getModule("toolbar")
    toolbar.addHandler("image", (value) => {
      if (value) {
        proxy.$refs.uploadRef.click()
      } else {
        quill.format("image", false)
      }
    })
    quill.root.addEventListener('paste', handlePasteCapture, true)
  }
})

// 上传前校检格式和大小
function handleBeforeUpload(file) {
  const type = ["image/jpeg", "image/jpg", "image/png", "image/svg"]
  const isJPG = type.includes(file.type)
  //检验文件格式
  if (!isJPG) {
    proxy.$modal.msgError(`Image format error!`)
    return false
  }
  // 校检文件大小
  if (props.fileSize) {
    const isLt = file.size / 1024 / 1024 < props.fileSize
    if (!isLt) {
      proxy.$modal.msgError(`File size cannot exceed ${props.fileSize} MB!`)
      return false
    }
  }
  return true
}

// 上传成功处理
function handleUploadSuccess(res, file) {
  // 如果上传成功
  if (res.code == 200) {
    // 获取富文本实例
    let quill = toRaw(quillEditorRef.value).getQuill()
    // 获取光标位置
    let length = quill.selection.savedRange.index
    // 插入图片，res.url为服务器返回的图片链接地址
    quill.insertEmbed(length, "image", import.meta.env.VITE_APP_BASE_API + res.data.fileName)
    // 调整光标到最后
    quill.setSelection(length + 1)
  } else {
    proxy.$modal.msgError("Image insertion failed")
  }
}

// 上传失败处理
function handleUploadError() {
  proxy.$modal.msgError("Image insertion failed")
}

// 复制粘贴图片处理
function handlePasteCapture(e) {
  const clipboard = e.clipboardData || window.clipboardData
  if (clipboard && clipboard.items) {
    for (let i = 0; i < clipboard.items.length; i++) {
      const item = clipboard.items[i]
      if (item.type.indexOf('image') !== -1) {
        e.preventDefault()
        const file = item.getAsFile()
        insertImage(file)
      }
    }
  }
}

function insertImage(file) {
  const formData = new FormData()
  formData.append("file", file)
  axios.post(uploadUrl.value, formData, { headers: { "Content-Type": "multipart/form-data", Authorization: headers.value.Authorization } }).then(res => {
    handleUploadSuccess(res.data)
  })
}
</script>

<style scoped>
.editor-img-uploader {
  display: none;
}
.editor, .ql-toolbar {
  white-space: pre-wrap !important;
  line-height: normal !important;
}
.quill-img {
  display: none;
}
.ql-snow .ql-tooltip[data-mode="link"]::before {
  content: "url:";
}
.ql-snow .ql-tooltip.ql-editing a.ql-action::after {
  border-right: 0px;
  content: "Save";
  padding-right: 0px;
}
.ql-snow .ql-tooltip[data-mode="video"]::before {
  content: "video url:";
}
.ql-snow .ql-picker.ql-size .ql-picker-label::before,
.ql-snow .ql-picker.ql-size .ql-picker-item::before {
  content: "14px";
}
.ql-snow .ql-picker.ql-size .ql-picker-label[data-value="small"]::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value="small"]::before {
  content: "10px";
}
.ql-snow .ql-picker.ql-size .ql-picker-label[data-value="large"]::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value="large"]::before {
  content: "18px";
}
.ql-snow .ql-picker.ql-size .ql-picker-label[data-value="huge"]::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value="huge"]::before {
  content: "32px";
}
.ql-snow .ql-picker.ql-header .ql-picker-label::before,
.ql-snow .ql-picker.ql-header .ql-picker-item::before {
  content: "Text";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="1"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="1"]::before {
  content: "Title 1";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="2"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="2"]::before {
  content: "Title 2";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="3"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="3"]::before {
  content: "Title 3";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="4"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="4"]::before {
  content: "Title 4";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="5"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="5"]::before {
  content: "Title 5";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="6"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="6"]::before {
  content: "Title 6";
}
.ql-snow .ql-picker.ql-font .ql-picker-label::before,
.ql-snow .ql-picker.ql-font .ql-picker-item::before {
  content: "Standard Font";
}
.ql-snow .ql-picker.ql-font .ql-picker-label[data-value="serif"]::before,
.ql-snow .ql-picker.ql-font .ql-picker-item[data-value="serif"]::before {
  content: "Serif Font";
}
.ql-snow .ql-picker.ql-font .ql-picker-label[data-value="monospace"]::before,
.ql-snow .ql-picker.ql-font .ql-picker-item[data-value="monospace"]::before {
  content: "Monospace Font";
}
</style>
