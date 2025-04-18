import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/views/Home'
import AppList from '@/views/app/AppList'
import PlanList from '@/views/plan/PlanList'
import PlanTabs from '@/views/plan/PlanTabs'
import RecordList from '@/views/record/RecordList'
import ExecutorList from '@/views/executor/ExecutorList'
import ProxyList from '@/views/proxy/ProxyList'

const routes = [
  {
    path: '/',
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
    path: '/executor/getList',
    name: 'ExecutorList',
    component: ExecutorList
  },
  {
    path: '/proxy/search',
    name: 'ProxyList',
    component: ProxyList
  }
]
const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router