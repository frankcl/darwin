<script setup>
import { IconClock, IconEdit, IconPlus, IconTrash } from '@tabler/icons-vue'
import { reactive, ref, watchEffect } from 'vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElConfigProvider,
  ElForm, ElFormItem, ElLoading, ElPagination,
  ElRow, ElTable, ElTableColumn
} from 'element-plus'
import { useUserStore } from '@/store'
import { formatDate } from '@/common/Time'
import {
  asyncRemoveAppSecret,
  asyncSearchAppSecret,
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
import AddAppSecret from '@/views/app_secret/AddAppSecret'
import EditAppSecret from '@/views/app_secret/EditAppSecret'
import AppSearch from '@/components/app/AppSearch'

const userStore = useUserStore()
const vLoading = ElLoading.directive
const appSecrets = ref([])
const total = ref(0)
const loading = ref(true)
const openAddDialog = ref(false)
const openEditDialog = ref(false)
const appSecret = reactive({})
const query = reactive(newSearchQuery({}))

const search = async () => {
  loading.value = true
  const request = newSearchRequest(query)
  if (query.app_id) request.app_id = query.app_id
  const pager = await asyncSearchAppSecret(request)
  total.value = pager.total
  appSecrets.value = pager.records
  loading.value = false
}

const openAdd = () => openAddDialog.value = true

const remove = async id => {
  const success = await asyncExecuteAfterConfirming(asyncRemoveAppSecret, id)
  if (success === undefined) return
  if (!success) {
    showMessage('删除应用秘钥失败', ERROR)
    return
  }
  showMessage('删除应用秘钥成功', SUCCESS)
  await search()
}

const update = id => {
  appSecret.id = id
  openEditDialog.value = true
}

watchEffect(() => search())
</script>

<template>
  <darwin-card>
    <template #title>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>平台管理</el-breadcrumb-item>
        <el-breadcrumb-item>应用秘钥</el-breadcrumb-item>
      </el-breadcrumb>
    </template>
    <el-form :model="query" label-width="80px" class="mw-50p mb-4">
      <el-form-item label="所属应用" prop="app_id">
        <app-search v-model="query.app_id" placeholder="根据应用名搜索" />
      </el-form-item>
    </el-form>
    <table-head title="应用秘钥列表">
      <template #right>
        <el-button type="primary" @click="openAdd" :disabled="!userStore.superAdmin">
          <IconPlus size="20" class="mr-1" />
          <span>新增</span>
        </el-button>
      </template>
    </table-head>
    <el-table ref="appSecretTable" :data="appSecrets" max-height="600" class="w-100p mb-4" v-loading="loading"
              stripe @sort-change="event => changeSearchQuerySort(event.prop, event.order, query)">
      <template #empty>没有应用秘钥</template>
      <el-table-column prop="name" label="秘钥名称" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.name }}</template>
      </el-table-column>
      <el-table-column prop="app_name" label="所属应用" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.app_name }}</template>
      </el-table-column>
      <el-table-column label="创建时间" prop="create_time" sortable="custom" show-overflow-tooltip>
        <template #default="scope">
          <div class="d-flex align-items-center">
            <IconClock size="16" class="mr-1" />
            <span>{{ formatDate(scope.row['create_time']) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="AccessKey" prop="access_key" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.access_key }}</template>
      </el-table-column>
      <el-table-column label="SecretKey" prop="secret_key" show-overflow-tooltip>
        <span>**********</span>
      </el-table-column>
      <el-table-column width="220">
        <template #header>操作</template>
        <template #default="scope">
          <el-button type="primary" @click="update(scope.row.id)" :disabled="!userStore.superAdmin">
            <IconEdit size="20" class="mr-1" />
            <span>编辑</span>
          </el-button>
          <el-button type="danger" @click="remove(scope.row.id)" :disabled="!userStore.superAdmin">
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
  <add-app-secret v-model="openAddDialog" @close="search" />
  <edit-app-secret v-model="openEditDialog" :id="appSecret.id" @close="search" />
</template>

<style scoped>
</style>