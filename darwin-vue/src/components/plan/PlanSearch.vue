<script setup>
import { onMounted, ref } from 'vue'
import { ElOption, ElSelect } from 'element-plus'
import { asyncSearchPlan } from '@/common/AsyncRequest'

const props = defineProps({
  'placeholder': { default: '根据计划名搜索' }
})
const planId = defineModel()
const emits = defineEmits(['change'])
const loading = ref(false)
const plans = ref([])
const planMap = new Map()

const search = async query => {
  loading.value = true
  try {
    planMap.clear()
    const pager = await asyncSearchPlan({ name: query })
    plans.value = pager.records
    plans.value.forEach(plan => planMap.set(plan.plan_id, plan))
  } finally {
    loading.value = false
  }
}

const handleChange = id => {
  const plan = planMap.get(id)
  if (plan) emits('change', JSON.parse(JSON.stringify(plan)))
}

onMounted(() => search(''))
</script>

<template>
  <el-select v-model="planId" @change="handleChange" clearable filterable remote :remote-method="search"
             :loading="loading" loading-text="搜索中 ..." :placeholder="props.placeholder">
    <el-option v-for="plan in plans" :key="plan.plan_id" :label="plan.name" :value="plan.plan_id" />
  </el-select>
</template>

<style scoped>

</style>