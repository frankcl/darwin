<script setup>
import { ref, watch, watchEffect } from 'vue'
import { ElDialog, ElRow, ElText } from 'element-plus'
import VideoPlayer from '@/components/data/VideoPlayer'
import { asyncPreview } from '@/common/AsyncRequest'

const props = defineProps(['recordKey'])
const emits = defineEmits(['close'])
const open = defineModel()
const openVideoPlayer = ref(open.value)
const previewVideoURL = ref()

const close = () => {
  emits('close')
  openVideoPlayer.value = false
}

watch(() => open.value, () => openVideoPlayer.value = open.value)
watchEffect(async () => {
  if (props.recordKey) previewVideoURL.value = await asyncPreview(props.recordKey)
})
</script>

<template>
  <el-dialog v-model="open" @close="close()" width="1200" align-center show-close>
    <el-row class="mb-3">
      <el-text class="text-xl font-600">视频预览</el-text>
    </el-row>
    <el-row justify="center">
      <video-player :open="openVideoPlayer" :videoURL="previewVideoURL" />
    </el-row>
  </el-dialog>
</template>

<style scoped>
</style>