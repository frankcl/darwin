<script setup>
import { IconEye, IconSearch } from '@tabler/icons-vue'
import { onMounted, ref } from 'vue'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElCol,
  ElInput, ElLoading, ElRow, ElTable, ElTableColumn, ElText
} from 'element-plus'
import { showMessage, WARNING } from '@/common/Feedback'
import {
  asyncConcurrencyQueueWait,
  asyncGetConcurrencyUnit,
  asyncWaitConcurrencyUnits
} from '@/common/AsyncRequest'
import DarwinCard from '@/components/data/Card'
import TableHead from '@/components/data/TableHead'
import QueueMemory from '@/views/dashboard/QueueMemory'
import QueuePriority from '@/views/dashboard/QueuePriority'
import QueueWait from '@/views/dashboard/QueueWait'
import ConDetail from '@/views/concurrency/ConDetail.vue'
import { normalizeTime } from '@/views/concurrency/common'

const vLoading = ElLoading.directive
const query = ref()
const conUnit = ref()
const conUnits = ref([])
const openView = ref(false)

const view = async name => {
  if (!name) {
    showMessage('请输入要查询的并发单元', WARNING)
    await getWaitConcurrencyUnits()
    return
  }
  conUnit.value = await asyncGetConcurrencyUnit(name)
  openView.value = true
}

const getWaitConcurrencyUnits = async () => {
  conUnits.value = await asyncWaitConcurrencyUnits(10)
  conUnits.value.forEach(async conUnit => {
    conUnit.computing = true
    const queueWait = await asyncConcurrencyQueueWait(conUnit.concurrency_unit)
    conUnit.wait_time = normalizeTime(queueWait.wait_time)
    conUnit.queue_ratio = queueWait.queue_ratio
    conUnit.five_minutes_fetch_count = queueWait.five_minutes_fetch_count
    conUnit.computing = false
  })
}

onMounted(async () => await getWaitConcurrencyUnits() )
</script>

<template>
  <darwin-card>
    <template #title>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>抓取控制</el-breadcrumb-item>
        <el-breadcrumb-item>并发队列</el-breadcrumb-item>
      </el-breadcrumb>
    </template>
    <el-row :gutter="50">
      <el-col :span="8"><queue-memory /></el-col>
      <el-col :span="8"><queue-wait /></el-col>
      <el-col :span="8"><queue-priority :height="185" /></el-col>
    </el-row>
    <table-head title="TOP10 排队并发单元">
      <template #right>
        <el-input class="w-200px mr-4" v-model="query" placeholder="查询并发单元" clearable />
        <el-button type="primary" @click="view(query)">
          <IconSearch size="20" class="mr-1" />
          <span>查询</span>
        </el-button>
      </template>
    </table-head>
    <el-table :data="conUnits" max-height="550" table-layout="auto" stripe>
      <template #empty>暂无排队数据</template>
      <el-table-column label="并发单元" show-overflow-tooltip>
        <template #default="scope">
          <el-text>{{ scope.row.concurrency_unit }}</el-text>
        </template>
      </el-table-column>
      <el-table-column label="排队数量" show-overflow-tooltip>
        <template #default="scope">
          <el-text>{{ scope.row.count }}</el-text>
        </template>
      </el-table-column>
      <el-table-column label="排队比例" show-overflow-tooltip>
        <template #default="scope">
          <el-text v-loading="scope.row.computing">{{ scope.row.queue_ratio + '%' }}</el-text>
        </template>
      </el-table-column>
      <el-table-column label="平均等待时间" show-overflow-tooltip>
        <template #default="scope">
          <el-text v-loading="scope.row.computing">{{ scope.row.wait_time }}</el-text>
        </template>
      </el-table-column>
      <el-table-column label="最近5分钟抓取数量" show-overflow-tooltip>
        <template #default="scope">
          <el-text v-loading="scope.row.computing">{{ scope.row.five_minutes_fetch_count }}</el-text>
        </template>
      </el-table-column>
      <el-table-column width="160">
        <template #header>操作</template>
        <template #default="scope">
          <el-button type="primary" @click="view(scope.row.concurrency_unit)">
            <IconEye size="20" class="mr-2" />
            查看
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </darwin-card>
  <con-detail v-model="openView" :con-unit="conUnit" @close="getWaitConcurrencyUnits" />
</template>

<style scoped>
</style>