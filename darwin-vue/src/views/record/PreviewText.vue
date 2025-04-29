<script setup>
import xmlFormat from 'xml-formatter'
import { ref, watchEffect } from 'vue'
import { ElDialog, ElRow, ElText } from 'element-plus'
import CodeEditor from '@/components/data/CodeEditor'
import { asyncPreview } from '@/common/AsyncRequest'

const props = defineProps(['recordKey', 'recordType'])
const open = defineModel()
const previewText = ref()
const previewType = ref('xml')

watchEffect(async () => {
  if (props.recordKey) {
    previewText.value = await asyncPreview(props.recordKey)
    if (props.recordType === 'XML') {
      previewType.value = 'xml'
      previewText.value = xmlFormat(previewText.value)
    } else if (props.recordType === 'JAVASCRIPT') {
      previewType.value = 'javascript'
    } else if (props.recordType === 'CSS') {
      previewType.value = 'css'
    } else if (props.recordType === 'HTML' || props.recordType === 'XHTML') {
      previewType.value = 'html'
    } else {
      previewType.value = 'xml'
    }
  }
})
</script>

<template>
  <el-dialog v-model="open" width="1200" align-center show-close>
    <el-row class="mb-3">
      <el-text class="text-xl font-600">文本预览</el-text>
    </el-row>
    <el-row justify="center">
      <code-editor v-if="previewText" :code="previewText" :lang="previewType" :read-only="true" />
    </el-row>
  </el-dialog>
</template>

<style scoped>
</style>