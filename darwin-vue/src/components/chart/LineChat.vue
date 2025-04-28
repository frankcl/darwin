<script setup>
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import {
  DatasetComponent,
  GridComponent,
  LegendComponent,
  TitleComponent,
  TooltipComponent,
  TransformComponent
} from 'echarts/components'
import { LabelLayout } from 'echarts/features'
import { CanvasRenderer } from 'echarts/renderers'
import { onMounted, onUnmounted, shallowRef, useTemplateRef, watchEffect } from 'vue'

echarts.use([
  CanvasRenderer,
  DatasetComponent,
  GridComponent,
  LabelLayout,
  LegendComponent,
  LineChart,
  TitleComponent,
  TooltipComponent,
  TransformComponent
])

const props = defineProps({
  width: { default: 450 },
  height: { default: 300 },
  title: { required: false },
  legend: { required: false, default: { orient: 'vertical', icon: 'rect', left: 'right', top: 50 } },
  grid: { required: false, default: { left: '5%', right: '5%', bottom: '5%', top: '5%', containLabel: true } },
  tooltip: { required: false, default: { trigger: 'axis', axisPointer: { type: 'cross' } } },
  dataset: { required: true },
  smooth: { default: true },
  xAxisName: { required: false },
  yAxisName: { required: false },
  xAxisEncode: { required: true },
  yAxisEncodes: { required: true }
})
const containerRef = useTemplateRef('container')
const chartRef = shallowRef()

const initOption = () => {
  if (!props.dataset) return {}
  const option = {
    title: props.title || {},
    legend: props.legend || {},
    grid: props.grid || {},
    tooltip: props.tooltip || {},
    dataset: {
      source: props.dataset
    },
    xAxis: {
      type: 'category',
      name: props.xAxisName,
      axisTick: {
        alignWithLabel: true
      },
      axisLabel: {
        align: 'center'
      }
    },
    yAxis: { type: 'value', name: props.yAxisName },
    series: []
  }
  props.yAxisEncodes.forEach(yAxisEncode => {
    option.series.push({
      type: 'line',
      name: yAxisEncode,
      smooth: props.smooth,
      encode: {
        x: props.xAxisEncode,
        y: yAxisEncode,
        tooltip: yAxisEncode
      }
    })
  })
  return option
}

const init = () => {
  if (!containerRef.value) return
  dispose()
  chartRef.value = echarts.init(containerRef.value, null, { width: props.width, height: props.height })
  chartRef.value.setOption(initOption())
}

const dispose = () => {
  if (chartRef.value) chartRef.value.dispose()
}

const resizeEventListener = () => {
  if (chartRef.value) chartRef.value.resize()
}

watchEffect(() => init())
onMounted(async () => {
  window.addEventListener('resize', resizeEventListener)
})
onUnmounted(() => {
  dispose()
  window.removeEventListener('resize', resizeEventListener)
})
</script>

<template>
  <div ref="container"></div>
</template>

<style scoped>
</style>