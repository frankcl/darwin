<script setup>
import { useRoute, useRouter } from 'vue-router'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElIcon, ElPageHeader,
  ElSpace, ElTabPane, ElTabs, ElText
} from 'element-plus'
import {
  ArrowRight, Document, DocumentCopy, Edit, Link
} from '@element-plus/icons-vue'
import EditPlan from '@/views/plan/EditPlan'
import SeedList from '@/views/seed/SeedList'
import EditRule from '@/views/rule/EditRule'

const route = useRoute()
const router = useRouter()
</script>

<template>
  <el-space direction="vertical" :size="20" :fill="true" style="min-width: 100%">
    <el-page-header @back="router.back()">
      <template #breadcrumb>
        <el-breadcrumb :separator-icon="ArrowRight">
          <el-breadcrumb-item :to="{ name: 'Home' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ name: 'PlanList' }">抓取计划</el-breadcrumb-item>
          <el-breadcrumb-item>完善计划</el-breadcrumb-item>
        </el-breadcrumb>
      </template>
      <template #content>
        <span class="text-large font-600 mr-3">爬虫计划</span>
      </template>
    </el-page-header>
    <el-tabs type="border-card">
      <el-tab-pane>
        <template #label>
          <el-icon><document /></el-icon>
          <el-text>基本信息</el-text>
        </template>
        <edit-plan :id="route.query.id"></edit-plan>
      </el-tab-pane>
      <el-tab-pane>
        <template #label>
          <el-icon><edit /></el-icon>
          <el-text>规则脚本</el-text>
        </template>
        <edit-rule :plan-id="route.query.id"></edit-rule>
      </el-tab-pane>
      <el-tab-pane>
        <template #label>
          <el-icon><Link /></el-icon>
          <el-text>种子URL</el-text>
        </template>
        <seed-list :id="route.query.id"></seed-list>
      </el-tab-pane>
      <el-tab-pane>
        <template #label>
          <el-icon><document-copy /></el-icon>
          <el-text>爬虫任务</el-text>
        </template>
      </el-tab-pane>
    </el-tabs>
  </el-space>
</template>

<style scoped>

</style>