/*! UEditorPlus v4.5.0*/
(function () {
  var parentWindow = window.parent
  window.dialog = parentWindow.$EDITORUI[window.frameElement.id.replace(/_iframe$/, '')]
  window.editor = dialog.editor
  window.UE = parentWindow.UE
  window.domUtils = UE.dom.domUtils
  window.utils = UE.utils
  window.browser = UE.browser
  window.ajax = UE.ajax
  window.$G = function (id) { return document.getElementById(id) }
  window.$focus = function (element) {
    setTimeout(function () {
      if (browser.ie) {
        var range = element.createTextRange()
        range.collapse(false)
        range.select()
      } else {
        element.focus()
      }
    }, 0)
  }

  utils.loadFile(document, {
    href: editor.options.themePath + editor.options.theme + '/dialogbase.css?cache=v20250415',
    tag: 'link', type: 'text/css', rel: 'stylesheet'
  })
  window.lang = editor.getLang(dialog.className.split('-')[2])
  document.documentElement.lang = editor.options.lang
  if (!lang) return

  domUtils.on(window, 'load', function () {
    var imagePath = editor.options.langImagePath || editor.options.langPath + editor.options.lang + '/images/'
    var labels = lang.static || {}
    for (var id in labels) {
      var element = $G(id)
      if (!element) continue
      var value = labels[id]
      // 克隆属性配置，避免同一语言再次打开弹窗时重复拼接图片路径。
      if (value.src || value.style) {
        value = utils.extend({}, value, false)
        if (value.src) value.src = imagePath + value.src
        if (value.style) value.style = value.style.replace(/url\s*\(/g, 'url(' + imagePath)
      }
      switch (element.tagName.toLowerCase()) {
        case 'var':
          element.parentNode.replaceChild(document.createTextNode(value), element)
          break
        case 'select':
          for (var index = 0; index < element.options.length; index++) {
            element.options[index].textContent = value.options[index]
          }
          for (var attribute in value) {
            if (attribute !== 'options') element.setAttribute(attribute, value[attribute])
          }
          break
        default:
          domUtils.setAttributes(element, value)
      }
    }
  })
})()
