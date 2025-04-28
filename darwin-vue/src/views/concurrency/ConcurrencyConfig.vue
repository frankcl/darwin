<script setup>
import { onMounted, ref, watch } from 'vue'
import {
  ElButton,
  ElCol,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElRow,
  ElSpace,
  ElTable,
  ElTableColumn, ElText
} from 'element-plus'
import { useUserStore } from '@/store'
import {
  asyncConcurrencyConnectionMap,
  asyncDefaultConcurrency,
  asyncUpdateConcurrencyConnectionMap,
  asyncUpdateDefaultConcurrency
} from '@/common/AsyncRequest'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'

const userStore = useUserStore()
const searchQuery = ref()
const prepareIndex = ref()
const defaultConcurrency = ref()
const concurrencyConnectionMap = ref({})
const concurrencyConnections = ref([])

const prepare = index => {
  if (prepareIndex.value && !concurrencyConnections.value[prepareIndex.value].key) {
    showMessage('请输入并发单元', ERROR)
    return
  }
  prepareIndex.value = index
}
const add = () => {
  const lastIndex = concurrencyConnections.value.length - 1
  if (lastIndex >= 0 && !concurrencyConnections.value[lastIndex].key) {
    remove(lastIndex)
    showMessage('请输入并发单元', ERROR)
  }
  prepareIndex.value = concurrencyConnections.value.push({ key: null, value: defaultConcurrency.value, show: true }) - 1
}
const remove = index => {
  concurrencyConnections.value.splice(index, 1)
  if (prepareIndex.value === index) prepareIndex.value = undefined
}
const save = index => {
  if (!concurrencyConnections.value[index].key) {
    showMessage('请输入并发单元', ERROR)
    return
  }
  prepareIndex.value = undefined
}
const handleRowClassName = record => record.row.show ? '' : 'hidden-row'

const update = async () => {
  if (!await asyncUpdateDefaultConcurrency({ max_concurrency: defaultConcurrency.value })) {
    showMessage('更新缺省最大并发数失败', ERROR)
    return
  }
  concurrencyConnectionMap.value = {}
  concurrencyConnections.value.forEach(o => {
    if (o.key && o.value) concurrencyConnectionMap.value[o.key] = o.value
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
    concurrencyConnections.value.push({ key: key, value: concurrencyConnectionMap.value[key], show: true })
  })
  prepareIndex.value = undefined
}

watch(() => searchQuery.value, () => {
  concurrencyConnections.value.forEach(concurrencyConnection => {
    if (!searchQuery.value) concurrencyConnection.show = true
    else concurrencyConnection.show = concurrencyConnection.key === searchQuery.value
  })
})
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
      <el-table :data="concurrencyConnections" max-height="850" table-layout="auto"
                stripe :row-class-name="handleRowClassName">
        <template #empty>暂无并发单元配置</template>
        <el-table-column label="并发单元" show-overflow-tooltip>
          <template #default="scope">
            <el-input v-if="scope.$index === prepareIndex" v-model="scope.row.key"
                      clearable placeholder="请输入并发单元"/>
            <el-text v-else>{{ scope.row.key }}</el-text>
          </template>
        </el-table-column>
        <el-table-column label="最大连接" show-overflow-tooltip>
          <template #default="scope">
            <el-input-number v-if="scope.$index === prepareIndex"
                             v-model="scope.row.value" :min="1" :max="100" clearable />
            <el-text v-else>{{ scope.row.value }}</el-text>
          </template>
        </el-table-column>
        <el-table-column width="250" align="center">
          <template #header>
            <el-input v-model="searchQuery" placeholder="搜索并发单元" @clear="searchQuery = undefined" clearable />
          </template>
          <template #default="scope">
            <el-button v-if="scope.$index === prepareIndex" type="primary" @click="save(scope.$index)">保存</el-button>
            <el-button v-else type="primary" @click="prepare(scope.$index)" :disabled="!userStore.superAdmin">修改</el-button>
            <el-button type="danger" @click="remove(scope.$index)" :disabled="!userStore.superAdmin">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-button type="primary" class="w100 mt-4" @click="add" :disabled="!userStore.superAdmin">新增并发单元</el-button>
      <el-row class="mt-4">
        <el-button type="primary" @click="update" :disabled="!userStore.superAdmin">保存</el-button>
        <el-button type="info" @click="reset" :disabled="!userStore.superAdmin">重置</el-button>
      </el-row>
    </el-form>
  </el-space>
</template>

<style scoped>
:deep(.el-table .hidden-row) {
  display: none !important;
}
</style>