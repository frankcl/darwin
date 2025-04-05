import { isUndefinedOrNull } from '@/common/assortment'

export const seedFormRules = {
  url: [
    { required: true, message: '请输入种子URL', trigger: 'change' }
  ],
  category: [
    { required: true, message: '请选择种子URL类型', trigger: 'change' }
  ]
}

export const transferFieldArray = (fieldArray, form, key) => {
  if (fieldArray.length === 0) return
  form[key] = {}
  fieldArray.forEach(field => {
    if (!isUndefinedOrNull(field.key) && !isUndefinedOrNull(field.value)) form[key][field.key] = field.value
  })
}