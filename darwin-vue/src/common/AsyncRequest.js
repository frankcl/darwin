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
export const asyncSearchJob = async request => await AxiosRequest.get('/api/job/search', {params: request})
export const asyncJobProgress = async id => await AxiosRequest.get('/api/job/progress', {params: {id: id}})
export const asyncBucketCountGroupByStatus = async id => await AxiosRequest.get('/api/job/bucketCountGroupByStatus', {params: {id: id}})
export const asyncGetPlanRules = async plan_id => await AxiosRequest.get('/api/rule/planRules', {params: {plan_id: plan_id}})
export const asyncGetRule = async id => await AxiosRequest.get('/api/rule/get', {params: {id: id}})
export const asyncAddRule = async rule => await AxiosRequest.put('/api/rule/add', rule)
export const asyncUpdateRule = async rule => await AxiosRequest.post('/api/rule/update', rule)
export const asyncRemoveRule = async id => await AxiosRequest.delete('/api/rule/delete', {params: {id: id}})
export const asyncGetHistory = async id => await AxiosRequest.get('/api/rule/history/get', {params: {id: id}})
export const asyncRemoveHistory = async id => await AxiosRequest.delete('/api/rule/history/delete', {params: {id: id}})
export const asyncSearchHistory = async request => await AxiosRequest.get('/api/rule/history/search', {params: request})
export const asyncRollbackRule = async request => await AxiosRequest.post('/api/rule/rollback', request)
export const asyncCompileScript = async request => await AxiosRequest.post('/api/script/compile', request)
export const asyncDebugScript = async request => await AxiosRequest.post('/api/script/debug', request)
export const asyncGetSeed = async key => await AxiosRequest.get('/api/seed/get', {params: {key: key}})
export const asyncSearchSeed = async request => await AxiosRequest.get('/api/seed/search', {params: request})
export const asyncAddSeed = async seed => await AxiosRequest.put('/api/seed/add', seed)
export const asyncUpdateSeed = async seed => await AxiosRequest.post('/api/seed/update', seed)
export const asyncRemoveSeed = async key => await AxiosRequest.delete('/api/seed/delete', {params: {key : key}})
export const asyncGetURL = async key => await AxiosRequest.get('/api/url/get', {params: {key: key}})
export const asyncPreviewURL = async key => await AxiosRequest.get('/api/url/preview', {params: {key: key}})
export const asyncSearchURL = async request => await AxiosRequest.get('/api/url/search', {params: request})
export const asyncGetExecutorList = async () => await AxiosRequest.get('/api/executor/getList')
export const asyncStartExecutor = async name => await AxiosRequest.get('/api/executor/start', {params: {name: name}})
export const asyncStopExecutor = async name => await AxiosRequest.get('/api/executor/stop', {params: {name: name}})
export const asyncGetProxy = async id => await AxiosRequest.get('/api/proxy/get', {params: {id: id}})
export const asyncCheckProxy = async id => await AxiosRequest.get('/api/proxy/check', {params: {id: id}})
export const asyncRemoveProxy = async id => await AxiosRequest.delete('/api/proxy/delete', {params: {id: id}})
export const asyncAddProxy = async proxy => await AxiosRequest.put('/api/proxy/add', proxy)
export const asyncUpdateProxy = async proxy => await AxiosRequest.post('/api/proxy/update', proxy)
export const asyncSearchProxy = async request => await AxiosRequest.get('/api/proxy/search', {params: request})

export const asyncResetUserApps = async () => {
  const userStore = useUserStore()
  if (!userStore.injected) return
  const apps = await asyncGetOwnApps()
  userStore.injectApps(apps)
}

export const newSearchQuery = searchQuery => {
  const rawSearchQuery = {
    current: 1,
    size: 10,
    sort_field: null,
    sort_order: null,
  }
  return searchQuery ? { ... rawSearchQuery, ... searchQuery } : rawSearchQuery
}

export const newSearchRequest = searchQuery => {
  const searchRequest = {
    current: searchQuery.current || 1,
    size: searchQuery.size || 10
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