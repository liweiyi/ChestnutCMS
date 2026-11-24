/*! UEditorPlus v4.5.0*/
(function () {
  var selectedTemplate;
  var preview = $G('preview');
  var previewItems = $G('preitem');

  function clearSelection() {
    for (var i = 0; i < previewItems.children.length; i++) {
      domUtils.setStyles(previewItems.children[i], {
        'background-color': '',
        border: 'white 1px solid'
      });
    }
  }

  function selectTemplate(index) {
    selectedTemplate = templates[index - 1];
    clearSelection();
    domUtils.setStyles(previewItems.children[index - 1], {
      'background-color': 'lemonChiffon',
      border: '#ccc 1px solid'
    });
    preview.innerHTML = selectedTemplate.preHtml || '';
  }

  function renderPreviews() {
    previewItems.innerHTML = '';
    templates.forEach(function (template, index) {
      var item = document.createElement('div');
      item.className = 'preitem';
      item.onclick = function () {
        selectTemplate(index + 1);
      };
      var image = document.createElement('img');
      image.src = 'images/' + template.pre;
      if (template.title) {
        image.alt = template.title;
        image.title = template.title;
      }
      item.appendChild(image);
      previewItems.appendChild(item);
    });
  }

  dialog.onok = function () {
    if (!$G('issave').checked) {
      editor.execCommand('cleardoc');
    }
    editor.execCommand('template', {
      html: selectedTemplate && selectedTemplate.html
    });
  };

  renderPreviews();
  window.pre = selectTemplate;
  selectTemplate(2);
})();
