<script setup>
import { onMounted, ref } from 'vue'
import { ElOption, ElSelect } from 'element-plus'
import { useUserStore } from '@/store'
import { asyncSearchApp } from '@/common/AsyncRequest'

const props = defineProps({
  'permissionCheck': { default: false },
  'placeholder': { default: '根据应用名搜索' }
})
const appId = defineModel()
const emits = defineEmits(['change'])
const userStore = useUserStore()
const loading = ref(false)
const apps = ref([])
const appMap = new Map()

const search = async query => {
  loading.value = true
  try {
    appMap.clear()
    const searchRequest = { name: query }
    if (props.permissionCheck !== undefined && props.permissionCheck) {
      if (userStore.injected && !userStore.superAdmin) searchRequest.app_ids = userStore.apps
    }
    const pager = await asyncSearchApp(searchRequest)
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
  <el-select v-model="appId" @change="handleChange" clearable filterable remote :remote-method="search"
             :loading="loading" loading-text="搜索中 ..." :placeholder="props.placeholder">
    <el-option v-for="app in apps" :key="app.id" :label="app.name" :value="app.id" />
  </el-select>
</template>

<style scoped>

</style>