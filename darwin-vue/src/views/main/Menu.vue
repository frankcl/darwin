<script setup>
import {
  IconApps, IconClockHour9, IconCpu, IconDashboard,
  IconDatabase, IconDeviceHeartMonitor, IconKey, IconNetwork,
  IconSettings, IconSpider, IconStackFront
} from '@tabler/icons-vue'
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const activeLink = ref(route.fullPath)
const handleClick = (path, query = undefined) => {
  activeLink.value = path
  if (query) {
    activeLink.value += '?'
    Object.keys(query).forEach((key, index) => {
      if (index > 0) activeLink.value += '&'
      activeLink.value += `${key}=${query[key]}`
    })
  }
  router.push({ path: path, query: query })
}
watch(() => route.fullPath, () => activeLink.value = route.fullPath)
</script>

<template>
  <div class="scroll-sidebar">
    <ul class="sidebar-menu">
      <li class="sidebar-menu-cap">首页</li>
      <li class="sidebar-menu-item">
        <a class="sidebar-menu-link" :class="{ active: activeLink === '/home' || activeLink === '/' }"
           @click="handleClick('/home')">
          <IconDashboard size="22" />
          <span>Dashboard</span>
        </a>
      </li>
      <li class="sidebar-menu-cap">基础功能</li>
      <li class="sidebar-menu-item">
        <a class="sidebar-menu-link" :class="{ active: activeLink === '/app/search'}"
           @click="handleClick('/app/search')">
          <IconApps size="22" />
          <span>爬虫应用</span>
        </a>
      </li>
      <li class="sidebar-menu-item">
        <a class="sidebar-menu-link" :class="{ active: activeLink === '/plan/search'}"
           @click="handleClick('/plan/search')">
          <IconSpider size="22" />
          <span>爬虫计划</span>
        </a>
      </li>
      <li class="sidebar-menu-item">
        <a class="sidebar-menu-link" :class="{ active: activeLink.startsWith('/record/search')}"
           @click="handleClick('/record/search')">
          <IconDatabase size="22" />
          <span>抓取数据</span>
        </a>
      </li>
      <li class="sidebar-menu-cap">抓取控制</li>
      <li class="sidebar-menu-item">
        <a class="sidebar-menu-link" :class="{ active: activeLink === '/concurrency/queue'}"
           @click="handleClick('/concurrency/queue')">
          <IconStackFront size="22" />
          <span>并发队列</span>
        </a>
      </li>
      <li class="sidebar-menu-item">
        <a class="sidebar-menu-link" :class="{ active: activeLink === '/concurrency/control'}"
           @click="handleClick('/concurrency/control')">
          <IconSettings size="22" />
          <span>并发控制</span>
        </a>
      </li>
      <li class="sidebar-menu-item">
        <a class="sidebar-menu-link" :class="{ active: activeLink === '/concurrency/crawlDelay'}"
           @click="handleClick('/concurrency/crawlDelay')">
          <IconClockHour9 size="22" />
          <span>抓取间隔</span>
        </a>
      </li>
      <li class="sidebar-menu-item">
        <a class="sidebar-menu-link" :class="{ active: activeLink === '/proxy/search'}"
           @click="handleClick('/proxy/search')">
          <IconNetwork size="22" />
          <span>代理管理</span>
        </a>
      </li>
      <li class="sidebar-menu-cap">平台管理</li>
      <li class="sidebar-menu-item">
        <a class="sidebar-menu-link" :class="{ active: activeLink === '/runner/getList?type=1'}"
           @click="handleClick('/runner/getList', { type: 1 })">
          <IconCpu size="22" />
          <span>核心进程</span>
        </a>
      </li>
      <li class="sidebar-menu-item">
        <a class="sidebar-menu-link" :class="{ active: activeLink === '/runner/getList?type=2'}"
           @click="handleClick('/runner/getList', { type: 2 })">
          <IconDeviceHeartMonitor size="22" />
          <span>监控进程</span>
        </a>
      </li>
      <li class="sidebar-menu-item">
        <a class="sidebar-menu-link" :class="{ active: activeLink === '/app_secret/search'}"
           @click="handleClick('/app_secret/search')">
          <IconKey size="22" />
          <span>应用秘钥</span>
        </a>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.scroll-sidebar {
  padding: 0 24px;
  height: calc(100vh - 70px);
  overflow-y: auto;
}
.sidebar-menu {
  list-style: none;
}
.sidebar-menu-cap {
  margin-top: 24px;
  color: #2A3547;
  font-size: 13px;
  font-weight: 700;
  padding: 3px 12px;
  line-height: 26px;
  text-transform: uppercase;
}
.sidebar-menu-link {
  color: #2A3547;
  font-size: 14px;
  display: -webkit-box;
  display: -ms-flexbox;
  display: flex;
  white-space: nowrap;
  -webkit-box-align: center;
  -ms-flex-align: center;
  align-items: center;
  -webkit-box-pack: start;
  -ms-flex-pack: start;
  justify-content: start;
  line-height: 25px;
  position: relative;
  margin: 0 0 2px;
  padding: 10px;
  border-radius: 7px;
  cursor: pointer;
  gap: 15px;
}
.active {
  background-color: var(--el-color-primary);
  color: #fff
}
.sidebar-menu-link:hover {
  background-color: rgba(93,135,255,0.1);
  color: var(--el-color-primary);
}
.sidebar-menu-link.active:hover {
  background-color: var(--el-color-primary);
  color: #fff
}
</style>