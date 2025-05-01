<script setup>
import { ref } from 'vue'
import { ElDivider, ElTabPane, ElTabs } from 'element-plus'
import DebugScript from '@/views/debug/DebugScript'
import AddRule from '@/views/rule/AddRule'

const props = defineProps(['planId'])
const ruleChanged = ref()

const handleRuleChange = rule => ruleChanged.value = rule
</script>

<template>
  <el-divider></el-divider>
  <el-tabs tab-position="left" class="rule-tabs">
    <el-tab-pane label="新增">
      <add-rule :plan-id="props.planId" @change="handleRuleChange" />
    </el-tab-pane>
    <el-tab-pane label="调试">
      <debug-script v-bind="ruleChanged" />
    </el-tab-pane>
  </el-tabs>
</template>

<style scoped>
.rule-tabs > .el-tabs__content {
  padding: 32px;
  color: #6b778c;
  font-size: 32px;
  font-weight: 600;
}
.el-tabs--right .el-tabs__content,
.el-tabs--left .el-tabs__content {
  height: 100%;
}
</style>