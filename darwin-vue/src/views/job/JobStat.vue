<script setup>
import { ElCol, ElDialog, ElRow } from 'element-plus'
import StatusDistribution from '@/views/dashboard/StatusDistribution'
import TypeDistribution from '@/views/dashboard/TypeDistribution'

defineProps(['id'])
const emits = defineEmits(['close'])
const open = defineModel()
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" align-center show-close>
    <el-row :gutter="30">
      <el-col :span="12">
        <status-distribution title="数据状态分布" :width="300" :job-id="id" />
      </el-col>
      <el-col :span="12">
        <type-distribution title="数据类型分布" :width="275" :job-id="id" />
      </el-col>
    </el-row>
  </el-dialog>
</template>

<style scoped>

</style>