<script setup>
import axios from 'axios'
import VuePdfApp from 'vue3-pdf-app'
import 'vue3-pdf-app/dist/icons/main.css'
import { ref, watchEffect } from 'vue'
import { ElDialog, ElRow, ElText } from 'element-plus'

const props = defineProps(['recordKey'])
const open = defineModel()
const previewPdfURL = ref()

watchEffect(async () => {
  if (props.recordKey) {
    const response = await axios.get('/api/url/previewPDF?key=' + props.recordKey, { responseType: 'blob' })
    const blob = new Blob([response.data], { type: 'application/pdf' })
    previewPdfURL.value = window.URL.createObjectURL(blob)
  }
})
</script>

<template>
  <el-dialog v-model="open" width="1200" align-center show-close>
    <el-row class="mb-3">
      <el-text class="text-xl font-600">PDF预览</el-text>
    </el-row>
    <el-row justify="center">
      <vue-pdf-app v-if="previewPdfURL" :pdf="previewPdfURL" class="preview" />
    </el-row>
  </el-dialog>
</template>

<style scoped>
.preview {
  width: 100%;
  height: 600px;
  max-width: 1200px;
}
</style>