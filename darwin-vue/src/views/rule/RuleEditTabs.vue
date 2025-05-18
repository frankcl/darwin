<script setup>
import { IconDeviceIpadHorizontalCode, IconEdit, IconVersions } from '@tabler/icons-vue'
import { ref } from 'vue'
import { ElDivider, ElTabPane, ElTabs} from 'element-plus'
import RuleSelect from '@/components/rule/RuleSelect'
import DebugScript from '@/views/debug/DebugScript'
import EditRule from '@/views/rule/EditRule'
import HistoryList from '@/views/rule/HistoryList'

const props = defineProps(['planId'])
const ruleId = ref()
const ruleEdited = ref()
const refreshSelector = ref(Date.now())
const refreshEdit = ref(Date.now())
const refreshHistory = ref(Date.now())

const handleClear = () => ruleId.value = ruleEdited.value = undefined
const handleUpdate = () => {
  refreshSelector.value = Date.now()
  refreshHistory.value = Date.now()
}
const handleChange = rule => {
  ruleEdited.value = rule
}
const handleRemove = () => {
  handleClear()
  handleUpdate()
}
</script>

<template>
  <div class="d-flex align-items-center mb-4">
    <label class="mr-4 fs-14px flex-shrink-0">请选择规则</label>
    <rule-select v-model="ruleId" :plan-id="props.planId" :refresh="refreshSelector" @clear="handleClear" />
  </div>
  <el-divider></el-divider>
  <el-tabs v-if="ruleId" tab-position="left" class="rule-tabs">
    <el-tab-pane label="编辑">
      <template #label>
        <IconEdit size="20" />
        <span class="ml-2">编辑</span>
      </template>
      <edit-rule :id="ruleId" :refresh="refreshEdit" @update="handleUpdate"
                 @change="handleChange" @remove="handleRemove" />
    </el-tab-pane>
    <el-tab-pane label="调试">
      <template #label>
        <IconDeviceIpadHorizontalCode size="20" />
        <span class="ml-2">调试</span>
      </template>
      <debug-script v-bind="ruleEdited" />
    </el-tab-pane>
    <el-tab-pane label="版本">
      <template #label>
        <IconVersions size="20" />
        <span class="ml-2">版本</span>
      </template>
      <history-list :rule-id="ruleId" :rule-name="ruleEdited ? ruleEdited.name : ''"
                    :refresh="refreshHistory" @rollback="refreshEdit = Date.now()" />
    </el-tab-pane>
  </el-tabs>
</template>

<style scoped>
</style>