<script setup>
import { format } from 'date-fns'
import { reactive, ref, useTemplateRef, watchEffect } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, Delete, Timer } from '@element-plus/icons-vue'
import {
  ElBreadcrumb,
  ElBreadcrumbItem,
  ElButton,
  ElCol,
  ElForm,
  ElFormItem,
  ElIcon,
  ElInput,
  ElLink,
  ElNotification,
  ElPageHeader,
  ElPagination,
  ElRadioButton,
  ElRadioGroup,
  ElRow,
  ElSpace,
  ElSwitch,
  ElTable,
  ElTableColumn,
  ElTooltip
} from 'element-plus'
import { useUserStore } from '@/store'
import {
  checkUserLogin, executeAsyncRequest,
  executeAsyncRequestAfterConfirm, fillSearchQuerySort,
  searchQueryToRequest,
} from '@/common/assortment'
import {
  asyncClosePlan, asyncDeletePlan, asyncExecutePlan,
  asyncOpenPlan, asyncSearchPlans
} from '@/common/service'
import AppSearch from '@/components/app/AppSearch'
import AddPlan from '@/views/plan/AddPlan'

const router = useRouter()
const formRef = useTemplateRef('formRef')
const tableRef = useTemplateRef('tableRef')
const userStore = useUserStore()
const openAddDialog = ref(false)
const plans = ref([])
const total = ref(0)
const query = reactive({
  current: 1,
  size: 10,
  name: null,
  app_id: null,
  app_ids: 'all',
  category: 'all',
  priority: 'all',
  fetch_method: 'all',
  status: 'all',
  sort_field: null,
  sort_order: null
})

const search = async () => {
  const request = searchQueryToRequest(query)
  if (query.name) request.name = query.name
  if (query.app_id) request.app_id = query.app_id
  if (query.category && query.category !== 'all') request.category = query.category
  if (query.priority && query.priority !== 'all') request.priority = query.priority
  if (query.status && query.status !== 'all') request.status = query.status
  if (query.fetch_method && query.fetch_method !== 'all') request.fetch_method = query.fetch_method
  if (query.app_ids && query.app_ids !== 'all') request.app_ids = query.app_ids
  const pager = await asyncSearchPlans(request)
  total.value = pager.total
  plans.value = pager.records
}

const remove = async id => {
  if (!checkUserLogin()) return
  const successHandle = () => ElNotification.success('删除计划成功')
  const failHandle = () => ElNotification.error('删除计划失败')
  if (!await executeAsyncRequestAfterConfirm(
    '删除提示', '是否确定删除该计划？', asyncDeletePlan, id, successHandle, failHandle)) return
  await search()
}

const openClose = async (row, status) => {
  const exceptionHandle = () => row.status = !status
  if (status) {
    const successHandle = () => ElNotification.success('开启计划成功')
    const failHandle = () => ElNotification.error('开启计划失败')
    await executeAsyncRequest(asyncOpenPlan, row.plan_id, successHandle, failHandle, exceptionHandle)
  } else {
    const successHandle = () => ElNotification.success('关闭计划成功')
    const failHandle = () => ElNotification.error('关闭计划失败')
    await executeAsyncRequest(asyncClosePlan, row.plan_id, successHandle, failHandle, exceptionHandle)
  }
}

const execute = async id => {
  const successHandle = () => ElNotification.success('执行计划成功')
  const failHandle = () => ElNotification.error('执行计划失败')
  await executeAsyncRequestAfterConfirm('提示', '执行计划将立即生成爬虫任务，请确实是否继续执行？',
    asyncExecutePlan, id, successHandle, failHandle)
}

watchEffect(() => search())
</script>

<template>
  <el-space direction="vertical" :size="20" :fill="true" style="min-width: 100%">
    <el-page-header @back="router.back()">
      <template #breadcrumb>
        <el-breadcrumb :separator-icon="ArrowRight">
          <el-breadcrumb-item :to="{ name: 'Home' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ name: 'PlanList' }">抓取计划</el-breadcrumb-item>
        </el-breadcrumb>
      </template>
      <template #content>
        <span class="text-large font-600 mr-3">计划列表</span>
      </template>
      <template #extra>
        <el-button :disabled="!userStore.injected" @click="openAddDialog = true">新增计划</el-button>
      </template>
    </el-page-header>
    <el-row style="min-width: 100%">
      <el-col :span="24">
        <el-form :model="query" ref="formRef" label-width="auto" style="max-width: 950px">
          <el-form-item v-if="userStore.injected" label="计划范围" prop="app_ids">
            <el-radio-group v-model="query.app_ids">
              <el-radio-button value="all">全部</el-radio-button>
              <el-radio-button :value="userStore.apps">我的计划</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-row>
            <el-col :span="8">
              <el-form-item label="计划状态" prop="status">
                <el-radio-group v-model="query.status">
                  <el-radio-button value="all">全部</el-radio-button>
                  <el-radio-button value="false">关闭</el-radio-button>
                  <el-radio-button value="true">开启</el-radio-button>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="16">
              <el-form-item label="抓取方式" prop="fetch_method">
                <el-radio-group v-model="query.fetch_method">
                  <el-radio-button value="all">全部</el-radio-button>
                  <el-radio-button value="0">本地IP</el-radio-button>
                  <el-radio-button value="1">代理IP</el-radio-button>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="8">
              <el-form-item label="计划类型" prop="category">
                <el-radio-group v-model="query.category">
                  <el-radio-button value="all">全部</el-radio-button>
                  <el-radio-button value="0">单次型</el-radio-button>
                  <el-radio-button value="1">周期型</el-radio-button>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="16">
              <el-form-item label="优先级" prop="priority">
                <el-radio-group v-model="query.priority">
                  <el-radio-button value="all">全部</el-radio-button>
                  <el-radio-button value="0">高优先级</el-radio-button>
                  <el-radio-button value="1">中优先级</el-radio-button>
                  <el-radio-button value="2">低优先级</el-radio-button>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="所属应用" prop="app_id">
            <el-col :span="15">
              <app-search v-model="query.app_id" placeholder="根据应用名称搜索"></app-search>
            </el-col>
          </el-form-item>
          <el-form-item label="计划搜索" prop="name">
            <el-col :span="15">
              <el-input v-model="query.name" clearable placeholder="根据计划名搜索" />
            </el-col>
            <el-col :span="1"></el-col>
            <el-col :span="8">
              <el-tooltip effect="dark" content="清除所有筛选条件" placement="right-end">
                <el-button @click="formRef.resetFields(); search()" :icon="Delete"></el-button>
              </el-tooltip>
            </el-col>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
    <el-table ref="tableRef" :data="plans" max-height="850" table-layout="auto"
              stripe @sort-change="event => fillSearchQuerySort(event, query)">
      <template #empty>没有计划数据</template>
      <el-table-column prop="name" label="计划名称" show-overflow-tooltip>
        <template #default="scope">
          {{ scope.row.name }}
        </template>
      </el-table-column>
      <el-table-column prop="category" label="类型" width="80" show-overflow-tooltip>
        <template #default="scope">
          <span v-if="scope.row.category === 0">单次型</span>
          <span v-else>周期型</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80" show-overflow-tooltip>
        <template #default="scope">
          <el-switch v-model="scope.row.status" @change="v => openClose(scope.row, v)"
                     style="--el-switch-on-color: #409eff; --el-switch-off-color: #8b8c8c"
                     inline-prompt size="large" active-text="开启" inactive-text="关闭" />
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="90" show-overflow-tooltip>
        <template #default="scope">
          <span v-if="scope.row.priority === 0">高优先级</span>
          <span v-else-if="scope.row.priority === 1">中优先级</span>
          <span v-else>低优先级</span>
        </template>
      </el-table-column>
      <el-table-column prop="fetch_method" label="抓取方式" width="100" show-overflow-tooltip>
        <template #default="scope">
          <span v-if="scope.row.fetch_method === 0">本地IP</span>
          <span v-else-if="scope.row.fetch_method === 1">代理IP</span>
          <span v-else>未知</span>
        </template>
      </el-table-column>
      <el-table-column prop="app_name" label="所属应用" show-overflow-tooltip>
        <template #default="scope">
          {{ scope.row.app_name }}
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="create_time" sortable="custom" show-overflow-tooltip>
        <template #default="scope">
          <el-icon><timer /></el-icon>
          {{ format(new Date(scope.row['create_time']), 'yyyy-MM-dd HH:mm:ss') }}
        </template>
      </el-table-column>
      <el-table-column label="修改时间" prop="update_time" sortable="custom" show-overflow-tooltip>
        <template #default="scope">
          <el-icon><timer /></el-icon>
          {{ format(new Date(scope.row['update_time']), 'yyyy-MM-dd HH:mm:ss') }}
        </template>
      </el-table-column>
      <el-table-column width="180" fixed="right">
        <template #header>
          操作
        </template>
        <template #default="scope">
          <el-link>
            <RouterLink :to="{ name: 'PlanPanel', query: { id: scope.row.plan_id } }">完善</RouterLink>
          </el-link>
          &nbsp;
          <el-link @click="execute(scope.row.plan_id)">执行</el-link>
          &nbsp;
          <el-link @click="remove(scope.row.plan_id)">删除</el-link>
        </template>
      </el-table-column>
    </el-table>
    <el-row justify="center" align="middle">
      <el-pagination background layout="prev, pager, next" :total="total"
                     v-model:page-size="query.size" v-model:current-page="query.current">
      </el-pagination>
    </el-row>
  </el-space>
  <add-plan v-model="openAddDialog" @close="search()"></add-plan>
</template>

<style scoped>
</style>