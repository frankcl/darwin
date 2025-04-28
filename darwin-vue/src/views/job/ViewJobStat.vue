<script setup>
import { ref, watch } from 'vue'
import { ElCol, ElDialog, ElRow } from 'element-plus'
import { categoryMap, statusMap } from '@/common/Constants'
import { asyncCountGroupByStatus, asyncCountGroupByCategory } from '@/common/AsyncRequest'
import PieChart from '@/components/chart/PieChart'

const props = defineProps(['id'])
const emits = defineEmits(['close'])
const open = defineModel()
const statusDataset = ref()
const categoryDataset = ref()
const color = [
  '#dd6b66',
  '#759aa0',
  '#e69d87',
  '#8dc1a9',
  '#ea7e53',
  '#eedd78',
  '#73a373',
  '#73b9bc',
  '#7289ab',
  '#91ca8c',
  '#f49f42'
]

const initTitleOption = text => {
  return { text: text, left: 'center', top: 30, textStyle: { fontSize: 16 } }
}

watch(() => props.id, async () => {
  if (props.id) {
    statusDataset.value = [['状态', '数量']]
    const statusCounts = await asyncCountGroupByStatus(props.id)
    statusCounts.forEach(statusCount => {
      statusDataset.value.push([statusMap[statusCount.status], statusCount.count])
    })

    categoryDataset.value = [['类型', '数量']]
    const categoryCounts = await asyncCountGroupByCategory(props.id)
    categoryCounts.forEach(categoryCount => {
      categoryDataset.value.push([categoryMap[categoryCount.category], categoryCount.count])
    })
  }
})
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" width="800" align-center show-close>
    <el-row justify="center" align="middle" class="w100" :gutter="20">
      <el-col :span="12">
        <pie-chart v-if="statusDataset" :dataset="statusDataset" item-name="状态" value="数量"
                   :title="initTitleOption('数据状态')" />
      </el-col>
      <el-col :span="12">
        <pie-chart v-if="categoryDataset" :dataset="categoryDataset" item-name="类型" value="数量"
                   :title="initTitleOption('数据类型')" :color="color" />
      </el-col>
    </el-row>
  </el-dialog>
</template>

<style scoped>

</style>