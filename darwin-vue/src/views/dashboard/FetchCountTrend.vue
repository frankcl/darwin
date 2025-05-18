<script setup>
import VueApexCharts from 'vue3-apexcharts'
import { onMounted, ref } from 'vue'
import { ElLoading } from 'element-plus'
import { pause } from '@/common/Time'
import { asyncFetchCountTrend } from '@/common/AsyncRequest'
import DarwinCard from '@/components/data/Card'

defineProps(['width', 'height'])
const vLoading = ElLoading.directive
const loading = ref(true)
const series = ref([])
const chartOptions = ref({})

onMounted(async () => {
  loading.value = true
  series.value.splice(0, series.value.length)
  const seriesMap = {}
  const categories = []
  const countTrends = await asyncFetchCountTrend()
  countTrends.forEach(contentTrend => {
    categories.push(contentTrend.key)
    contentTrend.values.forEach(v => {
      if (!seriesMap[v.name]) seriesMap[v.name] = []
      seriesMap[v.name].push(v.value)
    })
  })
  await pause(500)
  Object.keys(seriesMap).forEach(key => series.value.push({ name: key, data: seriesMap[key] }))
  chartOptions.value = {
    chart: { type: 'line', toolbar: { show: false }, offsetY: 10, offsetX: 10 },
    colors: ['#77B6EA', '#ea7e53', '#A6D854'],
    dataLabels: { enabled: true },
    stroke: { curve: 'smooth' },
    grid: { borderColor: '#e7e7e7', row: { colors: ['#f3f3f3', 'transparent'], opacity: 0.5 } },
    xaxis: { categories: categories, title: { text: '时间 / 小时', offsetY: -10 } },
    yaxis: { title: { text: '抓取量' } },
    legend: { position: 'top', horizontalAlign: 'right', floating: true, offsetY: 0, offsetX: -5 }
  }
  loading.value = false
})
</script>

<template>
  <darwin-card title="24小时抓取量趋势" :padding-bottom="0" :padding-top="10">
    <vue-apex-charts v-loading="loading" type="line" :width="width" :height="height"
                     :options="chartOptions" :series="series" />
  </darwin-card>
</template>

<style scoped>

</style>