<script setup>
import { reactive, ref, useTemplateRef, watchEffect } from 'vue'
import { useRouter } from 'vue-router'
import { Delete, Timer } from '@element-plus/icons-vue'
import {
  ElButton, ElCol, ElForm, ElFormItem, ElIcon, ElInput,
  ElPagination, ElRadioButton, ElRadioGroup, ElRow,
  ElSpace, ElSwitch, ElTable, ElTableColumn, ElTooltip
} from 'element-plus'
import { useUserStore } from '@/store'
import { formatDate } from '@/common/Time'
import {
  asyncClosePlan,
  asyncExecutePlan,
  asyncOpenPlan,
  asyncRemovePlan,
  asyncSearchPlan,
  changeSearchQuerySort,
  newSearchQuery,
  newSearchRequest
} from '@/common/AsyncRequest'
import {
  asyncExecuteAfterConfirming,
  ERROR, showMessage, SUCCESS
} from '@/common/Feedback'
import AppSearch from '@/components/app/AppSearch'
import AddPlan from '@/views/plan/AddPlan'

const router = useRouter()
const userStore = useUserStore()
const filterFormRef = useTemplateRef('filterForm')
const openAddDialog = ref(false)
const executing = ref()
const plans = ref([])
const total = ref(0)
const query = reactive(newSearchQuery({
  app_ids: 'all',
  category: 'all',
  priority: 'all',
  fetch_method: 'all',
  status: 'all'
}))

const search = async () => {
  const request = newSearchRequest(query)
  if (query.name) request.name = query.name
  if (query.app_id) request.app_id = query.app_id
  if (query.category && query.category !== 'all') request.category = query.category
  if (query.priority && query.priority !== 'all') request.priority = query.priority
  if (query.status && query.status !== 'all') request.status = query.status
  if (query.fetch_method && query.fetch_method !== 'all') request.fetch_method = query.fetch_method
  if (query.app_ids && query.app_ids !== 'all') request.app_ids = query.app_ids
  const pager = await asyncSearchPlan(request)
  total.value = pager.total
  plans.value = pager.records
}

const edit = id => router.push({ path: '/plan/tabs', query: { id: id }})

const remove = async id => {
  const success = await asyncExecuteAfterConfirming(asyncRemovePlan, id)
  if (success === undefined) return
  if (!success) {
    showMessage('删除计划失败', ERROR)
    return
  }
  showMessage('删除计划成功', SUCCESS)
  await search()
}

const openClose = async record => {
  try {
    const operation = record.status ? '开启' : '关闭'
    const asyncExecuteFunction = record.status ? asyncOpenPlan : asyncClosePlan
    if (await asyncExecuteFunction(record.plan_id)) showMessage(`${operation}计划成功`, SUCCESS)
    else showMessage(`${operation}计划失败`, ERROR)
  } catch (e) {
    record.status = !record.status
  }
}

const execute = async id => {
  executing.value = id
  try {
    const success = await asyncExecuteAfterConfirming(asyncExecutePlan, id)
    if (success === undefined) return
    if (!success) {
      showMessage('执行计划失败', ERROR)
      return
    }
    showMessage('执行计划成功', SUCCESS)
  } finally {
    executing.value = undefined
  }
}

watchEffect(() => search())
</script>

<template>
  <el-space direction="vertical" :size="20" :fill="true" class="w100">
    <el-form :model="query" ref="filterForm" label-width="80px" class="w100">
      <el-form-item v-if="userStore.injected" label="计划范围" prop="app_ids">
        <el-radio-group v-model="query.app_ids">
          <el-radio-button value="all">全部</el-radio-button>
          <el-radio-button :value="userStore.apps">我的计划</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-row>
        <el-col :span="10">
          <el-form-item label="计划状态" prop="status">
            <el-radio-group v-model="query.status">
              <el-radio-button value="all">全部</el-radio-button>
              <el-radio-button value="false">关闭</el-radio-button>
              <el-radio-button value="true">开启</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
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
        <el-col :span="10">
          <el-form-item label="计划类型" prop="category">
            <el-radio-group v-model="query.category">
              <el-radio-button value="all">全部</el-radio-button>
              <el-radio-button value="0">单次型</el-radio-button>
              <el-radio-button value="1">周期型</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
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
      <el-row>
        <el-col :span="10">
          <el-form-item label="所属应用" prop="app_id">
            <el-col :span="20">
              <app-search v-model="query.app_id" placeholder="根据应用名搜索" />
            </el-col>
          </el-form-item>
        </el-col>
        <el-col :span="11">
          <el-form-item label="搜索计划" prop="name">
            <el-input v-model="query.name" clearable placeholder="根据计划名搜索" />
          </el-form-item>
        </el-col>
        <el-col :span="1"></el-col>
        <el-col :span="2">
          <el-tooltip effect="dark" content="清除所有筛选条件" placement="right-end">
            <el-button @click="filterFormRef.resetFields()" :icon="Delete" />
          </el-tooltip>
        </el-col>
      </el-row>
    </el-form>
    <el-row align="middle">
      <el-col :span="12">
        <span class="text-xl font-bold ml-2">计划列表</span>
      </el-col>
      <el-col :span="12">
        <el-row justify="end">
          <el-button type="primary" @click="openAddDialog = true" :disabled="!userStore.injected">新增计划</el-button>
        </el-row>
      </el-col>
    </el-row>
    <el-table :data="plans" max-height="850" table-layout="auto"
              stripe @sort-change="event => changeSearchQuerySort(event.prop, event.order, query)">
      <template #empty>暂无计划数据</template>
      <el-table-column prop="name" label="计划名" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.name }}</template>
      </el-table-column>
      <el-table-column prop="category" label="类型" width="80" show-overflow-tooltip>
        <template #default="scope">
          <span v-if="scope.row.category === 0">单次型</span>
          <span v-else>周期型</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80" show-overflow-tooltip>
        <template #default="scope">
          <el-switch v-model="scope.row.status" @change="openClose(scope.row)"
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
        <template #default="scope">{{ scope.row.app_name }}</template>
      </el-table-column>
      <el-table-column label="创建时间" prop="create_time" sortable="custom" width="200" show-overflow-tooltip>
        <template #default="scope">
          <el-icon><timer /></el-icon>
          {{ formatDate(scope.row['create_time']) }}
        </template>
      </el-table-column>
      <el-table-column width="250">
        <template #header>操作</template>
        <template #default="scope">
          <el-button type="primary" @click="edit(scope.row.plan_id)">编辑</el-button>
          <el-button type="success" @click="execute(scope.row.plan_id)"
                     :loading="executing === scope.row.plan_id" :disabled="!userStore.injected">执行</el-button>
          <el-button type="danger" @click="remove(scope.row.plan_id)" :disabled="!userStore.injected">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-row justify="center" align="middle">
      <el-pagination background layout="prev, pager, next" :total="total"
                     v-model:page-size="query.size" v-model:current-page="query.current">
      </el-pagination>
    </el-row>
  </el-space>
  <add-plan v-model="openAddDialog" @close="search" />
</template>

<style scoped>
</style>