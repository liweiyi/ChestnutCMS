export default [
  {
    layout: 'colFormItem',
    tagIcon: 'input',
    label: '手机号',
    vModel: 'mobile',
    formId: 6,
    tag: 'el-input',
    placeholder: '请输入带国家/地区区号的手机号，例如+8613800138000',
    defaultValue: '',
    span: 24,
    style: { width: '100%' },
    clearable: true,
    prepend: '',
    append: '',
    'prefix-icon': 'Cellphone',
    'suffix-icon': '',
    maxlength: 16,
    'show-word-limit': true,
    readonly: false,
    disabled: false,
    required: true,
    changeTag: true,
    regList: [{
      pattern: '/^\\+[1-9][0-9]{1,14}$/',
      message: '手机号格式错误'
    }]
  }
]
