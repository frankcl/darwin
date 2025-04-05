import axios from '@/common/axios-plus'

const HTTP_GET = 'get'
const HTTP_PUT = 'put'
const HTTP_POST = 'post'
const HTTP_DELETE = 'delete'

export const asyncLogout = async () => {
  await axios({
    method: HTTP_GET,
    url: '/api/logout'
  })
}

export const asyncCurrentUser = async () => {
  return await axios({
    method: HTTP_GET,
    url: '/api/user/getCurrentUser'
  })
}

export const asyncGetAllUsers = async () => {
  return await axios({
    method: HTTP_GET,
    url: '/api/user/getAllUsers'
  })
}

export const asyncSearchApps = async request => {
  return await axios({
    method: HTTP_GET,
    url: '/api/app/search',
    params: request
  })
}

export const asyncGetApp = async id => {
  return await axios({
    method: HTTP_GET,
    url: '/api/app/get',
    params: {
      id: id
    }
  })
}

export const asyncDeleteApp = async id => {
  return await axios({
    method: HTTP_DELETE,
    url: '/api/app/delete',
    params: {
      id: id
    }
  })
}

export const asyncAddApp = async app => {
  return await axios({
    method: HTTP_PUT,
    url: '/api/app/add',
    data: app
  })
}

export const asyncUpdateApp = async app => {
  return await axios({
    method: HTTP_POST,
    url: '/api/app/update',
    data: app
  })
}

export const asyncGetAppUsers = async appId => {
  return await axios({
    method: HTTP_GET,
    url: '/api/app_user/getAppUsers',
    params: {
      app_id: appId
    }
  })
}

export const asyncGetOwnApps = async () => {
  return await axios({
    method: HTTP_GET,
    url: '/api/app_user/getOwnApps'
  })
}

export const asyncBatchUpdateAppUser = async request => {
  return await axios({
    method: HTTP_POST,
    url: '/api/app_user/batchUpdateAppUser',
    data: request
  })
}

export const asyncGetPlan = async id => {
  return await axios({
    method: HTTP_GET,
    url: '/api/plan/get',
    params: {
      id: id
    }
  })
}

export const asyncAddPlan = async plan => {
  return await axios({
    method: HTTP_PUT,
    url: '/api/plan/add',
    data: plan
  })
}

export const asyncUpdatePlan = async plan => {
  return await axios({
    method: HTTP_POST,
    url: '/api/plan/update',
    data: plan
  })
}

export const asyncDeletePlan = async id => {
  return await axios({
    method: HTTP_DELETE,
    url: '/api/plan/delete',
    params: {
      id: id
    }
  })
}

export const asyncOpenPlan = async id => {
  return await axios({
    method: HTTP_GET,
    url: '/api/plan/open',
    params: {
      id: id
    }
  })
}

export const asyncClosePlan = async id => {
  return await axios({
    method: HTTP_GET,
    url: '/api/plan/close',
    params: {
      id: id
    }
  })
}

export const asyncExecutePlan = async id => {
  return await axios({
    method: HTTP_GET,
    url: '/api/plan/execute',
    params: {
      id: id
    }
  })
}

export const asyncSearchPlans = async request => {
  return await axios({
    method: HTTP_GET,
    url: '/api/plan/search',
    params: request
  })
}

export const asyncGetPlanRules = async planId => {
  return await axios({
    method: HTTP_GET,
    url: '/api/rule/getPlanRules',
    params: {
      plan_id: planId
    }
  })
}

export const asyncGetRule = async id => {
  return await axios({
    method: HTTP_GET,
    url: '/api/rule/get',
    params: {
      id: id
    }
  })
}

export const asyncUpdateRule = async rule => {
  return await axios({
    method: HTTP_POST,
    url: '/api/rule/update',
    data: rule
  })
}

export const asyncGetSeed = async key => {
  return await axios({
    method: HTTP_GET,
    url: '/api/seed/get',
    params: {
      key: key
    }
  })
}

export const asyncAddSeed = async seed => {
  return await axios({
    method: HTTP_PUT,
    url: '/api/seed/add',
    data: seed
  })
}

export const asyncUpdateSeed = async seed => {
  return await axios({
    method: HTTP_POST,
    url: '/api/seed/update',
    data: seed
  })
}

export const asyncDeleteSeed = async key => {
  return await axios({
    method: HTTP_DELETE,
    url: '/api/seed/delete',
    params: {
      key : key
    }
  })
}

export const asyncSearchSeeds = async request => {
  return await axios({
    method: HTTP_GET,
    url: '/api/seed/search',
    params: request
  })
}