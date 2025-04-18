<script setup>
import { ref, watch, watchEffect } from 'vue'
import { ElDialog, ElImage, ElRow, ElText } from 'element-plus'
import JsonViewer from 'vue-json-viewer'
import VideoPlayer from '@/components/data/VideoPlayer'
import { asyncPreviewURL } from '@/common/AsyncRequest'
import { previewTitleMap } from '@/common/Constants'

const props = defineProps(['recordKey', 'recordType'])
const emits = defineEmits(['close'])
const open = defineModel()
const openVideoPlayer = ref(open.value)
const previewImageURL = ref()
const previewVideoURL = ref()
const previewPDFURL = ref()
const previewJSON = ref()
const previewHTML = ref()

const close = () => {
  emits('close')
  openVideoPlayer.value = false
}

watch(() => open.value, () => openVideoPlayer.value = open.value)
watchEffect(async () => {
  if (props.recordKey) {
    if (props.recordType === 'image') previewImageURL.value = await asyncPreviewURL(props.recordKey)
    else if (props.recordType === 'video') previewVideoURL.value = await asyncPreviewURL(props.recordKey)
    else if (props.recordType === 'pdf') previewPDFURL.value = await asyncPreviewURL(props.recordKey)
    else if (props.recordType === 'json') previewJSON.value = JSON.parse(await asyncPreviewURL(props.recordKey))
    else previewHTML.value = '/api/url/previewHTML?key=' + props.recordKey
  }
})
</script>

<template>
  <el-dialog v-model="open" @close="close()" width="800" align-center show-close>
    <el-row class="mb-3">
      <el-text class="text-xl font-600">{{ previewTitleMap[props.recordType] }}</el-text>
    </el-row>
    <el-row justify="center">
      <el-image v-if="recordType === 'image'" class="w100" :src="previewImageURL" />
      <video-player v-else-if="recordType === 'video'" :open="openVideoPlayer" :videoURL="previewVideoURL" />
      <json-viewer v-else-if="recordType === 'json'" class="w100" :value="previewJSON" :expand-depth=1 boxed sort />
      <iframe v-else-if="recordType === 'html'" :src="previewHTML" class="web-page-preview" sandbox="allow-scripts"/>
    </el-row>
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