<script setup>
import Cookies from 'js-cookie'
import { computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { UserFilled } from '@element-plus/icons-vue'
import {
  ElAvatar, ElBadge, ElFormItem,
  ElLink, ElPopover, ElRow, ElText
} from 'element-plus'
import { useUserStore } from '@/store'
import { checkLogin } from '@/common/Permission'
import { asyncCurrentUser, asyncLogout, asyncResetUserApps } from '@/common/AsyncRequest'

const route = useRoute()
const userStore = useUserStore()
const loginURL = computed(() => {
  const redirectURL = encodeURIComponent(import.meta.env.VITE_BASE_URL + route.fullPath)
  return `${import.meta.env.VITE_HYLIAN_BASE_URL}/?redirect=${redirectURL}`
})

const onLogout = async () => {
  await asyncLogout()
  useUserStore().$reset()
}

watch(() => userStore.id, async () => {
  if (userStore.id) await asyncResetUserApps()
}, { immediate: true })
onMounted(async () => {
  if (Cookies.get('TOKEN')) userStore.inject(await asyncCurrentUser())
})
</script>

<template>
  <el-row v-if="checkLogin()">
    <el-popover popper-style="box-shadow: rgb(14 18 22 / 35%) 0 10px 38px -10px, rgb(14 18 22 / 20%) 0 10px 20px -15px;padding: 20px;width: auto;min-width: 150px;max-width: 280px;">
      <template #reference>
        <el-badge v-if="userStore.superAdmin" is-dot>
          <el-avatar shape="circle" :size="30" fit="cover" :icon="UserFilled" :src="userStore.avatar" />
        </el-badge>
      </template>
      <template #default>
        <el-form-item class="mb-0" >
          <el-badge v-if="userStore.superAdmin" is-dot>
            <el-avatar shape="circle" :size="60" fit="cover" :icon="UserFilled" :src="userStore.avatar" />
          </el-badge>
        </el-form-item>
        <el-form-item class="mb-0" label="用户名" label-position="left" label-width="60">
          {{ userStore.username }}
        </el-form-item>
        <el-form-item class="mb-0" label="昵称" label-position="left" label-width="60">
          {{ userStore.name }}
        </el-form-item>
        <el-form-item class="mb-0" label="角色" label-position="left" label-width="60">
          <el-text v-if="userStore.superAdmin">超级管理员</el-text>
          <el-text v-else>普通用户</el-text>
        </el-form-item>
      </template>
    </el-popover>
    <el-text class="ml-2">欢迎您，{{ userStore.name }}</el-text>
    <el-link class="ml-2" @click="onLogout">退出</el-link>
  </el-row>
  <el-row v-else>
    <el-avatar shape="circle" :size="30" fit="cover" :icon="UserFilled" />
    <el-text class="ml-2">游客</el-text>
    <el-link class="ml-2" :href="loginURL">登录</el-link>
  </el-row>
</template>

<style scoped>
</style>