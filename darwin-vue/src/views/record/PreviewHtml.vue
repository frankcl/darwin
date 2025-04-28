<script setup>
import { ref, watchEffect } from 'vue'
import { ElDialog, ElRow, ElText } from 'element-plus'

const props = defineProps(['recordKey'])
const open = defineModel()
const previewHTML = ref()

watchEffect(async () => {
  if (props.recordKey) previewHTML.value = '/api/url/previewHTML?key=' + props.recordKey
})
</script>

<template>
  <el-dialog v-model="open" width="1200" align-center show-close>
    <el-row class="mb-3">
      <el-text class="text-xl font-600">网页预览</el-text>
    </el-row>
    <el-row justify="center">
      <iframe v-if="previewHTML" :src="previewHTML" class="web-page-preview" sandbox="allow-scripts" />
    </el-row>
  </el-dialog>
</template>

<style scoped>
.web-page-preview {
  width: 1200px;
  height: 600px;
  border: none;
  border-radius: 5px;
  box-shadow: 0 0 5px #8fb8e2;
}
</style>