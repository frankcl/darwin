<script setup>
import { reactive, ref, useTemplateRef, watchEffect } from 'vue'
import {
  ElButton, ElCol, ElForm, ElFormItem, ElIcon,
  ElPagination, ElRadioButton, ElRadioGroup,
  ElRow, ElSpace, ElTable, ElTableColumn, ElTooltip
} from 'element-plus'
import { Delete, Timer } from '@element-plus/icons-vue'
import { useUserStore } from '@/store'
import { formatDate } from '@/common/Time'
import {
  asyncCheckProxy,
  asyncRemoveProxy,
  asyncSearchProxy,
  changeSearchQuerySort,
  newSearchQuery,
  newSearchRequest
} from '@/common/AsyncRequest'
import { proxyCategoryMap } from '@/common/Constants'
import {
  asyncExecuteAfterConfirming,
  ERROR, showMessage, SUCCESS
} from '@/common/Feedback'
import AddProxy from '@/views/proxy/AddProxy'
import EditProxy from '@/views/proxy/EditProxy'

const userStore = useUserStore()
const filterFormRef = useTemplateRef('filterForm')
const openAddDialog = ref(false)
const openEditDialog = ref(false)
const proxyId = ref()
const proxies = ref([])
const checking = ref()
const total = ref(0)
const query = reactive(newSearchQuery({
  category: 'all',
  expired: 'all',
  sort_field: 'update_time',
  sort_order: 'descending'
}))

const search = async () => {
  const request = newSearchRequest(query)
  if (query.category && query.category !== 'all') request.category = query.category
  if (query.expired && query.expired !== 'all') request.expired = query.expired
  const pager = await asyncSearchProxy(request)
  total.value = pager.total
  proxies.value = pager.records
}

const edit = id => {
  proxyId.value = id
  openEditDialog.value = true
}

const remove = async id => {
  const success = await asyncExecuteAfterConfirming(asyncRemoveProxy, id)
  if (success === undefined) return
  if (!success) {
    showMessage('删除代理失败', ERROR)
    return
  }
  showMessage('删除代理成功', SUCCESS)
  await search()
}

const check = async proxy => {
  checking.value = proxy.id
  try {
    if (await asyncCheckProxy(proxy.id)) {
      showMessage(`代理: ${proxy.address}:${proxy.port} 有效`, SUCCESS)
      return
    }
    showMessage(`代理: ${proxy.address}:${proxy.port} 无效`, ERROR)
  } finally {
    checking.value = undefined
  }
}

watchEffect(() => search())
</script>

<template>
  <el-space direction="vertical" :size="20" :fill="true" class="w100">
    <el-form :model="query" ref="filterForm" label-width="80px" class="w100">
      <el-row :gutter="20">
        <el-col :span="7">
          <el-form-item label="有效性" prop="expired">
            <el-radio-group v-model="query.expired">
              <el-radio-button value="all">全部</el-radio-button>
              <el-radio-button value="false">有效</el-radio-button>
              <el-radio-button value="true">过期</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="类型" prop="category">
            <el-radio-group v-model="query.category">
              <el-radio-button value="all">全部</el-radio-button>
              <el-radio-button value="1">长期代理</el-radio-button>
              <el-radio-button value="2">短期代理</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="2">
          <el-tooltip effect="dark" content="清除所有筛选条件" placement="right-end">
            <el-button @click="filterFormRef.resetFields()" :icon="Delete" />
          </el-tooltip>
        </el-col>
      </el-row>
    </el-form>
    <el-row align="middle">
      <el-col :span="12">
        <span class="text-xl font-bold ml-2">代理列表</span>
      </el-col>
      <el-col :span="12">
        <el-row justify="end">
          <el-button type="primary" @click="openAddDialog = true" :disabled="!userStore.superAdmin">新增代理</el-button>
        </el-row>
      </el-col>
    </el-row>
    <el-table :data="proxies" max-height="850" table-layout="auto"
              stripe @sort-change="event => changeSearchQuerySort(event.prop, event.order, query)">
      <template #empty>暂无代理数据</template>
      <el-table-column prop="address" label="代理IP" width="150" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.address }}</template>
      </el-table-column>
      <el-table-column prop="port" label="代理端口" width="150" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.port }}</template>
      </el-table-column>
      <el-table-column prop="category" label="类型" width="120" show-overflow-tooltip>
        <template #default="scope">{{ proxyCategoryMap[scope.row.category] }}</template>
      </el-table-column>
      <el-table-column label="更新时间" prop="update_time" sortable="custom" show-overflow-tooltip>
        <template #default="scope">
          <el-icon><timer /></el-icon>
          {{ formatDate(scope.row['update_time']) }}
        </template>
      </el-table-column>
      <el-table-column label="过期时间" prop="expired_time" sortable="custom" show-overflow-tooltip>
        <template #default="scope">
          <el-icon><timer /></el-icon>
          {{ formatDate(scope.row['expired_time']) }}
        </template>
      </el-table-column>
      <el-table-column width="250">
        <template #header>操作</template>
        <template #default="scope">
          <el-button type="primary" @click="edit(scope.row.id)" :disabled="!userStore.superAdmin">编辑</el-button>
          <el-button type="success" @click="check(scope.row)"
                     :loading="checking === scope.row.id" :disabled="!userStore.superAdmin">检测</el-button>
          <el-button type="danger" @click="remove(scope.row.id)" :disabled="!userStore.superAdmin">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-row justify="center" align="middle">
      <el-pagination background layout="prev, pager, next" :total="total"
                     v-model:page-size="query.size" v-model:current-page="query.current">
      </el-pagination>
    </el-row>
  </el-space>
  <add-proxy v-model="openAddDialog" @close="search" />
  <edit-proxy v-model="openEditDialog" :id="proxyId" @close="search" />
</template>

<style scoped>
</style>