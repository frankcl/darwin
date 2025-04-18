<script setup>
import { reactive, ref, watch } from 'vue'
import { ElButton, ElPagination, ElRow, ElSpace, ElTable, ElTableColumn } from 'element-plus'
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
const openViewDialog = ref(false)
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
  openViewDialog.value = true
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

watch(() => [ props.refresh, props.ruleId, query ], async () => query.rule_id = props.ruleId, { immediate: true })
watch(() => [props.refresh, query], async () => await search(), { immediate: true })
</script>

<template>
  <el-space direction="vertical" :size="20" :fill="true" class="w100">
    <el-table :data="historyList" table-layout="auto" stripe>
      <template #empty>暂无历史版本</template>
      <el-table-column prop="version" label="版本" width="80">
        <template #default="scope">{{ scope.row.version }}</template>
      </el-table-column>
      <el-table-column prop="modifier" label="变更人" width="80" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.modifier }}</template>
      </el-table-column>
      <el-table-column prop="update_time" label="变更时间" width="180" show-overflow-tooltip>
        <template #default="scope">{{ formatDate(scope.row['update_time']) }}</template>
      </el-table-column>
      <el-table-column prop="change_log" label="变更原因" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.change_log }}</template>
      </el-table-column>
      <el-table-column width="230">
        <template #header>操作</template>
        <template #default="scope">
          <el-button type="primary" @click="view(scope.row.id)">查看</el-button>
          <el-button type="success" @click="rollback(scope.row.id)" :disabled="!userStore.injected">回滚</el-button>
          <el-button type="danger" @click="remove(scope.row.id)" :disabled="!userStore.injected">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-row justify="center" align="middle">
      <el-pagination background layout="prev, pager, next" :total="total"
                     v-model:page-size="query.size" v-model:current-page="query.current">
      </el-pagination>
    </el-row>
  </el-space>
  <view-history v-model="openViewDialog" :id="historyId" :name="ruleName" />
</template>

<style scoped>
</style>