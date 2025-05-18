<script setup>
import { ref, watchEffect } from 'vue'
import { ElDialog } from 'element-plus'
import DarwinCard from '@/components/data/Card'

const props = defineProps(['recordKey'])
const open = defineModel()
const previewHTML = ref()

watchEffect(async () => {
  try {
    if (props.recordKey) previewHTML.value = '/api/url/previewHTML?key=' + props.recordKey
  } catch (e) {
    open.value = false
  }
})
</script>

<template>
  <el-dialog v-model="open" width="1200" align-center show-close>
    <darwin-card title="网页预览">
      <iframe v-if="previewHTML" :src="previewHTML" class="web-page-preview" sandbox="allow-scripts" />
    </darwin-card>
  </el-dialog>
</template>

<style scoped>
.web-page-preview {
  width: 800px;
  height: 600px;
  border: none;
  border-radius: 5px;
  box-shadow: 0 0 5px #8fb8e2;
}
</style>