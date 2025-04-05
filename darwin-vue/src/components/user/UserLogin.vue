<script setup>
import { onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { UserFilled } from '@element-plus/icons-vue'
import { ElAvatar, ElBadge, ElCol, ElLink, ElPopover, ElRow, ElSpace, ElText } from 'element-plus'
import { useUserStore } from '@/store'
import { logout, refreshUser } from '@/common/assortment'

const route = useRoute()
const userStore = useUserStore()
const redirectURL = import.meta.env.VITE_HYLIAN_BASE_URL + '/?redirect=' +
  encodeURIComponent(import.meta.env.VITE_BASE_URL + route.fullPath)

watch(() => userStore.id, async () => await userStore.fillApps(), { immediate: true })
onMounted(() => refreshUser(true))
</script>

<template>
  <el-row v-if="userStore.injected">
    <el-popover :width="180" popper-class="user-profile-popper">
      <template #reference>
        <el-badge v-if="userStore.superAdmin" is-dot>
          <el-avatar shape="circle" :size="30" fit="cover" :icon="UserFilled" :src="userStore.avatar"></el-avatar>
        </el-badge>
      </template>
      <template #default>
        <el-space direction="vertical" :size="20" :fill="true" style="min-width: 100%">
          <el-row>
            <el-badge v-if="userStore.superAdmin" is-dot>
              <el-avatar shape="circle" :size="60" fit="cover" :icon="UserFilled" :src="userStore.avatar"></el-avatar>
            </el-badge>
          </el-row>
          <el-row>
            <el-col :span="9">用户名:</el-col>
            <el-col :span="15">{{ userStore.username }}</el-col>
          </el-row>
          <el-row>
            <el-col :span="9">名称:</el-col>
            <el-col :span="15">{{ userStore.name }}</el-col>
          </el-row>
          <el-row>
            <el-col :span="9">租户:</el-col>
            <el-col :span="15" v-if="userStore.tenant">{{ userStore.tenant.name }}</el-col>
          </el-row>
          <el-row>
            <el-col :span="9">角色:</el-col>
            <el-col :span="15">
              <span v-if="userStore.superAdmin">超级管理员</span>
              <span v-else>普通用户</span>
            </el-col>
          </el-row>
        </el-space>
      </template>
    </el-popover>
    &nbsp;&nbsp;
    <el-text class="inline-flex-block">欢迎您，{{ userStore.name }}</el-text>&nbsp;&nbsp;
    <el-link @click="logout()">退出</el-link>
  </el-row>
  <el-row v-else>
    <el-avatar shape="circle" :size="30" fit="cover" :icon="UserFilled"></el-avatar>&nbsp;&nbsp;
    <span class="inline-flex-block">游客</span>&nbsp;&nbsp;
    <el-link :href="redirectURL">登录</el-link>
  </el-row>
</template>

<style scoped>
.user-profile-popper {
  box-shadow: rgb(14 18 22 / 35%) 0 10px 38px -10px, rgb(14 18 22 / 20%) 0 10px 20px -15px;
  padding: 20px;
}
</style>