import { useUserStore } from '@/store'

const NO_PERMISSION_REQUESTS = [
  '/api/app/add',
  '/api/app/update',
  '/api/app/delete',
  '/api/app_secret/add',
  '/api/app_secret/update',
  '/api/app_secret/delete',
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
  '/api/debug/compileScript',
  '/api/debug/debugScript',
  '/api/debug/debugURL',
  '/api/seed/add',
  '/api/seed/update',
  '/api/seed/delete',
  '/api/seed/deleteByPlan',
  '/api/seed/fetch',
  '/api/url/dispatch',
  '/api/url/delete',
  '/api/job/dispatch',
  '/api/job/delete',
  '/api/proxy/add',
  '/api/proxy/update',
  '/api/proxy/delete',
  '/api/proxy/check',
  '/api/runner/start',
  '/api/runner/stop',
  '/api/runner/popMessage',
  '/api/concurrency/updateDefaultConcurrency',
  '/api/concurrency/updateConcurrencyConnectionMap',
  '/api/concurrency/updateDefaultCrawlDelay',
  '/api/concurrency/updateCrawlDelayMap',
]

export const checkLogin = () => useUserStore().injected
export const checkPermission = requestURL => {
  if (!NO_PERMISSION_REQUESTS.includes(requestURL)) return true
  return checkLogin()
}