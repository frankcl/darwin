<script setup>
import { ElAside, ElContainer, ElFooter, ElHeader, ElIcon, ElMain, ElMenu, ElMenuItem, ElRow } from 'element-plus'
import { Document, Grid, Guide, Histogram, Link, List } from '@element-plus/icons-vue'
import UserLogin from '@/components/user/UserLogin.vue'
</script>

<template>
  <el-container>
    <el-header class="home-header">
      <el-row align="middle" justify="end" class="h100">
        <user-login></user-login>
      </el-row>
    </el-header>
    <el-container class="home-container">
      <el-aside class="home-aside">
        <el-menu class="aside-menu" :collapse="false">
          <el-menu-item index="1">
            <el-icon><Grid /></el-icon>
            <RouterLink :to="{ name: 'AppList' }">爬虫应用</RouterLink>
          </el-menu-item>
          <el-menu-item index="2">
            <el-icon><List /></el-icon>
            <RouterLink :to="{ name: 'PlanList' }">抓取计划</RouterLink>
          </el-menu-item>
          <el-menu-item index="3">
            <el-icon><Document /></el-icon>
            <span>抓取数据</span>
          </el-menu-item>
          <el-menu-item index="4">
            <el-icon><Histogram /></el-icon>
            <span>爬虫队列</span>
          </el-menu-item>
          <el-menu-item index="5">
            <el-icon><Guide /></el-icon>
            <span>调度执行</span>
          </el-menu-item>
          <el-menu-item index="6">
            <el-icon><Link /></el-icon>
            <span>代理IP</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main>
        <RouterView></RouterView>
      </el-main>
    </el-container>
    <el-footer class="home-footer"></el-footer>
  </el-container>
</template>

<style scoped>
.home-container {
  height: 100vh;
}
.home-header {
  height: 56px;
}
.home-aside {
  width: 150px;
}
.home-footer {
}
.aside-menu {
  border-right: none;
}
</style>