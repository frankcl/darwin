<script setup>
import { IconCircleCheck, IconRefresh } from '@tabler/icons-vue'
import { computed, onMounted, ref } from 'vue'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElInputNumber, ElRow
} from 'element-plus'
import { useUserStore } from '@/store'
import MutableTable from '@/components/data/MutableTable'
import {
  asyncConcurrencyConnectionMap,
  asyncDefaultConcurrency,
  asyncUpdateConcurrencyConnectionMap,
  asyncUpdateDefaultConcurrency
} from '@/common/AsyncRequest'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import DarwinCard from '@/components/data/Card'
import TableHead from '@/components/data/TableHead'

const userStore = useUserStore()
const defaultConcurrency = ref()
const concurrencyConnectionMap = ref({})
const concurrencyConnections = ref([])
const columns = computed(() => {
  return [
    { name: '并发单元' },
    {
      name: '最大连接', type: 'number', min: 1, max: 100,
      default: defaultConcurrency.value ? defaultConcurrency.value : 5
    }
  ]
})

const update = async () => {
  if (!await asyncUpdateDefaultConcurrency({ default_concurrency: defaultConcurrency.value })) {
    showMessage('更新默认连接数失败', ERROR)
    return
  }
  concurrencyConnectionMap.value = {}
  concurrencyConnections.value.forEach(concurrencyConnection => {
    if (concurrencyConnection[0] !== undefined && concurrencyConnection[1] !== undefined) {
      concurrencyConnectionMap.value[concurrencyConnection[0]] = concurrencyConnection[1]
    }
  })
  if (!await asyncUpdateConcurrencyConnectionMap(concurrencyConnectionMap.value)) {
    showMessage('更新并发配置失败', ERROR)
    return
  }
  showMessage('更新成功', SUCCESS)
  await reset()
}

const reset = async () => {
  defaultConcurrency.value = await asyncDefaultConcurrency()
  concurrencyConnectionMap.value = await asyncConcurrencyConnectionMap()
  concurrencyConnections.value.splice(0, concurrencyConnections.value.length)
  Object.keys(concurrencyConnectionMap.value).forEach(key => {
    concurrencyConnections.value.push([key, concurrencyConnectionMap.value[key]])
  })
}

onMounted(async () => await reset())
</script>

<template>
  <darwin-card>
    <template #title>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>抓取控制</el-breadcrumb-item>
        <el-breadcrumb-item>并发控制</el-breadcrumb-item>
      </el-breadcrumb>
    </template>
    <table-head title="并发配置">
      <template #right>
        <label class="mr-4 fs-14px">默认连接数</label>
        <el-input-number v-model="defaultConcurrency" :min="1" :max="100"
                         :disabled="!userStore.superAdmin" style="margin-right: 12px;" />
      </template>
    </table-head>
    <mutable-table v-model="concurrencyConnections" :columns="columns" />
    <el-row class="mt-4">
      <el-button type="primary" @click="update" :disabled="!userStore.superAdmin">
        <IconCircleCheck size="20" class="mr-1" />
        <span>保存</span>
      </el-button>
      <el-button type="info" @click="reset" :disabled="!userStore.superAdmin">
        <IconRefresh size="20" class="mr-1" />
        <span>重置</span>
      </el-button>
    </el-row>
  </darwin-card>
</template>

<style scoped>
</style>