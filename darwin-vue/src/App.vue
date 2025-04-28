<script setup>
import { useRouter } from 'vue-router'
import {
  ElAside, ElCol, ElContainer, ElFooter,
  ElHeader, ElLink, ElMain, ElRow
} from 'element-plus'
import UserView from '@/components/user/UserView'
import IMenu from '@/views/Menu'

const router = useRouter()
</script>

<template>
  <el-container>
    <el-header class="header">
      <el-row align="middle" class="h100">
        <el-col :span="18">
          <el-link @click="router.push({ path: '/home' })">首页</el-link>
        </el-col>
        <el-col :span="6">
          <el-row justify="end"><user-view /></el-row>
        </el-col>
      </el-row>
    </el-header>
    <el-container class="main-container">
      <el-aside class="aside"><i-menu /></el-aside>
      <el-main>
        <RouterView></RouterView>
      </el-main>
    </el-container>
    <el-footer class="footer"></el-footer>
  </el-container>
</template>

<style scoped>
.main-container {
  height: 100vh;
}
.header {
  height: 56px;
}
.aside {
  width: 150px;
}
.footer {
}
</style>
