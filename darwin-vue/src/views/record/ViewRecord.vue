<script setup>
import { ref, watchEffect } from 'vue'
import { ElDescriptions, ElDescriptionsItem, ElDialog, ElRow } from 'element-plus'
import JsonViewer from 'vue-json-viewer'
import { asyncGetApp, asyncGetJob, asyncGetPlan, asyncGetURL } from '@/common/AsyncRequest'
import { formatDate } from '@/common/Time'
import { categoryMap, concurrentLevelMap, fetchMethodMap, priorityMap, statusMap } from '@/common/Constants'

const props = defineProps(['recordKey'])
const open = defineModel()
const emits = defineEmits(['close'])
const record = ref()

watchEffect(async () => {
  if (props.recordKey) {
    record.value = await asyncGetURL(props.recordKey)
    if (record.value.fetch_method === undefined) record.value.fetch_method = 0
    if (record.value.plan_id) {
      const plan = await asyncGetPlan(record.value.plan_id)
      record.value.plan_name = plan ? plan.name : '未知'
    }
    if (record.value.job_id) {
      const job = await asyncGetJob(record.value.job_id)
      record.value.job_name = job ? job.name : '未知'
    }
    if (record.value.app_id) {
      const app = await asyncGetApp(record.value.app_id)
      record.value.app_name = app ? app.name : '未知'
    }
  }
})
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" width="1200" align-center show-close>
    <el-row class="mb-3" align="middle">
      <span class="text-xl font-bold ml-2">数据信息</span>
    </el-row>
    <el-descriptions v-if="record" direction="vertical" :column="5" border>
      <el-descriptions-item label="抓取URL" class-name="wide-column">{{ record.url }}</el-descriptions-item>
      <el-descriptions-item label="状态" width="140">{{ statusMap[record.status] }}</el-descriptions-item>
      <el-descriptions-item label="类型" width="140">
        {{ record.category ? categoryMap[record.category] : '未知' }}
      </el-descriptions-item>
      <el-descriptions-item label="创建时间" width="300">{{ formatDate(record.create_time) }}</el-descriptions-item>
      <el-descriptions-item label="所属应用" width="350">{{ record.app_name }}</el-descriptions-item>
      <el-descriptions-item label="父URL" class-name="wide-column">
        {{ record.parent_url ? record.parent_url : '暂无' }}
      </el-descriptions-item>
      <el-descriptions-item label="抓取方式">{{ fetchMethodMap[record.fetch_method] }}</el-descriptions-item>
      <el-descriptions-item label="HTTP状态码">{{ record.http_code ? record.http_code : '暂无' }}</el-descriptions-item>
      <el-descriptions-item label="入队时间">{{ formatDate(record.push_time) }}</el-descriptions-item>
      <el-descriptions-item label="所属计划">{{ record.plan_name }}</el-descriptions-item>
      <el-descriptions-item label="重定向URL" class-name="wide-column">
        {{ record.redirect_url ? record.redirect_url : '暂无' }}
      </el-descriptions-item>
      <el-descriptions-item label="优先级">{{ priorityMap[record.priority] }}</el-descriptions-item>
      <el-descriptions-item label="并发级别">{{ concurrentLevelMap[record.concurrency_level] }}</el-descriptions-item>
      <el-descriptions-item label="出队时间">{{ formatDate(record.pop_time) }}</el-descriptions-item>
      <el-descriptions-item label="所属任务">{{ record.job_name }}</el-descriptions-item>
      <el-descriptions-item label="抓取内容" class-name="wide-column">
        {{ record.fetch_content_url ? record.fetch_content_url : '暂无' }}
      </el-descriptions-item>
      <el-descriptions-item label="抓取深度">{{ record.depth }}</el-descriptions-item>
      <el-descriptions-item label="媒体类型">
        {{ record.media_type && record.media_type.alias ? record.media_type.alias : '暂无' }}
      </el-descriptions-item>
      <el-descriptions-item label="抓取时间">{{ formatDate(record.fetch_time) }}</el-descriptions-item>
      <el-descriptions-item label="MimeType">{{ record.mime_type ? record.mime_type : '未知' }}</el-descriptions-item>
      <el-descriptions-item label="HTTP编码">
        {{ record.media_type && record.media_type.charset ? record.media_type.charset : (record.html_charset ? record.html_charset : '未知') }}
      </el-descriptions-item>
      <el-descriptions-item label="探测编码">{{ record.charset ? record.charset : '未知' }}</el-descriptions-item>
      <el-descriptions-item label="内容长度">
        {{ record.content_length ? record.content_length : '暂无' }}
      </el-descriptions-item>
      <el-descriptions-item label="分发数据">{{ record.allow_dispatch ? '允许' : '禁止' }}</el-descriptions-item>
      <el-descriptions-item label="HTTP抓取">{{ record.fetched ? '是' : '否' }}</el-descriptions-item>
      <el-descriptions-item v-if="record.field_map && Object.keys(record.field_map).length > 0"
                            label="结构化数据" :span="5">
        <json-viewer class="w100" :value="record.field_map" :expand-depth=0 boxed sort />
      </el-descriptions-item>
      <el-descriptions-item v-if="record.custom_map && Object.keys(record.custom_map).length > 0"
                            label="自定义数据" :span="5">
        <json-viewer class="w100" :value="record.custom_map" :expand-depth=0 boxed sort />
      </el-descriptions-item>
      <el-descriptions-item v-if="record.headers && Object.keys(record.headers).length > 0"
                            label="HTTP Header" :span="5">
        <json-viewer class="w100" :value="record.headers" :expand-depth=0 boxed sort />
      </el-descriptions-item>
    </el-descriptions>
  </el-dialog>
</template>

<style scoped>
:deep(.wide-column) {
  max-width: 600px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: wrap;
}
</style>