import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/views/Home'
import AppList from '@/views/app/AppList'
import PlanList from '@/views/plan/PlanList'
import PlanTabs from '@/views/plan/PlanTabs'
import RecordList from '@/views/record/RecordList'
import ProxyList from '@/views/proxy/ProxyList'
import ConcurrencyConfig from '@/views/concurrency/ConcurrencyConfig'
import ConcurrencyQueue from '@/views/concurrency/ConcurrencyQueue'
import RunnerList from '@/views/runner/RunnerList'

const routes = [
  {
    path: '/',
    alias: '/home',
    name: 'Home',
    component: Home
  },
  {
    path: '/app/search',
    name: 'AppList',
    component: AppList
  },
  {
    path: '/plan/search',
    name: 'PlanList',
    component: PlanList
  },
  {
    path: '/plan/tabs',
    name: 'PlanTabs',
    component: PlanTabs
  },
  {
    path: '/record/search',
    name: 'RecordList',
    component: RecordList
  },
  {
    path: '/runner/getList',
    name: 'RunnerList',
    component: RunnerList
  },
  {
    path: '/proxy/search',
    name: 'ProxyList',
    component: ProxyList
  },
  {
    path: '/concurrency/config',
    name: 'ConcurrencyConfig',
    component: ConcurrencyConfig
  },
  {
    path: '/concurrency/queue',
    name: 'ConcurrencyQueue',
    component: ConcurrencyQueue
  }
]
const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router