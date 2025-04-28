<script setup>
import JsonViewer from 'vue-json-viewer'
import { ref, watchEffect } from 'vue'
import { ElDialog, ElRow, ElText } from 'element-plus'
import { asyncPreview } from '@/common/AsyncRequest'

const props = defineProps(['recordKey'])
const open = defineModel()
const previewJSON = ref()

watchEffect(async () => {
  if (props.recordKey) previewJSON.value = JSON.parse(await asyncPreview(props.recordKey))
})
</script>

<template>
  <el-dialog v-model="open" width="1200" align-center show-close>
    <el-row class="mb-3">
      <el-text class="text-xl font-600">JSON预览</el-text>
    </el-row>
    <el-row justify="center">
      <json-viewer v-if="previewJSON" class="json-preview" :value="previewJSON" :expand-depth=5 boxed sort />
    </el-row>
  </el-dialog>
</template>

<style scoped>
.json-preview {
  width: 100%;
  max-width: 1200px;
}
</style>