<script setup>
import { onMounted, ref } from 'vue'
import { ElOption, ElSelect } from 'element-plus'
import { asyncSearchJob } from '@/common/AsyncRequest'

const props = defineProps({
  'placeholder': { default: '根据任务名搜索' }
})
const jobId = defineModel()
const emits = defineEmits(['change'])
const loading = ref(false)
const jobs = ref([])
const jobMap = new Map()

const search = async query => {
  loading.value = true
  try {
    jobMap.clear()
    const pager = await asyncSearchJob({ name: query })
    jobs.value = pager.records
    jobs.value.forEach(job => jobMap.set(job.job_id, job))
  } finally {
    loading.value = false
  }
}

const handleChange = id => {
  const job = jobMap.get(id)
  if (job) emits('change', JSON.parse(JSON.stringify(job)))
}

onMounted(() => search(''))
</script>

<template>
  <el-select v-model="jobId" @change="handleChange" clearable filterable remote :remote-method="search"
             :loading="loading" loading-text="搜索中 ..." :placeholder="props.placeholder">
    <el-option v-for="job in jobs" :key="job.job_id" :label="job.name" :value="job.job_id" />
  </el-select>
</template>

<style scoped>

</style>