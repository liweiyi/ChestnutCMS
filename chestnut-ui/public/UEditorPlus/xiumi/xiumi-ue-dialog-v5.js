UE.registerUI('xiumi-dialog', function (editor, uiName) {
  const btn = new UE.ui.Button({
    name: 'xiumi-connect',
    title: '秀米',
    onclick() {
      const dialog = new UE.ui.Dialog({
        iframeUrl: editor.getOpt('UEDITOR_HOME_URL') + 'xiumi/xiumi-ue-dialog-v5.html', // 注意这个路径要指向 xiumi-ue-dialog-v5.html
        editor,
        name: 'xiumi-connect',
        title: '秀米图文消息助手',
        cssRules: 'width: ' + (window.innerWidth - 60) + 'px; height: ' + (window.innerHeight - 60) + 'px;',
      });
      dialog.render();
      dialog.open();
    },
  });

  return btn;
});