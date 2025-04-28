<script setup>
import { ref, watchEffect } from 'vue'
import { ElCol, ElRow, ElSpace, ElTable, ElTableColumn } from 'element-plus'
import { categoryMap, statusMap } from '@/common/Constants'
import BarChat from '@/components/chart/BarChat'
import LineChat from '@/components/chart/LineChat'
import {
  asyncAverageContentLength,
  asyncContentTrend,
  asyncPercentage,
  asyncStatusTrend,
  asyncTotalTrend,
  asyncTopHosts
} from '@/common/AsyncRequest'

const trendTotalItems = ['URL', 'HOST', 'DOMAIN']
const trendTotal = ref()
const trendStatus = ref()
const trendContent = ref()
const statePercentage = ref()
const stateContentLength = ref()
const topHosts = ref([])

const initTitleOption = text => {
  return { text: text, left: 'center', textStyle: { fontSize: 16 } }
}

const initGridOption = (left = 0, top = 0, right = 0, bottom = 0) => {
  return { left: left, right: right, bottom: bottom, top: top, containLabel: true }
}

const initTrend = async (trendRef, items, asyncDashboards) => {
  trendRef.value = []
  trendRef.value.push(['hour', ...items])
  const dashboards = await asyncDashboards()
  dashboards.forEach(dashboard => {
    const valueMap = {}
    dashboard.values.forEach(v => valueMap[v.name] = v.value)
    const data = [dashboard.hour]
    items.forEach(item => data.push(valueMap[item] === undefined ? 0 : valueMap[item]))
    trendRef.value.push(data)
  })
}

const initState = async (stateRef, items, asyncDashboards) => {
  stateRef.value = []
  stateRef.value.push(['统计', '全部', ...items])
  const dashboardMap = await asyncDashboards()
  Object.keys(dashboardMap).forEach(key => {
    const data = [key]
    const valueMap = {}
    dashboardMap[key].forEach(dashboard => valueMap[dashboard.name] = dashboard.value)
    stateRef.value[0].forEach((item, index) => {
      if (index > 0) data.push(valueMap[item] === undefined ? 0 : valueMap[item])
    })
    stateRef.value.push(data)
  })
}

watchEffect(async () => {
  await initTrend(trendTotal, trendTotalItems, asyncTotalTrend)
  await initTrend(trendStatus, Object.values(statusMap), asyncStatusTrend)
  await initTrend(trendContent, Object.values(categoryMap), asyncContentTrend)
  await initState(statePercentage, Object.values(categoryMap), asyncPercentage)
  await initState(stateContentLength, Object.values(categoryMap), asyncAverageContentLength)
  topHosts.value = await asyncTopHosts()
})
</script>

<template>
  <el-space direction="vertical" :size="20" :fill="true" class="w100">
    <el-row :gutter="20">
      <el-col :span="16">
        <line-chat v-if="trendTotal" :title="initTitleOption('抓取总量')"
                   :grid="initGridOption(10, 80, 85, 10)"
                   x-axis-name="时间/小时" y-axis-name="数量"
                   x-axis-encode="hour" :y-axis-encodes="trendTotalItems"
                   :dataset="trendTotal" width="700" height="250" />
      </el-col>
      <el-col :span="8">
        <el-row class="mb-2" justify="center">
          <span class="font-bold text-16px">抓取量TOP10站点</span>
        </el-row>
        <el-table :data="topHosts" max-height="200" table-layout="auto" stripe>
          <template #empty>暂无抓取数据</template>
          <el-table-column prop="host" label="站点" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.host }}</template>
          </el-table-column>
          <el-table-column prop="count" label="抓取量" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.count }}</template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="16">
        <line-chat v-if="trendStatus" :title="initTitleOption('抓取状态')"
                   :grid="initGridOption(10, 80, 150, 10)"
                   :legend="{ type: 'scroll', orient: 'vertical', icon: 'rect', left: 'right', top: 50 }"
                   x-axis-name="时间/小时" y-axis-name="数量"
                   x-axis-encode="hour" :y-axis-encodes="Object.values(statusMap)"
                   :dataset="trendStatus" width="700" height="250" />
      </el-col>
      <el-col :span="8">
        <bar-chat v-if="statePercentage" :title="initTitleOption('抓取成功率/排队率')"
                  :grid="initGridOption(10, 80, 85, 10)"
                  x-axis-name="数据类型" y-axis-name="百分比" y-axis-label-format="{value}%"
                  x-axis-encode="统计" :y-axis-encodes="['全部', ...Object.values(categoryMap)]"
                  :dataset="statePercentage" width="350" height="250" />
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="16">
        <line-chat v-if="trendContent" :title="initTitleOption('抓取类型')"
                   :grid="initGridOption(10, 80, 85, 10)"
                   x-axis-name="时间/小时" y-axis-name="数量"
                   x-axis-encode="hour" :y-axis-encodes="Object.values(categoryMap)"
                   :dataset="trendContent" width="700" height="250" />
      </el-col>
      <el-col :span="8">
        <bar-chat v-if="stateContentLength" :title="initTitleOption('平均内容长度')"
                  :grid="initGridOption(10, 80, 85, 10)"
                  x-axis-name="数据类型" y-axis-name="内容长度" y-axis-label-format="{value} KB"
                  x-axis-encode="统计" :y-axis-encodes="['全部', ...Object.values(categoryMap)]"
                  :dataset="stateContentLength" width="350" height="250" />
      </el-col>
    </el-row>
  </el-space>
</template>

<style scoped>

</style>