<script setup>
import { ref, watch } from 'vue'
import { ElDialog, ElRow } from 'element-plus'
import { statusMap } from '@/common/Constants'
import { asyncBucketCountGroupByStatus } from '@/common/AsyncRequest'
import PieShapeChart from '@/components/chart/PieShapeChart'

const props = defineProps(['id'])
const emits = defineEmits(['close'])
const open = defineModel()
const show = ref(false)
const data = ref([])

watch(() => props.id, async () => {
  if (props.id) {
    data.value = []
    const counts = await asyncBucketCountGroupByStatus(props.id)
    counts.forEach(count => data.value.push({ name: statusMap[count.status], value: count.count }))
  }
})
watch(() => open.value, () => {
  if (open.value) setTimeout(() => show.value = open.value, 100)
  else show.value = open.value
})
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" width="800" align-center show-close>
    <el-row justify="center" align="middle" class="w100">
      <pie-shape-chart v-if="show" :data="data" width="800" height="300" title="数据状态分桶统计" />
    </el-row>
  </el-dialog>
</template>

<style scoped>

</style>