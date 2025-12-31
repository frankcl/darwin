import { useUserStore } from '@/store'
import AxiosRequest from '@/common/AxiosRequest'

export const asyncLogout = async () => await AxiosRequest.get('/api/logout')
export const asyncCurrentUser = async () => await AxiosRequest.get('/api/user/currentUser')
export const asyncAllUsers = async () => await AxiosRequest.get('/api/user/allUsers')
export const asyncGetApp = async id => await AxiosRequest.get('/api/app/get', {params: {id: id}})
export const asyncSearchApp = async request => await AxiosRequest.get('/api/app/search', {params: request})
export const asyncRemoveApp = async id => await AxiosRequest.delete('/api/app/delete', {params: {id: id}})
export const asyncAddApp = async app => await AxiosRequest.put('/api/app/add', app)
export const asyncUpdateApp = async app => await AxiosRequest.post('/api/app/update', app)
export const asyncGetAppSecret = async id => await AxiosRequest.get('/api/app_secret/get', {params: {id: id}})
export const asyncSearchAppSecret = async request => await AxiosRequest.get('/api/app_secret/search', {params: request})
export const asyncRemoveAppSecret = async id => await AxiosRequest.delete('/api/app_secret/delete', {params: {id: id}})
export const asyncAddAppSecret = async app_secret => await AxiosRequest.put('/api/app_secret/add', app_secret)
export const asyncUpdateAppSecret = async app_secret => await AxiosRequest.post('/api/app_secret/update', app_secret)
export const asyncRandomAccessKey = async () => await AxiosRequest.get('/api/app_secret/randomAccessKey')
export const asyncRandomSecretKey = async () => await AxiosRequest.get('/api/app_secret/randomSecretKey')
export const asyncGetAppUsers = async app_id => await AxiosRequest.get('/api/app_user/getAppUsers', {params: {app_id: app_id}})
export const asyncGetOwnApps = async () => await AxiosRequest.get('/api/app_user/getOwnApps')
export const asyncBatchUpdateAppUser = async request => await AxiosRequest.post('/api/app_user/batchUpdateAppUser', request)
export const asyncGetPlan = async id => await AxiosRequest.get('/api/plan/get', {params: {id: id}})
export const asyncSearchPlan = async request => await AxiosRequest.get('/api/plan/search', {params: request})
export const asyncAddPlan = async plan => await AxiosRequest.put('/api/plan/add', plan)
export const asyncUpdatePlan = async plan => await AxiosRequest.post('/api/plan/update', plan)
export const asyncRemovePlan = async id => await AxiosRequest.delete('/api/plan/delete', {params: {id: id}})
export const asyncOpenPlan = async id => await AxiosRequest.get('/api/plan/open', {params: {id: id}})
export const asyncClosePlan = async id => await AxiosRequest.get('/api/plan/close', {params: {id: id} })
export const asyncExecutePlan = async id => await AxiosRequest.get('/api/plan/execute', {params: {id: id}})
export const asyncGetJob = async id => await AxiosRequest.get('/api/job/get', {params: {id: id}})
export const asyncRemoveJob = async id => await AxiosRequest.delete('/api/job/delete', {params: {id: id}})
export const asyncSearchJob = async request => await AxiosRequest.get('/api/job/search', {params: request})
export const asyncJobProgress = async id => await AxiosRequest.get('/api/job/progress', {params: {id: id}})
export const asyncJobSuccessRate = async id => await AxiosRequest.get('/api/job/successRate', {params: {id: id}})
export const asyncDispatchJob = async id => await AxiosRequest.get('/api/job/dispatch', {params: {id: id}})
export const asyncGetPlanRules = async plan_id => await AxiosRequest.get('/api/rule/planRules', {params: {plan_id: plan_id}})
export const asyncGetRule = async id => await AxiosRequest.get('/api/rule/get', {params: {id: id}})
export const asyncGetTemplate = async type => await AxiosRequest.get('/api/rule/getTemplate', {params: {type: type}})
export const asyncAddRule = async rule => await AxiosRequest.put('/api/rule/add', rule)
export const asyncUpdateRule = async rule => await AxiosRequest.post('/api/rule/update', rule)
export const asyncRemoveRule = async id => await AxiosRequest.delete('/api/rule/delete', {params: {id: id}})
export const asyncGetHistory = async id => await AxiosRequest.get('/api/rule/history/get', {params: {id: id}})
export const asyncRemoveHistory = async id => await AxiosRequest.delete('/api/rule/history/delete', {params: {id: id}})
export const asyncSearchHistory = async request => await AxiosRequest.get('/api/rule/history/search', {params: request})
export const asyncRollbackRule = async request => await AxiosRequest.post('/api/rule/rollback', request)
export const asyncCompileScript = async request => await AxiosRequest.post('/api/debug/compileScript', request)
export const asyncDebugScript = async request => await AxiosRequest.post('/api/debug/debugScript', request, {timeout: 30000})
export const asyncDebugURL = async request => await AxiosRequest.get('/api/debug/debugURL', {params: request, timeout: 30000})
export const asyncGetSeed = async key => await AxiosRequest.get('/api/seed/get', {params: {key: key}})
export const asyncSearchSeed = async request => await AxiosRequest.get('/api/seed/search', {params: request})
export const asyncAddSeed = async seed => await AxiosRequest.put('/api/seed/add', seed)
export const asyncUpdateSeed = async seed => await AxiosRequest.post('/api/seed/update', seed)
export const asyncRemoveSeed = async key => await AxiosRequest.delete('/api/seed/delete', {params: {key : key}})
export const asyncRemovePlanSeeds = async plan_id => await AxiosRequest.delete('/api/seed/deleteByPlan', {params: {plan_id : plan_id}})
export const asyncGetURL = async key => await AxiosRequest.get('/api/url/get', {params: {key: key}})
export const asyncRemoveURL = async key => await AxiosRequest.delete('/api/url/delete', {params: {key: key}})
export const asyncGetLineageNode = async key => await AxiosRequest.get('/api/url/getLineageNode', {params: {key: key}})
export const asyncGetLineageChildren = async parent_key => await AxiosRequest.get('/api/url/getLineageChildren', {params: {parent_key: parent_key}})
export const asyncPreview = async key => await AxiosRequest.get('/api/url/preview', {params: {key: key}})
export const asyncSearchURL = async request => await AxiosRequest.get('/api/url/search', {params: request})
export const asyncDispatchURL = async key => await AxiosRequest.get('/api/url/dispatch', {params: {key: key}})
export const asyncGetRunners = async type => await AxiosRequest.get('/api/runner/getList', {params: {type: type}})
export const asyncRunnerRunning = async key => await AxiosRequest.get('/api/runner/isRunning', {params: {key: key}})
export const asyncRunnerMessageCount = async key => await AxiosRequest.get('/api/runner/messageCount', {params: {key: key}})
export const asyncStartRunner = async key => await AxiosRequest.get('/api/runner/start', {params: {key: key}})
export const asyncStopRunner = async key => await AxiosRequest.get('/api/runner/stop', {params: {key: key}})
export const asyncPopMessage = async key => await AxiosRequest.get('/api/runner/popMessage', {params: {key: key}})
export const asyncGetProxy = async id => await AxiosRequest.get('/api/proxy/get', {params: {id: id}})
export const asyncCheckProxy = async id => await AxiosRequest.get('/api/proxy/check', {params: {id: id}})
export const asyncRemoveProxy = async id => await AxiosRequest.delete('/api/proxy/delete', {params: {id: id}})
export const asyncAddProxy = async proxy => await AxiosRequest.put('/api/proxy/add', proxy)
export const asyncUpdateProxy = async proxy => await AxiosRequest.post('/api/proxy/update', proxy)
export const asyncSearchProxy = async request => await AxiosRequest.get('/api/proxy/search', {params: request})
export const asyncGetConcurrencyUnit = async name => await AxiosRequest.get('/api/concurrency/getConcurrencyUnit', {params: {name: name}})
export const asyncConcurrencyQueueWait = async name => await AxiosRequest.get('/api/concurrency/getConcurrencyQueueWait', {params: {name: name}})
export const asyncWaitConcurrencyUnits = async n => await AxiosRequest.get('/api/concurrency/waitConcurrencyUnits', {params: {n: n}})
export const asyncDefaultConcurrency = async () => await AxiosRequest.get('/api/concurrency/getDefaultConcurrency')
export const asyncConcurrencyConnectionMap = async () => await AxiosRequest.get('/api/concurrency/getConcurrencyConnectionMap')
export const asyncUpdateDefaultConcurrency = async request => await AxiosRequest.post('/api/concurrency/updateDefaultConcurrency', request)
export const asyncUpdateConcurrencyConnectionMap = async request => await AxiosRequest.post('/api/concurrency/updateConcurrencyConnectionMap', request)
export const asyncDefaultCrawlDelay = async () => await AxiosRequest.get('/api/concurrency/getDefaultCrawlDelay')
export const asyncCrawlDelayMap = async () => await AxiosRequest.get('/api/concurrency/getCrawlDelayMap')
export const asyncUpdateDefaultCrawlDelay = async request => await AxiosRequest.post('/api/concurrency/updateDefaultCrawlDelay', request)
export const asyncUpdateCrawlDelayMap = async request => await AxiosRequest.post('/api/concurrency/updateCrawlDelayMap', request)
export const asyncFetchCountTrend = async () => await AxiosRequest.get('/api/dashboard/fetchCountTrend')
export const asyncDownAnalysis = async content_type => await AxiosRequest.get('/api/dashboard/getDownAnalysis', {params: {content_type: content_type}})
export const asyncFetchAnalysis = async content_type => await AxiosRequest.get('/api/dashboard/getFetchAnalysis', {params: {content_type: content_type}})
export const asyncStatusGroupCount = async request => await AxiosRequest.get('/api/dashboard/statusGroupCount', {params: request})
export const asyncContentGroupCount = async request => await AxiosRequest.get('/api/dashboard/contentGroupCount', {params: request})
export const asyncQueueWaitPriority = async () => await AxiosRequest.get('/api/dashboard/queueWaitPriority')
export const asyncHostFetchCount = async () => await AxiosRequest.get('/api/dashboard/hostFetchCount')
export const asyncGetQueueMemory = async () => await AxiosRequest.get('/api/dashboard/getQueueMemory')
export const asyncGetQueueWait = async () => await AxiosRequest.get('/api/dashboard/getQueueWait', { timeout: 30000 })

export const asyncResetUserApps = async () => {
  const userStore = useUserStore()
  if (!userStore.injected) return
  const apps = await asyncGetOwnApps()
  userStore.injectApps(apps)
}

export const newSearchQuery = searchQuery => {
  const rawSearchQuery = {
    page_num: 1,
    page_size: 10,
    sort_field: null,
    sort_order: null,
  }
  return searchQuery ? { ... rawSearchQuery, ... searchQuery } : rawSearchQuery
}

export const newSearchRequest = searchQuery => {
  const searchRequest = {
    page_num: searchQuery.page_num || 1,
    page_size: searchQuery.page_size || 10
  }
  if (searchQuery.sort_field && searchQuery.sort_order) {
    searchRequest.order_by = JSON.stringify([{
      field: searchQuery.sort_field,
      asc: searchQuery.sort_order === 'ascending' }])
  }
  searchQueryTimeToRequestTime(searchQuery, searchRequest, 'create_time')
  searchQueryTimeToRequestTime(searchQuery, searchRequest, 'fetch_time')
  return searchRequest
}

export const changeSearchQuerySort = (field, order, searchQuery) => {
  if (field) {
    searchQuery.sort_field = field
    searchQuery.sort_order = order
  }
}

const searchQueryTimeToRequestTime = (searchQuery, searchRequest, key) => {
  if (searchQuery[key] && searchQuery[key].length === 2) {
    searchRequest[key] = JSON.stringify({
      include_lower: true,
      include_upper: true,
      start: searchQuery[key][0].getTime(),
      end: searchQuery[key][1].getTime()
    })
  }
}