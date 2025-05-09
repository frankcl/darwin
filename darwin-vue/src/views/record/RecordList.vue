<script setup>
import { reactive, ref, useTemplateRef, watch, watchEffect } from 'vue'
import { useRoute } from 'vue-router'
import { CopyDocument, Delete, DocumentCopy, Download, Timer } from '@element-plus/icons-vue'
import {
  ElButton, ElCol, ElDatePicker, ElForm, ElFormItem, ElIcon, ElInput,
  ElOption, ElPagination, ElPopover, ElRadioButton, ElRadioGroup,
  ElRow, ElSelect, ElSpace, ElTable, ElTableColumn, ElText, ElTooltip
} from 'element-plus'
import { useUserStore } from '@/store'
import { formatDate } from '@/common/Time'
import { writeClipboard } from '@/common/Clipboard'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import { categoryMap, priorityMap, statusMap } from '@/common/Constants'
import {
  asyncSearchURL,
  changeSearchQuerySort,
  newSearchQuery,
  newSearchRequest
} from '@/common/AsyncRequest'
import AppSearch from '@/components/app/AppSearch'
import JobSearch from '@/components/job/JobSearch'
import PlanSearch from '@/components/plan/PlanSearch'
import ViewRecord from '@/views/record/ViewRecord'
import PreviewHtml from '@/views/record/PreviewHtml'
import PreviewImage from '@/views/record/PreviewImage'
import PreviewVideo from '@/views/record/PreviewVideo'
import PreviewPdf from '@/views/record/PreviewPdf'
import PreviewJson from '@/views/record/PreviewJson'
import PreviewText from '@/views/record/PreviewText'

const route = useRoute()
const userStore = useUserStore()
const filterFormRef = useTemplateRef('filterForm')
const openViewDialog = ref(false)
const openPreviewHTML = ref(false)
const openPreviewJson = ref(false)
const openPreviewPdf = ref(false)
const openPreviewText = ref(false)
const openPreviewImage = ref(false)
const openPreviewVideo = ref(false)
const viewKey = ref()
const previewKey = ref()
const previewType = ref()
const copiedURL = ref()
const records = ref([])
const exporting = ref(false)
const total = ref(0)
const query = reactive(newSearchQuery({
  category: 'all',
  priority: 'all',
  sort_field: 'fetch_time',
  sort_order: 'descending'
}))

const prepareSearchRequest = () => {
  const request = newSearchRequest(query)
  if (query.app_id) request.app_id = query.app_id
  if (query.plan_id) request.plan_id = query.plan_id
  if (query.job_id) request.job_id = query.job_id
  if (query.url) request.url = query.url
  if (query.host) request.host = query.host
  if (query.domain) request.domain = query.domain
  if (query.priority && query.priority !== 'all') request.priority = query.priority
  if (query.category && query.category !== 'all') request.category = query.category
  if (query.status && query.status.length > 0) request.status = JSON.stringify(query.status)
  return request
}

const search = async () => {
  const request = prepareSearchRequest()
  const pager = await asyncSearchURL(request)
  total.value = pager.total
  records.value = pager.records
}

const exportData = async () => {
  exporting.value = true
  try {
    const request = prepareSearchRequest()
    let exportURL = '/api/url/export?'
    Object.keys(request).forEach(key => exportURL += key + '=' + encodeURIComponent(request[key]) + '&')
    console.log(exportURL)
    window.location = exportURL
  } finally {
    exporting.value = false
  }
}

const view = record => {
  viewKey.value = record.key
  openViewDialog.value = true
}

const preview = record => {
  previewKey.value = record.key
  previewType.value = record.media_type.alias
  if (previewType.value === 'VIDEO') openPreviewVideo.value = true
  else if (previewType.value === 'IMAGE') openPreviewImage.value = true
  else if (previewType.value === 'PDF') openPreviewPdf.value = true
  else if (previewType.value === 'JSON') openPreviewJson.value = true
  else if (previewType.value === 'HTML' || previewType.value === 'XHTML') openPreviewHTML.value = true
  else if (previewType.value === 'PLAIN' || previewType.value === 'CSS' ||
    previewType.value === 'JAVASCRIPT' || previewType.value === 'XML') openPreviewText.value = true
  else showMessage('媒体类型不支持预览', ERROR)
}

const copy = async record => {
  await writeClipboard(record.url)
  copiedURL.value = `URL#${record.key}`
  showMessage('复制URL成功', SUCCESS)
}

watch(() => route.query, async () => {
  if (route.query.job_id) query.job_id = route.query.job_id
  if (route.query.plan_id) query.plan_id = route.query.plan_id
}, { immediate: true })
watchEffect(async () => await search())
</script>

<template>
  <el-space direction="vertical" :size="20" :fill="true" class="w100">
    <el-form :model="query" ref="filterForm" label-width="80px" class="w100">
      <el-row>
        <el-col :span="11">
          <el-form-item label="类型" prop="category">
            <el-radio-group v-model="query.category">
              <el-radio-button value="all">全部</el-radio-button>
              <el-radio-button value="1">网页</el-radio-button>
              <el-radio-button value="2">资源</el-radio-button>
              <el-radio-button value="3">视频流</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="1"></el-col>
        <el-col :span="11">
          <el-form-item label="优先级" prop="priority">
            <el-radio-group v-model="query.priority">
              <el-radio-button value="all">全部</el-radio-button>
              <el-radio-button value="0">高优先级</el-radio-button>
              <el-radio-button value="1">中优先级</el-radio-button>
              <el-radio-button value="2">低优先级</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="11">
          <el-form-item label="抓取状态" prop="status">
            <el-select v-model="query.status" multiple clearable placeholder="请选择">
              <el-option v-for="code in Object.keys(statusMap)" :key="code" :label="statusMap[code]" :value="code" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="1"></el-col>
        <el-col :span="10">
          <el-form-item label="抓取时间" prop="fetch_time">
            <el-date-picker v-model="query.fetch_time" type="datetimerange" format="YYYY-MM-DD HH:mm:ss"
                            date-format="YYYY/MM/DD" time-format="HH:mm:ss" clearable
                            start-placeholder="起始时间" end-placeholder="结束时间" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="11">
          <el-form-item label="所属任务" prop="job_id">
            <job-search v-model="query.job_id" placeholder="根据任务名搜索" />
          </el-form-item>
        </el-col>
        <el-col :span="1"></el-col>
        <el-col :span="10">
          <el-form-item label="所属计划" prop="plan_id">
            <plan-search v-model="query.plan_id" placeholder="根据计划名搜索" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="11">
          <el-form-item label="所属应用" prop="app_id">
            <app-search v-model="query.app_id" placeholder="根据应用名搜索" />
          </el-form-item>
        </el-col>
        <el-col :span="1"></el-col>
        <el-col :span="10">
          <el-form-item label="搜索URL" prop="url">
            <el-input v-model="query.url" clearable placeholder="根据URL搜索数据" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="11">
          <el-form-item label="搜索站点" prop="host">
            <el-input v-model="query.host" clearable placeholder="根据站点搜索数据" />
          </el-form-item>
        </el-col>
        <el-col :span="1"></el-col>
        <el-col :span="10">
          <el-form-item label="搜索域" prop="url">
            <el-input v-model="query.domain" clearable placeholder="根据DOMAIN搜索数据" />
          </el-form-item>
        </el-col>
        <el-col :span="1"></el-col>
        <el-col :span="1">
          <el-tooltip effect="dark" content="清除所有筛选条件" placement="top">
            <el-button @click="filterFormRef.resetFields()" :icon="Delete" />
          </el-tooltip>
        </el-col>
      </el-row>
    </el-form>
    <el-row align="middle">
      <el-col :span="12">
        <span class="text-xl font-bold ml-2">数据列表</span>
      </el-col>
      <el-col :span="12">
        <el-row justify="end">
          <el-button type="primary" @click="exportData" :loading="exporting" :disabled="!userStore.injected">
            <span class="mr-1">数据导出</span>
            <el-icon><download /></el-icon>
          </el-button>
        </el-row>
      </el-col>
    </el-row>
    <el-table :data="records" max-height="850" table-layout="auto"
              stripe @sort-change="event => changeSearchQuerySort(event.prop, event.order, query)">
      <template #empty>暂无抓取数据</template>
      <el-table-column prop="url" label="抓取URL" show-overflow-tooltip>
        <template #default="scope">
          <el-icon v-if="copiedURL === `URL#${scope.row.key}`"><document-copy /></el-icon>
          <el-popover v-else content="点击复制">
            <template #reference>
              <el-icon @click="copy(scope.row)"><copy-document /></el-icon>
            </template>
          </el-popover>
          <el-text class="ml-2">{{ scope.row.url }}</el-text>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="类型" width="100" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.category ? categoryMap[scope.row.category] : '未知' }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" show-overflow-tooltip>
        <template #default="scope">{{ statusMap[scope.row.status] }}</template>
      </el-table-column>
      <el-table-column prop="media_type" label="媒体类型" width="100" show-overflow-tooltip>
        <template #default="scope">
          {{ scope.row.media_type && scope.row.media_type.alias ? scope.row.media_type.alias : '未知' }}
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="90" show-overflow-tooltip>
        <template #default="scope">{{ priorityMap[scope.row.priority] }}</template>
      </el-table-column>
      <el-table-column label="抓取时间" prop="fetch_time" width="200" sortable="custom" show-overflow-tooltip>
        <template #default="scope">
          <el-icon><timer /></el-icon>
          <el-text class="ml-2">{{ formatDate(scope.row['fetch_time']) }}</el-text>
        </template>
      </el-table-column>
      <el-table-column width="160">
        <template #header>操作</template>
        <template #default="scope">
          <el-button type="primary" @click="view(scope.row)">查看</el-button>
          <el-button type="success" @click="preview(scope.row)" :disabled="scope.row.status !== 0">预览</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-row justify="center" align="middle">
      <el-pagination background layout="prev, pager, next" :total="total"
                     v-model:page-size="query.page_size" v-model:current-page="query.page_num">
      </el-pagination>
    </el-row>
  </el-space>
  <view-record v-model="openViewDialog" :record-key="viewKey" />
  <preview-html v-if="openPreviewHTML" v-model="openPreviewHTML" :record-key="previewKey" />
  <preview-image v-if="openPreviewImage" v-model="openPreviewImage" :record-key="previewKey" />
  <preview-video v-if="openPreviewVideo" v-model="openPreviewVideo" :record-key="previewKey" />
  <preview-pdf v-if="openPreviewPdf" v-model="openPreviewPdf" :record-key="previewKey" />
  <preview-json v-if="openPreviewJson" v-model="openPreviewJson" :record-key="previewKey" />
  <preview-text v-if="openPreviewText" v-model="openPreviewText" :record-key="previewKey" :record-type="previewType" />
</template>

<style scoped>
</style>