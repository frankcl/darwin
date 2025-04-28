<script setup>
import { ref, watchEffect } from 'vue'
import { ElDialog, ElImage, ElRow, ElText } from 'element-plus'
import { asyncPreview } from '@/common/AsyncRequest'

const props = defineProps(['recordKey'])
const open = defineModel()
const previewImageURL = ref()

watchEffect(async () => {
  if (props.recordKey) previewImageURL.value = await asyncPreview(props.recordKey)
})
</script>

<template>
  <el-dialog v-model="open" width="1200" align-center show-close>
    <el-row class="mb-3">
      <el-text class="text-xl font-600">图片预览</el-text>
    </el-row>
    <el-row justify="center">
      <el-image v-if="previewImageURL" class="w100" :src="previewImageURL" />
    </el-row>
  </el-dialog>
</template>

<style scoped>
</style>