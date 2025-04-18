import { format } from 'date-fns'

export const pause = ms => new Promise(resolve => setTimeout(resolve, ms))
export const formatDate = (input, dateFormat='yyyy-MM-dd HH:mm:ss') => {
  if (input === undefined) return '暂无'
  if (typeof input === 'number') return format(new Date(input), dateFormat)
  if (input instanceof Date) return format(input, dateFormat)
  throw new Error('unsupported input for formating date')
}