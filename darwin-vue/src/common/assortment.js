import Cookies from 'js-cookie'
import { ElMessageBox, ElNotification } from 'element-plus'
import { useUserStore } from '@/store'
import {
  asyncCurrentUser,
  asyncLogout
} from './service'

export const isUndefinedOrNull = o => o === undefined || o === null
export const sleep = ms => new Promise(resolve => setTimeout(resolve, ms))

export const isJsonStr = str => {
  if (typeof str === 'string') {
    try {
      const obj = JSON.parse(str)
      return typeof obj === 'object' && obj
    } catch (e) {
      console.log('error: not json str[' + str + ']!' + e)
      return false
    }
  }
}

export const isLogin = () => Cookies.get('TOKEN') !== undefined

export const logout = async () => {
  await asyncLogout()
  useUserStore().clear()
}

export const executeAsyncRequest = async (asyncExecuteFunc, params,
  successHandle, failHandle, exceptionHandle, formElement) => {
  if (formElement && !await formElement.validate(valid => valid)) return false
  try {
    const success = await asyncExecuteFunc(params)
    if (success && successHandle) successHandle()
    if (!success && failHandle) failHandle()
    return success
  } catch (e) {
    if (exceptionHandle) exceptionHandle()
    throw e
  }
}

export const executeAsyncRequestAfterConfirm = (title, tips, asyncExecuteFunc, params,
  successHandle, failHandle, exceptionHandle, formElement) => {
  return ElMessageBox.confirm(tips, title, { confirmButtonText: '确认', cancelButtonText: '取消'}).
    then(async () => {
      return await executeAsyncRequest(
        asyncExecuteFunc, params, successHandle, failHandle, exceptionHandle, formElement)
    }).catch(() => {})
}

export const fillSearchQuerySort = (event, query) => {
  if (!event || !event.prop) return
  query.sort_field = event.prop
  query.sort_order = event.order
}

export const searchQueryToRequest = query => {
  const request = {
    current: query.current || 1,
    size: query.size || 20
  }
  if (query.sort_field && query.sort_order) {
    request.order_by = JSON.stringify([{ field: query.sort_field, asc: query.sort_order === 'ascending' }])
  }
  return request
}

export const refreshUser = async (force = false) => {
  if (isLogin()) {
    const userStore = useUserStore()
    if (!force && userStore.injected) return
    userStore.inject(await asyncCurrentUser())
  }
}

export const checkUserLogin = () => {
  const userStore = useUserStore()
  if (!userStore.injected) ElNotification.error('尚未登录')
  return userStore.injected
}