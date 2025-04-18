const isNull = o => o === undefined || o === null

export const seedFormRules = {
  url: [
    { required: true, message: '请输入种子URL', trigger: 'change' }
  ],
  category: [
    { required: true, message: '请选择种子类型', trigger: 'change' }
  ]
}

export const fillSeedMapField = (seed, mapKey, options) => {
  seed[mapKey] = {}
  options.forEach(option => {
    if (!isNull(option.key) && !isNull(option.value)) {
      seed[mapKey][option.key] = option.value
    }
  })
}