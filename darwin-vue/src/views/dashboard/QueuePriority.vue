<script setup>
import VueApexCharts from 'vue3-apexcharts'
import { computed, onMounted, ref } from 'vue'
import { ElLoading } from 'element-plus'
import { pause } from '@/common/Time'
import { priorityMap } from '@/common/Constants'
import { asyncQueueWaitPriority } from '@/common/AsyncRequest'
import DarwinCard from '@/components/data/Card'

defineProps(['width', 'height'])
const vLoading = ElLoading.directive
const loading = ref(false)
const series = ref([])
const labels = ref([])
const chartOptions = computed(() => {
  return {
    chart: { type: 'radialBar' },
    plotOptions: {
      radialBar: {
        offsetY: 0,
        startAngle: 0,
        endAngle: 270,
        hollow: {
          margin: 5,
          size: '30%',
          background: 'transparent'
        },
        dataLabels: {
          name: {
            show: false,
          },
          value: {
            show: false,
          }
        },
        barLabels: {
          enabled: true,
          useSeriesColors: true,
          offsetX: -8,
          fontSize: '10px',
          formatter: function(seriesName, opts) {
            return seriesName + '优先级:  ' + opts.w.globals.series[opts.seriesIndex]
          },
        },
      }
    },
    responsive: [{
      breakpoint: 480,
      options: {
        legend: {
          show: true
        }
      }
    }],
    colors: ['#dd6b66', '#F09148', '#55B7E6'],
    labels: labels.value
  }
})

onMounted(async () => {
  loading.value = true
  series.value.splice(0, series.value.length)
  labels.value.splice(0, labels.value.length)
  const queuePriority = await asyncQueueWaitPriority()
  await pause(500)
  queuePriority.forEach(priorityCount => {
    labels.value.push(priorityMap[priorityCount.priority])
    series.value.push(priorityCount.count)
  })
  loading.value = false
})
</script>

<template>
  <darwin-card title="排队优先级分布" :padding-top="0" :padding-right="0" :padding-bottom="0" :padding-left="0">
    <vue-apex-charts v-loading="loading" :width="width" :height="height" :options="chartOptions" :series="series" />
  </darwin-card>
</template>

<style scoped>

</style>