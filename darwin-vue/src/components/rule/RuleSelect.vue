<script setup>
import { ref, watch } from 'vue'
import { ElOption, ElSelect } from 'element-plus'
import { asyncGetPlanRules } from '@/common/AsyncRequest'

const props = defineProps({
  planId: { required: true },
  refresh: { default: Date.now() },
  placeholder: { default: '请选择规则' },
  clearable: { default: true }
})
const emits = defineEmits(['change', 'clear'])
const selected = defineModel()
const rules = ref([])
const ruleMap = new Map()

const handleChange = id => {
  const rule = ruleMap.get(id)
  if (rule) emits('change', JSON.parse(JSON.stringify(rule)))
}

watch(() => [props.planId, props.refresh], async () => {
  ruleMap.clear()
  rules.value = await asyncGetPlanRules(props.planId)
  rules.value.forEach(rule => ruleMap.set(rule.id, rule))
  if (!selected.value && rules.value.length > 0) selected.value = rules.value[0].id
}, { immediate: true })
</script>

<template>
  <el-select v-model="selected" @change="handleChange" @clear="emits('clear')"
             filterable :clearable="props.clearable" :placeholder="props.placeholder">
    <el-option v-for="rule in rules" :key="rule.id" :label="rule.name" :value="rule.id" />
  </el-select>
</template>

<style scoped>

</style>