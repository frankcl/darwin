<script setup>
import { useRouter } from 'vue-router'
import { ElIcon, ElLink, ElMenu, ElMenuItem } from 'element-plus'
import { Document, Grid, Guide, Histogram, Link, List } from '@element-plus/icons-vue'

const router = useRouter()

const navigate = path => router.push({ path: path })
</script>

<template>
  <el-menu class="menu" :collapse="false">
    <el-menu-item index="1">
      <el-icon><Grid /></el-icon>
      <el-link class="ml-1" @click="navigate('/app/search')">应用</el-link>
    </el-menu-item>
    <el-menu-item index="2">
      <el-icon><List /></el-icon>
      <el-link class="ml-1" @click="navigate('/plan/search')">计划</el-link>
    </el-menu-item>
    <el-menu-item index="3">
      <el-icon><Document /></el-icon>
      <el-link class="ml-1" @click="navigate('/record/search')">数据</el-link>
    </el-menu-item>
    <el-menu-item index="4">
      <el-icon><Histogram /></el-icon>
      <span class="ml-1" >队列</span>
    </el-menu-item>
    <el-menu-item index="5">
      <el-icon><Guide /></el-icon>
      <el-link class="ml-1" @click="navigate('/runner/tabs')">进程</el-link>
    </el-menu-item>
    <el-menu-item index="6">
      <el-icon><Link /></el-icon>
      <el-link class="ml-1" @click="navigate('/proxy/search')">代理</el-link>
    </el-menu-item>
  </el-menu>
</template>

<style scoped>
.menu {
  border-right: none;
}
</style>