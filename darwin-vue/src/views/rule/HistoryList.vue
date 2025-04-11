<script setup>
import { format } from 'date-fns'
import { reactive, ref, watch } from 'vue'
import { ElLink, ElNotification, ElPagination, ElRow, ElSpace, ElTable, ElTableColumn } from 'element-plus'
import { executeAsyncRequestAfterConfirm } from '@/common/assortment'
import { asyncDeleteHistory, asyncGetHistoryList, asyncRollbackRule } from '@/common/service'
import History from '@/views/rule/History'

const props = defineProps(['ruleId', 'ruleName', 'refresh'])
const emits = defineEmits(['rollback'])
const openViewDialog = ref(false)
const historyId = ref()
const historyList = ref([])
const total = ref(0)
const query = reactive({
  current: 1,
  size: 10,
  rule_id: props.ruleId
})

const getHistoryList = async () => {
  const pager = await asyncGetHistoryList(query)
  total.value = pager.total
  historyList.value = pager.records
}

const viewHistory = id => {
  historyId.value = id
  openViewDialog.value = true
}

const remove = async id => {
  const successHandle = () => ElNotification.success('删除规则历史成功')
  const failHandle = () => ElNotification.error('删除规则历史失败')
  if (!await executeAsyncRequestAfterConfirm(
    '删除提示', '是否确定删除规则历史？', asyncDeleteHistory, id, successHandle, failHandle)) return
  await getHistoryList()
}

const rollback = async id => {
  const request = { rule_id: props.ruleId, rule_history_id: id }
  const successHandle = () => ElNotification.success('回滚计划成功')
  const failHandle = () => ElNotification.error('回滚计划失败')
  if (!await executeAsyncRequestAfterConfirm(
    '回滚提示', '是否确定回滚至此版本规则？', asyncRollbackRule, request, successHandle, failHandle)) return
  await getHistoryList()
  emits('rollback')
}

watch(() => [ props.refresh, props.ruleId, query ], () => {
  query.rule_id = props.ruleId
  getHistoryList()
}, { immediate: true })
</script>

<template>
  <el-space direction="vertical" :size="20" :fill="true" class="w100">
    <el-table ref="tableRef" :data="historyList" table-layout="auto" stripe>
      <template #empty>暂无历史版本</template>
      <el-table-column prop="version" label="版本" width="80">
        <template #default="scope">
          {{ scope.row.version }}
        </template>
      </el-table-column>
      <el-table-column prop="modifier" label="变更人" width="80" show-overflow-tooltip>
        <template #default="scope">
          {{ scope.row.modifier }}
        </template>
      </el-table-column>
      <el-table-column prop="update_time" label="变更时间" width="180" show-overflow-tooltip>
        <template #default="scope">
          {{ format(new Date(scope.row['update_time']), 'yyyy-MM-dd HH:mm:ss') }}
        </template>
      </el-table-column>
      <el-table-column prop="change_log" label="变更原因" show-overflow-tooltip>
        <template #default="scope">
          {{ scope.row.change_log }}
        </template>
      </el-table-column>
      <el-table-column width="180">
        <template #header>
          操作
        </template>
        <template #default="scope">
          <el-link @click="viewHistory(scope.row.id)">查看</el-link>
          &nbsp;&nbsp;
          <el-link @click="rollback(scope.row.id)">回滚</el-link>
          &nbsp;&nbsp;
          <el-link @click="remove(scope.row.id)">删除</el-link>
        </template>
      </el-table-column>
    </el-table>
    <el-row justify="center" align="middle">
      <el-pagination background layout="prev, pager, next" :total="total"
                     v-model:page-size="query.size" v-model:current-page="query.current">
      </el-pagination>
    </el-row>
  </el-space>
  <history v-model="openViewDialog" :id="historyId" :name="ruleName" />
</template>

<style scoped>
</style>