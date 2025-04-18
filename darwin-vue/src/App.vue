<script setup>
import {
  ElAside, ElContainer, ElFooter,
  ElHeader, ElMain,  ElRow
} from 'element-plus'
import UserView from '@/components/user/UserView'
import IMenu from '@/views/Menu'
</script>

<template>
  <el-container>
    <el-header class="header">
      <el-row align="middle" justify="end" class="h100"><user-view /></el-row>
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
