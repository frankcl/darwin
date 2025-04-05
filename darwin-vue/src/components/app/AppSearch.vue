<script setup>
import {onMounted, ref} from 'vue'
import { ElOption, ElSelect } from 'element-plus'
import { asyncSearchApps } from '@/common/service'

const props = defineProps({
  'placeholder': { default: '根据应用名称搜索' }
})
const model = defineModel()
const emits = defineEmits(['change'])
const loading = ref(false)
const apps = ref([])
const appMap = new Map()

const search = async query => {
  loading.value = true
  try {
    const pager = await asyncSearchApps({ name: query })
    apps.value = pager.records
    apps.value.forEach(app => appMap.set(app.id, app))
  } finally {
    loading.value = false
  }
}

const handleChange = id => {
  const app = appMap.get(id)
  if (app) emits('change', JSON.parse(JSON.stringify(app)))
}

onMounted(() => search(''))
</script>

<template>
  <el-select v-model="model" @change="handleChange" clearable filterable remote :remote-method="search"
             :loading="loading" loading-text="搜索中 ..." :placeholder="props.placeholder || '根据应用名称搜索'">
    <el-option v-for="app in apps" :key="app.id" :label="app.name" :value="app.id"></el-option>
  </el-select>
</template>

<style scoped>

</style>