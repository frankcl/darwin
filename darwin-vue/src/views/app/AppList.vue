<script setup>
import { format } from 'date-fns'
import { reactive, ref, useTemplateRef, watchEffect } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, Timer } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElCol, ElForm, ElFormItem,
  ElIcon, ElInput, ElLink, ElNotification, ElPageHeader, ElPagination, ElRadioButton,
  ElRadioGroup, ElRow, ElSpace, ElTable, ElTableColumn
} from 'element-plus'
import { useUserStore } from '@/store'
import {
  checkUserLogin,
  executeAsyncRequestAfterConfirm,
  fillSearchQuerySort,
  searchQueryToRequest
} from '@/common/assortment'
import { asyncDeleteApp, asyncSearchApps } from '@/common/service'
import AddApp from '@/views/app/AddApp'
import AppUser from '@/views/app/AppUser'
import EditApp from '@/views/app/EditApp'

const router = useRouter()
const tableRef = useTemplateRef('tableRef')
const userStore = useUserStore()
const apps = ref([])
const total = ref(0)
const openAddDialog = ref(false)
const openEditDialog = ref(false)
const openAppUserDialog = ref(false)
const appId = ref()
const app = reactive({})
const query = reactive({
  current: 1,
  size: 10,
  name: null,
  app_ids: 'all',
  sort_field: null,
  sort_order: null
})

const search = async () => {
  const request = searchQueryToRequest(query)
  if (query.name) request.name = query.name
  if (query.app_ids && query.app_ids !== 'all') request.app_ids = query.app_ids
  const pager = await asyncSearchApps(request)
  total.value = pager.total
  apps.value = pager.records
}

const remove = async id => {
  if (!checkUserLogin()) return
  const successHandle = () => ElNotification.success('删除应用成功')
  const failHandle = () => ElNotification.success('删除应用失败')
  if (!await executeAsyncRequestAfterConfirm('删除提示', '是否确定删除应用信息？',
    asyncDeleteApp, id, successHandle, failHandle)) return
  await search()
  await userStore.fillApps()
}

const member = row => {
  if (!checkUserLogin()) return
  app.id = row.id
  app.name = row.name
  openAppUserDialog.value = true
}

const edit = id => {
  if (!checkUserLogin()) return
  appId.value = id
  openEditDialog.value = true
}

watchEffect(() => search())
</script>

<template>
  <el-space direction="vertical" :size="20" :fill="true" style="min-width: 100%">
    <el-page-header @back="router.back()">
      <template #breadcrumb>
        <el-breadcrumb :separator-icon="ArrowRight">
          <el-breadcrumb-item :to="{ name: 'Home' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ name: 'AppList' }">爬虫应用</el-breadcrumb-item>
        </el-breadcrumb>
      </template>
      <template #content>
        <span class="text-large font-600">应用列表</span>
      </template>
      <template #extra>
        <el-button :disabled="!userStore.injected" @click="openAddDialog = true">新增应用</el-button>
      </template>
    </el-page-header>
    <el-row style="min-width: 100%">
      <el-col :span="24">
        <el-form :model="query" ref="formRef" label-width="auto" style="max-width: 400px">
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
      </el-col>
    </el-row>
    <el-table ref="tableRef" :data="apps" max-height="850" table-layout="auto"
              stripe @sort-change="event => fillSearchQuerySort(event, query)">
      <template #empty>没有应用数据</template>
      <el-table-column prop="name" label="应用名" show-overflow-tooltip>
        <template #default="scope">
          {{ scope.row.name }}
        </template>
      </el-table-column>
      <el-table-column prop="creator" label="创建人" width="100" show-overflow-tooltip>
        <template #default="scope">
          {{ scope.row.creator }}
        </template>
      </el-table-column>
      <el-table-column prop="modifier" label="修改人" width="100" show-overflow-tooltip>
        <template #default="scope">
          {{ scope.row.modifier }}
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
          应用操作
        </template>
        <template #default="scope">
          <el-link @click="member(scope.row)">应用成员</el-link>&nbsp;
          <el-link @click="edit(scope.row.id)">编辑</el-link>&nbsp;
          <el-link @click="remove(scope.row.id)">删除</el-link>
        </template>
      </el-table-column>
    </el-table>
    <el-row justify="center" align="middle">
      <el-pagination background layout="prev, pager, next" :total="total"
                     v-model:page-size="query.size" v-model:current-page="query.current" />
    </el-row>
  </el-space>
  <add-app v-model="openAddDialog" @close="search(); userStore.fillApps()"></add-app>
  <edit-app v-model="openEditDialog" :id="appId" @close="search()"></edit-app>
  <app-user v-model="openAppUserDialog" v-bind="app" @close="search(); userStore.fillApps()"></app-user>
</template>

<style scoped>
</style>