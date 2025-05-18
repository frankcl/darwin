<script setup>
import { ElBreadcrumb, ElBreadcrumbItem, ElCol, ElRow } from 'element-plus'
import DownAnalysis from '@/views/dashboard/DownAnalysis'
import FetchAnalysis from '@/views/dashboard/FetchAnalysis'
import QueueWait from '@/views/dashboard/QueueWait'
import DarwinCard from '@/components/data/Card'
import StatusDistribution from '@/views/dashboard/StatusDistribution'
import TypeDistribution from '@/views/dashboard/TypeDistribution'
import FetchCountTrend from '@/views/dashboard/FetchCountTrend'
import HostFetchCount from '@/views/dashboard/HostFetchCount'

</script>

<template>
  <darwin-card>
    <template #title>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>Dashboard</el-breadcrumb-item>
      </el-breadcrumb>
    </template>
    <el-row :gutter="20">
      <el-col :span="8">
        <fetch-analysis />
      </el-col>
      <el-col :span="16">
        <fetch-count-trend :height="240" />
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="8">
        <type-distribution title="24小时数据类型分布" :width="276" />
      </el-col>
      <el-col :span="8">
        <status-distribution title="24小时数据状态分布" :width="300" />
      </el-col>
      <el-col :span="8">
        <host-fetch-count />
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="8">
        <queue-wait />
      </el-col>
      <el-col :span="8">
        <down-analysis />
      </el-col>
    </el-row>
  </darwin-card>
</template>

<style scoped>
</style>