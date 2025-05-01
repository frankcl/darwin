<script setup>
import { reactive, ref, watchEffect } from 'vue'
import {
  ElButton, ElCol, ElForm, ElFormItem, ElIcon, ElInput, ElPagination,
  ElPopover, ElRow, ElSpace, ElTable, ElTableColumn, ElText
} from 'element-plus'
import { useUserStore } from '@/store'
import { CopyDocument, DocumentCopy } from '@element-plus/icons-vue'
import { writeClipboard } from '@/common/Clipboard'
import {
  asyncExecuteAfterConfirming,
  ERROR, showMessage, SUCCESS
} from '@/common/Feedback'
import {
  asyncRemoveSeed,
  asyncSearchSeed, changeSearchQuerySort,
  newSearchQuery,
  newSearchRequest
} from '@/common/AsyncRequest'
import DebugSeed from '@/views/debug/DebugSeed'
import AddSeed from '@/views/seed/AddSeed'
import EditSeed from '@/views/seed/EditSeed'

const props = defineProps(['planId'])
const userStore = useUserStore()
const openAddDialog = ref(false)
const openEditDialog = ref(false)
const openDebugDialog = ref(false)
const seedKey = ref()
const copiedURL = ref()
const seeds = ref([])
const total = ref(0)
const query = reactive(newSearchQuery())

const search = async () => {
  const request = newSearchRequest(query)
  if (query.url) request.url = query.url
  if (query.plan_id) request.plan_id = query.plan_id
  const pager = await asyncSearchSeed(request)
  total.value = pager.total
  seeds.value = pager.records
}

const edit = key => {
  seedKey.value = key
  openEditDialog.value = true
}

const debug = key => {
  seedKey.value = key
  openDebugDialog.value = true
}

const remove = async key => {
  const success = await asyncExecuteAfterConfirming(asyncRemoveSeed, key)
  if (success === undefined) return
  if (!success) {
    showMessage('删除种子URL失败', ERROR)
    return
  }
  showMessage('删除种子URL成功', SUCCESS)
  await search()
}

const copy = async seed => {
  await writeClipboard(seed.url)
  copiedURL.value = `URL#${seed.key}`
  showMessage('复制种子URL成功', SUCCESS)
}

watchEffect( () => query.plan_id = props.planId)
watchEffect(async () => await search())
</script>

<template>
  <el-space direction="vertical" :size="20" :fill="true" class="w100">
    <el-form :model="query" ref="filterForm" label-width="50px">
      <el-col :span="12">
        <el-form-item label="搜索" prop="url">
          <el-input v-model="query.url" clearable placeholder="搜索种子URL" />
        </el-form-item>
      </el-col>
    </el-form>
    <el-row align="middle">
      <el-col :span="12">
        <span class="text-xl font-bold ml-2">种子列表</span>
      </el-col>
      <el-col :span="12">
        <el-row justify="end">
          <el-button type="primary" @click="openAddDialog = true" :disabled="!userStore.injected">新增种子</el-button>
        </el-row>
      </el-col>
    </el-row>
    <el-table :data="seeds" max-height="850" table-layout="auto"
              stripe @sort-change="event => changeSearchQuerySort(event.prop, event.order, query)">
      <template #empty>暂无种子URL</template>
      <el-table-column prop="url" label="种子URL" show-overflow-tooltip>
        <template #default="scope">
          <el-icon v-if="copiedURL === `URL#${scope.row.key}`"><document-copy /></el-icon>
          <el-popover v-else content="点击复制">
            <template #reference>
              <el-icon @click="copy(scope.row)"><copy-document /></el-icon>
            </template>
          </el-popover>
          <el-text class="ml-2" :href="scope.row.url" target="_blank">{{ scope.row.url }}</el-text>
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="90" show-overflow-tooltip>
        <template #default="scope">
          <span v-if="scope.row.priority === 0">高优先级</span>
          <span v-else-if="scope.row.priority === 1">中优先级</span>
          <span v-else>低优先级</span>
        </template>
      </el-table-column>
      <el-table-column prop="fetch_method" label="抓取方式" width="100" show-overflow-tooltip>
        <template #default="scope">
          <span v-if="scope.row.fetch_method === 0">本地IP</span>
          <span v-else-if="scope.row.fetch_method === 1">代理IP</span>
          <span v-else>未知</span>
        </template>
      </el-table-column>
      <el-table-column width="250">
        <template #header>操作</template>
        <template #default="scope">
          <el-button type="primary" @click="edit(scope.row.key)" :disabled="!userStore.injected">修改</el-button>
          <el-button type="success" @click="debug(scope.row.key)" :loading="openDebugDialog && seedKey === scope.row.key"
                     :disabled="!userStore.injected">调试</el-button>
          <el-button type="danger" @click="remove(scope.row.key)" :disabled="!userStore.injected">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-row justify="center" align="middle">
      <el-pagination background layout="prev, pager, next" :total="total"
                     v-model:page-size="query.page_size" v-model:current-page="query.page_num" />
    </el-row>
  </el-space>
  <add-seed v-if="openAddDialog" v-model="openAddDialog" :plan-id="props.planId" @close="search" />
  <edit-seed v-if="openEditDialog" v-model="openEditDialog" :seed-key="seedKey" @close="search" />
  <debug-seed v-if="openDebugDialog" v-model="openDebugDialog" :seed-key="seedKey" :plan-id="props.planId" />
</template>

<style scoped>
</style>