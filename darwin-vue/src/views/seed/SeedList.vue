<script setup>
import { reactive, ref, useTemplateRef, watchEffect } from 'vue'
import {
  ElButton, ElCol, ElForm, ElFormItem, ElInput, ElLink,
  ElNotification, ElPagination, ElRow, ElSpace, ElTable, ElTableColumn
} from 'element-plus'
import { useUserStore } from '@/store'
import {
  checkUserLogin,
  executeAsyncRequestAfterConfirm,
  fillSearchQuerySort,
  searchQueryToRequest,
} from '@/common/assortment'
import { asyncDeleteSeed, asyncSearchSeeds } from '@/common/service'
import AddSeed from '@/views/seed/AddSeed'
import EditSeed from '@/views/seed/EditSeed'

const props = defineProps(['id'])
const formRef = useTemplateRef('formRef')
const tableRef = useTemplateRef('tableRef')
const userStore = useUserStore()
const openAddDialog = ref(false)
const openEditDialog = ref(false)
const seedKey = ref()
const seeds = ref([])
const total = ref(0)
const query = reactive({
  current: 1,
  size: 10,
  url: null,
  plan_id: null,
  sort_field: null,
  sort_order: null
})

const search = async () => {
  const request = searchQueryToRequest(query)
  if (query.url) request.url = query.url
  if (query.plan_id) request.plan_id = query.plan_id
  const pager = await asyncSearchSeeds(request)
  total.value = pager.total
  seeds.value = pager.records
}

const edit = key => {
  if (!checkUserLogin()) return
  seedKey.value = key
  openEditDialog.value = true
}

const remove = async key => {
  if (!checkUserLogin()) return
  const successHandle = () => ElNotification.success('删除种子URL成功')
  const failHandle = () => ElNotification.error('删除种子URL失败')
  if (!await executeAsyncRequestAfterConfirm(
    '删除提示', '是否确定删除该种子URL？', asyncDeleteSeed, key, successHandle, failHandle)) return
  await search()
}

watchEffect( () => query.plan_id = props.id)
watchEffect(async () => await search())
</script>

<template>
  <el-space direction="vertical" :size="20" :fill="true" class="w100">
    <el-form :model="query" ref="formRef" label-width="50px">
      <el-row>
        <el-col :span="16">
          <el-form-item label="搜索" prop="url">
            <el-input v-model="query.url" clearable placeholder="搜索种子URL" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-row justify="end">
            <el-button :disabled="!userStore.injected" @click="openAddDialog = true">新增种子</el-button>
          </el-row>
        </el-col>
      </el-row>
    </el-form>
    <el-table ref="tableRef" :data="seeds" max-height="850" table-layout="auto"
              stripe @sort-change="event => fillSearchQuerySort(event, query)">
      <template #empty>暂无种子URL</template>
      <el-table-column prop="name" label="URL" show-overflow-tooltip>
        <template #default="scope">
          {{ scope.row.url }}
        </template>
      </el-table-column>
      <el-table-column prop="category" label="类型" width="100" show-overflow-tooltip>
        <template #default="scope">
          <span v-if="scope.row.category === 2">列表页</span>
          <span v-else-if="scope.row.category === 3">媒体资源</span>
          <span v-else-if="scope.row.category === 4">视频流</span>
          <span v-else>内容页</span>
        </template>
      </el-table-column>
      <el-table-column prop="concurrent_level" label="并发级别" width="100" show-overflow-tooltip>
        <template #default="scope">
          <span v-if="scope.row.concurrent_level === 1">HOST</span>
          <span v-else>DOMAIN</span>
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
      <el-table-column width="120">
        <template #header>
          操作
        </template>
        <template #default="scope">
          <el-link @click="edit(scope.row.key)">修改</el-link>
          &nbsp;
          <el-link @click="remove(scope.row.key)">删除</el-link>
        </template>
      </el-table-column>
    </el-table>
    <el-row justify="center" align="middle">
      <el-pagination background layout="prev, pager, next" :total="total"
                     v-model:page-size="query.size" v-model:current-page="query.current" />
    </el-row>
  </el-space>
  <add-seed v-model="openAddDialog" :plan-id="props.id" @close="search()"></add-seed>
  <edit-seed v-model="openEditDialog" :seed-key="seedKey" @close="search()"></edit-seed>
</template>

<style scoped>
</style>