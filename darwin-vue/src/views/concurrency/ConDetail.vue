<script setup>
import { ref, watch } from 'vue'
import { ElDescriptions, ElDescriptionsItem, ElDialog } from 'element-plus'
import DarwinCard from '@/components/data/Card'
import { asyncConcurrencyQueueWait } from '@/common/AsyncRequest'
import { normalizeTime } from '@/views/concurrency/common'

const props = defineProps(['conUnit'])
const emits = defineEmits(['close'])
const open = defineModel()
const queueWait = ref({})

watch(() => props.conUnit, async () => {
  if (props.conUnit) {
    queueWait.value = await asyncConcurrencyQueueWait(props.conUnit.name)
    queueWait.value.wait_time = normalizeTime(queueWait.value.wait_time)
  }
})
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" align-center show-close>
    <darwin-card :title="`并发单元:${conUnit.name}`">
      <el-descriptions border :column="1">
        <el-descriptions-item label="抓取能力">{{ conUnit.fetch_capacity }}</el-descriptions-item>
        <el-descriptions-item label="排队数量">{{ conUnit.queuing_records }}</el-descriptions-item>
        <el-descriptions-item label="抓取数量">{{ conUnit.fetching_records }}</el-descriptions-item>
        <el-descriptions-item label="过期连接">{{ conUnit.expired_records }}</el-descriptions-item>
        <el-descriptions-item label="闲置连接">{{ conUnit.spare_records }}</el-descriptions-item>
        <el-descriptions-item label="排队比例">{{ queueWait.queue_ratio }}%</el-descriptions-item>
        <el-descriptions-item label="平均等待时间">{{ queueWait.wait_time }}</el-descriptions-item>
      </el-descriptions>
    </darwin-card>
  </el-dialog>
</template>

<style scoped>

</style>