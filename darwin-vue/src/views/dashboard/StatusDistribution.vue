<script setup>
import VueApexCharts from 'vue3-apexcharts'
import { computed, ref, watch } from 'vue'
import { ElLoading } from 'element-plus'
import { statusMap } from '@/common/Constants'
import { pause } from '@/common/Time'
import { asyncStatusGroupCount } from '@/common/AsyncRequest'
import DarwinCard from '@/components/data/Card'
import { queryTimeRange } from '@/views/dashboard/common'

const props = defineProps(['jobId', 'width', 'height', 'title'])
const vLoading = ElLoading.directive
const computing = ref(true)
const series = ref([])
const labels = ref([])
const chartOptions = computed(() => {
  return {
    chart: { type: 'pie', height: 300 },
    colors:['#427AB2','#A6D854','#FAC858','#FF9896',
      '#AFC7E8','#F09148','#48C0AA','#DBDB8D',
      '#E53528','#EEA599','#55B7E6','#80CDC1'],
    labels: labels.value,
    // fill:{ type:'gradient', },
  }
})

watch(() => props.jobId, async () => {
  computing.value = true
  series.value.splice(0, series.value.length)
  labels.value.splice(0, labels.value.length)
  const statusGroupCount = props.jobId ?
    await asyncStatusGroupCount({ job_id: props.jobId }) :
    await asyncStatusGroupCount({ 'time_range': JSON.stringify(queryTimeRange()) })
  await pause(500)
  statusGroupCount.forEach(statusCount => {
    labels.value.push(statusMap[statusCount.status])
    series.value.push(statusCount.count)
  })
  computing.value = false
}, { immediate: true })
</script>

<template>
  <darwin-card :title="title">
    <vue-apex-charts v-loading="computing" :width="width" :height="height" :options="chartOptions" :series="series" />
  </darwin-card>
</template>

<style scoped>
</style>