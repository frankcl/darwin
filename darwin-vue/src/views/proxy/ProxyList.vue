<script setup>
import { IconClock, IconCircleDashedCheck, IconEdit, IconPlus, IconTrash } from '@tabler/icons-vue'
import { reactive, ref, watchEffect } from 'vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElConfigProvider,
  ElPagination, ElRow, ElTable, ElTableColumn
} from 'element-plus'
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
import DarwinCard from '@/components/data/Card'
import TableHead from '@/components/data/TableHead'

const userStore = useUserStore()
const openAdd = ref(false)
const openEdit = ref(false)
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
  openEdit.value = true
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
  <darwin-card>
    <template #title>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>抓取控制</el-breadcrumb-item>
        <el-breadcrumb-item>代理管理</el-breadcrumb-item>
      </el-breadcrumb>
    </template>
    <table-head title="代理列表">
      <template #right>
        <el-button type="primary" @click="openAdd = true" :disabled="!userStore.superAdmin">
          <IconPlus size="20" class="mr-1" />
          <span>新增</span>
        </el-button>
      </template>
    </table-head>
    <el-table :data="proxies" max-height="550" table-layout="auto" stripe class="mb-4"
              @sort-change="event => changeSearchQuerySort(event.prop, event.order, query)">
      <template #empty>暂无代理数据</template>
      <el-table-column prop="address" label="代理IP" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.address }}</template>
      </el-table-column>
      <el-table-column prop="port" label="代理端口" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.port }}</template>
      </el-table-column>
      <el-table-column label="更新时间" prop="update_time" sortable="custom" show-overflow-tooltip>
        <template #default="scope">
          <div class="d-flex align-items-center">
            <IconClock size="16" class="mr-1" />
            <span>{{ formatDate(scope.row['update_time']) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column width="320">
        <template #header>操作</template>
        <template #default="scope">
          <el-button type="primary" @click="edit(scope.row.id)" :disabled="!userStore.superAdmin">
            <IconEdit size="20" class="mr-1" />
            <span>编辑</span>
          </el-button>
          <el-button type="success" @click="check(scope.row)"
                     :loading="checking === scope.row.id" :disabled="!userStore.superAdmin">
            <IconCircleDashedCheck size="20" class="mr-1" />
            <span>检测</span>
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
  <add-proxy v-model="openAdd" @close="search" />
  <edit-proxy v-model="openEdit" :id="proxyId" @close="search" />
</template>

<style scoped>
</style>