<script setup>
import { reactive, ref, useTemplateRef, watch, watchEffect } from 'vue'
import { useRouter } from 'vue-router'
import { Delete, Timer } from '@element-plus/icons-vue'
import {
  ElButton, ElCol, ElDatePicker, ElForm, ElFormItem, ElIcon, ElLoading,
  ElPagination, ElRadioButton, ElRadioGroup, ElRow, ElSpace, ElTable,
  ElTableColumn, ElText, ElTooltip
} from 'element-plus'
import { formatDate, pause } from '@/common/Time'
import {
  asyncJobProgress,
  asyncSearchJob,
  changeSearchQuerySort,
  newSearchQuery,
  newSearchRequest
} from '@/common/AsyncRequest'
import ViewJob from '@/views/job/ViewJob'
import ViewJobStat from '@/views/job/ViewJobStat'

const router = useRouter()
const props = defineProps(['planId'])
const vLoading = ElLoading.directive
const filterForm = useTemplateRef('filterForm')
const openViewDialog = ref(false)
const openViewStatDialog = ref(false)
const jobId = ref()
const viewStatJobId = ref()
const jobs = ref([])
const total = ref(0)
const query = reactive(newSearchQuery({
  plan_id: props.planId,
  status: 'all',
  priority: 'all'
}))

const search = async () => {
  const request = newSearchRequest(query)
  if (query.priority && query.priority !== 'all') request.priority = query.priority
  if (query.status && query.status !== 'all') request.status = query.status
  if (query.plan_id) request.plan_id = query.plan_id
  const pager = await asyncSearchJob(request)
  total.value = pager.total
  jobs.value = pager.records
  jobs.value.forEach(job => {
    job.loading = true
    progress(job)
  })
}

const progress = async job => {
  await pause(1000)
  const progress = await asyncJobProgress(job.job_id)
  job.progress = progress * 100 + '%'
  job.loading = false
}

const view = id => {
  jobId.value = id
  openViewDialog.value = true
}

const viewStat = id => {
  viewStatJobId.value = id
  openViewStatDialog.value = true
}

const viewURL = job_id => router.push({ path: '/record/search', query: { job_id: job_id } })

watch(() => props.planId, () => query.plan_id = props.planId, { immediate: true })
watchEffect(async () => await search())
</script>

<template>
  <el-space direction="vertical" :size="20" :fill="true" class="w100">
    <el-form :model="query" ref="filterForm" label-width="80px" class="w100">
      <el-form-item label="任务状态" prop="status">
        <el-radio-group v-model="query.status">
          <el-radio-button value="all">全部</el-radio-button>
          <el-radio-button value="true">运行</el-radio-button>
          <el-radio-button value="false">结束</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="优先级" prop="priority">
        <el-radio-group v-model="query.priority">
          <el-radio-button value="all">全部</el-radio-button>
          <el-radio-button value="0">高优先级</el-radio-button>
          <el-radio-button value="1">中优先级</el-radio-button>
          <el-radio-button value="2">低优先级</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-row>
        <el-col :span="12">
          <el-form-item label="创建时间" prop="create_time">
            <el-date-picker v-model="query.create_time" type="datetimerange" format="YYYY-MM-DD HH:mm:ss"
                            date-format="YYYY/MM/DD" time-format="HH:mm:ss" clearable
                            start-placeholder="起始时间" end-placeholder="结束时间" />
          </el-form-item>
        </el-col>
        <el-col :span="1"></el-col>
        <el-col :span="2">
          <el-tooltip effect="dark" content="清除所有筛选条件" placement="right-end">
            <el-button @click="filterForm.resetFields()" :icon="Delete" />
          </el-tooltip>
        </el-col>
      </el-row>
    </el-form>
    <el-table :data="jobs" max-height="850" table-layout="auto"
              stripe @sort-change="event => changeSearchQuerySort(event.prop, event.order, query)">
      <template #empty>暂无任务数据</template>
      <el-table-column prop="name" label="任务名" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.name }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80" show-overflow-tooltip>
        <template #default="scope">
          <span v-if="scope.row.status">运行</span>
          <span v-else>结束</span>
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="90" show-overflow-tooltip>
        <template #default="scope">
          <span v-if="scope.row.priority === 0">高优先级</span>
          <span v-else-if="scope.row.priority === 1">中优先级</span>
          <span v-else>低优先级</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="create_time" width="200" sortable="custom" show-overflow-tooltip>
        <template #default="scope">
          <el-icon><timer /></el-icon>
          {{ formatDate(scope.row['create_time']) }}
        </template>
      </el-table-column>
      <el-table-column label="抓取进度" prop="progress" width="100">
        <template #default="scope">
          <el-text v-loading="scope.row.loading" element-loading-text="Loading...">
            {{ scope.row.progress }}
          </el-text>
        </template>
      </el-table-column>
      <el-table-column width="230">
        <template #header>操作</template>
        <template #default="scope">
          <el-button type="primary" @click="view(scope.row.job_id)">查看</el-button>
          <el-button type="success" @click="viewStat(scope.row.job_id)">统计</el-button>
          <el-button type="primary" @click="viewURL(scope.row.job_id)">数据</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-row justify="center" align="middle">
      <el-pagination background layout="prev, pager, next" :total="total"
                     v-model:page-size="query.size" v-model:current-page="query.current">
      </el-pagination>
    </el-row>
  </el-space>
  <view-job v-model="openViewDialog" :id="jobId" />
  <view-job-stat v-model="openViewStatDialog" :id="viewStatJobId" />
</template>

<style scoped>
</style>