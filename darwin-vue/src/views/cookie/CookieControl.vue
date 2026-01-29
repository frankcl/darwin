<script setup>
import { IconCircleCheck, IconRefresh } from '@tabler/icons-vue'
import { computed, onMounted, ref } from 'vue'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElRow
} from 'element-plus'
import { useUserStore } from '@/store'
import MutableTable from '@/components/data/MutableTable'
import {
  asyncGetCookieMap,
  asyncUpdateCookieMap
} from '@/common/AsyncRequest'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import DarwinCard from '@/components/data/Card'
import TableHead from '@/components/data/TableHead'

const userStore = useUserStore()
const cookieMap = ref({})
const cookies = ref([])
const columns = computed(() => {
  return [
    { name: 'Cookie单元' },
    { name: 'Cookie值' }
  ]
})

const update = async () => {
  cookieMap.value = {}
  cookies.value.forEach(cookie => {
    if (cookie[0] !== undefined && cookie[1] !== undefined) {
      cookieMap.value[cookie[0]] = cookie[1]
    }
  })
  if (!await asyncUpdateCookieMap(cookieMap.value)) {
    showMessage('更新Cookie配置失败', ERROR)
    return
  }
  showMessage('更新成功', SUCCESS)
  await reset()
}

const reset = async () => {
  cookieMap.value = await asyncGetCookieMap()
  cookies.value.splice(0, cookies.value.length)
  Object.keys(cookieMap.value).forEach(key => {
    cookies.value.push([key, cookieMap.value[key]])
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
        <el-breadcrumb-item>Cookie管理</el-breadcrumb-item>
      </el-breadcrumb>
    </template>
    <table-head title="Cookie管理"></table-head>
    <mutable-table v-model="cookies" :columns="columns" />
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