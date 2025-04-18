<script setup>
import { ref, watchEffect } from 'vue'
import {ElDescriptions, ElDescriptionsItem, ElDialog, ElRow} from 'element-plus'
import JsonViewer from 'vue-json-viewer'
import { asyncGetURL } from '@/common/AsyncRequest'
import { formatDate } from '@/common/Time'
import { categoryMap, concurrentLevelMap, fetchMethodMap, priorityMap, statusMap } from '@/common/Constants'

const props = defineProps(['recordKey'])
const open = defineModel()
const emits = defineEmits(['close'])
const record = ref()

const formatMimeType = record => {
  if (record.mime_type && record.sub_mime_type) return record.mime_type + '/' + record.sub_mime_type
  if (record.mime_type) return record.mime_type
  return '未知'
}

watchEffect(async () => {
  if (props.recordKey) {
    record.value = await asyncGetURL(props.recordKey)
    if (record.value.fetch_method === undefined) record.value.fetch_method = 0
  }
})
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" width="1200" align-center show-close>
    <el-row class="mb-3" align="middle">
      <span class="text-xl font-bold ml-2">数据信息</span>
    </el-row>
    <el-descriptions v-if="record" direction="vertical" :column="4" border>
      <el-descriptions-item label="抓取URL">{{ record.url }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ formatDate(record.create_time) }}</el-descriptions-item>
      <el-descriptions-item label="类型">{{ categoryMap[record.category] }}</el-descriptions-item>
      <el-descriptions-item label="状态">{{ statusMap[record.status] }}</el-descriptions-item>
      <el-descriptions-item label="父URL">
        {{ record.parent_url ? record.parent_url : '暂无' }}
      </el-descriptions-item>
      <el-descriptions-item label="入队时间">{{ formatDate(record.push_time) }}</el-descriptions-item>
      <el-descriptions-item label="抓取方式">{{ fetchMethodMap[record.fetch_method] }}</el-descriptions-item>
      <el-descriptions-item label="HTTP状态码">{{ record.http_code ? record.http_code : '暂无' }}</el-descriptions-item>
      <el-descriptions-item label="重定向URL">
        {{ record.redirect_url ? record.redirect_url : '暂无' }}
      </el-descriptions-item>
      <el-descriptions-item label="出队时间">{{ formatDate(record.pop_time) }}</el-descriptions-item>
      <el-descriptions-item label="优先级">{{ priorityMap[record.priority] }}</el-descriptions-item>
      <el-descriptions-item label="并发级别">{{ concurrentLevelMap[record.concurrent_level] }}</el-descriptions-item>
      <el-descriptions-item label="抓取内容">
        {{ record.fetch_content_url ? record.fetch_content_url : '暂无' }}
      </el-descriptions-item>
      <el-descriptions-item label="抓取时间">{{ formatDate(record.fetch_time) }}</el-descriptions-item>
      <el-descriptions-item label="MimeType">{{ formatMimeType(record) }}</el-descriptions-item>
      <el-descriptions-item label="抓取深度">{{ record.depth }}</el-descriptions-item>
      <el-descriptions-item v-if="record.field_map && Object.keys(record.field_map).length > 0"
                            label="结构化数据" :span="4">
        <json-viewer class="w100" :value="record.field_map" :expand-depth=0 boxed sort />
      </el-descriptions-item>
      <el-descriptions-item v-if="record.user_defined_map && Object.keys(record.user_defined_map).length > 0"
                            label="自定义数据" :span="4">
        <json-viewer class="w100" :value="record.user_defined_map" :expand-depth=0 boxed sort />
      </el-descriptions-item>
      <el-descriptions-item v-if="record.headers && Object.keys(record.headers).length > 0"
                            label="HTTP Header" :span="4">
        <json-viewer class="w100" :value="record.headers" :expand-depth=0 boxed sort />
      </el-descriptions-item>
    </el-descriptions>
  </el-dialog>
</template>

<style scoped>
</style>