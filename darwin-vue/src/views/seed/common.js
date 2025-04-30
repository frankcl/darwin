export const seedFormRules = {
  url: [
    { required: true, message: '请输入种子URL', trigger: 'change' }
  ],
  category: [
    { required: true, message: '请选择种子类型', trigger: 'change' }
  ]
}

export const fillMap = (seed, mapKey, options) => {
  seed[mapKey] = {}
  options.forEach(option => {
    if (Array.isArray(option) && option.length === 2) {
      seed[mapKey][option[0]] = option[1]
    }
  })
}