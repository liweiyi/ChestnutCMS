<template>
  <div class="quill-editor">
    <slot name="toolbar"></slot>
    <div ref="editor"></div>
    
    <!-- 素材选择组件 -->
    <cms-resource-dialog 
      :open.sync="openResourceDialog"
      rtype="image"
      :upload-limit="uploadLimit"
      @ok="handleResourceDialogOk">
    </cms-resource-dialog>
  </div>
</template>

<script>
  import CMSResourceDialog from "@/views/cms/contentcore/resourceDialog";
  import _Quill from 'quill'
  import "quill/dist/quill.core.css";
  import "quill/dist/quill.snow.css";
  import "quill/dist/quill.bubble.css";
  import defaultOptions from './options'
  const Quill = window.Quill || _Quill

  // pollfill
  if (typeof Object.assign != 'function') {
    Object.defineProperty(Object, 'assign', {
      value(target, varArgs) {
        if (target == null) {
          throw new TypeError('Cannot convert undefined or null to object')
        }
        const to = Object(target)
        for (let index = 1; index < arguments.length; index++) {
          const nextSource = arguments[index]
          if (nextSource != null) {
            for (const nextKey in nextSource) {
              if (Object.prototype.hasOwnProperty.call(nextSource, nextKey)) {
                to[nextKey] = nextSource[nextKey]
              }
            }
          }
        }
        return to
      },
      writable: true,
      configurable: true
    })
  }

  let BlockEmbed = _Quill.imports['blots/embed'];
  class ImageBlot extends BlockEmbed {
      static create(value) {
          let node = super.create();
          node.setAttribute('src', value.src);
          if (value.iurl) {
            node.setAttribute('iurl', value.iurl);
          }
          if (value.alt) {
            node.setAttribute('alt', value.alt);
          }
          if (value.width) {
            node.setAttribute('width', value.width);
          }
          return node;
      }

      static value(node) {
          return {
              src: node.getAttribute('src'),
              iurl: node.getAttribute('iurl'),
              alt: node.getAttribute('alt'),
              width: node.getAttribute('width'),
          }
      }
  }
  ImageBlot.blotName = 'image';
  ImageBlot.tagName = 'img';
  _Quill.register(ImageBlot);

  // export
  export default {
    name: 'cms-quill-editor',
    components: {
      'cms-resource-dialog': CMSResourceDialog
    },
    data() {
      return {
        _options: {},
        _content: '',
        defaultOptions,
        openResourceDialog: false,
        uploadLimit: 10
      }
    },
    props: {
      content: String,
      value: String,
      disabled: {
        type: Boolean,
        default: false
      },
      options: {
        type: Object,
        required: false,
        default: () => ({})
      },
      globalOptions: {
        type: Object,
        required: false,
        default: () => ({})
      }
    },
    mounted() {
      this.initialize()
    },
    beforeDestroy() {
      this.quill = null
      delete this.quill
    },
    methods: {
      // Init Quill instance
      initialize() {
        if (this.$el) {

          // Options
          this._options = Object.assign({}, this.defaultOptions, this.globalOptions, this.options)

          // Instance
          this.quill = new Quill(this.$refs.editor, this._options)
          
          this.quill.enable(false)

          // Set editor content
          if (this.value || this.content) {
            this.quill.pasteHTML(this.value || this.content)
          }

          // Disabled editor
          if (!this.disabled) {
            this.quill.enable(true)
          }
          
          this.quill.getModule("toolbar").addHandler("image", (value) => {
            this.uploadType = "image";
            if (value) {
              this.openResourceDialog = true;
              this.currentSelection = this.quill.getSelection();
            } else {
              this.quill.format("image", false);
            }
          });

          // Mark model as touched if editor lost focus
          this.quill.on('selection-change', range => {
            if (!range) {
              this.$emit('blur', this.quill)
            } else {
              this.$emit('focus', this.quill)
            }
          })

          // Update model if text changes
          this.quill.on('text-change', (delta, oldDelta, source) => {
            let html = this.$refs.editor.children[0].innerHTML
            const quill = this.quill
            const text = this.quill.getText()
            if (html === '<p><br></p>') html = ''
            this._content = html
            this.$emit('input', this._content)
            this.$emit('change', { html, text, quill })
          })

          // Emit ready event
          this.$emit('ready', this.quill)
        }
      },
      handleResourceDialogOk(results) {
        if (results && results.length > 0) {
          for (let i = 0; i < results.length; i++) {
            let r = results[i];
            // 获取光标所在位置
            let length = this.currentSelection.index;
            // 插入图片  res.url为服务器返回的图片地址
            if (r.net) {
              this.quill.insertEmbed(length, "image", { 
                src: r.src,
                width: 600
              });
            } else {
              this.quill.insertEmbed(length, "image", { 
                src: r.src,
                iurl: r.path,
                alt: r.name,
                width: 600
              });
            }
            this.quill.setSelection(length + 1);
          }
        }
      }
    },
    watch: {
      // Watch content change
      content(newVal, oldVal) {
        if (this.quill) {
          if (newVal && newVal !== this._content) {
            this._content = newVal
            this.quill.pasteHTML(newVal)
          } else if(!newVal) {
            this.quill.setText('')
          }
        }
      },
      // Watch content change
      value(newVal, oldVal) {
        if (this.quill) {
          if (newVal && newVal !== this._content) {
            this._content = newVal
            this.quill.pasteHTML(newVal)
          } else if(!newVal) {
            this.quill.setText('')
          }
        }
      },
      // Watch disabled change
      disabled(newVal, oldVal) {
        if (this.quill) {
          this.quill.enable(!newVal)
        }
      }
    }
  }
</script>
<style>
.quill-editor, .ql-toolbar {
  white-space: pre-wrap !important;
  line-height: normal !important;
}
</style>
