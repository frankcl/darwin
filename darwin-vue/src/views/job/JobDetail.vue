<script setup>
import { ref, watchEffect } from 'vue'
import { ElDescriptions, ElDescriptionsItem, ElDialog, ElLoading, ElText } from 'element-plus'
import { pause } from '@/common/Time'
import { asyncGetJob, asyncJobProgress, asyncJobSuccessRate } from '@/common/AsyncRequest'
import DarwinCard from '@/components/data/Card'

const props = defineProps(['id'])
const vLoading = ElLoading.directive
const open = defineModel()
const emits = defineEmits(['close'])
const job = ref()

const jobProgress = async job => {
  job.computeProgress = true
  const progress = await asyncJobProgress(job.job_id)
  job.progress = progress * 100 + '%'
  await pause(1000)
  job.computeProgress = false
}

const jobSuccessRate = async job => {
  job.computeSuccessRate = true
  const successRate = await asyncJobSuccessRate(job.job_id)
  job.successRate = successRate * 100 + '%'
  await pause(1000)
  job.computeSuccessRate = false
}

watchEffect(async () => {
  if (props.id) {
    job.value = await asyncGetJob(props.id)
    if (job.value.fetch_method === undefined) job.value.fetch_method = 0
    jobProgress(job.value)
    jobSuccessRate(job.value)
  }
})
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" align-center show-close>
    <darwin-card title="任务详情">
      <el-descriptions v-if="job" direction="vertical" :column="2" border>
        <el-descriptions-item label="任务名称" :span="2">{{ job.name }}</el-descriptions-item>
        <el-descriptions-item label="任务状态">{{ job.status ? '运行' : '结束' }}</el-descriptions-item>
        <el-descriptions-item label="执行人">{{ job.executor ? job.executor : '未知' }}</el-descriptions-item>
        <el-descriptions-item label="抓取进度">
          <el-text v-loading="job.computeProgress">{{ job.progress }}</el-text>
        </el-descriptions-item>
        <el-descriptions-item label="成功率">
          <el-text v-loading="job.computeSuccessRate">{{ job.successRate }}</el-text>
        </el-descriptions-item>
      </el-descriptions>
    </darwin-card>
  </el-dialog>
</template>

<style scoped>
</style>