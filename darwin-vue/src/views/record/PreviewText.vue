<script setup>
import xmlFormat from 'xml-formatter'
import { ref, watchEffect } from 'vue'
import { ElDialog } from 'element-plus'
import TextEditor from '@/components/data/TextEditor'
import { asyncPreview } from '@/common/AsyncRequest'
import DarwinCard from '@/components/data/Card'

const props = defineProps(['recordKey', 'recordType'])
const open = defineModel()
const previewText = ref()
const previewType = ref()

watchEffect(async () => {
  if (props.recordKey) {
    try {
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
        previewType.value = undefined
      }
    } catch {
      open.value = false
    }
  }
})
</script>

<template>
  <el-dialog v-model="open" align-center show-close>
    <darwin-card title="文本预览">
      <text-editor v-if="previewText" title="文本阅读器" v-model="previewText" :lang="previewType" :read-only="true" />
    </darwin-card>
  </el-dialog>
</template>

<style scoped>
</style>