<template>
  <div class="ueditor">
    <vue-ueditor-wrap 
      v-model="contentHtml"
      :editor-id="editorId"
      :config="editorConfig"
      :editorDependencies="editorDependencies"
      @before-init="handleBeforeInit"
      @ready="handleReady"
      :style="styles" />
    <!-- 素材库 -->
    <cms-resource-dialog 
      :open.sync="openResourceDialog"
      :upload-limit="100" 
      :rtype="resourceType"
      @ok="handleResourceDialogOk">
    </cms-resource-dialog>
    <!-- 栏目选择组件 -->
    <cms-catalog-selector
      :open="openCatalogSelector"
      @ok="handleCatalogSelectorOk"
      @close="handleCatalogSelectorClose"></cms-catalog-selector>
    <!-- 内容选择组件 -->
    <cms-content-selector
      :open="openContentSelector"
      :contentType="contentType"
      @ok="handleContentSelectorOk"
      @close="handleContentSelectorClose"></cms-content-selector>
    <!-- 第三方视频 -->
    <cms-third-video :open.sync="openThirdVideoDialog" @ok="handleThirdVideoDialogOk"></cms-third-video>
    <!-- 百度地图 -->
    <cms-baidu-map :open.sync="openBaiduMapDialog" @ok="handleBaiduMapDialogOk"></cms-baidu-map>
  </div>
</template>

<script>
import VueUEditorWrap from '@/components/UEditorWrap';
import CMSCatalogSelector from "@/views/cms/contentcore/catalogSelector";
import CMSContentSelector from "@/views/cms/contentcore/contentSelector";
import CMSResourceDialog from "@/views/cms/contentcore/resourceDialog";
import CMSUeditorThirdVideo from "./thrid-video";
import CMSUeditorBaiduMap from "./baidu-map";
import { checkSensitiveWords } from "@/api/word/sensitiveWord";
import { checkFallibleWords } from "@/api/word/errorProneWord"

const UE_HOME = '/UEditorPlus/';

export default {
  name: "UEditorPlus",
  props: {
    /* 编辑器的内容 */
    value: {
      type: String,
      default: "",
    },
    /* 默认高度 */
    height: {
      type: Number,
      default: 500,
    },
    editorId: {
      type: String,
      default: "ueditor"
    },
    configs: {
      type: Object,
      default: () => ({})
    }
  },
  components: {
    "vue-ueditor-wrap": VueUEditorWrap,
    'cms-catalog-selector': CMSCatalogSelector,
    'cms-content-selector': CMSContentSelector,
    "cms-resource-dialog": CMSResourceDialog,
    "cms-third-video": CMSUeditorThirdVideo,
    "cms-baidu-map": CMSUeditorBaiduMap
  },
  computed: {
    styles() {
      let style = {};
      // if (this.height) {
      //   style.minHeight = `${this.height}px`;
      // }
      return style;
    },
  },
  watch: {
    contentHtml (newV) {
      this.$emit('input', newV)
    },
    value (newV) {
      this.contentHtml = newV
    }
  },
  created() {
    this.init();
  },
  beforeDestroy() {
  },
  data() {
    return {
      contentHtml: "",
      editorDependencies: [
        'ueditor.config.js',
        'ueditor.all.js',
        'xiumi/xiumi-ue-dialog-v5.js',
        'xiumi/xiumi-ue-v5.css',
      ],
      editorConfig: {
        UEDITOR_HOME_URL: UE_HOME,
        serverUrl: undefined,
        catchRemoteImageEnable: false, // 自动下载远程图片
        maximumWords:10000, // 最大字数
        initialFrameHeight: this.height, // 初始高度
        autoSaveEnable: false, // 自动保存
        enableDragUpload: false,
        enablePasteUpload: false,
        imagePopup: false, // 关闭默认图片选择快捷工具栏
        pageBreakTag: '__XY_UEDITOR_PAGE_BREAK__',
        iframeCssUrlsAddition: [
          UE_HOME + 'themes/placeholder.css'
        ],
        lang: this.$i18n.locale ? this.$i18n.locale.toLowerCase() : 'zh-cn',
        langPath: UE_HOME + "lang/",
        iframeUrlMap: {
          anchor: UE_HOME + "dialogs/anchor/anchor.html?20220503",
          link: UE_HOME + "dialogs/link/link.html?20220503",
          spechars: UE_HOME + "dialogs/spechars/spechars.html?20220503",
          searchreplace: UE_HOME + "dialogs/searchreplace/searchreplace.html?20220503",
          // insertvideo: UE_HOME + "dialogs/video/video.html?20220503",
          help: UE_HOME + "dialogs/help/help.html?20220503",
          preview: UE_HOME + "dialogs/preview/preview.html?20220503",
          emotion: UE_HOME + "dialogs/emotion/emotion.html?20220503",
          wordimage: UE_HOME + "dialogs/wordimage/wordimage.html?20220902",
          formula: UE_HOME + "dialogs/formula/formula.html?20220902",
          attachment: UE_HOME + "dialogs/attachment/attachment.html?20220503",
          insertframe: UE_HOME + "dialogs/insertframe/insertframe.html?20220503",
          edittip: UE_HOME + "dialogs/table/edittip.html?20220503",
          edittable: UE_HOME + "dialogs/table/edittable.html?20220503",
          edittd: UE_HOME + "dialogs/table/edittd.html?20220503",
          scrawl: UE_HOME + "dialogs/scrawl/scrawl.html?20220503",
          template: UE_HOME + "dialogs/template/template.html?20220503",
          background: UE_HOME + "dialogs/background/background.html?20220503",
          map: UE_HOME + "dialogs/map/map.html?20220503",
        },
        toolbars: [
          [
            "fullscreen",   // 全屏
            "source",       // 源代码
            "|",
            "undo",         // 撤销
            "redo",         // 重做
            "|",
            "bold",         // 加粗
            "italic",       // 斜体
            "underline",    // 下划线
            "fontborder",   // 字符边框
            "strikethrough",// 删除线
            "superscript",  // 上标
            "subscript",    // 下标
            "removeformat", // 清除格式
            "formatmatch",  // 格式刷
            "autotypeset",  // 自动排版
            "blockquote",   // 引用
            "pasteplain",   // 纯文本粘贴模式
            "|",
            "forecolor",    // 字体颜色
            "backcolor",    // 背景色
            "insertorderedlist",   // 有序列表
            "insertunorderedlist", // 无序列表
            "selectall",    // 全选
            "cleardoc",     // 清空文档
            "|",
            "rowspacingtop",// 段前距
            "rowspacingbottom",    // 段后距
            "lineheight",          // 行间距
            "|",
            "emotion",             // 表情
            "customstyle",         // 自定义标题
            "paragraph",           // 段落格式
            "fontfamily",          // 字体
            "fontsize",            // 字号
            "|",
            "directionalityltr",   // 从左向右输入
            "directionalityrtl",   // 从右向左输入
            "indent",              // 首行缩进
            "|",
            "justifyleft",         // 居左对齐
            "justifycenter",       // 居中对齐
            "justifyright",
            "justifyjustify",      // 两端对齐
            "|",
            "touppercase",         // 字母大写
            "tolowercase",         // 字母小写
            "|",
            "link",                // 超链接
            "unlink",              // 取消链接
            "anchor",              // 锚点
            "|",
            "imagenone",           // 图片默认
            "imageleft",           // 图片左浮动
            "imageright",          // 图片右浮动
            "imagecenter",         // 图片居中
            "|",
            // "scrawl",              // 涂鸦
            "xy-third-video",         // 视频
            'xy-resource',
            'xy-content',
            "xy-check-word",
            // "insertframe",         // 插入Iframe
            "xy-baidu-map",        // 百度地图
            "insertcode",          // 插入代码
            "pagebreak",           // 分页
            "template",            // 模板
            "background",          // 背景
            "formula",             // 公式
            "|",
            "horizontal",          // 分隔线
            "date",                // 日期
            "time",                // 时间
            "spechars",            // 特殊字符
            "|",
            "inserttable",         // 插入表格
            "deletetable",         // 删除表格
            "insertparagraphbeforetable",     // 表格前插入行
            "insertrow",           // 前插入行
            "deleterow",           // 删除行
            "insertcol",           // 前插入列
            "deletecol",           // 删除列
            "mergecells",          // 合并多个单元格
            "mergeright",          // 右合并单元格
            "mergedown",           // 下合并单元格
            "splittocells",        // 完全拆分单元格
            "splittorows",         // 拆分成行
            "splittocols",         // 拆分成列
            "|",
            "searchreplace",       // 查询替换
            "xiumi-dialog",
          ],
        ]
      },
      openResourceDialog: false,
      resourceType: 'image',
      openThirdVideoDialog: false,
      openCatalogSelector: false,
      openContentSelector: false,
      openBaiduMapDialog: false,
      xyContentBtnValue: 'catalog',
      contentType: '',
      checkWordType: 'sensitive'
    };
  },
  methods: {
    init() {
      Object.keys(this.configs).forEach(key => {
        this.editorConfig[key] = this.configs[key];
      })
    },
    handleBeforeInit(editorId) {
      console.log('ueditor-plus.before-init', editorId)
      this.addXyContentButton(editorId)
      this.addXyResourceButton(editorId)
      this.addThirdVideoButton(editorId)
      this.addXyWordCheckButton(editorId)
      this.addBaiduMapButton(editorId)
    },
    handleReady(editorInstance) {
      // console.log('ueditor-plus.ready: ' + editorInstance.key, editorInstance)
      this.addPopup(editorInstance)
      // addXyContentButton
      editorInstance.onXyContentButtonClick = this.handleXyContentButtonClick
      // addXyResourceButton
      editorInstance.onXyResourceButtonClick = this.handleXyResourceButtonClick
      // addThirdVideoButton
      editorInstance.onThirdVideoButtonClick = this.handleThirdVideoButtonClick
      // addXyWordCheckButton
      editorInstance.onXyWordCheck = this.handleXyWordCheck
      editorInstance.onXyWordHighlight = this.handleXyWordHighlight
      editorInstance.onXyWordReplace = this.handleXyWordReplace
      editorInstance.onXyWordReplace2 = this.handleXyWordReplace2
      // addBaiduMapButton
      editorInstance.onBaiduMapButtonClick = this.handleBaiduMapButtonClick
    },
    addPopup(editor) {
      var domUtils = baidu.editor.dom.domUtils;
      var popup = new baidu.editor.ui.Popup({
        editor: editor,
        content: "",
        className: "edui-bubble",
        _onEditButtonClick: function () {
          this.hide();
          editor.ui._dialogs.linkDialog.open();
        },
        _onImgEditButtonClick: function (name) {
          this.hide();
          if (name == "xy-resource") {
            editor.execCommand("xy-resource", 'image');
          } else {
            editor.ui._dialogs[name] && editor.ui._dialogs[name].open();
          }
        },
        _onImgSetFloat: function (value) {
          this.hide();
          editor.execCommand("imagefloat", value);
        },
        _setIframeAlign: function (value) {
          var frame = popup.anchorEl;
          var newFrame = frame.cloneNode(true);
          switch (value) {
            case -2:
              newFrame.setAttribute("align", "");
              break;
            case -1:
              newFrame.setAttribute("align", "left");
              break;
            case 1:
              newFrame.setAttribute("align", "right");
              break;
          }
          frame.parentNode.insertBefore(newFrame, frame);
          domUtils.remove(frame);
          popup.anchorEl = newFrame;
          popup.showAnchor(popup.anchorEl);
        },
        _updateIframe: function () {
          var frame = (editor._iframe = popup.anchorEl);
          if (domUtils.hasClass(frame, "ueditor_baidumap")) {
            editor.selection.getRange().selectNode(frame).select();
            editor.ui._dialogs.mapDialog.open();
            popup.hide();
          } else {
            editor.ui._dialogs.insertframeDialog.open();
            popup.hide();
          }
        },
        _onRemoveButtonClick: function (cmdName) {
          editor.execCommand(cmdName);
          this.hide();
        },
        queryAutoHide: function (el) {
          if (el && el.ownerDocument == editor.document) {
            if (
              el.tagName.toLowerCase() == "img" ||
              domUtils.findParentByTagName(el, "a", true)
            ) {
              return el !== popup.anchorEl;
            }
          }
          return baidu.editor.ui.Popup.prototype.queryAutoHide.call(this, el);
        }
      });
      popup.render();
      editor.addListener("mouseover", function (t, evt) {
        evt = evt || window.event;
        var el = evt.target || evt.srcElement;
        if (
          editor.ui._dialogs.insertframeDialog &&
          /iframe/gi.test(el.tagName)
        ) {
          var html = popup.formatHtml(
            "<nobr>" +
            '<span onclick=$$._setIframeAlign(-2) class="edui-clickable">' +
            editor.getLang("default") +
            '</span>&nbsp;&nbsp;<span onclick=$$._setIframeAlign(-1) class="edui-clickable">' +
            editor.getLang("justifyleft") +
            '</span>&nbsp;&nbsp;<span onclick=$$._setIframeAlign(1) class="edui-clickable">' +
            editor.getLang("justifyright") +
            "</span>&nbsp;&nbsp;" +
            ' <span onclick="$$._updateIframe( this);" class="edui-clickable">' +
            editor.getLang("modify") +
            "</span></nobr>"
          );
          if (html) {
            popup.getDom("content").innerHTML = html;
            popup.anchorEl = el;
            popup.showAnchor(popup.anchorEl);
          } else {
            popup.hide();
          }
        }
      });
      editor.addListener("selectionchange", function (t, causeByUi) {
        if (!causeByUi) return;
        var html = "",
          str = "",
          img = editor.selection.getRange().getClosedNode(),
          dialogs = editor.ui._dialogs;
        if (img && img.tagName == "IMG") {
          var dialogName = "xy-resource";
          if (img.getAttribute("anchorname")) {
            dialogName = "anchorDialog";
            html = popup.formatHtml(
              "<nobr>" +
              '<span onclick=$$._onImgEditButtonClick("anchorDialog") class="edui-clickable">' +
              editor.getLang("modify") +
              "</span>&nbsp;&nbsp;" +
              "<span onclick=$$._onRemoveButtonClick('anchor') class=\"edui-clickable\">" +
              editor.getLang("delete") +
              "</span></nobr>"
            );
          }
          if (
            domUtils.hasClass(img, "loadingclass") ||
            domUtils.hasClass(img, "loaderrorclass")
          ) {
            dialogName = "";
          }
          if (dialogName == "") {
            return;
          }

          var actions = [];
          actions.push('<nobr />');
          actions.push('<span onclick=$$._onImgSetFloat("none") class="edui-clickable edui-popup-action-item">' +
            editor.getLang("default") +
            "</span>");
          actions.push('<span onclick=$$._onImgSetFloat("left") class="edui-clickable edui-popup-action-item">' +
            editor.getLang("justifyleft") +
            "</span>");
          actions.push('<span onclick=$$._onImgSetFloat("right") class="edui-clickable edui-popup-action-item">' +
            editor.getLang("justifyright") +
            "</span>");
          actions.push('<span onclick=$$._onImgSetFloat("center") class="edui-clickable edui-popup-action-item">' +
            editor.getLang("justifycenter") +
            "</span>");
          if (img.getAttribute('data-formula-image') !== null) {
            actions.push("<span onclick=\"$$._onImgEditButtonClick('formulaDialog');\" class='edui-clickable edui-popup-action-item'>" +
                editor.getLang("formulaedit") + "</span>");
          }
          if (img.getAttribute("data-word-image")) {
            actions.push("<span onclick=\"$$._onImgEditButtonClick('wordimageDialog');\" class='edui-clickable edui-popup-action-item'>" +
              editor.getLang("save") +
              "</span>");
          } else {
            actions.push("<span onclick=\"$$._onImgEditButtonClick('" + dialogName + '\');" class="edui-clickable edui-popup-action-item">' +
              editor.getLang("modify") +
              "</span>");
          }
          actions.push("</nobr>");

          !html && (html = popup.formatHtml(actions.join("")));
        }
        if (editor.ui._dialogs.linkDialog) {
          var link = editor.queryCommandValue("link");
          var url;
          if (
            link &&
            (url = link.getAttribute("_href") || link.getAttribute("href", 2))
          ) {
            var txt = url;
            if (url.length > 30) {
              txt = url.substring(0, 20) + "...";
            }
            if (html) {
              html += '<div style="height:5px;"></div>';
            }
            html += popup.formatHtml(
              "<nobr>" +
              editor.getLang("anchorMsg") +
              ': <a target="_blank" href="' +
              url +
              '" title="' +
              url +
              '" >' +
              txt +
              "</a>" +
              ' <span class="edui-clickable" onclick="$$._onEditButtonClick();">' +
              editor.getLang("modify") +
              "</span>" +
              ' <span class="edui-clickable" onclick="$$._onRemoveButtonClick(\'unlink\');"> ' +
              editor.getLang("clear") +
              "</span></nobr>"
            );
            popup.showAnchor(link);
          }
        }

        if (html) {
          popup.getDom("content").innerHTML = html;
          popup.anchorEl = img || link;
          popup.showAnchor(popup.anchorEl);
        } else {
          popup.hide();
        }
      });
    },
    addXyContentButton(editorId) {
      const that = this
      window.UE.registerUI('xy-content', function (editor, uiName) {
        editor.registerCommand(uiName,{
          execCommand:function(cmdName,value){
            editor.onXyContentButtonClick(cmdName, value);
          }
        });
        const _onMenuClick = function() {
            editor.execCommand(uiName, this.value);
          }
        const items = [
          {
            label: that.$t('CMS.UEditor.InsertCatalogLink'),
            value: "catalog",
            theme: editor.options.theme,
            onclick: _onMenuClick
          },
          {
            label: that.$t('CMS.UEditor.InsertContentLink'),
            value: "content",
            theme: editor.options.theme,
            onclick: _onMenuClick
          },
          {
            label: that.$t('CMS.UEditor.InsertImageGroup'),
            value: "img_group",
            theme: editor.options.theme,
            onclick: _onMenuClick
          }
        ];
        const ui = new UE.ui.MenuButton({
          editor: editor,
          className: "edui-for-" + uiName,
          title: that.$t('CMS.UEditor.InsertContentLink'),
          items: items,
          onbuttonclick: function() {
            editor.execCommand(uiName);
          }
        });
        return ui;
      });
    },
    handleXyContentButtonClick(cmd, value) {
      if (value) {
        this.xyContentBtnValue = value
        this.contentType = value == 'img_group' ? "image" : ""
      }
      if (this.xyContentBtnValue == 'catalog') {
        this.openCatalogSelector = true
      } else {
        this.openContentSelector = true
      }
    },
    handleCatalogSelectorOk(catalogs) {
      if (catalogs && catalogs.length > 0) {
        var editor = window.UE.getEditor(this.editorId)
        editor.execCommand("insertHTML", '<a href="' + catalogs[0].props.internalUrl + '">' + catalogs[0].name + '</a>');
        this.openCatalogSelector = false;
      } else {
        this.$modal.msgWarning(this.$t('Common.SelectFirst'));
      }
    },
    handleCatalogSelectorClose() {
      this.openCatalogSelector = false;
    },
    handleContentSelectorOk(contents) {
      if (contents && contents.length > 0) {
        var editor = window.UE.getEditor(this.editorId)
        if (this.contentType == 'image') {
          // 插入组图
          const html = '<p class="text-align:center;"><img src="/UEditorPlus/themes/default/images/spacer.gif" ex_cid="'
            + contents[0].contentId + '" title="' + contents[0].title + '" class="img_group_placeholder" /></p>'
          editor.execCommand("insertHTML", html);
        } else {
          editor.execCommand("insertHTML", '<a href="' + contents[0].internalUrl + '">' + contents[0].title + '</a>');
        }
        this.openContentSelector = false;
      } else {
        this.$modal.msgWarning(this.$t('Common.SelectFirst'));
      }
    },
    handleContentSelectorClose() {
      this.openContentSelector = false;
      this.contentType = ''
    },
    addXyResourceButton(editorId) {
      const that = this
      window.UE.registerUI('xy-resource', function (editor, uiName) {
        editor.registerCommand(uiName,{
          execCommand:function(cmdName,value){
            editor.onXyResourceButtonClick(cmdName, value);
          }
        });
        const _onMenuClick = function() {
            editor.execCommand(uiName, this.value);
          }
        const items = [
          {
            label: that.$t('CMS.UEditor.ResourceType.Image'),
            value: "image",
            theme: editor.options.theme,
            onclick: _onMenuClick
          },
          {
            label: that.$t('CMS.UEditor.ResourceType.Audio'),
            value: "audio",
            theme: editor.options.theme,
            onclick: _onMenuClick
          },
          {
            label: that.$t('CMS.UEditor.ResourceType.Video'),
            value: "video",
            theme: editor.options.theme,
            onclick: _onMenuClick
          },
          {
            label: that.$t('CMS.UEditor.ResourceType.File'),
            value: "file",
            theme: editor.options.theme,
            onclick: _onMenuClick
          }
        ];
        const ui = new UE.ui.MenuButton({
          editor: editor,
          className: "edui-for-" + uiName,
          title: that.$t('CMS.UEditor.InsertResource'),
          items: items,
          onbuttonclick: function() {
            editor.execCommand(uiName);
          }
        });
        return ui;
      });
    },
    handleXyResourceButtonClick(cmd, value) {
      if (value) {
        this.resourceType = value
      }
      this.openResourceDialog = true
    },
    handleResourceDialogOk (results) {
      if (results && results.length > 0) {
        const r = results[0];
        var html = '';
        results.forEach(r => {
          if (r.resourceType == 'image') {
            html += '<p><img src="' + r.src + '" iurl="' + r.path + '" class="art-body-img" /></p>'
          } else {
            html += '<p><a href="' + r.src + '" iurl="' + r.path + '" target="_blank" class="art-body-' + r.resourceType + '">' + r.name + '</a></p>'
          }
        });
        if (html && html.length > 0) {
          var editor = window.UE.getEditor(this.editorId)
          editor.execCommand("insertHTML",html);
        }
      }
    },
    // 插入第三方视频分享
    addThirdVideoButton(editorId) {
      const that = this
      window.UE.registerUI('xy-third-video', function (editor, uiName) {
        editor.registerCommand(uiName, {
          execCommand: function(cmdName){
            editor.onThirdVideoButtonClick(cmdName);
          }
        });
        const ui = new UE.ui.Button({
          className: "edui-for-" + uiName,
          name: that.$t('CMS.UEditor.InsertThirdVideo'),
          title: that.$t('CMS.UEditor.InsertThirdVideo'),
          onclick: function() {
            editor.execCommand(uiName);
          }
        });
        return ui;
      });
    },
    handleThirdVideoButtonClick(cmd) {
      this.openThirdVideoDialog = true
    },
    handleThirdVideoDialogOk (result) {
      if (result && result.length > 0) {
        var editor = window.UE.getEditor(this.editorId)
        editor.execCommand("insertHTML", result);
      }
    },
    addXyWordCheckButton(eidtorId) {
      const that = this
      window.UE.registerUI('xy-check-word', function (editor, uiName) {
        editor.registerCommand(uiName,{
          execCommand:function(cmdName,value){
            editor.onXyWordCheck(cmdName, value)
          }
        });
        editor.registerCommand('xy-highlight-word',{
          execCommand:function(cmdName, words){
            editor.onXyWordHighlight(cmdName, words)
          }
        });
        editor.registerCommand('xy-replace-word',{
          execCommand:function(cmdName, list){
            editor.onXyWordReplace(cmdName, words)
          }
        });
        editor.registerCommand('xy-replace-word2',{
          execCommand:function(cmdName, words, replacement){
            editor.onXyWordReplace2(cmdName, words, replacement)
          }
        });
        const _onMenuClick = function() {
          editor.execCommand(uiName, this.value);
        }
        const items = [
          {
            label: that.$t('CMS.UEditor.SensitiveWordCheck'),
            value: "sensitive",
            theme: editor.options.theme,
            onclick: _onMenuClick
          },
          {
            label: that.$t('CMS.UEditor.FallibleWordCheck'),
            value: "fallible",
            theme: editor.options.theme,
            onclick: _onMenuClick
          }
        ];
        const ui = new UE.ui.MenuButton({
          editor: editor,
          className: "edui-for-" + uiName,
          title: "Check Word",
          items: items,
          onbuttonclick: function() {
            editor.execCommand(uiName);
          }
        });
        return ui;
      });
    },
    handleXyWordCheck(cmd, value) {
      this.sensitiveType = value || this.sensitiveType
      const editor = window.UE.getEditor(this.editorId)
      if (value == 'sensitive') {
        const txt = editor.getContentTxt();
        if (!txt || txt.length == 0) {
          return;
        }
        checkSensitiveWords(editor.getContentTxt()).then(response => {
          if (response.data.length == 0) {
            this.$modal.msgSuccess(this.$t('CMS.UEditor.NoSensitiveWord'));
          } else {
            this.$prompt(response.data.join("<br/>"), this.$t('CMS.UEditor.FoundSensitiveWord'), {
              confirmButtonText: this.$t('CMS.UEditor.ReplaceWord'),
              cancelButtonText: this.$t('CMS.UEditor.HighlightWord'),
              inputPlaceholder: this.$t('CMS.UEditor.InputReplacement'),
              inputValue: "*",
              dangerouslyUseHTMLString: true,
              distinguishCancelAndClose: true
            }).then(({ value }) => {
              editor.execCommand('xy-replace-word2', response.data, value);
            }).catch(() => {
              editor.execCommand('xy-highlight-word', response.data);
            });
          }
        })
      } else if (value == 'fallible') {
        checkFallibleWords(editor.getContentTxt()).then(response => {
          if (response.data.length == 0) {
            this.$modal.msgSuccess(this.$t('CMS.UEditor.NoFallibleWord'));
          } else {
            const str = response.data.map(item => item.w + " > " + item.r).join("<br/>");
            this.$confirm(str, this.$t('CMS.UEditor.FoundFallibleWord'), {
              confirmButtonText: this.$t('CMS.UEditor.ReplaceWord'),
              cancelButtonText: this.$t('CMS.UEditor.HighlightWord'),
              dangerouslyUseHTMLString: true,
              distinguishCancelAndClose: true
            }).then(() => {
              editor.execCommand('xy-replace-word', response.data)
            }).catch(() => {  
              editor.execCommand('xy-highlight-word', response.data.map(item => item.w))
            });
          }
        })
      }
    },
    handleXyWordHighlight(cmd, words) {
      const editor = window.UE.getEditor(this.editorId)
      let html = editor.getContent();
      // 先去掉可能存在的高亮标签
      html = html.replace(new RegExp("<span class=\"warning_tip\">([^<]*)</span>", "ig"), '$1');
      words.forEach(word => {
        html = html.replace(new RegExp(word, "ig"), '<span class="warning_tip">' + word + '</span>')
      })
      editor.setContent(html)
    },
    handleXyWordReplace(cmd, list) {
      const editor = window.UE.getEditor(this.editorId)
      let html = editor.getContent();
      // 先去掉可能存在的高亮标签
      html = html.replace(new RegExp("<span class=\"warning_tip\">([^<]*)</span>", "ig"), '$1');
      list.forEach(item => {
        html = html.replace(new RegExp(item.w, "ig"), item.r)
      })
      editor.setContent(html)
    },
    handleXyWordReplace2(cmd, words, replacement) {
      const editor = window.UE.getEditor(this.editorId)
      let html = editor.getContent();
      // 先去掉可能存在的高亮标签
      html = html.replace(new RegExp("<span class=\"warning_tip\">([^<]*)</span>", "ig"), '$1');
      words.forEach(word => {
        html = html.replace(new RegExp(word, "ig"), replacement)
      })
      editor.setContent(html)
    },
    // 插入地图
    addBaiduMapButton(editorId) {
      const that = this
      window.UE.registerUI('xy-baidu-map', function (editor, uiName) {
        editor.registerCommand(uiName, {
          execCommand: function(cmdName){
            editor.onBaiduMapButtonClick(cmdName);
          }
        });
        const ui = new UE.ui.Button({
          className: "edui-for-" + uiName,
          name: that.$t('CMS.UEditor.InsertBaiduMap'),
          title: that.$t('CMS.UEditor.InsertBaiduMap'),
          onclick: function() {
            editor.execCommand(uiName);
          }
        });
        return ui;
      });
    },
    handleBaiduMapButtonClick(cmd, value) {
      this.openBaiduMapDialog = true
    },
    handleBaiduMapDialogOk(result) {
      if (result) {
        var editor = window.UE.getEditor(this.editorId)
        editor.execCommand('inserthtml', '<img width="' + result.width + '" height="'+result.height+'" src="' + result.src + '"/>');
      }
    },
  },
};
</script>
<style>
.edui-for-xy-resource .edui-menubutton-body .edui-icon {
  background-image: url('./images/image.png') !important;
  /* background-size: contain; */
}
.edui-for-xy-third-video .edui-button-body .edui-icon {
  background-image: url('./images/video.png') !important;
}
.edui-for-xy-content .edui-menubutton-body .edui-icon {
  background-image: url('./images/content.png') !important;
}
.edui-for-xy-check-word .edui-menubutton-body .edui-icon {
  background-image: url('./images/word.png') !important;
}
.edui-for-xy-baidu-map .edui-button-body .edui-icon {
  background-image: url('./images/map.png') !important;
}
</style>
