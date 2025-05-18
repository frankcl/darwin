export const normalizeTime = (time, timeUnit) => {
  const units = ['毫秒', '秒', '分钟', '小时']
  for (let i = 0, factor = 1000; i < units.length; i++) {
    timeUnit.value = units[i]
    if (i > 0) factor = 60
    if (time < factor) return parseFloat(time.toFixed(2))
    if (i === units.length - 1) break
    time /= factor
  }
  timeUnit.value = '小时'
  return parseFloat(time.toFixed(2))
}

export const normalizeStorage = (storage, storageUnit) => {
  const units = ['B', 'KB', 'MB', 'GB']
  for (let i = 0, factor = 1024; i < units.length; i++) {
    storageUnit.value = units[i]
    if (storage < factor) return parseFloat(storage.toFixed(2))
    storage /= factor
  }
  storageUnit.value = 'TB'
  return parseFloat(storage.toFixed(2))
}

export const queryTimeRange = () => {
  return {
    start: Date.now() - 86400000,
    include_lower: true
  }
}