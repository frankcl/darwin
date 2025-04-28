<script setup>
import { useRouter } from 'vue-router'
import { ElIcon, ElLink, ElMenu, ElMenuItem, ElSubMenu } from 'element-plus'
import { Cpu, Document, Grid, Guide, Link, List, Memo, Monitor, Place, Reading } from '@element-plus/icons-vue'

const router = useRouter()

const navigate = (path, query) => router.push({ path: path, query: query })
</script>

<template>
  <el-menu class="menu" :collapse="false">
    <el-menu-item index="1">
      <el-icon><Grid /></el-icon>
      <el-link class="ml-1" @click="navigate('/app/search')">爬虫应用</el-link>
    </el-menu-item>
    <el-menu-item index="2">
      <el-icon><List /></el-icon>
      <el-link class="ml-1" @click="navigate('/plan/search')">爬虫计划</el-link>
    </el-menu-item>
    <el-menu-item index="3">
      <el-icon><Document /></el-icon>
      <el-link class="ml-1" @click="navigate('/record/search')">抓取数据</el-link>
    </el-menu-item>
    <el-sub-menu index="4">
      <template #title>
        <el-icon><Place /></el-icon>
        <span class="ml-1" >并发控制</span>
      </template>
      <el-menu-item index="4-1">
        <el-icon><Memo /></el-icon>
        <el-link class="ml-1" @click="navigate('/concurrency/queue')">并发队列</el-link>
      </el-menu-item>
      <el-menu-item index="4-2">
        <el-icon><Reading /></el-icon>
        <el-link class="ml-1" @click="navigate('/concurrency/config')">并发配置</el-link>
      </el-menu-item>
    </el-sub-menu>
    <el-sub-menu index="5">
      <template #title>
        <el-icon><Guide /></el-icon>
        <span class="ml-1" >进程管理</span>
      </template>
      <el-menu-item index="5-1">
        <el-icon><Cpu /></el-icon>
        <el-link class="ml-1" @click="navigate('/runner/getList', { type: 1 })">核心进程</el-link>
      </el-menu-item>
      <el-menu-item index="5-2">
        <el-icon><Monitor /></el-icon>
        <el-link class="ml-1" @click="navigate('/runner/getList', { type: 2 })">监控进程</el-link>
      </el-menu-item>
    </el-sub-menu>
    <el-menu-item index="6">
      <el-icon><Link /></el-icon>
      <el-link class="ml-1" @click="navigate('/proxy/search')">代理管理</el-link>
    </el-menu-item>
  </el-menu>
</template>

<style scoped>
.menu {
  border-right: none;
}
</style>