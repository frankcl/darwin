export const ruleFormRules = {
  name: [
    { required: true, message: '请输入规则名称', trigger: 'change' }
  ],
  regex: [
    { required: true, message: '请输入匹配规则', trigger: 'change' }
  ],
  script_type: [
    { required: true, message: '请选择脚本类型', trigger: 'change' }
  ],
  script: [
    { required: true, message: '请输入脚本代码', trigger: 'change' }
  ],
  change_log: [
    { required: true, message: '请输入变更原因', trigger: 'change' }
  ]
}

export const debugFormRules = {
  url: [
    { required: true, message: '请输入调试URL', trigger: 'change'}
  ]
}

export const langMap = { 1: 'Groovy', 2: 'JavaScript' }