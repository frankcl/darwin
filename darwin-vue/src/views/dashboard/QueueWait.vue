<script setup>
import { IconHelp } from '@tabler/icons-vue'
import { onMounted, ref } from 'vue'
import { ElCol, ElLoading, ElProgress, ElRow, ElStatistic, ElTooltip } from 'element-plus'
import { pause } from '@/common/Time'
import { asyncGetQueueWait } from '@/common/AsyncRequest'
import DarwinCard from '@/components/data/Card'
import { normalizeTime } from '@/views/dashboard/common'

const vLoading = ElLoading.directive
const computing = ref(true)
const waitTimeUnit = ref('毫秒')
const queueWait = ref({})
const colors = [
  { color: '#f56c6c', percentage: 80 },
  { color: '#e6a23c', percentage: 60 },
  { color: '#5cb87a', percentage: 50 },
]

onMounted(async () => {
  computing.value = true
  await pause(500)
  queueWait.value = await asyncGetQueueWait()
  queueWait.value.wait_time = normalizeTime(queueWait.value.wait_time, waitTimeUnit)
  queueWait.value.water_level = '安全'
  if (queueWait.value.queue_ratio > 80) queueWait.value.water_level = '危险'
  else if (queueWait.value.queue_ratio > 60) queueWait.value.water_level = '警告'
  computing.value = false
})
</script>

<template>
  <darwin-card title="排队分析">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-statistic :value="queueWait.wait_count" v-loading="computing" class="mb-2">
          <template #title>
            <span class="fs-14px">排队数据量</span>
          </template>
        </el-statistic>
        <el-statistic :value="queueWait.wait_time" v-loading="computing">
          <template #title>
            <span class="fs-14px">平均等待时间</span>
          </template>
          <template #suffix>{{ waitTimeUnit }}</template>
        </el-statistic>
      </el-col>
      <el-col :span="12">
        <el-progress type="circle" :percentage="queueWait.queue_ratio"
                     :stroke-width="10" :color="colors" v-loading="computing">
          <template #default>
            <span class="instruction mb-2">
              排队比例
              <el-tooltip effect="dark" placement="top" content="排队数据占比：分子是排队数据，分母是排队数据与正在抓取数据之和">
                <IconHelp size="12" class="ml-1"/>
              </el-tooltip>
            </span>
            <span class="percentage">{{ queueWait.queue_ratio }}%</span>
            <span class="instruction" :class="{ red: queueWait.water_level === '危险',
                    yellow: queueWait.water_level === '警告', green: queueWait.water_level === '安全' }">
              {{ queueWait.water_level }}
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