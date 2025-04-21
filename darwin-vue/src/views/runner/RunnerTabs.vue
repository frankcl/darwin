<script setup>
import {
  ElIcon, ElTabPane, ElTabs, ElText
} from 'element-plus'
import { Cpu, Monitor } from '@element-plus/icons-vue'
import RunnerList from '@/views/runner/RunnerList'

</script>

<template>
  <el-tabs type="border-card">
    <el-tab-pane>
      <template #label>
        <el-icon><cpu /></el-icon>
        <el-text class="ml-2">核心进程</el-text>
      </template>
      <runner-list title="核心进程列表" :type="1" />
    </el-tab-pane>
    <el-tab-pane>
      <template #label>
        <el-icon><monitor /></el-icon>
        <el-text class="ml-2">监控进程</el-text>
      </template>
      <runner-list title="监控进程列表" :type="2" />
    </el-tab-pane>
  </el-tabs>
</template>

<style scoped>
</style>