<script setup>
import { IconArrowBackUp, IconClock, IconFileDescription, IconTrash } from '@tabler/icons-vue'
import { reactive, ref, watch } from 'vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import { ElButton, ElConfigProvider, ElPagination, ElRow, ElTable, ElTableColumn } from 'element-plus'
import { useUserStore } from '@/store'
import { formatDate } from '@/common/Time'
import {
  asyncExecuteAfterConfirming,
  ERROR, showMessage, SUCCESS
} from '@/common/Feedback'
import {
  asyncRemoveHistory,
  asyncSearchHistory,
  asyncRollbackRule,
  newSearchQuery
} from '@/common/AsyncRequest'
import ViewHistory from '@/views/rule/ViewHistory'

const props = defineProps(['ruleId', 'ruleName', 'refresh'])
const emits = defineEmits(['rollback'])
const userStore = useUserStore()
const openView = ref(false)
const historyId = ref()
const historyList = ref([])
const total = ref(0)
const query = reactive(newSearchQuery({ rule_id: props.ruleId }))

const search = async () => {
  const pager = await asyncSearchHistory(query)
  historyList.value = pager.records
  total.value = pager.total
}

const view = id => {
  historyId.value = id
  openView.value = true
}

const remove = async id => {
  const success = await asyncExecuteAfterConfirming(asyncRemoveHistory, id)
  if (success === undefined) return
  if (!success) {
    showMessage('删除规则历史失败', ERROR)
    return
  }
  showMessage('删除规则历史成功', SUCCESS)
  await search()
}

const rollback = async id => {
  const request = { rule_id: props.ruleId, rule_history_id: id }
  const success = await asyncExecuteAfterConfirming(asyncRollbackRule, request)
  if (success === undefined) return
  if (!success) {
    showMessage('回滚规则失败', ERROR)
    return
  }
  showMessage('回滚规则成功', SUCCESS)
  await search()
  emits('rollback')
}

watch(() => props.ruleId, async () => query.rule_id = props.ruleId, { immediate: true })
watch(() => [props.refresh, query.rule_id], async () => await search(), { immediate: true })
</script>

<template>
  <el-table class="mb-4 ml-2 mr-2" max-height="550" :data="historyList" table-layout="auto" stripe>
    <template #empty>暂无历史数据</template>
    <el-table-column prop="version" label="版本" width="80">
      <template #default="scope">{{ scope.row.version }}</template>
    </el-table-column>
    <el-table-column prop="modifier" label="变更人" width="100" show-overflow-tooltip>
      <template #default="scope">{{ scope.row.modifier }}</template>
    </el-table-column>
    <el-table-column prop="update_time" label="变更时间" show-overflow-tooltip>
      <template #default="scope">
        <div class="d-flex align-items-center">
          <IconClock size="16" class="mr-1" />
          <span>{{ formatDate(scope.row['update_time']) }}</span>
        </div>
      </template>
    </el-table-column>
    <el-table-column prop="change_log" label="变更原因" show-overflow-tooltip>
      <template #default="scope">{{ scope.row.change_log }}</template>
    </el-table-column>
    <el-table-column width="300">
      <template #header>操作</template>
      <template #default="scope">
        <el-button type="primary" plain @click="view(scope.row.id)">
          <IconFileDescription size="20" class="mr-1" />
          <span>查看</span>
        </el-button>
        <el-button type="success" @click="rollback(scope.row.id)" :disabled="!userStore.injected">
          <IconArrowBackUp size="20" class="mr-1" />
          <span>回滚</span>
        </el-button>
        <el-button type="danger" @click="remove(scope.row.id)" :disabled="!userStore.injected">
          <IconTrash size="20" class="mr-1" />
          <span>删除</span>
        </el-button>
      </template>
    </el-table-column>
  </el-table>
  <el-row justify="center" align="middle">
    <el-config-provider :locale="zhCn">
      <el-pagination background layout="total, prev, pager, next, jumper" :total="total"
                     v-model:page-size="query.page_size" v-model:current-page="query.page_num">
      </el-pagination>
    </el-config-provider>
  </el-row>
  <view-history v-model="openView" :id="historyId" :name="ruleName" />
</template>

<style scoped>
</style>