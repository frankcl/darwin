<script setup>
import JsonViewer from 'vue-json-viewer'
import { ref, watchEffect } from 'vue'
import { ElDialog } from 'element-plus'
import { asyncPreview } from '@/common/AsyncRequest'
import DarwinCard from '@/components/data/Card'

const props = defineProps(['recordKey'])
const open = defineModel()
const previewJSON = ref()

watchEffect(async () => {
  try {
    if (props.recordKey) previewJSON.value = JSON.parse(await asyncPreview(props.recordKey))
  } catch {
    open.value = false
  }
})
</script>

<template>
  <el-dialog v-model="open" align-center show-close>
    <darwin-card title="JSON预览">
      <json-viewer v-if="previewJSON" class="json-preview" :value="previewJSON" :expand-depth=5 sort />
    </darwin-card>
  </el-dialog>
</template>

<style scoped>
.json-preview {
  width: 100%;
  max-width: 1200px;
}
</style>