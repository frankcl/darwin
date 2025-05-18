<script setup>
import { IconClock, IconEdit, IconPlus, IconTrash, IconUsersGroup } from '@tabler/icons-vue'
import { reactive, ref, watchEffect } from 'vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElConfigProvider,
  ElForm, ElFormItem, ElInput, ElLoading, ElPagination, ElRadioButton,
  ElRadioGroup, ElRow, ElTable, ElTableColumn
} from 'element-plus'
import { useUserStore } from '@/store'
import { formatDate } from '@/common/Time'
import {
  asyncRemoveApp,
  asyncResetUserApps,
  asyncSearchApp,
  changeSearchQuerySort,
  newSearchQuery,
  newSearchRequest
} from '@/common/AsyncRequest'
import {
  asyncExecuteAfterConfirming,
  ERROR, showMessage, SUCCESS
} from '@/common/Feedback'
import DarwinCard from '@/components/data/Card'
import TableHead from '@/components/data/TableHead'
import AddApp from '@/views/app/AddApp'
import AppUser from '@/views/app/AppUser'
import EditApp from '@/views/app/EditApp'

const userStore = useUserStore()
const vLoading = ElLoading.directive
const apps = ref([])
const total = ref(0)
const loading = ref(true)
const openAddDialog = ref(false)
const openEditDialog = ref(false)
const openAppUserDialog = ref(false)
const app = reactive({})
const query = reactive(newSearchQuery({ app_ids: 'all' }))

const refresh = async () => {
  await asyncResetUserApps()
  await search()
}

const search = async () => {
  loading.value = true
  const request = newSearchRequest(query)
  if (query.name) request.name = query.name
  if (query.app_ids && query.app_ids !== 'all') request.app_ids = query.app_ids
  const pager = await asyncSearchApp(request)
  total.value = pager.total
  apps.value = pager.records
  loading.value = false
}

const openAdd = () => openAddDialog.value = true

const remove = async id => {
  const success = await asyncExecuteAfterConfirming(asyncRemoveApp, id)
  if (success === undefined) return
  if (!success) {
    showMessage('删除应用失败', ERROR)
    return
  }
  showMessage('删除应用成功', SUCCESS)
  await refresh()
}

const updateAppUser = row => {
  app.id = row.id
  app.name = row.name
  openAppUserDialog.value = true
}

const update = id => {
  app.id = id
  openEditDialog.value = true
}

watchEffect(() => search())
</script>

<template>
  <darwin-card>
    <template #title>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>基础功能</el-breadcrumb-item>
        <el-breadcrumb-item>爬虫应用</el-breadcrumb-item>
      </el-breadcrumb>
    </template>
    <el-form :model="query" label-width="80px" class="mw-50p mb-4">
      <el-form-item label="应用范围" prop="app_ids">
        <el-radio-group v-model="query.app_ids" :disabled="!userStore.injected">
          <el-radio-button value="all">全部应用</el-radio-button>
          <el-radio-button :value="userStore.apps">我的应用</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="应用搜索" prop="name">
        <el-input v-model="query.name" clearable placeholder="根据应用名搜索" />
      </el-form-item>
    </el-form>
    <table-head title="应用列表">
      <template #right>
        <el-button type="primary" @click="openAdd" :disabled="!userStore.injected">
          <IconPlus size="20" class="mr-1" />
          <span>新增</span>
        </el-button>
      </template>
    </table-head>
    <el-table ref="appTable" :data="apps" max-height="600" class="w-100p mb-4" v-loading="loading"
              stripe @sort-change="event => changeSearchQuerySort(event.field, event.order, query)">
      <template #empty>没有应用数据</template>
      <el-table-column prop="name" label="应用名" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.name }}</template>
      </el-table-column>
      <el-table-column prop="creator" label="创建人" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.creator }}</template>
      </el-table-column>
      <el-table-column label="创建时间" prop="create_time" sortable="custom" show-overflow-tooltip>
        <template #default="scope">
          <div class="d-flex align-items-center">
            <IconClock size="16" class="mr-1" />
            <span>{{ formatDate(scope.row['create_time']) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="应用说明" prop="comment" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.comment }}</template>
      </el-table-column>
      <el-table-column width="300">
        <template #header>操作</template>
        <template #default="scope">
          <el-button type="primary" @click="update(scope.row.id)" :disabled="!userStore.injected">
            <IconEdit size="20" class="mr-1" />
            <span>编辑</span>
          </el-button>
          <el-button type="primary" plain @click="updateAppUser(scope.row)">
            <IconUsersGroup size="20" class="mr-1" />
            <span>成员</span>
          </el-button>
          <el-button type="danger" @click="remove(scope.row.id)" :disabled="!userStore.injected">
            <IconTrash size="20" class="mr-1" />
            <span>删除</span>
          </el-button>
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
  <add-app v-model="openAddDialog" @close="refresh" />
  <edit-app v-model="openEditDialog" :id="app.id" @close="search" />
  <app-user v-model="openAppUserDialog" v-bind="app" @close="refresh" />
</template>

<style scoped>
</style>