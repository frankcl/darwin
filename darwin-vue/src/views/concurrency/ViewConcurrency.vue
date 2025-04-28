<script setup>
import { ElDescriptions, ElDescriptionsItem, ElDialog } from 'element-plus'

defineProps(['concurrencyUnit'])
const emits = defineEmits(['close'])
const open = defineModel()
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" width="800" align-center show-close>
    <el-descriptions :title="`并发单元信息:${concurrencyUnit.name}`" border :column="1">
      <el-descriptions-item label="抓取能力">{{ concurrencyUnit.fetch_capacity }}</el-descriptions-item>
      <el-descriptions-item label="排队数">{{ concurrencyUnit.queuing_records }}</el-descriptions-item>
      <el-descriptions-item label="抓取数">{{ concurrencyUnit.fetching_records }}</el-descriptions-item>
      <el-descriptions-item label="过期连接">{{ concurrencyUnit.expired_records }}</el-descriptions-item>
      <el-descriptions-item label="闲置连接">{{ concurrencyUnit.spare_records }}</el-descriptions-item>
    </el-descriptions>
  </el-dialog>
</template>

<style scoped>

</style>