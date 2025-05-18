<script setup>
import {
  IconCircleX, IconExclamationCircleFilled, IconPlayerPlayFilled,
  IconPlayerStopFilled, IconProgress, IconProgressHelp
} from '@tabler/icons-vue'
import { computed, ref, watchEffect } from 'vue'
import { useRoute } from 'vue-router'
import {
  ElBadge, ElBreadcrumb, ElBreadcrumbItem,
  ElButton, ElLoading,
  ElNotification,
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
import DarwinCard from '@/components/data/Card'
import TableHead from '@/components/data/TableHead'

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
  <darwin-card>
    <template #title>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>后台进程</el-breadcrumb-item>
        <el-breadcrumb-item>{{ title }}</el-breadcrumb-item>
      </el-breadcrumb>
    </template>
    <table-head title="进程列表" />
    <el-table :data="runners" table-layout="auto" stripe>
      <template #empty>暂无后台进程</template>
      <el-table-column prop="name" label="进程" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.name }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120" show-overflow-tooltip>
        <template #default="scope">
          <el-text v-loading="scope.row.loading">
            <div v-if="scope.row.status === undefined" class="d-flex align-items-center">
              <IconProgressHelp size="20" color="#e6a23c" class="mr-1" />
              <span>未知</span>
            </div>
            <div v-else-if="scope.row.status" class="d-flex align-items-center">
              <IconProgress size="20" color="#95D475" class="mr-1" />
              <span>运行</span>
            </div>
            <div v-else class="d-flex align-items-center">
              <IconCircleX size="20" color="#f56c6c" class="mr-1" />
              <span>停止</span>
            </div>
          </el-text>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="说明" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.description }}</template>
      </el-table-column>
      <el-table-column width="240">
        <template #header>操作</template>
        <template #default="scope">
          <el-button v-if="!scope.row.status" type="success" @click="start(scope.row)" :loading="starting === scope.row.key"
                     :disabled="!userStore.superAdmin">
            <IconPlayerPlayFilled size="20" class="mr-1" />
            <span>启动</span>
          </el-button>
          <el-button v-else type="danger" @click="stop(scope.row)" :loading="stopping === scope.row.key"
                     :disabled="!userStore.superAdmin">
            <IconPlayerStopFilled size="20" class="mr-1" />
            <span>停止</span>
          </el-button>
          <el-badge v-if="scope.row.message_num > 0" :value="scope.row.message_num"
                    class="badge-button">
            <el-button type="warning" @click="popMessage(scope.row)"
                       :disabled="!userStore.superAdmin">
              <IconExclamationCircleFilled size="20" class="mr-1" />
              <span>异常</span>
            </el-button>
          </el-badge>
          <el-button v-else type="warning" @click="popMessage(scope.row)"
                     :disabled="!userStore.superAdmin || scope.row.message_num === 0">
            <IconExclamationCircleFilled size="20" class="mr-1" />
            <span>异常</span>
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </darwin-card>
</template>

<style scoped>
.badge-button {
  margin-top: 10px;
  margin-bottom: 10px;
  margin-left: 12px;
}
</style>