<script setup>
import * as echarts from 'echarts/core'
import { PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  DatasetComponent,
  LegendComponent,
  TransformComponent
} from 'echarts/components'
import { LabelLayout, UniversalTransition } from 'echarts/features'
import { CanvasRenderer } from 'echarts/renderers'
import { onMounted, onUnmounted, ref, useTemplateRef, watch } from 'vue'

echarts.use([
  TitleComponent,
  TooltipComponent,
  GridComponent,
  DatasetComponent,
  TransformComponent,
  LegendComponent,
  PieChart,
  LabelLayout,
  UniversalTransition,
  CanvasRenderer
])

const props = defineProps({
  title: { required: false },
  width: { default: 250 },
  height: { default: 250 },
  fontSize: { default: '18' },
  data: { required: true }
})

const initTitleOption = () => {
  if (!props.title) return {}
  return {
    text: props.title,
    left: 'center',
    textStyle: {
      fontSize: props.fontSize
    }
  }
}

const initSeriesOption = () => {
  return [{
    type: 'pie',
    radius: '50%',
    data: props.data,
    label: {
      position: 'outside',
      formatter: '{d}%'
    },
    emphasis: {
      itemStyle: {
        shadowBlur: 10,
        shadowOffsetX: 0,
        shadowColor: 'rgba(0, 0, 0, 0.5)'
      }
    }
  }]
}

const initOption = () => {
  return {
    title: initTitleOption(),
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: initSeriesOption()
  }
}

const containerRef = useTemplateRef('container')
const chartRef = ref()

const resizeEventListener = () => {
  if (chartRef.value) chartRef.value.resize()
}

const init = () => {
  if (!containerRef.value) return
  dispose()
  containerRef.value.removeAttribute('_echarts_instance_')
  chartRef.value = echarts.init(containerRef.value, null, { width: props.width, height: props.height })
  chartRef.value.setOption(initOption())
}
const dispose = () => {
  if (chartRef.value) chartRef.value.dispose()
}

watch(() => props.data, () => init(), { immediate: true })
onMounted(() => {
  init()
  window.addEventListener('resize', resizeEventListener)
})
onUnmounted(() => {
  dispose()
  window.removeEventListener('resize', resizeEventListener)
})
</script>

<template>
  <div id="container" ref="container" class="container"></div>
</template>

<style scoped>
.container {
  width: 100%;
  display: flex;
  justify-content: center;
  flex: 1 1 auto;
}
</style>