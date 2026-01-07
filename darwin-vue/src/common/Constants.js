export const statusMap = {
  0: '抓取成功',
  1: '抓取失败',
  2: '排队中',
  3: '抓取中',
  4: '抓取超时',
  5: '过期',
  6: '错误',
  7: '溢出',
  8: '排队超时',
  9: '解析错误'
}

export const priorityMap = {
  0: '高',
  1: '中',
  2: '低',
}

export const fetchMethodMap = {
  0: '本地IP',
  1: '长效代理',
  2: '短效代理',
}

export const contentTypeMap = {
  1: '网页',
  2: '图片',
  3: '视频',
  4: '音频',
  5: '其他',
}

export const planCategoryMap = {
  0: '单次型',
  1: '周期型',
}

export const concurrentLevelMap = {
  0: 'DOMAIN',
  1: 'HOST'
}

export const linkScopeMap = {
  1: '全部',
  2: 'DOMAIN',
  3: 'HOST'
}

export const scriptLangMap = {
  1: 'Groovy',
  2: 'JavaScript'
}

export const httpRequestMap = {
  GET: 'GET',
  POST: 'POST'
}

export const postMediaTypeMap = {
  JSON: 'JSON',
  FORM: 'FORM'
}