<script setup>
import { IconCaretDownFilled, IconCaretUpFilled } from '@tabler/icons-vue'
import { ref, watch } from 'vue'
import { ElCol, ElLoading, ElOption, ElProgress, ElRow, ElSelect, ElStatistic } from 'element-plus'
import { pause } from '@/common/Time'
import { contentTypeMap } from '@/common/Constants'
import { asyncFetchAnalysis } from '@/common/AsyncRequest'
import DarwinCard from '@/components/data/Card'

const vLoading = ElLoading.directive
const computing = ref(true)
const contentType = ref('全部')
const analyzeResult = ref({})

watch(() => contentType.value, async () => {
  computing.value = true
  await pause(500)
  analyzeResult.value = await asyncFetchAnalysis(contentType.value === '全部' ? undefined : contentType.value)
  computing.value = false
}, { immediate: true })
</script>

<template>
  <darwin-card title="24小时抓取量">
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
        <el-statistic :value="analyzeResult.fetch_success_count" v-loading="computing">
          <template #title>
            <span class="fs-14px">成功抓取数量</span>
          </template>
        </el-statistic>
        <el-statistic :value="analyzeResult.fetch_count" v-loading="computing" class="mt-4">
          <template #title>
            <span class="fs-14px">抓取总量</span>
          </template>
          <template #suffix>
            <div class="d-flex align-items-center fs-xs">
              <span v-if="analyzeResult.proportion !== undefined" class="mr-2">相比昨天</span>
              <span v-if="analyzeResult.proportion > 0" class="fw-500 green d-flex">
                {{ analyzeResult.proportion }}%
                <IconCaretUpFilled size="16" />
              </span>
              <span v-else-if="analyzeResult.proportion < 0" class="fw-500 red d-flex">
                {{ analyzeResult.proportion }}%
                <IconCaretDownFilled size="16" />
              </span>
              <span v-else-if="analyzeResult.proportion === 0" class="fw-500 gray d-flex">
                {{ analyzeResult.proportion }}%
              </span>
            </div>
          </template>
        </el-statistic>
      </el-col>
      <el-col :span="12">
        <el-progress type="circle" :percentage="analyzeResult.fetch_success_ratio"
                     v-loading="computing" :stroke-width="10">
          <template #default>
            <span class="percentage">{{ analyzeResult.fetch_success_ratio }}%</span>
            <span class="instruction">抓取成功率</span>
          </template>
        </el-progress>
      </el-col>
    </el-row>
  </darwin-card>
</template>

<style scoped>
.red {
  color: #f56c6c;
}
.gray {
  color: #939392;
}
.green {
  color: #5cb87a;
}
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
</style>