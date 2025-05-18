<script setup>
import VueApexCharts from 'vue3-apexcharts'
import { computed, ref, watch } from 'vue'
import { ElLoading } from 'element-plus'
import { contentTypeMap } from '@/common/Constants'
import { pause } from '@/common/Time'
import { asyncContentGroupCount } from '@/common/AsyncRequest'
import DarwinCard from '@/components/data/Card'
import { queryTimeRange } from '@/views/dashboard/common'

const props = defineProps(['jobId', 'width', 'height', 'title'])
const vLoading = ElLoading.directive
const computing = ref(true)
const series = ref([])
const labels = ref([])
const chartOptions = computed(() => {
  return {
    chart: { type: 'pie', height: 300  },
    colors:[
      '#91ca8c','#f49f42','#dd6b66','#8dc7e3',
      '#ea7e53','#eedd78','#b36784','#73b9bc',
      '#7289ab','#e69d87','#8dc1a9','#759aa0',
    ],
    labels: labels.value,
    // fill:{ type:'gradient', },
  }
})

watch(() => props.jobId, async () => {
  computing.value = true
  series.value.splice(0, series.value.length)
  labels.value.splice(0, labels.value.length)
  const contentGroupCount = props.jobId ?
    await asyncContentGroupCount({ job_id: props.jobId }) :
    await asyncContentGroupCount({ 'time_range': JSON.stringify(queryTimeRange()) })
  await pause(500)
  contentGroupCount.forEach(contentCount => {
    labels.value.push(contentTypeMap[contentCount.content_type])
    series.value.push(contentCount.count)
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