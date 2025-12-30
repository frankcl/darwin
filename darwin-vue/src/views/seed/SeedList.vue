<script setup>
import { IconBug, IconCopy, IconCopyCheck, IconEdit, IconPlus, IconTrash } from '@tabler/icons-vue'
import { reactive, ref, watchEffect } from 'vue'
import {
  ElButton, ElCol, ElConfigProvider, ElForm, ElFormItem, ElInput, ElLink,
  ElPagination, ElRow, ElTable, ElTableColumn
} from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import { useUserStore } from '@/store'
import { writeClipboard } from '@/common/Clipboard'
import { fetchMethodMap, priorityMap } from '@/common/Constants'
import { asyncExecuteAfterConfirming, ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import {
  asyncRemoveSeed,
  asyncRemovePlanSeeds,
  asyncSearchSeed,
  changeSearchQuerySort,
  newSearchQuery,
  newSearchRequest
} from '@/common/AsyncRequest'
import TableHead from '@/components/data/TableHead'
import DebugSeed from '@/views/debug/DebugSeed'
import AddSeed from '@/views/seed/AddSeed'
import EditSeed from '@/views/seed/EditSeed'

const props = defineProps(['planId'])
const userStore = useUserStore()
const openAdd = ref(false)
const openEdit = ref(false)
const openDebug = ref(false)
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

const update = key => {
  seedKey.value = key
  openEdit.value = true
}

const debug = key => {
  seedKey.value = key
  openDebug.value = true
}

const remove = async key => {
  const success = await asyncExecuteAfterConfirming(asyncRemoveSeed, key)
  if (success === undefined) return
  if (!success) {
    showMessage('删除种子失败', ERROR)
    return
  }
  showMessage('删除种子成功', SUCCESS)
  await search()
}

const removePlanSeeds = async plan_id => {
  const success = await asyncExecuteAfterConfirming(asyncRemovePlanSeeds, plan_id)
  if (success === undefined) return
  if (!success) {
    showMessage('删除计划相关种子失败', ERROR)
    return
  }
  showMessage('删除计划相关种子成功', SUCCESS)
  await search()
}

const copy = async seed => {
  await writeClipboard(seed.url)
  copiedURL.value = `URL#${seed.key}`
  showMessage('复制种子成功', SUCCESS)
}

watchEffect( () => query.plan_id = props.planId)
watchEffect(async () => await search())
</script>

<template>
  <el-form :model="query" label-width="80px" class="mt-4 mb-4">
    <el-col :span="12">
      <el-form-item label="搜索种子" prop="url">
        <el-input v-model="query.url" clearable placeholder="根据URL搜索" />
      </el-form-item>
    </el-col>
  </el-form>
  <table-head title="种子列表">
    <template #right>
      <el-button type="primary" @click="openAdd = true" :disabled="!userStore.injected">
        <IconPlus size="20" class="mr-1" />
        <span>新增</span>
      </el-button>
      <el-button type="warning" @click="removePlanSeeds(props.planId)" :disabled="!userStore.injected">
        <IconPlus size="20" class="mr-1" />
        <span>删除所有种子</span>
      </el-button>
    </template>
  </table-head>
  <el-table :data="seeds" max-height="550" table-layout="auto" stripe class="mb-4"
            @sort-change="event => changeSearchQuerySort(event.prop, event.order, query)">
    <template #empty>暂无种子数据</template>
    <el-table-column prop="url" label="种子URL" show-overflow-tooltip>
      <template #default="scope">
        <span class="d-flex align-items-center">
          <IconCopyCheck v-if="copiedURL === `URL#${scope.row.key}`" class="flex-shrink-0"  size="16" />
          <IconCopy v-else class="flex-shrink-0" @click="copy(scope.row)" size="16" />
          <el-link class="ml-2" :href="scope.row.url" :underline="false" target="_blank">
            {{ scope.row.url }}
          </el-link>
        </span>
      </template>
    </el-table-column>
    <el-table-column prop="priority" label="优先级" width="70" show-overflow-tooltip>
      <template #default="scope">{{ priorityMap[scope.row.priority] }}</template>
    </el-table-column>
    <el-table-column prop="fetch_method" label="抓取方式" width="100" show-overflow-tooltip>
      <template #default="scope">{{ fetchMethodMap[scope.row.fetch_method] }}</template>
    </el-table-column>
    <el-table-column width="320">
      <template #header>操作</template>
      <template #default="scope">
        <el-button type="primary" @click="update(scope.row.key)" :disabled="!userStore.injected">
          <IconEdit size="20" class="mr-1" />
          <span>修改</span>
        </el-button>
        <el-button type="success" @click="debug(scope.row.key)" :loading="openDebug && seedKey === scope.row.key"
                   :disabled="!userStore.injected">
          <IconBug size="20" class="mr-1" />
          <span>调试</span>
        </el-button>
        <el-button type="danger" @click="remove(scope.row.key)" :disabled="!userStore.injected">
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
  <add-seed v-if="openAdd" v-model="openAdd" :plan-id="props.planId" @close="search" />
  <edit-seed v-if="openEdit" v-model="openEdit" :seed-key="seedKey" @close="search" />
  <debug-seed v-if="openDebug" v-model="openDebug" :seed-key="seedKey" :plan-id="props.planId" />
</template>

<style scoped>
</style>