<script setup>
import axios from 'axios'
import { ref, watchEffect } from 'vue'
import { ElDialog, ElLoading } from 'element-plus'
import { ERROR, showMessage } from '@/common/Feedback'
import DarwinCard from '@/components/data/Card'
import AudioPlayer from '@/components/data/AudioPlayer'

const props = defineProps(['recordKey'])
const emits = defineEmits(['close'])
const open = defineModel()
const vLoading = ElLoading.directive
const loading = ref(true)
const openPlayer = ref(open.value)
const previewURL = ref()

const close = () => {
  emits('close')
  openPlayer.value = false
}

watchEffect(async () => {
  if (props.recordKey) {
    loading.value = true
    const future = axios.get('/api/url/previewStream?key=' + props.recordKey, {responseType: 'blob'})
    future.then(response => {
      const blob = new Blob([response.data])
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
  <el-dialog v-model="open" @close="close()" align-center show-close>
    <darwin-card title="音频预览">
      <audio-player v-loading="loading" :open="openPlayer" :audioURL="previewURL" />
    </darwin-card>
  </el-dialog>
</template>

<style scoped>

</style>