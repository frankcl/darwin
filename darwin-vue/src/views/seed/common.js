import { ERROR, showMessage } from '@/common/Feedback'

export const seedFormRules = {
  url: [
    { required: true, message: '请输入种子URL', trigger: 'change' }
  ],
  category: [
    { required: true, message: '请选择种子类型', trigger: 'change' }
  ]
}

export const fieldTypes = ['number', 'string', 'boolean', 'object']

export const fillMap = (seed, mapKey, options) => {
  seed[mapKey] = {}
  options.forEach(option => {
    if (Array.isArray(option) && option.length === 2) {
      seed[mapKey][option[0]] = option[1]
    }
  })
}

const isNumber = str => {
  if (str.trim() === '') return false
  return !isNaN(Number(str))
}

export const isObject = obj => {
  return typeof obj === 'object' && obj !== null
}

const parseJSON = value => {
  try {
    return JSON.parse(value)
  } catch {
    return null
  }
}

export const fillRequestBody = (seed, options) => {
  const mapKey = 'request_body'
  seed[mapKey] = {}
  for (const option of options) {
    if (Array.isArray(option) && option.length === 3) {
      if (option[2] === 'boolean') {
        const v = option[1].toLowerCase()
        if (v !== 'true' && v !== 'false') {
          showMessage(`请求体非法布尔值：${option[0]}`, ERROR)
          return false
        }
        seed[mapKey][option[0]] = Boolean(v)
      } else if (option[2] === 'number') {
        if (!isNumber(option[1])) {
          showMessage(`请求体非法数值：${option[0]}`, ERROR)
          return false
        }
        seed[mapKey][option[0]] = Number(option[1])
      } else if (option[2] === 'object') {
        seed[mapKey][option[0]] = parseJSON(option[1])
        if (!isObject(seed[mapKey][option[0]])) {
          showMessage(`请求体非法对象：${option[0]}`, ERROR)
          return false
        }
      }
      else seed[mapKey][option[0]] = option[1]
    }
  }
  return true
}