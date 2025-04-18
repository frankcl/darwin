<script setup>
import { ref } from 'vue'
import { ElCol, ElDivider, ElFormItem, ElTabPane, ElTabs } from 'element-plus'
import RuleSelect from '@/components/rule/RuleSelect'
import EditRule from '@/views/rule/EditRule'
import DebugRule from '@/views/rule/DebugRule'
import HistoryList from '@/views/rule/HistoryList'

const props = defineProps(['planId'])
const ruleId = ref()
const ruleChanged = ref()
const refreshSelector = ref(Date.now())
const refreshEdit = ref(Date.now())
const refreshHistory = ref(Date.now())

const handleClear = () => ruleId.value = ruleChanged.value = undefined
const handleRefresh = () => {
  refreshSelector.value = Date.now()
  refreshHistory.value = Date.now()
}
const handleRuleChange = rule => {
  ruleChanged.value = rule
}
const handleRuleRemove = () => {
  handleClear()
  handleRefresh()
}
</script>

<template>
    <el-form-item label="请选择规则">
      <el-col :span="8">
        <rule-select v-model="ruleId" :plan-id="props.planId"
                     :force-refresh="refreshSelector" @clear="handleClear" />
      </el-col>
    </el-form-item>
    <el-divider></el-divider>
    <el-tabs v-if="ruleId" tab-position="left" class="rule-tabs">
      <el-tab-pane label="编辑">
        <edit-rule :id="ruleId" :refresh="refreshEdit"
                   @update="handleRefresh" @change="handleRuleChange" @remove="handleRuleRemove" />
      </el-tab-pane>
      <el-tab-pane label="调试">
        <debug-rule v-bind="ruleChanged" />
      </el-tab-pane>
      <el-tab-pane label="版本">
        <history-list :rule-id="ruleId" :rule-name="ruleChanged ? ruleChanged.name : ''"
                      :refresh="refreshHistory" @rollback="refreshEdit = Date.now()" />
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