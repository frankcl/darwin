<script setup>
import * as echarts from 'echarts/core'
import { PieChart } from 'echarts/charts'
import {
  DatasetComponent,
  GridComponent,
  LegendComponent,
  TitleComponent,
  TooltipComponent,
  TransformComponent
} from 'echarts/components'
import { LabelLayout, UniversalTransition } from 'echarts/features'
import { CanvasRenderer } from 'echarts/renderers'
import { onMounted, onUnmounted, shallowRef, useTemplateRef, watchEffect } from 'vue'

echarts.use([
  CanvasRenderer,
  DatasetComponent,
  GridComponent,
  LabelLayout,
  LegendComponent,
  PieChart,
  TitleComponent,
  TooltipComponent,
  TransformComponent,
  UniversalTransition
])

const props = defineProps({
  width: { default: 350 },
  height: { default: 350 },
  title: { required: false },
  color: { required: false },
  legend: { required: false, default: { orient: 'horizontal', icon: 'rect', left: 'right', top: 60 } },
  grid: { required: false, default: { left: '5%', right: '5%', bottom: '5%', top: '5%', containLabel: true } },
  tooltip: { required: false, default: { trigger: 'item', axisPointer: { type: 'cross' } } },
  emphasis: { required: false, default: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' }}},
  dataset: { required: true },
  itemName: { required: false },
  value: { required: true }
})

const initOption = () => {
  if (!props.dataset) return {}
  return {
    title: props.title,
    legend: props.legend,
    grid: props.grid,
    tooltip: props.tooltip,
    dataset: {
      source: props.dataset
    },
    series: [{
      type: 'pie',
      radius: '50%',
      center: ['50%', '50%'],
      color: props.color,
      encode: {
        itemName: props.itemName,
        value: props.value
      },
      label: {
        position: 'outside',
        formatter: '{d}%'
      },
      emphasis: props.emphasis
    }]
  }
}

const containerRef = useTemplateRef('container')
const chartRef = shallowRef()

const resizeEventListener = () => {
  if (chartRef.value) chartRef.value.resize()
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

watchEffect(() => init())
onMounted(() => {
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