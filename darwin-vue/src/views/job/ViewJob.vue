<script setup>
import { ref, watchEffect } from 'vue'
import { ElDescriptions, ElDescriptionsItem, ElDialog, ElLoading, ElText } from 'element-plus'
import { pause } from '@/common/Time'
import { fetchMethodMap, priorityMap } from '@/common/Constants'
import { asyncGetJob, asyncJobProgress } from '@/common/AsyncRequest'

const props = defineProps(['id'])
const vLoading = ElLoading.directive
const open = defineModel()
const emits = defineEmits(['close'])
const job = ref()
const progress = ref('N/A')
const progressLoading = ref(true)

const jobProgress = async id => {
  progressLoading.value = true
  const processRate = await asyncJobProgress(id)
  progress.value = processRate * 100 + '%'
  await pause(1000)
  progressLoading.value = false
}

watchEffect(async () => {
  if (props.id) {
    job.value = await asyncGetJob(props.id)
    if (job.value.fetch_method === undefined) job.value.fetch_method = 0
    await jobProgress(props.id)
  }
})
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" width="1000" align-center show-close>
    <el-descriptions v-if="job" direction="vertical" :column="3" border>
      <el-descriptions-item label="任务名称">{{ job.name }}</el-descriptions-item>
      <el-descriptions-item label="任务状态">{{ job.status ? '运行' : '结束' }}</el-descriptions-item>
      <el-descriptions-item label="优先级">{{ priorityMap[job.priority] }}</el-descriptions-item>
      <el-descriptions-item label="重复抓取">{{ job.allow_repeat ? '允许' : '禁止' }}</el-descriptions-item>
      <el-descriptions-item label="抓取方式">{{ fetchMethodMap[job.fetch_method] }}</el-descriptions-item>
      <el-descriptions-item label="抓取进度">
        <el-text v-loading="progressLoading">{{ progress }}</el-text>
      </el-descriptions-item>
    </el-descriptions>
  </el-dialog>
</template>

<style scoped>
</style>