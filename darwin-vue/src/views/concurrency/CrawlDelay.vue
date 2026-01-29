<script setup>
import { IconCircleCheck, IconRefresh } from '@tabler/icons-vue'
import { computed, onMounted, ref } from 'vue'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElInputNumber, ElRow
} from 'element-plus'
import { useUserStore } from '@/store'
import MutableTable from '@/components/data/MutableTable'
import {
  asyncCrawlDelayMap,
  asyncDefaultCrawlDelay,
  asyncUpdateCrawlDelayMap,
  asyncUpdateDefaultCrawlDelay
} from '@/common/AsyncRequest'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import DarwinCard from '@/components/data/Card'
import TableHead from '@/components/data/TableHead'

const userStore = useUserStore()
const defaultCrawlDelay = ref()
const crawlDelayMap = ref({})
const crawlDelays = ref([])
const columns = computed(() => {
  return [
    { name: '并发单元' },
    {
      name: '抓取间隔（ms）', type: 'number', min: 100, max: 20000, step: 100,
      default: defaultCrawlDelay.value ? defaultCrawlDelay.value : 1000
    }
  ]
})

const update = async () => {
  if (!await asyncUpdateDefaultCrawlDelay({ default_crawl_delay: defaultCrawlDelay.value })) {
    showMessage('更新默认抓取间隔失败', ERROR)
    return
  }
  crawlDelayMap.value = {}
  crawlDelays.value.forEach(crawlDelay => {
    if (crawlDelay[0] !== undefined && crawlDelay[1] !== undefined) {
      crawlDelayMap.value[crawlDelay[0]] = crawlDelay[1]
    }
  })
  if (!await asyncUpdateCrawlDelayMap(crawlDelayMap.value)) {
    showMessage('更新抓取间隔配置失败', ERROR)
    return
  }
  showMessage('更新成功', SUCCESS)
  await reset()
}

const reset = async () => {
  defaultCrawlDelay.value = await asyncDefaultCrawlDelay()
  crawlDelayMap.value = await asyncCrawlDelayMap()
  crawlDelays.value.splice(0, crawlDelays.value.length)
  Object.keys(crawlDelayMap.value).forEach(key => {
    crawlDelays.value.push([key, crawlDelayMap.value[key]])
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
        <el-breadcrumb-item>抓取间隔</el-breadcrumb-item>
      </el-breadcrumb>
    </template>
    <table-head title="抓取间隔配置">
      <template #right>
        <label class="mr-4 fs-14px">默认抓取间隔</label>
        <el-input-number v-model="defaultCrawlDelay" :min="100" :max="20000" :step="100"
                         :disabled="!userStore.superAdmin" style="margin-right: 12px;" />
      </template>
    </table-head>
    <mutable-table v-model="crawlDelays" :columns="columns" />
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