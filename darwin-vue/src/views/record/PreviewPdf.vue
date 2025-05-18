<script setup>
import axios from 'axios'
import VuePdfApp from 'vue3-pdf-app'
import 'vue3-pdf-app/dist/icons/main.css'
import { ref, watchEffect } from 'vue'
import { ElDialog, ElLoading } from 'element-plus'
import { ERROR, showMessage } from '@/common/Feedback'
import DarwinCard from '@/components/data/Card'

const props = defineProps(['recordKey'])
const open = defineModel()
const vLoading = ElLoading.directive
const loading = ref(true)
const previewURL = ref()

watchEffect(async () => {
  if (props.recordKey) {
    loading.value = true
    const future = axios.get('/api/url/previewStream?key=' + props.recordKey, {responseType: 'blob'})
    future.then(response => {
      const blob = new Blob([response.data], {type: 'application/pdf'})
      previewURL.value = window.URL.createObjectURL(blob)
      loading.value = false
    }).catch(() => {
      showMessage('数据失效', ERROR)
      open.value = false
    })
  }
})
</script>

<template>
  <el-dialog v-model="open" align-center show-close>
    <darwin-card title="PDF预览">
      <vue-pdf-app v-loading="loading" :pdf="previewURL" class="preview" />
    </darwin-card>
  </el-dialog>
</template>

<style scoped>
.preview {
  width: 100%;
  height: 600px;
  max-width: 1200px;
}
</style>