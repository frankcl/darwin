<script setup>
import { IconContract, IconListDetails, IconScript, IconSeedling } from '@tabler/icons-vue'
import { useRoute } from 'vue-router'
import { ElBreadcrumb, ElBreadcrumbItem, ElTabPane, ElTabs } from 'element-plus'
import DarwinCard from '@/components/data/Card'
import EditPlan from '@/views/plan/EditPlan'
import SeedList from '@/views/seed/SeedList'
import RuleOperation from '@/views/rule/RuleOperation'
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
    <el-tabs>
      <el-tab-pane>
        <template #label>
          <IconContract size="20" />
          <span class="ml-2">编辑计划</span>
        </template>
        <edit-plan :id="route.query.plan_id" />
      </el-tab-pane>
      <el-tab-pane>
        <template #label>
          <IconScript size="20" />
          <span class="ml-2">规则脚本</span>
        </template>
        <rule-operation :plan-id="route.query.plan_id" />
      </el-tab-pane>
      <el-tab-pane>
        <template #label>
          <IconSeedling size="20" />
          <span class="ml-2">种子列表</span>
        </template>
        <seed-list :plan-id="route.query.plan_id" />
      </el-tab-pane>
      <el-tab-pane>
        <template #label>
          <IconListDetails size="20" />
          <span class="ml-2">爬虫任务</span>
        </template>
        <job-list :plan-id="route.query.plan_id" />
      </el-tab-pane>
    </el-tabs>
  </darwin-card>
</template>

<style scoped>
</style>