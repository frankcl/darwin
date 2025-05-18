<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElCol, ElLoading, ElProgress, ElRow, ElStatistic } from 'element-plus'
import { pause } from '@/common/Time'
import { asyncGetQueueMemory } from '@/common/AsyncRequest'
import DarwinCard from '@/components/data/Card'
import { normalizeStorage } from '@/views/dashboard/common'

const vLoading = ElLoading.directive
const computing = ref(true)
const memory = ref({})
const useMemoryUnit = ref('B')
const maxMemoryUnit = ref('B')
const danger = computed(() => memory.value.water_level === '危险')
const warning = computed(() => memory.value.water_level === '警告')
const normal = computed(() => memory.value.water_level === '安全')
const colors = [
  { color: '#f56c6c', percentage: 95 },
  { color: '#e6a23c', percentage: 85 },
  { color: '#5cb87a', percentage: 70 },
]

onMounted(async () => {
  computing.value = true
  await pause(500)
  memory.value = await asyncGetQueueMemory()
  memory.value.use_memory = normalizeStorage(memory.value.use_memory, useMemoryUnit)
  memory.value.max_memory = normalizeStorage(memory.value.max_memory, maxMemoryUnit)
  computing.value = false
})
</script>

<template>
  <darwin-card title="队列内存">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-statistic :value="memory.use_memory" v-loading="computing" class="mb-2">
          <template #title>
            <span class="fs-14px">使用内存</span>
          </template>
          <template #suffix>{{ useMemoryUnit }}</template>
        </el-statistic>
        <el-statistic :value="memory.max_memory" v-loading="computing">
          <template #title>
            <span class="fs-14px">最大内存</span>
          </template>
          <template #suffix>{{ maxMemoryUnit }}</template>
        </el-statistic>
      </el-col>
      <el-col :span="12">
        <el-progress type="circle" :percentage="memory.use_ratio"
                     :stroke-width="10" :color="colors" v-loading="computing">
          <template #default>
            <span class="instruction mb-2">内存使用率</span>
            <span class="percentage">{{ memory.use_ratio }}%</span>
            <span class="instruction" :class="{ red: danger, yellow: warning, green: normal }">
              {{ memory.water_level }}
            </span>
          </template>
        </el-progress>
      </el-col>
    </el-row>
  </darwin-card>
</template>

<style scoped>
.percentage {
  display: block;
  font-size: 24px;
}
.instruction {
  display: block;
  margin-top: 10px;
  font-size: 12px;
  font-weight: 500;
}
.red {
  color: #f56c6c;
}
.yellow {
  color: #e6a23c;
}
.green {
  color: #5cb87a;
}
</style>