import { useUserStore } from '@/store'

const NO_PERMISSION_REQUESTS = [
  '/api/app/add',
  '/api/app/update',
  '/api/app/delete',
  '/api/app_user/getOwnApps',
  '/api/app_user/batchUpdateAppUser',
  '/api/plan/add',
  '/api/plan/update',
  '/api/plan/delete',
  '/api/plan/open',
  '/api/plan/close',
  '/api/plan/execute',
  '/api/rule/add',
  '/api/rule/update',
  '/api/rule/delete',
  '/api/rule/deleteHistory',
  '/api/rule/rollback',
  '/api/seed/add',
  '/api/seed/update',
  '/api/seed/delete',
  '/api/proxy/add',
  '/api/proxy/update',
  '/api/proxy/delete',
  '/api/proxy/check',
  '/api/runner/start',
  '/api/runner/stop',
  '/api/runner/popMessage',
  '/api/concurrency/updateDefaultMaxConcurrency',
  '/api/concurrency/updateConcurrencyConnectionMap',
]

export const checkLogin = () => useUserStore().injected
export const checkPermission = requestURL => {
  if (!NO_PERMISSION_REQUESTS.includes(requestURL)) return true
  return checkLogin()
}