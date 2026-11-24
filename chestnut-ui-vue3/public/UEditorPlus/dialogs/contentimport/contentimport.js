/*! UEditorPlus v4.5.0*/
function upload(a) {
  var b = new XMLHttpRequest,
    c = new FormData,
    d = editor.getOpt("imageUrlPrefix"),
    e = editor.getActionUrl(editor.getOpt("imageActionName")),
    f = "png";
  if (a.type && (f = a.type.substr("image/".length)), c.append("upfile", a, a.name || "blob." + f), c.append("type", "ajax"), b.open("post", e, !1), b.setRequestHeader("X-Requested-With", "XMLHttpRequest"), b.send(c), 200 == b.status) {
    var g = new Function("return " + b.responseText)();
    return "SUCCESS" == g.state && g.url ? d + g.url : a
  }
}

function processWord(a) {
  $(".file-tip").text(lang.convertingWord), $(".file-result").html("").hide();
  var b = new FileReader;
  b.onload = function(a) {
    mammoth.convertToHtml({
      arrayBuffer: a.target.result
    }, {
      convertImage: mammoth.images.imgElement(function(a) {
        return a.readAsArrayBuffer().then(function(b) {
          const c = new Blob([b], {
              type: a.contentType
            }),
            d = upload(c);
          return {
            src: d,
            alt: lang.documentImage
          }
        })
      })
    }).then(function(a) {
      $(".file-tip").text(lang.converted), contentImport.data.result = a.value, $(".file-result").html(a.value).show()
    }, function(a) {
      $(".file-tip").text(lang.wordFailed + a)
    })
  }, b.onerror = function(a) {
    $(".file-tip").text(lang.wordFailed + a)
  }, b.readAsArrayBuffer(a)
}

function processMarkdown(a) {
  var b = new showdown.Converter,
    c = b.makeHtml(a);
  $(".file-tip").text(lang.converted), contentImport.data.result = c, $(".file-result").html(c).show()
}

function processMarkdownFile(a) {
  $(".file-tip").text(lang.convertingMarkdown), $(".file-result").html("").hide();
  var b = new FileReader;
  b.onload = function(a) {
    processMarkdown(a.target.result)
  }, b.onerror = function(a) {
    $(".file-tip").text(lang.markdownFailed + a)
  }, b.readAsText(a, "UTF-8")
}

function addUploadButtonListener() {
  g("contentImport").addEventListener("change", function() {
    const a = this.files[0],
      b = a.name,
      c = b.substring(b.lastIndexOf(".") + 1).toLowerCase();
    switch (c) {
      case "docx":
      case "doc":
        processWord(a);
        break;
      case "md":
        processMarkdownFile(a);
        break;
      default:
        $(".file-tip").text(lang.unsupportedFormat + c)
    }
  }), g("fileInputConfirm").addEventListener("click", function() {
    processMarkdown(g("fileInputContent").value), $(".file-input").hide()
  })
}

function addOkListener() {
  dialog.onok = function() {
    return contentImport.data.result ? (editor.fireEvent("saveScene"), editor.execCommand("inserthtml", contentImport.data.result), void editor.fireEvent("saveScene")) : (alert(lang.selectFileFirst), !1)
  }, dialog.oncancel = function() {}
}
var contentImport = {},
  g = $G;
contentImport.data = {
  result: null
}, contentImport.init = function(a, b) {
  addUploadButtonListener(), addOkListener()
};
