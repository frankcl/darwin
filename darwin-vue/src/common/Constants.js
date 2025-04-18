export const statusMap = {
  0: '成功',
  1: '创建完成',
  2: '排队失败',
  3: '排队中',
  4: '抓取中',
  5: '状态非法',
  6: '抓取超时',
  7: '抓取失败',
  8: '溢出'
}

export const priorityMap = {
  0: '高优先级',
  1: '中优先级',
  2: '低优先级',
}

export const fetchMethodMap = {
  0: '本地IP',
  1: '代理IP'
}

export const categoryMap = {
  1: '内容页',
  2: '列表页',
  3: '图片视频',
  4: '视频流'
}

export const concurrentLevelMap = {
  0: 'DOMAIN',
  1: 'HOST'
}

export const executorStatusMap = {
  0: '停止',
  1: '运行',
  2: '错误'
}

export const proxyCategoryMap = {
  1: '长期代理',
  2: '短期代理'
}

export const previewTitleMap = {
  'html': '网页预览',
  'json': 'JSON预览',
  'image': '图片预览',
  'video': '视频预览',
  'pdf': '文档预览'
}