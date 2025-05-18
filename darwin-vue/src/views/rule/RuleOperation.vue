<script setup>
import { ref } from 'vue'
import { ElRadio, ElRadioGroup } from 'element-plus'
import RuleAddTabs from '@/views/rule/RuleAddTabs'
import RuleEditTabs from '@/views/rule/RuleEditTabs'

defineProps(['planId'])
const edit = ref(true)
</script>

<template>
  <div class="d-flex align-items-center mb-4">
    <label class="mr-4 fs-14px flex-shrink-0">请选择操作</label>
    <el-radio-group v-model="edit" label="请选择操作">
      <el-radio :value="true">编辑</el-radio>
      <el-radio :value="false">新增</el-radio>
    </el-radio-group>
  </div>
  <rule-edit-tabs v-if="edit" :plan-id="planId" />
  <rule-add-tabs v-else :plan-id="planId" @add="edit = true" />
</template>

<style scoped>
</style>