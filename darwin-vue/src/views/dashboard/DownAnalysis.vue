<script setup>
import { ref, watch } from 'vue'
import { ElCol, ElLoading, ElOption, ElRow, ElSelect, ElStatistic } from 'element-plus'
import { contentTypeMap } from '@/common/Constants'
import { pause } from '@/common/Time'
import { asyncDownAnalysis } from '@/common/AsyncRequest'
import DarwinCard from '@/components/data/Card'
import { normalizeStorage, normalizeTime } from '@/views/dashboard/common'

const vLoading = ElLoading.directive
const computing = ref(true)
const contentType = ref('全部')
const downTimeUnit = ref('毫秒')
const contentLengthUnit = ref('B')
const analyzeResult = ref({})

watch(() => contentType.value, async () => {
  computing.value = true
  await pause(500)
  analyzeResult.value = await asyncDownAnalysis(contentType.value === '全部' ? undefined : contentType.value)
  analyzeResult.value.avg_down_time = normalizeTime(analyzeResult.value.avg_down_time, downTimeUnit)
  analyzeResult.value.avg_content_length = normalizeStorage(analyzeResult.value.avg_content_length, contentLengthUnit)
  computing.value = false
}, { immediate: true })
</script>

<template>
  <darwin-card title="24小时数据内容">
    <div class="d-flex align-items-center mb-4">
      <label class="mr-4 fs-14px flex-shrink-0">数据类型</label>
      <el-select v-model="contentType" size="default">
        <el-option label="全部" value="全部" />
        <el-option v-for="key in Object.keys(contentTypeMap)" :key="key"
                   :label="contentTypeMap[key]" :value="parseInt(key)" />
      </el-select>
    </div>
    <el-row class="mb-4"/>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-statistic :value="analyzeResult.avg_down_time" v-loading="computing">
          <template #title>
            <span class="fs-14px">平均下载时长</span>
          </template>
          <template #suffix>{{ downTimeUnit }}</template>
        </el-statistic>
      </el-col>
      <el-col :span="12">
        <el-statistic :value="analyzeResult.avg_content_length" v-loading="computing">
          <template #title>
            <span class="fs-14px">平均内容长度</span>
          </template>
          <template #suffix>{{ contentLengthUnit }}</template>
        </el-statistic>
      </el-col>
    </el-row>
  </darwin-card>
</template>

<style scoped>

</style>