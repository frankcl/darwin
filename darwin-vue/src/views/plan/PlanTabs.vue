<script setup>
import { useRoute } from 'vue-router'
import {
  ElIcon, ElTabPane, ElTabs, ElText
} from 'element-plus'
import {
  Document, DocumentCopy, Edit, Link
} from '@element-plus/icons-vue'
import EditPlan from '@/views/plan/EditPlan'
import SeedList from '@/views/seed/SeedList'
import RuleOperation from '@/views/rule/RuleOperation'
import JobList from '@/views/job/JobList'

const route = useRoute()
</script>

<template>
  <el-tabs type="border-card">
    <el-tab-pane>
      <template #label>
        <el-icon><document /></el-icon>
        <el-text class="ml-2">计划详情</el-text>
      </template>
      <edit-plan :id="route.query.id" />
    </el-tab-pane>
    <el-tab-pane>
      <template #label>
        <el-icon><edit /></el-icon>
        <el-text class="ml-2">规则脚本</el-text>
      </template>
      <rule-operation :plan-id="route.query.id" />
    </el-tab-pane>
    <el-tab-pane>
      <template #label>
        <el-icon><Link /></el-icon>
        <el-text class="ml-2">种子列表</el-text>
      </template>
      <seed-list :plan-id="route.query.id" />
    </el-tab-pane>
    <el-tab-pane>
      <template #label>
        <el-icon><document-copy /></el-icon>
        <el-text class="ml-2">爬虫任务</el-text>
      </template>
      <job-list :plan-id="route.query.id" />
    </el-tab-pane>
  </el-tabs>
</template>

<style scoped>
</style>