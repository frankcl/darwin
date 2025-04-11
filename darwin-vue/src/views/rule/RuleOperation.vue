<script setup>
import { ref } from 'vue'
import { ElFormItem, ElRadio, ElRadioGroup } from 'element-plus'
import RuleAddTabs from '@/views/rule/RuleAddTabs'
import RuleEditTabs from '@/views/rule/RuleEditTabs'

const props = defineProps(['planId'])
const operator = ref(true)
</script>

<template>
  <el-form-item label="请选择操作">
    <el-radio-group v-model="operator">
      <el-radio :value="true">编辑</el-radio>
      <el-radio :value="false">新增</el-radio>
    </el-radio-group>
  </el-form-item>
  <rule-edit-tabs v-if="operator" :plan-id="planId" />
  <rule-add-tabs v-else :plan-id="planId" />
</template>

<style scoped>
</style>