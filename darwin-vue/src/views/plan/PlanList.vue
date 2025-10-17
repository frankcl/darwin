<script setup>
import {
  IconClearAll, IconFileDescription, IconDatabase, IconPlayerPlay,
  IconPlus, IconSpider, IconTrash, IconChevronDown, IconChevronUp, IconCopy, IconCopyCheck
} from '@tabler/icons-vue'
import { reactive, ref, useTemplateRef, watchEffect } from 'vue'
import { useRouter } from 'vue-router'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElCol, ElConfigProvider,
  ElDropdown, ElDropdownMenu, ElDropdownItem, ElForm, ElFormItem, ElInput, ElPagination,
  ElRadioButton, ElRadioGroup, ElRow, ElSwitch, ElTable, ElTableColumn, ElLoading
} from 'element-plus'
import { useUserStore } from '@/store'
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
import { planCategoryMap } from '@/common/Constants'
import { writeClipboard } from '@/common/Clipboard'
import { asyncExecuteAfterConfirming, ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import DarwinCard from '@/components/data/Card'
import TableHead from '@/components/data/TableHead'
import AppSearch from '@/components/app/AppSearch'
import AddPlan from '@/views/plan/AddPlan'

const router = useRouter()
const userStore = useUserStore()
const formRef = useTemplateRef('form')
const vLoading = ElLoading.directive
const loading = ref(true)
const showMore = ref(false)
const openAddDialog = ref(false)
const executing = ref()
const copiedID = ref()
const plans = ref([])
const total = ref(0)
const query = reactive(newSearchQuery({
  app_ids: 'all',
  category: 'all',
  status: 'all'
}))

const copy = async plan => {
  await writeClipboard(plan.plan_id)
  copiedID.value = `ID#${plan.plan_id}`
  showMessage('复制计划ID成功', SUCCESS)
}

const search = async () => {
  loading.value = true
  const request = newSearchRequest(query)
  if (query.name) request.name = query.name
  if (query.app_id) request.app_id = query.app_id
  if (query.category !== undefined && query.category !== 'all') request.category = query.category
  if (query.status && query.status !== 'all') request.status = query.status
  if (query.app_ids && query.app_ids !== 'all') request.app_ids = query.app_ids
  const pager = await asyncSearchPlan(request)
  total.value = pager.total
  plans.value = pager.records
  loading.value = false
}

const openAdd = () => openAddDialog.value = true

const view = plan => router.push({ path: '/plan/tabs', query: { plan_id: plan.plan_id, name: plan.name }})

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

const openClose = async plan => {
  try {
    const operation = plan.status ? '开启' : '关闭'
    const asyncExecuteFunction = plan.status ? asyncOpenPlan : asyncClosePlan
    if (await asyncExecuteFunction(plan.plan_id)) showMessage(`${operation}计划成功`, SUCCESS)
    else showMessage(`${operation}计划失败`, ERROR)
  } catch {
    plan.status = !plan.status
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

const handleCommand = async (command, plan) => {
  if (command === 'remove') {
    await remove(plan.plan_id)
  } else if (command === 'jobList') {
    await router.push({ path: '/job/search', query: { plan_id: plan.plan_id, name: plan.name } })
  } else if (command === 'recordList') {
    await router.push({ path: '/record/search', query: { plan_id: plan.plan_id } })
  }
}

watchEffect(() => search())
</script>

<template>
  <darwin-card>
    <template #title>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>基础功能</el-breadcrumb-item>
        <el-breadcrumb-item>爬虫计划</el-breadcrumb-item>
      </el-breadcrumb>
    </template>
    <el-form :model="query" ref="form" label-width="80px" class="mb-4">
      <el-row :gutter="20">
        <el-col :span="10">
          <el-form-item label="计划范围" prop="app_ids">
            <el-radio-group v-model="query.app_ids" :disabled="!userStore.injected">
              <el-radio-button value="all">全部计划</el-radio-button>
              <el-radio-button :value="userStore.apps">我的计划</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="10">
          <el-form-item label="计划状态" prop="status">
            <el-radio-group v-model="query.status">
              <el-radio-button value="all">全部</el-radio-button>
              <el-radio-button value="true">开启</el-radio-button>
              <el-radio-button value="false">关闭</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="2">
          <el-button type="primary" plain @click="formRef.resetFields()">
            <IconClearAll size="20" class="mr-1" />
            <span>清除筛选</span>
          </el-button>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="10">
          <el-form-item label="计划类型" prop="category">
            <el-radio-group v-model="query.category">
              <el-radio-button value="all">全部</el-radio-button>
              <el-radio-button v-for="key in Object.keys(planCategoryMap)" :value="parseInt(key)" :key="key">
                {{ planCategoryMap[key] }}
              </el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="10">
          <el-form-item label="所属应用" prop="app_id">
            <app-search v-model="query.app_id" placeholder="根据应用名搜索" />
          </el-form-item>
        </el-col>
        <el-col v-if="!showMore" :span="2">
          <el-button type="primary" plain @click="showMore = !showMore">
            <IconChevronDown size="20" class="mr-1" />显示更多
          </el-button>
        </el-col>
      </el-row>
      <el-row v-if="showMore" :gutter="20">
        <el-col :span="20">
          <el-form-item label="搜索计划" prop="name">
            <el-input v-model="query.name" clearable placeholder="根据计划名搜索" />
          </el-form-item>
        </el-col>
        <el-col v-if="showMore" :span="2">
          <el-button type="primary" plain @click="showMore = !showMore">
            <IconChevronUp size="20" class="mr-1" />隐藏更多
          </el-button>
        </el-col>
      </el-row>
    </el-form>
    <table-head title="计划列表">
      <template #right>
        <el-button type="primary" @click="openAdd" :disabled="!userStore.injected">
          <IconPlus size="20" class="mr-1" />
          <span>新增</span>
        </el-button>
      </template>
    </table-head>
    <el-table :data="plans" max-height="650" table-layout="auto" stripe class="w-100p mb-4" v-loading="loading"
              @sort-change="event => changeSearchQuerySort(event.prop, event.order, query)">
      <template #empty>暂无计划数据</template>
      <el-table-column prop="name" label="计划名" show-overflow-tooltip>
        <template #default="scope">
          <span class="d-flex align-items-center">
            <IconCopyCheck v-if="copiedID === `ID#${scope.row.plan_id}`" class="flex-shrink-0"  size="16" />
            <IconCopy v-else class="flex-shrink-0" @click="copy(scope.row)" size="16" />
            <span class="ml-2">{{ scope.row.name }}</span>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="类型" min-width="80" show-overflow-tooltip>
        <template #default="scope">{{ planCategoryMap[scope.row.category] }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" min-width="80" show-overflow-tooltip>
        <template #default="scope">
          <el-switch v-model="scope.row.status" @change="openClose(scope.row)"
                     style="--el-switch-on-color: #409eff; --el-switch-off-color: #8b8c8c"
                     inline-prompt size="large" active-text="开启" inactive-text="关闭" />
        </template>
      </el-table-column>
      <el-table-column prop="modifier" label="变更人" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.modifier }}</template>
      </el-table-column>
      <el-table-column prop="app_name" label="所属应用" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.app_name }}</template>
      </el-table-column>
      <el-table-column width="330">
        <template #header>操作</template>
        <template #default="scope">
          <el-button type="primary" plain @click="view(scope.row)">
            <IconFileDescription size="20" class="mr-1" />
            <span>查看</span>
          </el-button>
          <el-button type="success" @click="execute(scope.row.plan_id)"
                     :loading="executing === scope.row.plan_id" :disabled="!userStore.injected">
            <IconPlayerPlay size="20" class="mr-1" />
            <span>执行</span>
          </el-button>
          <el-dropdown trigger="click" placement="bottom-end" style="margin-left: 12px"
                       @command="c => handleCommand(c, scope.row)">
            <el-button type="primary">
              <span>更多操作</span>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="jobList">
                  <IconSpider size="20" class="mr-2" />
                  <span>爬虫任务</span>
                </el-dropdown-item>
                <el-dropdown-item command="recordList">
                  <IconDatabase size="20" class="mr-2" />
                  <span>抓取数据</span>
                </el-dropdown-item>
                <el-dropdown-item command="remove" >
                  <IconTrash size="20" class="mr-2" />
                  <span>删除计划</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>
    <el-row justify="center" align="middle">
      <el-config-provider :locale="zhCn">
        <el-pagination background layout="total, prev, pager, next, jumper" :total="total"
                       v-model:page-size="query.page_size" v-model:current-page="query.page_num">
        </el-pagination>
      </el-config-provider>
    </el-row>
  </darwin-card>
  <add-plan v-model="openAddDialog" @close="search" />
</template>

<style scoped>
</style>