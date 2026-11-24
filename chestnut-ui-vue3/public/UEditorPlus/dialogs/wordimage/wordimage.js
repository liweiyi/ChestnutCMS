/*! UEditorPlus v4.5.0*/
function formatWordImageMessage(message, values) {
  return message.replace(/\{(\w+)\}/g, function(match, key) {
    return Object.prototype.hasOwnProperty.call(values, key) ? values[key] : match
  })
}

function showWordImageTip(message, color) {
  $(".image-tip").empty().append($("<span>").css("color", color || "").text(message))
}

function addUploadButtonListener() {
  g("saveFile").addEventListener("change", function() {
    showWordImageTip(lang.transferring), uploader.addFile(this.files), uploader.upload()
  })
}

function addOkListener() {
  dialog.onok = function() {
    if (imageUrls.length) {
      var a = editor.getOpt("imageUrlPrefix"),
        b = domUtils.getElementsByTagName(editor.document, "img");
      editor.fireEvent("saveScene");
      for (var c, d = 0; c = b[d++];) {
        var e = c.getAttribute("data-word-image");
        if (e)
          for (var f, g = 0; f = imageUrls[g++];)
            if (e.indexOf(f.name.replace(" ", "")) != -1) {
              c.src = a + f.url, c.setAttribute("_src", a + f.url), c.setAttribute("title", f.title), domUtils.removeAttributes(c, ["data-word-image", "style", "width", "height"]), editor.fireEvent("selectionchange");
              break
            }
      }
      editor.fireEvent("saveScene")
    }
  }, dialog.oncancel = function() {}
}

function showLocalPath(a) {
  var b = editor.selection.getRange().getClosedNode(),
    c = editor.execCommand("wordimage");
  if (1 == c.length || b && "IMG" == b.tagName) return void(g(a).value = c[0]);
  var d = c[0],
    e = d.lastIndexOf("/") || 0,
    f = d.lastIndexOf("\\") || 0,
    h = e > f ? "/" : "\\";
  d = d.substring(0, d.lastIndexOf(h) + 1), g(a).value = d;
  for (var i = [], j = 0, k = c.length; j < k; j++) {
    var b = c[j];
    i.push(b.substring(b.lastIndexOf(h) + 1, b.length))
  }
  showWordImageTip(formatWordImageMessage(lang.selectFiles, { names: i.join(", "), count: c.length }), '#ff0000')
}

function createCopyButton(a, b) {
  var c = g(b).value;
  c.startsWith("file:////") && (c = c.substring(8)), c = decodeURI(c), g(a).setAttribute("data-clipboard-text", c);
  var d = new Clipboard("[data-clipboard-text]");
  d.on("success", function(a) {
    g("copyButton").textContent = lang.copyPathSuccess
  })
}
var wordImage = {},
  g = $G,
  flashObj, flashContainer;
wordImage.init = function(a, b) {
  showLocalPath("fileUrl"), createCopyButton("copyButton", "fileUrl"), addUploadButtonListener(), addOkListener()
};
