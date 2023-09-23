<template>
  <div id="ryCkeditor5">
    <ckeditor 
      :editor="editor" 
      v-model="editorContent" 
      :disabled="disabled"
      :config="editorConfig"
      @ready="onEditorReady"
      @focus="onEditorFocus"
      @blur="onEditorBlur"
      @input="onEditorInput"
      @destroy="onEditorDestroy"
    ></ckeditor>
  </div>
</template>

<script>
import {getToken} from '@/utils/auth'
// CKEditor5 富文本编辑器
import CKEditor from '@ckeditor/ckeditor5-vue2';
import Editor from 'ckeditor5-custom-build/build/ckeditor';
import "ckeditor5-custom-build/build/translations/zh";
import axios from "axios";


class ImageUploadAdapter {

  constructor(loader, uploadUrl) {
    this.loader = loader;
    this.uploadUrl = uploadUrl;
  }

  async upload() {
    const data = new FormData();
    data.append("file", await this.loader.file);

    const res = await axios({
      url: this.uploadUrl,
      method: "POST",
      data,
      headers: {
        Authorization: 'Bearer ' + getToken()
      },
      withCredentials: true, // true 为不允许带 token, false 为允许
    });
    // 方法返回数据格式： {default: "url"}
    return {
      default: res.data.data.src,
      iurl: res.data.data.internalUrl
    };
  }

  abort() {
    this.loader.abort()
  }
}

export default {
  name: 'CKEditor5',
  components: {
    "ckeditor": CKEditor.component
  },
  props: {
    value: {
      type: String,
      default: "",
    },
    placeholder: {
      type: String,
      default: "",
    },
    minHeight: {
      type: Number,
      default: 700,
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      // CKEditor
      editor: Editor,
      editorContent: this.value, //v-model初始化值
      editorConfig: {	
        language: 'zh-cn',  //简体中文显示
        placeholder: '请输入...', //默认填充内容
        toolbar: {
          items: [
            'undo', 'redo',
            'findAndReplace',
            '|',
            'bold', 'italic', 'underline', 'strikethrough', 'superscript', 'subscript', 'removeFormat',
            '|',
            'fontFamily', 'fontSize', 'fontColor', 'fontBackgroundColor', 'highlight',
            '|',
            'heading', 
            'alignment',
            'indent',
            'outdent',
            '-',
            'bulletedList', 'numberedList', 'todoList',
            '|',
            'link',
            'insertTable',
            'blockQuote',
            'pageBreak',
            'horizontalLine',
            'imageUpload', 
            'mediaEmbed',
            'code', 
            'codeBlock', 
            'htmlEmbed', 
            'specialCharacters',
            '|',
            'sourceEditing',
          ],
          shouldNotGroupWhenFull: true
        },
        fontSize: {
          options: [ 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36 ]
        },
        fontFamily: {
          options: [ 
            'default', 
            'Blackoak Std', 
            '宋体,SimSun', 
            '新宋体,NSimSun', 
            '黑体,SimHei', 
            '微软雅黑,Microsoft YaHei', 
            '楷体_GB2312,KaiTi_GB2312', 
            '隶书,LiSu', 
            '幼圆,YouYuan', 
            '华文细黑,STXihei', 
            '细明体,MingLiU', 
            '新细明体,PMingLiU' 
          ]
        },
        //表格配置
        table: {
          contentToolbar: [
            'tableColumn', 'tableRow', 'mergeTableCells', 'tableCellProperties', 'tableProperties'
          ],
          tableProperties: {
            // The default styles for tables in the editor.
            // They should be synchronized with the content styles.
            defaultProperties: {
              borderStyle: 'solid',
              borderColor: '#666',
              borderWidth: '1px',
              alignment: 'left',
              width: '550px',
              height: '100px'
            },
            // The default styles for table cells in the editor.
            // They should be synchronized with the content styles.
            tableCellProperties: {
              defaultProperties: {
                horizontalAlignment: 'center',
                verticalAlignment: 'bottom',
                padding: '10px',
                borderStyle: 'solid',
                borderColor: '#666',
                borderWidth: '1px',
              }
            }
          }
        },
        // 允许所有标签存在iurl属性和cc-开头的属性，允许class存在cc-开头的样式名称
        htmlSupport: {
          allow: [
            {
              // name: /^(img|a|video|audio|source)$/,
              name: /[\s\S]+/,
              classes: /^cc-.*$/,
              attributes: [
                {
                  key: /^cc-.*$/,
                  value: true
                },
                {
                  key: "iurl",
                  value: true
                }
              ]
            }
          ]
        },
        // 第三方视频引用支持
        mediaEmbed: {
          previewsInData: true,
          providers: [
            {
              name: 'youku',
              url: [
                // https://v.youku.com/v_show/id_XNTk0NjI0NjExMg==.html?spm=a2hja.14919748_WEBHOME_NEW.drawer3.d_zj1_1&scm=20140719.rcmd.37023.video_XNTk0NjI0NjExMg==
                // <iframe height=498 width=510 src='https://player.youku.com/embed/XNTk0NjI0NjExMg==' frameborder=0 'allowfullscreen'></iframe>
                /^https:\/\/v\.youku\.com\/v_show\/id_([\w=]+)\.html/,
                /^<iframe[^>]+src=['"](https:\/\/player\.youku\.com\/[^'"]+)['"].*?<\/iframe>/,
              ],
              html: match => {
                const playerUrl = match[1].startsWith('https://') ? match[1] : "https://player.youku.com/embed/" + match[1];
                console.log('palyerUrl.youku', playerUrl);
                return  '<div style="position: relative;text-align:center;">' + 
                          `<iframe width="100%" height="510" src="${playerUrl}" frameborder="0" allowfullscreen></iframe>` +
                        '</div>';
              }
            },
            {
              name: 'qq',
              url: [
                // https://v.qq.com/x/cover/mzc00200cgo4wcc/r00454x3b5p.html
                // <iframe frameborder="0" src="https://v.qq.com/txp/iframe/player.html?vid=r00454x3b5p" allowFullScreen="true"></iframe>
                // https://v.qq.com/x/page/w350438y1fs.html
                // <iframe frameborder="0" src="https://v.qq.com/txp/iframe/player.html?vid=w350438y1fs" allowFullScreen="true"></iframe>
                /^https:\/\/v\.qq\.com\/x\/cover\/[\w]+\/(\w+)\.html/,
                /^https:\/\/v\.qq\.com\/x\/page\/(\w+)\.html/,
                /^<iframe[^>]+src=['"](https:\/\/v\.qq\.com\/[^'"]+)['"].*?<\/iframe>/,
              ],
              html: match => {
                const playerUrl = match[1].startsWith('https://') ? match[1] : "https://v.qq.com/txp/iframe/player.html?vid=" + match[1];
                console.log('palyerUrl.qq', playerUrl);
                return  '<div style="position: relative;text-align:center;">' +
                          `<iframe width="100%" height="510" src="${playerUrl}" frameborder="0" allowfullscreen="true"></iframe>` +
                        '</div>';
              }
            },
            {
              name: 'iqiyi',
              url: [
                // http://www.iqiyi.com/v_19rxkql7qk.html
                // http://www.iqiyi.com/a_1dwtgvklkr5.html
                /^http:\/\/www\.iqiyi\.com\/[\w]_(\w+)\.html/,
              ],
              html: match => {
                const playerUrl = match[0];
                console.log('palyerUrl', playerUrl);
                return  '<div style="position: relative;text-align:center;">' +
                          `<iframe width="100%" height="510" src="${playerUrl}" frameborder="0" allowfullscreen="true"></iframe>` +
                        '</div>';
              }
            },
            {
              name: 'bilibili',
              url: [
                // <iframe src="//player.bilibili.com/player.html?aid=694195827&bvid=BV1r24y1W7E1&cid=1003788458&page=1" scrolling="no" border="0" frameborder="no" framespacing="0" allowfullscreen="true"> </iframe>
                // <iframe src="//player.bilibili.com/player.html?aid=947755350&bvid=BV1YW4y137Kk&cid=966243153&page=1" scrolling="no" border="0" frameborder="no" framespacing="0" allowfullscreen="true"> </iframe>
                /^<iframe[^>]+src=['"](\/\/player\.bilibili\.com\/[^'"]+)['"].*?<\/iframe>/,
              ],
              html: match => {
                const playerUrl = match[1];
                console.log('palyerUrl', playerUrl);
                return  '<div style="position: relative;text-align:center;">' +
                          `<iframe width="100%" height="510" src="${playerUrl}"` + 
                          ' scrolling="no" border="0" frameborder="no" framespacing="0" allowfullscreen="true"></iframe>' +
                        '</div>';
              }
            }, 
            {
              name: 'haokan',
              url: [
                // https://haokan.baidu.com/v?vid=17491163422580795321&pd=pcshare&hkRelaunch=p1%3Dpc%26p2%3Dvideoland%26p3%3Dshare_input
                /^https:\/\/haokan\.baidu\.com\/v\?vid=.*?share_input/,
              ],
              html: match => {
                const playerUrl = match[0];
                console.log('palyerUrl', playerUrl);
                return  '<div style="position: relative;text-align:center;">' +
                          `<iframe width="100%" height="510" src="${playerUrl}"` + 
                          ' scrolling="no" border="0" frameborder="no" framespacing="0" allowfullscreen="true"></iframe>' +
                        '</div>';
              }
            }
          ]
        }
      }
    };
  },
  watch: {
    editorContent: function () {
      this.value = this.editorContent;
    },
    value: function() {
      this.editorContent = this.value;
    }
  },
  methods: {
    onEditorReady( editor )  {
      console.log("CKEditor ready...");
      // 设置下编辑器最小高度
      editor.editing.view.change(writer => {
        writer.setStyle('min-height', '800px', editor.editing.view.document.getRoot());
      });
      // 图片上传
      editor.plugins.get('FileRepository').createUploadAdapter = (loader) => {
        return new ImageUploadAdapter(loader, process.env.VUE_APP_BASE_API + "/cms/resource/upload")
      }
      // 设置图片iurl属性
      const imageUploadEditing = editor.plugins.get( 'ImageUploadEditing' );
      imageUploadEditing.on( 'uploadComplete', ( evt, { data, imageElement } ) => {
        console.log("uploadComplete", data, imageElement)
        editor.model.change( writer => {
          writer.setAttribute( 'iurl', data.iurl, imageElement );
        });
      });
      editor.model.schema.extend('imageBlock', {
        allowAttributes: ['iurl'],
      });
      editor.model.schema.extend('imageInline', {
        allowAttributes: ['iurl'],
      });
      editor.conversion.for('downcast').add((dispatcher) => {
          dispatcher.on('attribute:iurl:imageInline', (evt, data, conversionApi) => {
            const viewWriter = conversionApi.writer;
            const img = conversionApi.mapper.toViewElement(data.item);
            if (img.name === 'img' && data.attributeNewValue !== null) {
              viewWriter.setAttribute('iurl', data.attributeNewValue, img);
            }
          });
          
          dispatcher.on('attribute:iurl:imageBlock', (evt, data, conversionApi) => {
            const viewWriter = conversionApi.writer;
            let figure = conversionApi.mapper.toViewElement(data.item);
            if (figure.name === 'figure' && figure.childCount > 0) {
              figure._children.forEach(item => {
                if (item.name === 'img') {
                  viewWriter.setAttribute('iurl', data.attributeNewValue, item);
                }
              })
            }
          })
        }
      );
      // ready event
      this.$emit("ready", editor);
    },
    onEditorFocus(editor) {
      console.log("CKEditor focus...");
      this.$emit("focus", editor);
    },
    onEditorBlur(editor) {
      console.log("CKEditor blur...");
      this.$emit("blur", editor);
    },
    onEditorInput(editor) {
      console.log("CKEditor input...");
      this.$emit("input", editor);
    },
    onEditorDestroy(editor) {
      console.log("CKEditor destory...");
      this.$emit("destory", editor);
    }
  }
}
</script>