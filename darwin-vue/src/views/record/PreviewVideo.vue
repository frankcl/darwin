<script setup>
import { ref, watch, watchEffect } from 'vue'
import { ElDialog } from 'element-plus'
import VideoPlayer from '@/components/data/VideoPlayer'
import { asyncPreview } from '@/common/AsyncRequest'
import DarwinCard from '@/components/data/Card'

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
  try {
    if (props.recordKey) previewVideoURL.value = await asyncPreview(props.recordKey)
  } catch {
    open.value = false
  }
})
</script>

<template>
  <el-dialog v-model="open" @close="close()" align-center show-close>
    <darwin-card title="视频预览">
      <video-player :open="openVideoPlayer" :videoURL="previewVideoURL" />
    </darwin-card>
  </el-dialog>
</template>

<style scoped>
</style>