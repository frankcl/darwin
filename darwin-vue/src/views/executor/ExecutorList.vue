<script setup>
import { ref, watchEffect } from 'vue'
import {
  ElButton,
  ElIcon,
  ElNotification, ElRow,
  ElTable,
  ElTableColumn
} from 'element-plus'
import { Timer } from '@element-plus/icons-vue'
import { useUserStore } from '@/store'
import { executorStatusMap } from '@/common/Constants'
import { formatDate } from '@/common/Time'
import {
  asyncExecuteAfterConfirming,
  ERROR, INFO, showMessage, SUCCESS
} from '@/common/Feedback'
import {
  asyncGetExecutorList,
  asyncStartExecutor,
  asyncStopExecutor
} from '@/common/AsyncRequest'

const userStore = useUserStore()
const starting = ref()
const stopping = ref()
const executors = ref([])

const getExecutors = async () => executors.value = await asyncGetExecutorList()

const start = async executor => {
  starting.value = executor.id
  try {
    const success = await asyncExecuteAfterConfirming(asyncStartExecutor, executor.name, '是否确定启动执行器?')
    if (success === undefined) return
    if (success) {
      showMessage(`启动 ${executor.chinese_name} 成功`, SUCCESS)
      await getExecutors()
      return
    }
    showMessage(`启动 ${executor.chinese_name} 失败`, ERROR)
    await getExecutors()
  } finally {
    starting.value = undefined
  }
}

const stop = async executor => {
  stopping.value = executor.id
  try {
    const success = await asyncExecuteAfterConfirming(asyncStopExecutor, executor.name, '是否确定停止执行器?')
    if (success === undefined) return
    if (success) {
      showMessage(`停止 ${executor.chinese_name} 成功`, SUCCESS)
      await getExecutors()
      return
    }
    showMessage(`停止 ${executor.chinese_name} 失败`, ERROR)
    await getExecutors()
  } finally {
    stopping.value = undefined
  }
}

const viewError = cause => {
  if (cause) {
    ElNotification.error({
      title: '错误信息',
      message: cause || '暂无错误',
      type: 'error',
      duration: 0
    })
    return
  }
  showMessage('暂无错误信息', INFO)
}

watchEffect(async () => await getExecutors())
</script>

<template>
  <el-row class="mb-3" align="middle">
    <span class="text-xl font-bold ml-2">执行器列表</span>
  </el-row>
  <el-table :data="executors" table-layout="auto" stripe>
    <template #empty>暂无执行器</template>
    <el-table-column prop="chinese_name" label="执行器" show-overflow-tooltip>
      <template #default="scope">{{ scope.row.chinese_name }}</template>
    </el-table-column>
    <el-table-column prop="status" label="状态" width="80" show-overflow-tooltip>
      <template #default="scope">{{ executorStatusMap[scope.row.status] }}</template>
    </el-table-column>
    <el-table-column label="更新时间" prop="update_time" width="200" show-overflow-tooltip>
      <template #default="scope">
        <el-icon><timer /></el-icon>
        {{ formatDate(scope.row['update_time']) }}
      </template>
    </el-table-column>
    <el-table-column prop="comment" label="说明" show-overflow-tooltip>
      <template #default="scope">{{ scope.row.comment }}</template>
    </el-table-column>
    <el-table-column width="250">
      <template #header>操作</template>
      <template #default="scope">
        <el-button type="success" @click="start(scope.row)" :loading="starting === scope.row.id"
                   :disabled="!userStore.superAdmin">启动</el-button>
        <el-button type="danger" @click="stop(scope.row)" :loading="stopping === scope.row.id"
                   :disabled="!userStore.superAdmin">停止</el-button>
        <el-button type="warning" @click="viewError(scope.row.cause)">错误</el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<style scoped>
</style>