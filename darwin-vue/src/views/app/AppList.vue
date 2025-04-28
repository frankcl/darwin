<script setup>
import { reactive, ref, watchEffect } from 'vue'
import { Timer } from '@element-plus/icons-vue'
import {
  ElButton, ElCol, ElForm, ElFormItem,
  ElIcon, ElInput, ElPagination, ElRadioButton,
  ElRadioGroup, ElRow, ElSpace, ElTable, ElTableColumn
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
import AddApp from '@/views/app/AddApp'
import AppUser from '@/views/app/AppUser'
import EditApp from '@/views/app/EditApp'


const userStore = useUserStore()
const apps = ref([])
const total = ref(0)
const openAddDialog = ref(false)
const openEditDialog = ref(false)
const openAppUserDialog = ref(false)
const appId = ref()
const app = reactive({})
const query = reactive(newSearchQuery({ app_ids: 'all' }))

const refresh = async () => {
  await asyncResetUserApps()
  await search()
}

const search = async () => {
  const request = newSearchRequest(query)
  if (query.name) request.name = query.name
  if (query.app_ids && query.app_ids !== 'all') request.app_ids = query.app_ids
  const pager = await asyncSearchApp(request)
  total.value = pager.total
  apps.value = pager.records
}

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
  appId.value = id
  openEditDialog.value = true
}

watchEffect(() => search())
</script>

<template>
  <el-space direction="vertical" :size="20" :fill="true" class="w100">
    <el-form :model="query" label-width="80px" class="mw400px">
      <el-form-item v-if="userStore.injected" label="应用范围" prop="app_ids">
        <el-radio-group v-model="query.app_ids">
          <el-radio-button value="all">全部应用</el-radio-button>
          <el-radio-button :value="userStore.apps">我的应用</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="应用搜索" prop="name">
        <el-input v-model="query.name" clearable placeholder="根据应用名搜索" />
      </el-form-item>
    </el-form>
    <el-row align="middle">
      <el-col :span="12">
        <span class="text-xl font-bold ml-2">应用列表</span>
      </el-col>
      <el-col :span="12">
        <el-row justify="end">
          <el-button type="primary" @click="openAddDialog = true" :disabled="!userStore.injected">新增应用</el-button>
        </el-row>
      </el-col>
    </el-row>
    <el-table ref="appTable" :data="apps" max-height="850" table-layout="auto"
              stripe @sort-change="event => changeSearchQuerySort(event.field, event.order, query)">
      <template #empty>没有应用数据</template>
      <el-table-column prop="name" label="应用名" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.name }}</template>
      </el-table-column>
      <el-table-column prop="creator" label="创建人" width="100" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.creator }}</template>
      </el-table-column>
      <el-table-column label="创建时间" prop="create_time" sortable="custom" show-overflow-tooltip>
        <template #default="scope">
          <el-icon><timer /></el-icon>
          {{ formatDate(scope.row['create_time']) }}
        </template>
      </el-table-column>
      <el-table-column label="应用说明" prop="comment" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.comment }}</template>
      </el-table-column>
      <el-table-column width="230">
        <template #header>操作</template>
        <template #default="scope">
          <el-button type="primary" @click="update(scope.row.id)" :disabled="!userStore.injected">编辑</el-button>
          <el-button type="primary" plain @click="updateAppUser(scope.row)">成员</el-button>
          <el-button type="danger" @click="remove(scope.row.id)" :disabled="!userStore.injected">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-row justify="center" align="middle">
      <el-pagination background layout="prev, pager, next" :total="total"
                     v-model:page-size="query.page_size"
                     v-model:current-page="query.page_num" />
    </el-row>
  </el-space>
  <add-app v-model="openAddDialog" @close="refresh" />
  <edit-app v-model="openEditDialog" :id="appId" @close="search" />
  <app-user v-model="openAppUserDialog" v-bind="app" @close="refresh" />
</template>

<style scoped>
</style>