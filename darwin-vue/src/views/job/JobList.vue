<script setup>
import { useRoute } from 'vue-router'
import { ElBreadcrumb, ElBreadcrumbItem } from 'element-plus'
import DarwinCard from '@/components/data/Card'
import JobList from '@/views/job/JobListInner'

const route = useRoute()
</script>

<template>
  <darwin-card>
    <template #title>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>基础功能</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/plan/search' }">爬虫计划</el-breadcrumb-item>
        <el-breadcrumb-item>{{ route.query.name }}</el-breadcrumb-item>
      </el-breadcrumb>
    </template>
    <job-list :plan-id="route.query.plan_id" />
  </darwin-card>
</template>

<style scoped>

</style>