<script setup>
import { IconDeviceIpadHorizontalCode, IconNews } from '@tabler/icons-vue'
import { ref } from 'vue'
import { ElDivider, ElTabPane, ElTabs } from 'element-plus'
import DebugScript from '@/views/debug/DebugScript'
import AddRule from '@/views/rule/AddRule'

const props = defineProps(['planId'])
const emits = defineEmits(['add'])
const newRule = ref()

const handleAdd = () => emits('add')
const handleChange = rule => newRule.value = rule
</script>

<template>
  <el-divider></el-divider>
  <el-tabs tab-position="left" class="rule-tabs">
    <el-tab-pane label="新增">
      <template #label>
        <IconNews size="20" />
        <span class="ml-2">新增</span>
      </template>
      <add-rule :plan-id="props.planId" @change="handleChange" @add="handleAdd" />
    </el-tab-pane>
    <el-tab-pane label="调试">
      <template #label>
        <IconDeviceIpadHorizontalCode size="20" />
        <span class="ml-2">调试</span>
      </template>
      <debug-script v-bind="newRule" />
    </el-tab-pane>
  </el-tabs>
</template>

<style scoped>
</style>