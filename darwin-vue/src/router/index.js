import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/views/Home'
import AppList from '@/views/app/AppList'
import PlanList from '@/views/plan/PlanList'
import PlanTabs from '@/views/plan/PlanTabs'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    children: [
      {
        path: 'app/appList',
        name: 'AppList',
        component: AppList
      },
      {
        path: 'plan/planList',
        name: 'PlanList',
        component: PlanList
      },
      {
        path: 'plan/tabs',
        name: 'PlanTabs',
        component: PlanTabs
      }
    ]
  }
]
const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async to => {
})

export default router