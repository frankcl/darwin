<script setup>
import { computed, ref, watchEffect } from 'vue'
import { useRoute } from 'vue-router'
import {
  ElBadge,
  ElButton, ElLoading,
  ElNotification, ElRow,
  ElTable,
  ElTableColumn, ElText
} from 'element-plus'
import { useUserStore } from '@/store'
import {
  asyncExecuteAfterConfirming,
  ERROR, showMessage, SUCCESS
} from '@/common/Feedback'
import {
  asyncRunnerMessageCount,
  asyncRunnerRunning,
  asyncGetRunners,
  asyncPopMessage,
  asyncStartRunner,
  asyncStopRunner
} from '@/common/AsyncRequest'

const route = useRoute()
const title = computed(() => route.query.type === '1' ? '核心进程' : '监控进程')
const vLoading = ElLoading.directive
const userStore = useUserStore()
const starting = ref()
const stopping = ref()
const runners = ref([])

const getRunners = async type => {
  if (type) {
    runners.value = await asyncGetRunners(type)
    runners.value.forEach(runner => getRunnerRunning(runner))
  }
}

const getRunnerRunning = async runner => {
  runner.loading = true
  runner.status = await asyncRunnerRunning(runner.key)
  runner.loading = false
}

const getRunnerMessageCount = async key => await asyncRunnerMessageCount(key)

const start = async runner => {
  starting.value = runner.key
  try {
    const success = await asyncExecuteAfterConfirming(asyncStartRunner, runner.key, '是否确定启动执行器?')
    if (success === undefined) return
    if (success) {
      showMessage(`启动 ${runner.name} 成功`, SUCCESS)
      await getRunnerRunning(runner)
      return
    }
    showMessage(`启动 ${runner.name} 失败`, ERROR)
    await getRunnerRunning(runner)
  } finally {
    starting.value = undefined
  }
}

const stop = async runner => {
  stopping.value = runner.key
  try {
    const success = await asyncExecuteAfterConfirming(asyncStopRunner, runner.key, '是否确定停止执行器?')
    if (success === undefined) return
    if (success) {
      showMessage(`停止 ${runner.name} 成功`, SUCCESS)
      await getRunnerRunning(runner)
      return
    }
    showMessage(`停止 ${runner.name} 失败`, ERROR)
    await getRunnerRunning(runner)
  } finally {
    stopping.value = undefined
  }
}

const popMessage = async runner => {
  const message = await asyncPopMessage(runner.key)
  ElNotification.error({
    title: '错误消息',
    message: message.message,
    type: 'error',
    duration: 0
  })
  runner.message_num = await getRunnerMessageCount(runner.key)
}

watchEffect(async () => await getRunners(route.query.type))
</script>

<template>
  <el-row class="mb-3" align="middle">
    <span class="text-xl font-bold ml-2">{{ title }}</span>
  </el-row>
  <el-table :data="runners" table-layout="auto" stripe>
    <template #empty>暂无执行进程</template>
    <el-table-column prop="name" label="进程" show-overflow-tooltip>
      <template #default="scope">{{ scope.row.name }}</template>
    </el-table-column>
    <el-table-column prop="status" label="状态" width="80" show-overflow-tooltip>
      <template #default="scope">
        <el-text v-loading="scope.row.loading">
          <span v-if="scope.row.status === undefined">未知</span>
          <span v-else-if="scope.row.status">运行</span>
          <span v-else>停止</span>
        </el-text>
      </template>
    </el-table-column>
    <el-table-column prop="description" label="说明" show-overflow-tooltip>
      <template #default="scope">{{ scope.row.description }}</template>
    </el-table-column>
    <el-table-column width="200">
      <template #header>操作</template>
      <template #default="scope">
        <el-button v-if="!scope.row.status" type="success" @click="start(scope.row)" :loading="starting === scope.row.key"
                   :disabled="!userStore.superAdmin">启动</el-button>
        <el-button v-else type="danger" @click="stop(scope.row)" :loading="stopping === scope.row.key"
                   :disabled="!userStore.superAdmin">停止</el-button>
        <el-badge v-if="scope.row.message_num > 0" :value="scope.row.message_num"
                  class="badge-button">
          <el-button type="warning" @click="popMessage(scope.row)"
                     :disabled="!userStore.superAdmin">异常</el-button>
        </el-badge>
        <el-button v-else type="warning" @click="popMessage(scope.row)"
                   :disabled="!userStore.superAdmin || scope.row.message_num === 0">异常</el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<style scoped>
.badge-button {
  margin-top: 10px;
  margin-bottom: 10px;
  margin-left: 12px;
}
</style>