<script setup>
import Cookies from 'js-cookie'
import { IconChevronDown, IconHelp, IconMenu2, IconPower, IconUserPlus } from '@tabler/icons-vue'
import { computed, onMounted, onUnmounted, ref, useTemplateRef, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElAvatar, ElLink, ElText } from 'element-plus'
import { useUserStore } from '@/store'
import { checkLogin } from '@/common/Permission'
import { asyncCurrentUser, asyncLogout, asyncResetUserApps } from '@/common/AsyncRequest'
import ImageGuest from '@/assets/images/guest.jpg'

const emits = defineEmits(['showSidebar'])
const route = useRoute()
const userStore = useUserStore()
const loginURL = computed(() => {
  const redirectURL = encodeURIComponent(import.meta.env.VITE_BASE_URL + route.fullPath)
  return `${import.meta.env.VITE_HYLIAN_BASE_URL}/?redirect=${redirectURL}`
})
const navUserRef = useTemplateRef('navUser')
const dropdownMenuRef = useTemplateRef('dropdownMenu')
const isOpenDropdownMenu = ref(false)

const handleLogout = async () => {
  await asyncLogout()
  useUserStore().$reset()
  isOpenDropdownMenu.value = false
}

const clickInsideElement = (event, element) => {
  const rect = element.getBoundingClientRect()
  return event.clientX >= rect.left && event.clientX <= rect.right &&
    event.clientY >= rect.top && event.clientY <= rect.bottom
}

const handleClick = event => {
  if (clickInsideElement(event, navUserRef.value)) isOpenDropdownMenu.value = !isOpenDropdownMenu.value
  else if (!clickInsideElement(event, dropdownMenuRef.value)) isOpenDropdownMenu.value = false
}

watch(() => userStore.id, async () => {
  if (userStore.id) await asyncResetUserApps()
}, { immediate: true })
onMounted(async () => {
  if (Cookies.get('TOKEN')) userStore.inject(await asyncCurrentUser())
  window.addEventListener('click', handleClick)
})
onUnmounted(() => window.removeEventListener('click', handleClick))
</script>

<template>
  <nav class="navbar">
    <ul class="navbar-nav">
      <li class="d-xl-none nav-item">
        <el-link class="nav-link nav-icon-hover" :underline="false" @click="emits('showSidebar')">
          <IconMenu2 />
        </el-link>
      </li>
    </ul>
    <div class="flex-grow-1 d-flex justify-content-end">
      <ul class="navbar-nav">
        <li class="dropdown" :class="{ open: isOpenDropdownMenu }">
          <div ref="navUser" class="d-flex navbar-user align-items-center">
            <el-link class="nav-link" :underline="false">
              <el-avatar shape="circle" fit="cover" :src="userStore.avatar || ImageGuest" />
              <div class="flex-grow-1 ml-3">
                <span class="d-block fs-m fw-500">{{ userStore.name || '游客' }}</span>
                <small v-if="checkLogin">
                  <el-text v-if="userStore.superAdmin" class="fs-xs">超级管理员</el-text>
                  <el-text class="fs-xs" v-else>普通用户</el-text>
                </small>
              </div>
              <IconChevronDown class="ml-3" size="14" />
            </el-link>
          </div>
          <ul ref="dropdownMenu" class="dropdown-menu" :class="{ 'd-none': !isOpenDropdownMenu }">
            <li v-if="checkLogin()">
              <el-link class="dropdown-link" :underline="false">
                <IconHelp size="20" /><span class="ml-2">帮助</span>
              </el-link>
            </li>
            <li v-else>
              <el-link class="dropdown-link" :underline="false" :href="loginURL">
                <IconUserPlus size="20" /><span class="ml-2">注册/登录</span>
              </el-link>
            </li>
            <li v-if="checkLogin()">
              <el-link class="dropdown-link" :underline="false" @click="handleLogout">
                <IconPower size="20" /><span class="ml-2">退出</span>
              </el-link>
            </li>
          </ul>
        </li>
      </ul>
    </div>
  </nav>
</template>

<style scoped>
.navbar {
  display: -webkit-box;
  display: -ms-flexbox;
  display: flex;
  -ms-flex-wrap: nowrap;
  flex-wrap: nowrap;
  -webkit-box-align: center;
  -ms-flex-align: center;
  align-items: center;
  -webkit-box-pack: justify;
  -ms-flex-pack: justify;
  justify-content: space-between;
  position: relative;
  min-height: 70px;
}
.navbar-nav {
  display: -webkit-box;
  display: -ms-flexbox;
  display: flex;
  -webkit-box-orient: horizontal;
  -webkit-box-direction: normal;
  -ms-flex-direction: row;
  flex-direction: row;
  padding-left: 0;
  margin-bottom: 0;
  list-style: none;
}
.navbar-user {
  min-width: 12rem;
  text-align: left;
}
.nav-link {
  padding: 8px 16px;
  height: 70px;
  display: -webkit-box;
  display: -ms-flexbox;
  display: flex;
  -webkit-box-align: center;
  -ms-flex-align: center;
  align-items: center;
  position: relative;
  z-index: 2;
}
.nav-link:focus, .nav-link:hover {
  color: var(--el-color-primary);
}
.nav-icon-hover:hover:before {
  content: "";
  position: absolute;
  left: 50%;
  top: 50%;
  -webkit-transform: translate(-50%,-50%);
  transform: translate(-50%,-50%);
  height: 40px;
  width: 40px;
  z-index: -1;
  border-radius: 100px;
  -webkit-transition: all 0.3s ease-in-out;
  transition: all 0.3s ease-in-out;
  background-color: #ecf2ff
}
.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  left: auto;
  margin-top: 0;
  border-radius: 8px;
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.175);
  border: 0;
  padding: 5px 0;
  min-width: 200px;
  color: #373a3c;
  text-align: left;
  list-style: none;
  background-color: #fff;
  z-index: 1000;
}
.open > .dropdown-menu {
  transform: scale(1);
  opacity: 1;
  -webkit-animation: scaleDrop 0.3s both;
  -moz-animation: scaleDrop 0.3s both;
  -ms-animation: scaleDrop 0.3s both;
  animation: scaleDrop 0.3s both;
  -webkit-transform-origin: 100% 0;
  transform-origin: 100% 0;
}
.dropdown-menu li {
  padding: 7px 20px;
}
.dropdown-menu li:hover {
  border-radius: 8px;
  background-color: #F6F9FC;
}
.dropdown-link {
  color: #2A3547;
}
.dropdown-link:focus,.dropdown-link:hover {
  color: #2A3547;
}
@keyframes scaleDrop {
  0% {
    opacity: 0;
    -webkit-transform: scale(0);
    -moz-transform: scale(0);
    -ms-transform: scale(0);
    transform: scale(0);
  }
  100% {
    opacity: 1;
    -webkit-transform: scale(1);
    -moz-transform: scale(1);
    -ms-transform: scale(1);
    transform: scale(1);
  }
}
@media (min-width: 1280px) {
  .d-xl-none {
    display: none !important;
  }
}
</style>