<script setup>
import { onMounted, ref } from 'vue'
import {
  ElButton,
  ElCol,
  ElForm,
  ElFormItem,
  ElInputNumber,
  ElRow,
  ElSpace,
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

const userStore = useUserStore()
const defaultConcurrency = ref()
const concurrencyConnectionMap = ref({})
const concurrencyConnections = ref([])
const columns = [{ name: '并发单元' }, { name: '最大连接', type: 'number', min: 1, max: 100, default: 20 }]

const update = async () => {
  if (!await asyncUpdateDefaultConcurrency({ default_concurrency: defaultConcurrency.value })) {
    showMessage('更新缺省最大并发数失败', ERROR)
    return
  }
  concurrencyConnectionMap.value = {}
  concurrencyConnections.value.forEach(concurrencyConnection => {
    if (concurrencyConnection[0] !== undefined && concurrencyConnection[1] !== undefined) {
      concurrencyConnectionMap.value[concurrencyConnection[0]] = concurrencyConnection[1]
    }
  })
  if (!await asyncUpdateConcurrencyConnectionMap(concurrencyConnectionMap.value)) {
    showMessage('更新并发连接配置失败', ERROR)
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
  <el-space direction="vertical" :size="20" :fill="true" class="w100">
    <el-row align="middle">
      <span class="text-xl font-bold ml-2">并发配置</span>
    </el-row>
    <el-form ref="form" label-width="150" label-position="right">
      <el-row>
        <el-col :span="12">
          <el-row align="middle" class="h100">
            <span class="font-bold">并发单元配置列表</span>
          </el-row>
        </el-col>
        <el-col :span="12">
          <el-row justify="end">
            <el-form-item label="默认连接数">
              <el-input-number v-model="defaultConcurrency" :min="1" :max="100"
                               :disabled="!userStore.superAdmin" style="margin-right: 12px;" />
            </el-form-item>
          </el-row>
        </el-col>
      </el-row>
      <mutable-table v-model="concurrencyConnections" :columns="columns" />
      <el-row class="mt-4">
        <el-button type="primary" @click="update" :disabled="!userStore.superAdmin">保存</el-button>
        <el-button type="info" @click="reset" :disabled="!userStore.superAdmin">重置</el-button>
      </el-row>
    </el-form>
  </el-space>
</template>

<style scoped>
</style>