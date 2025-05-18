export const normalizeTime = waitTime => {
  waitTime = waitTime / 1000
  const units = ['秒', '分钟', '小时']
  for (let i = 0, factor = 60; i < units.length; i++) {
    if (waitTime < factor) return `${waitTime.toFixed(2)}${units[i]}`
    if (i === units.length - 1) break
    waitTime /= factor
  }
  return `${waitTime.toFixed(2)}小时`
}