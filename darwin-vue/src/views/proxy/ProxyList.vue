<script setup>
import { reactive, ref, watchEffect } from 'vue'
import {
  ElButton, ElCol, ElIcon, ElPagination,
  ElRow, ElSpace, ElTable, ElTableColumn
} from 'element-plus'
import { Timer } from '@element-plus/icons-vue'
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
import {
  asyncExecuteAfterConfirming,
  ERROR, showMessage, SUCCESS
} from '@/common/Feedback'
import AddProxy from '@/views/proxy/AddProxy'
import EditProxy from '@/views/proxy/EditProxy'

const userStore = useUserStore()
const openAddDialog = ref(false)
const openEditDialog = ref(false)
const proxyId = ref()
const proxies = ref([])
const checking = ref()
const total = ref(0)
const query = reactive(newSearchQuery({
  category: 1,
  sort_field: 'update_time',
  sort_order: 'descending'
}))

const search = async () => {
  const request = newSearchRequest(query)
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
      <el-table-column prop="address" label="代理IP" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.address }}</template>
      </el-table-column>
      <el-table-column prop="port" label="代理端口" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.port }}</template>
      </el-table-column>
      <el-table-column label="更新时间" prop="update_time" sortable="custom" show-overflow-tooltip>
        <template #default="scope">
          <el-icon><timer /></el-icon>
          {{ formatDate(scope.row['update_time']) }}
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
                     v-model:page-size="query.page_size" v-model:current-page="query.page_num">
      </el-pagination>
    </el-row>
  </el-space>
  <add-proxy v-model="openAddDialog" @close="search" />
  <edit-proxy v-model="openEditDialog" :id="proxyId" @close="search" />
</template>

<style scoped>
</style>