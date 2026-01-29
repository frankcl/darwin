import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/views/main/Home.vue'
import AppList from '@/views/app/AppList'
import AppSecretList from '@/views/app_secret/AppSecretList'
import PlanList from '@/views/plan/PlanList'
import PlanTabs from '@/views/plan/PlanTabs'
import JobList from '@/views/job/JobList'
import RecordList from '@/views/record/RecordList'
import ProxyList from '@/views/proxy/ProxyList'
import CrawlDelay from '@/views/concurrency/CrawlDelay'
import ConControl from '@/views/concurrency/ConControl'
import ConcurrencyQueue from '@/views/concurrency/ConQueue'
import CookieControl from '@/views/cookie/CookieControl'
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
    path: '/app_secret/search',
    name: 'AppSecretList',
    component: AppSecretList
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
    path: '/job/search',
    name: 'JobList',
    component: JobList
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
    path: '/concurrency/control',
    name: 'ConControl',
    component: ConControl
  },
  {
    path: '/concurrency/crawlDelay',
    name: 'CrawlDelay',
    component: CrawlDelay
  },
  {
    path: '/concurrency/queue',
    name: 'ConcurrencyQueue',
    component: ConcurrencyQueue
  },
  {
    path: '/cookie/control',
    name: 'CookieControl',
    component: CookieControl
  }
]
const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router