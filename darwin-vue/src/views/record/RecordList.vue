<script setup>
import {
  IconChevronDown, IconChevronUp, IconClearAll,
  IconClock, IconCopy, IconCopyCheck, IconDownload, IconEye,
  IconFileDescription, IconHierarchy3, IconSend2, IconTrash
} from '@tabler/icons-vue'
import { reactive, ref, useTemplateRef, watch, watchEffect } from 'vue'
import { useRoute } from 'vue-router'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElCol, ElConfigProvider,
  ElDatePicker, ElDropdown, ElDropdownItem, ElDropdownMenu, ElForm, ElFormItem, ElInput, ElLink, ElLoading, ElOption,
  ElPagination, ElRadioButton, ElRadioGroup, ElRow, ElSelect, ElTable, ElTableColumn
} from 'element-plus'
import { useUserStore } from '@/store'
import { formatDate } from '@/common/Time'
import { writeClipboard } from '@/common/Clipboard'
import { asyncExecuteAfterConfirming, ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import { contentTypeMap, fetchMethodMap, httpRequestMap, priorityMap, statusMap } from '@/common/Constants'
import {
  asyncDispatchURL,
  asyncRemoveURL,
  asyncSearchURL,
  changeSearchQuerySort,
  newSearchQuery,
  newSearchRequest
} from '@/common/AsyncRequest'
import DarwinCard from '@/components/data/Card'
import TableHead from '@/components/data/TableHead'
import AppSearch from '@/components/app/AppSearch'
import JobSearch from '@/components/job/JobSearch'
import PlanSearch from '@/components/plan/PlanSearch'
import ViewRecord from '@/views/record/ViewRecord'
import ViewLineage from '@/views/record/ViewLineage'
import PreviewImage from '@/views/record/PreviewImage'
import PreviewVideo from '@/views/record/PreviewVideo'
import PreviewPdf from '@/views/record/PreviewPdf'
import PreviewJson from '@/views/record/PreviewJson'
import PreviewText from '@/views/record/PreviewText'
import PreviewAudio from '@/views/record/PreviewAudio'

const route = useRoute()
const userStore = useUserStore()
const formRef = useTemplateRef('form')
const vLoading = ElLoading.directive
const loading = ref(true)
const openView = ref(false)
const openLineage = ref(false)
const openPreviewJson = ref(false)
const openPreviewPdf = ref(false)
const openPreviewText = ref(false)
const openPreviewImage = ref(false)
const openPreviewVideo = ref(false)
const openPreviewAudio = ref(false)
const viewKey = ref()
const previewKey = ref()
const previewType = ref()
const copiedURL = ref()
const showMore = ref(false)
const records = ref([])
const exporting = ref(false)
const total = ref(0)
const query = reactive(newSearchQuery({
  content_type: 'all',
  priority: 'all',
  http_request: 'all',
  fetch_method: 'all',
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
  if (query.priority !== undefined && query.priority !== 'all') request.priority = query.priority
  if (query.content_type && query.content_type !== 'all') request.content_type = query.content_type
  if (query.http_request && query.http_request !== 'all') request.http_request = query.http_request
  if (query.fetch_method !== undefined && query.fetch_method !== 'all') request.fetch_method = query.fetch_method
  if (query.status && query.status.length > 0) request.status = JSON.stringify(query.status)
  return request
}

const search = async () => {
  loading.value = true
  const request = prepareSearchRequest()
  const pager = await asyncSearchURL(request)
  total.value = pager.total
  records.value = pager.records
  loading.value = false
}

const exportData = async () => {
  exporting.value = true
  try {
    const request = prepareSearchRequest()
    let exportURL = '/api/url/export?'
    Object.keys(request).forEach(key => exportURL += key + '=' + encodeURIComponent(request[key]) + '&')
    window.location = exportURL
  } finally {
    exporting.value = false
  }
}

const lineage = record => {
  openLineage.value = true
  viewKey.value = record.key
}

const remove = async record => {
  const success = await asyncExecuteAfterConfirming(asyncRemoveURL, record.key)
  if (success === undefined) return
  if (!success) {
    showMessage('删除数据失败', ERROR)
    return
  }
  showMessage('删除数据成功', SUCCESS)
  await search()
}

const dispatch = async record => {
  const success = await asyncExecuteAfterConfirming(asyncDispatchURL, record.key)
  if (success === undefined) return
  if (!success) {
    showMessage('分发数据失败', ERROR)
    return
  }
  showMessage('分发数据成功', SUCCESS)
}

const view = record => {
  viewKey.value = record.key
  openView.value = true
}

const preview = record => {
  previewKey.value = record.key
  previewType.value = record.media_type.alias
  if (previewType.value === 'VIDEO') openPreviewVideo.value = true
  else if (previewType.value === 'AUDIO') openPreviewAudio.value = true
  else if (previewType.value === 'IMAGE') openPreviewImage.value = true
  else if (previewType.value === 'PDF') openPreviewPdf.value = true
  else if (previewType.value === 'JSON') openPreviewJson.value = true
  else if (previewType.value === 'HTML' || previewType.value === 'XHTML' ||
    previewType.value === 'PLAIN' || previewType.value === 'CSS' ||
    previewType.value === 'JAVASCRIPT' || previewType.value === 'XML') openPreviewText.value = true
  else showMessage('媒体类型不支持预览', ERROR)
}

const copy = async record => {
  await writeClipboard(record.url)
  copiedURL.value = `URL#${record.key}`
  showMessage('复制成功', SUCCESS)
}

const handleCommand = async (command, record) => {
  if (command === 'lineage') {
    await lineage(record)
  } else if (command === 'dispatch') {
    await dispatch(record)
  } else if (command === 'remove') {
    await remove(record)
  }
}

watch(() => route.query, async () => {
  if (route.query.job_id) query.job_id = route.query.job_id
  if (route.query.plan_id) query.plan_id = route.query.plan_id
  if (route.query.host) query.host = route.query.host
}, { immediate: true })
watchEffect(async () => await search())
</script>

<template>
  <darwin-card>
    <template #title>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>基础功能</el-breadcrumb-item>
        <el-breadcrumb-item>抓取数据</el-breadcrumb-item>
      </el-breadcrumb>
    </template>
    <el-form :model="query" ref="form" label-width="80px" class="mb-4">
      <el-row :gutter="20">
        <el-col :span="10">
          <el-form-item label="数据类型" prop="content_type">
            <el-radio-group v-model="query.content_type">
              <el-radio-button value="all">全部</el-radio-button>
              <el-radio-button v-for="key in Object.keys(contentTypeMap)" :key="key" :value="parseInt(key)">
                {{ contentTypeMap[key] }}
              </el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="10">
          <el-form-item label="优先级" prop="priority">
            <el-radio-group v-model="query.priority">
              <el-radio-button value="all">全部</el-radio-button>
              <el-radio-button v-for="key in Object.keys(priorityMap)" :key="key" :value="parseInt(key)">
                {{ priorityMap[key] }}
              </el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="2">
          <el-button type="primary" plain @click="formRef.resetFields()">
            <IconClearAll size="20" class="mr-1" />
            <span>清除筛选</span>
          </el-button>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="10">
          <el-form-item label="抓取方式" prop="fetch_method">
            <el-radio-group v-model="query.fetch_method">
              <el-radio-button value="all">全部</el-radio-button>
              <el-radio-button v-for="key in Object.keys(fetchMethodMap)" :key="key" :value="parseInt(key)">
                {{ fetchMethodMap[key] }}
              </el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="10">
          <el-form-item label="HTTP请求" prop="http_request">
            <el-radio-group v-model="query.http_request">
              <el-radio-button value="all">全部</el-radio-button>
              <el-radio-button v-for="key in Object.keys(httpRequestMap)" :key="key" :value="key">
                {{ httpRequestMap[key] }}
              </el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="10">
          <el-form-item label="数据状态" prop="status">
            <el-select v-model="query.status" multiple clearable placeholder="请选择">
              <el-option v-for="code in Object.keys(statusMap)" :key="code" :label="statusMap[code]" :value="code" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="10">
          <el-form-item label="抓取时间" prop="fetch_time">
            <el-date-picker v-model="query.fetch_time" type="datetimerange" format="YYYY-MM-DD HH:mm:ss"
                            date-format="YYYY/MM/DD" time-format="HH:mm:ss" clearable
                            start-placeholder="起始时间" end-placeholder="结束时间" />
          </el-form-item>
        </el-col>
        <el-col v-if="!showMore" :span="2">
          <el-button type="primary" plain @click="showMore = !showMore">
            <IconChevronDown size="20" class="mr-1" />显示更多
          </el-button>
        </el-col>
      </el-row>
      <el-row v-if="showMore" :gutter="20">
        <el-col :span="10">
          <el-form-item label="所属任务" prop="job_id">
            <job-search v-model="query.job_id" placeholder="根据任务名搜索" />
          </el-form-item>
        </el-col>
        <el-col :span="10">
          <el-form-item label="所属计划" prop="plan_id">
            <plan-search v-model="query.plan_id" placeholder="根据计划名搜索" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row v-if="showMore" :gutter="20">
        <el-col :span="10">
          <el-form-item label="所属应用" prop="app_id">
            <app-search v-model="query.app_id" placeholder="根据应用名搜索" />
          </el-form-item>
        </el-col>
        <el-col :span="10">
          <el-form-item label="搜索URL" prop="url">
            <el-input v-model="query.url" clearable placeholder="根据URL搜索数据" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row v-if="showMore" :gutter="20">
        <el-col :span="10">
          <el-form-item label="搜索站点" prop="host">
            <el-input v-model="query.host" clearable placeholder="根据站点搜索数据" />
          </el-form-item>
        </el-col>
        <el-col :span="10">
          <el-form-item label="搜索域" prop="url">
            <el-input v-model="query.domain" clearable placeholder="根据DOMAIN搜索数据" />
          </el-form-item>
        </el-col>
        <el-col v-if="showMore" :span="2">
          <el-button type="primary" plain @click="showMore = !showMore">
            <IconChevronUp size="20" class="mr-1" />隐藏更多
          </el-button>
        </el-col>
      </el-row>
    </el-form>
    <table-head title="数据列表">
      <template #right>
        <el-button type="primary" @click="exportData" :loading="exporting" :disabled="!userStore.injected">
          <IconDownload size="20" class="mr-1" />
          <span>数据导出</span>
        </el-button>
      </template>
    </table-head>
    <el-table :data="records" max-height="550" table-layout="auto" stripe class="mb-4" v-loading="loading"
              @sort-change="event => changeSearchQuerySort(event.prop, event.order, query)">
      <template #empty>暂无抓取数据</template>
      <el-table-column prop="url" label="抓取URL" show-overflow-tooltip>
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
      <el-table-column prop="content_type" label="数据类型" width="80" show-overflow-tooltip>
        <template #default="scope">
          {{ scope.row.content_type ? contentTypeMap[scope.row.content_type] : '未知' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="数据状态" width="90" show-overflow-tooltip>
        <template #default="scope">{{ statusMap[scope.row.status] }}</template>
      </el-table-column>
      <el-table-column prop="media_type" label="媒体类型" width="80" show-overflow-tooltip>
        <template #default="scope">
          {{ scope.row.media_type && scope.row.media_type.alias ? scope.row.media_type.alias : '未知' }}
        </template>
      </el-table-column>
      <el-table-column prop="http_code" label="HTTP状态码" width="100" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.http_code ? scope.row.http_code : '未知' }}</template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="70" show-overflow-tooltip>
        <template #default="scope">{{ priorityMap[scope.row.priority] }}</template>
      </el-table-column>
      <el-table-column label="抓取时间" prop="fetch_time" width="200" sortable="custom" show-overflow-tooltip>
        <template #default="scope">
          <div class="d-flex align-items-center">
            <IconClock size="16" class="mr-1" />
            <span>{{ formatDate(scope.row['fetch_time']) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column width="330">
        <template #header>操作</template>
        <template #default="scope">
          <el-button type="primary" plain @click="view(scope.row)">
            <IconFileDescription size="20" class="mr-1" />
            <span>查看</span>
          </el-button>
          <el-button type="success" @click="preview(scope.row)" :disabled="scope.row.status !== 0">
            <IconEye size="20" class="mr-1" />
            <span>预览</span>
          </el-button>
          <el-dropdown trigger="click" placement="bottom-end" style="margin-left: 12px"
                       @command="c => handleCommand(c, scope.row)">
            <el-button type="primary">
              <span>更多操作</span>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="lineage">
                  <IconHierarchy3 size="20" class="mr-2" />
                  <span>血缘关系</span>
                </el-dropdown-item>
                <el-dropdown-item command="dispatch">
                  <IconSend2 size="20" class="mr-2" />
                  <span>分发数据</span>
                </el-dropdown-item>
                <el-dropdown-item command="remove">
                  <IconTrash size="20" class="mr-2" />
                  <span>删除数据</span>
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
  </darwin-card>
  <view-record v-if="openView" v-model="openView" :record-key="viewKey" />
  <view-lineage v-if="openLineage" v-model="openLineage" :record-key="viewKey" />
  <preview-image v-if="openPreviewImage" v-model="openPreviewImage" :record-key="previewKey" />
  <preview-video v-if="openPreviewVideo" v-model="openPreviewVideo" :record-key="previewKey" />
  <preview-audio v-if="openPreviewAudio" v-model="openPreviewAudio" :record-key="previewKey" />
  <preview-pdf v-if="openPreviewPdf" v-model="openPreviewPdf" :record-key="previewKey" />
  <preview-json v-if="openPreviewJson" v-model="openPreviewJson" :record-key="previewKey" />
  <preview-text v-if="openPreviewText" v-model="openPreviewText" :record-key="previewKey" :record-type="previewType" />
</template>

<style scoped>
</style>