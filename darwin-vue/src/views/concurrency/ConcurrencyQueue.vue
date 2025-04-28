<script setup>
import { onMounted, ref } from 'vue'
import {
  ElButton,
  ElCol,
  ElDescriptions,
  ElDescriptionsItem,
  ElInput,
  ElProgress,
  ElRow,
  ElTable,
  ElTableColumn,
  ElText
} from 'element-plus'
import { showMessage, WARNING } from '@/common/Feedback'
import {
  asyncConcurrencyQueueMemory,
  asyncGetConcurrencyUnit,
  asyncTopConcurrencyUnits
} from '@/common/AsyncRequest'
import ViewConcurrency from '@/views/concurrency/ViewConcurrency'

const memory = ref({})
const percentage = ref(0)
const waterLevel = ref('安全')
const searchUnit = ref()
const units = ref([])
const concurrencyUnit = ref()
const openViewDialog = ref(false)

const colors = [
  { color: '#f56c6c', percentage: 95 },
  { color: '#e6a23c', percentage: 85 },
  { color: '#5cb87a', percentage: 70 },
]

const view = async unit => {
  if (!unit) {
    showMessage('请输入要查询的并发单元', WARNING)
    return
  }
  concurrencyUnit.value = await asyncGetConcurrencyUnit(unit)
  concurrencyUnit.value.name = unit
  openViewDialog.value = true
}

const topConcurrencyUnits = async () => units.value = await asyncTopConcurrencyUnits(10)

onMounted(async () => {
  memory.value = await asyncConcurrencyQueueMemory()
  await topConcurrencyUnits()
  const totalMemory = memory.value.maxmemory === 0 ? memory.value.total_system_memory : memory.value.maxmemory
  percentage.value = totalMemory === 0 ? 100 : memory.value.used_memory_rss * 100 / totalMemory
  percentage.value = parseFloat(percentage.value.toFixed(2))
  if (percentage.value <= 70) waterLevel.value = '安全'
  else if (percentage.value <= 85) waterLevel.value = '警告'
  else waterLevel.value = '危险'
})
</script>

<template>
  <el-row :gutter="50">
    <el-col :span="18">
      <el-descriptions title="并发队列实时内存" border :column="1">
        <el-descriptions-item label="占用内存">
          {{ (memory.used_memory / 1024 / 1024).toFixed(2) + ' MB' }}
        </el-descriptions-item>
        <el-descriptions-item label="驻留内存">
          {{ (memory.used_memory_rss / 1024 / 1024).toFixed(2) + ' MB' }}
        </el-descriptions-item>
        <el-descriptions-item label="配置最大内存">
          {{ (memory.maxmemory / 1024 / 1024).toFixed(2) + ' MB' }}
        </el-descriptions-item>
        <el-descriptions-item label="系统最大内存">
          {{ (memory.total_system_memory / 1024 / 1024).toFixed(2) + ' MB' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-col>
    <el-col :span="6">
      <el-row align="middle" justify="center" class="mb-16px"><span class="text-16px font-bold">内存使用率</span></el-row>
      <el-row align="middle" justify="center" style="height: 161px">
        <el-progress type="dashboard" :percentage="percentage" :color="colors">
          <template #default>
            <span class="percentage-value">{{ percentage }}%</span>
            <span class="percentage-label">{{ waterLevel }}</span>
          </template>
        </el-progress>
      </el-row>
    </el-col>
  </el-row>
  <el-row align="middle" class="mt-8 mb-4">
    <el-col :span="10">
      <span class="text-16px font-bold">Top 10 并发单元</span>
    </el-col>
    <el-col :span="14">
      <el-row align="middle" justify="end">
        <el-input class="w180px mr-3" v-model="searchUnit" placeholder="查询并发单元" clearable />
        <el-button type="primary" @click="view(searchUnit)">查询</el-button>
      </el-row>
    </el-col>
  </el-row>
  <el-table :data="units" max-height="550" table-layout="auto" stripe>
    <template #empty>暂无排队数据</template>
    <el-table-column label="并发单元" show-overflow-tooltip>
      <template #default="scope">
        <el-text>{{ scope.row.concurrency_unit }}</el-text>
      </template>
    </el-table-column>
    <el-table-column label="排队链接数" show-overflow-tooltip>
      <template #default="scope">
        <el-text>{{ scope.row.count }}</el-text>
      </template>
    </el-table-column>
    <el-table-column width="160">
      <template #header>操作</template>
      <template #default="scope">
        <el-button type="primary" @click="view(scope.row.concurrency_unit)">查看</el-button>
      </template>
    </el-table-column>
  </el-table>
  <view-concurrency v-model="openViewDialog" :concurrency-unit="concurrencyUnit" @close="topConcurrencyUnits" />
</template>

<style scoped>
.percentage-value {
  display: block;
  margin-top: 10px;
  font-size: 28px;
}
.percentage-label {
  display: block;
  margin-top: 10px;
  font-size: 12px;
}
</style>