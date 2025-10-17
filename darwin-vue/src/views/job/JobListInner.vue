<script setup>
import {
  IconChartBar, IconCircleCheck, IconClearAll, IconClock,
  IconDatabase, IconFileDescription, IconProgress, IconSend2
} from '@tabler/icons-vue'
import { reactive, ref, useTemplateRef, watch, watchEffect } from 'vue'
import { useRouter } from 'vue-router'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import {
  ElButton, ElCol, ElConfigProvider, ElDatePicker, ElDropdown, ElDropdownItem, ElDropdownMenu, ElForm,
  ElFormItem, ElLoading, ElPagination, ElRadioButton,
  ElRadioGroup, ElRow, ElTable, ElTableColumn, ElText
} from 'element-plus'
import { formatDate, pause } from '@/common/Time'
import {
  asyncDispatchJob,
  asyncJobProgress, asyncJobSuccessRate,
  asyncSearchJob,
  changeSearchQuerySort,
  newSearchQuery,
  newSearchRequest
} from '@/common/AsyncRequest'
import TableHead from '@/components/data/TableHead'
import JobDetail from '@/views/job/JobDetail'
import JobStat from '@/views/job/JobStat'
import { asyncExecuteAfterConfirming, ERROR, showMessage, SUCCESS } from '@/common/Feedback'

const router = useRouter()
const props = defineProps(['planId'])
const vLoading = ElLoading.directive
const formRef = useTemplateRef('form')
const openDetailDialog = ref(false)
const openStatDialog = ref(false)
const jobId = ref()
const jobs = ref([])
const total = ref(0)
const query = reactive(newSearchQuery({
  plan_id: props.planId,
  status: 'all',
}))

const search = async () => {
  const request = newSearchRequest(query)
  if (query.status && query.status !== 'all') request.status = query.status
  if (query.plan_id) request.plan_id = query.plan_id
  const pager = await asyncSearchJob(request)
  total.value = pager.total
  jobs.value = pager.records
  jobs.value.forEach(job => {
    job.computeProgress = job.computeSuccessRate = true
    progress(job)
    successRate(job)
  })
}

const progress = async job => {
  const progress = await asyncJobProgress(job.job_id)
  job.progress = progress * 100 + '%'
  await pause(1000)
  job.computeProgress = false
}

const successRate = async job => {
  const successRate = await asyncJobSuccessRate(job.job_id)
  job.successRate = successRate * 100 + '%'
  await pause(500)
  job.computeSuccessRate = false
}

const view = id => {
  jobId.value = id
  openDetailDialog.value = true
}

const stat = id => {
  jobId.value = id
  openStatDialog.value = true
}

const viewData = job_id => router.push({ path: '/record/search', query: { job_id: job_id } })

const dispatch = async job_id => {
  const success = await asyncExecuteAfterConfirming(asyncDispatchJob, job_id)
  if (success === undefined) return
  if (!success) {
    showMessage('分发任务数据失败', ERROR)
    return
  }
  showMessage('分发任务数据成功', SUCCESS)
}

const handleCommand = async (command, job_id) => {
  if (command === 'data') {
    await viewData(job_id)
  } else if (command === 'dispatch') {
    await dispatch(job_id)
  }
}

watch(() => props.planId, () => query.plan_id = props.planId, { immediate: true })
watchEffect(async () => await search())
</script>

<template>
  <el-form :model="query" ref="form" label-width="80px" class="mt-4 mb-4">
    <el-form-item label="任务状态" prop="status">
      <el-radio-group v-model="query.status">
        <el-radio-button value="all">全部</el-radio-button>
        <el-radio-button value="true">运行</el-radio-button>
        <el-radio-button value="false">结束</el-radio-button>
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
        <el-button type="primary" plain @click="formRef.resetFields()">
          <IconClearAll size="20" class="mr-1" />
          <span>清除筛选</span>
        </el-button>
      </el-col>
    </el-row>
  </el-form>
  <table-head title="任务列表" />
  <el-table :data="jobs" max-height="550" table-layout="auto" stripe class="mb-4"
            @sort-change="event => changeSearchQuerySort(event.prop, event.order, query)">
    <template #empty>暂无任务数据</template>
    <el-table-column prop="name" label="任务名" show-overflow-tooltip>
      <template #default="scope">{{ scope.row.name }}</template>
    </el-table-column>
    <el-table-column prop="status" label="状态" width="70" show-overflow-tooltip>
      <template #default="scope">
        <div v-if="scope.row.status" class="d-flex align-items-center">
          <IconProgress size="16" color="#95D475" class="mr-1 flex-shrink-0" />
          <span>运行</span>
        </div>
        <div v-else class="d-flex align-items-center">
          <IconCircleCheck size="16" color="#409eff" class="mr-1 flex-shrink-0" />
          <span>结束</span>
        </div>
      </template>
    </el-table-column>
    <el-table-column prop="executor" label="执行人" width="70" show-overflow-tooltip>
      <template #default="scope">
        {{ scope.row.executor ? scope.row.executor : '未知' }}
      </template>
    </el-table-column>
    <el-table-column label="创建时间" prop="create_time" sortable="custom" show-overflow-tooltip>
      <template #default="scope">
        <div class="d-flex align-items-center">
          <IconClock size="16" class="mr-1" />
          <span>{{ formatDate(scope.row['create_time']) }}</span>
        </div>
      </template>
    </el-table-column>
    <el-table-column label="抓取进度" prop="progress" width="80">
      <template #default="scope">
        <el-text v-loading="scope.row.computeProgress" element-loading-text="Loading...">
          {{ scope.row.progress }}
        </el-text>
      </template>
    </el-table-column>
    <el-table-column label="成功率" prop="successRate" width="80">
      <template #default="scope">
        <el-text v-loading="scope.row.computeSuccessRate" element-loading-text="Loading...">
          {{ scope.row.successRate }}
        </el-text>
      </template>
    </el-table-column>
    <el-table-column width="330">
      <template #header>操作</template>
      <template #default="scope">
        <el-button type="primary" plain @click="view(scope.row.job_id)">
          <IconFileDescription size="20" class="mr-1" />
          <span>查看</span>
        </el-button>
        <el-button type="success" @click="stat(scope.row.job_id)">
          <IconChartBar size="20" class="mr-1" />
          <span>统计</span>
        </el-button>
        <el-dropdown trigger="click" placement="bottom-end" style="margin-left: 12px"
                     @command="c => handleCommand(c, scope.row.job_id)">
          <el-button type="primary">
            <span>更多操作</span>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="data">
                <IconDatabase size="20" class="mr-2" />
                <span>数据</span>
              </el-dropdown-item>
              <el-dropdown-item command="dispatch">
                <IconSend2 size="20" class="mr-2" />
                <span>分发</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
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
  <job-detail v-if="openDetailDialog" v-model="openDetailDialog" :id="jobId" />
  <job-stat v-if="openStatDialog" v-model="openStatDialog" :id="jobId" />
</template>

<style scoped>
</style>