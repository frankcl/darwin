<script setup>
import WaveSurfer from 'wavesurfer.js'
import TimelinePlugin from 'wavesurfer.js/dist/plugins/timeline.esm.js'
import { IconPlayerPlayFilled, IconPlayerStopFilled } from '@tabler/icons-vue'
import { onUnmounted, ref, watch } from 'vue'
import { ElButton } from 'element-plus'

const props = defineProps(['audioURL', 'open'])
const playing = ref(false)
const player = ref()

const init = () => {
  player.value = WaveSurfer.create({
    container: '#waveform',
    url: props.audioURL,
    autoCenter: false,
    waveColor: '#4F4A85',
    progressColor: '#383351',
    plugins: [TimelinePlugin.create()]
  })
}

const destroy = () => {
  if (player.value) player.value.destroy()
}

const playPause = () => {
  player.value.playPause()
  playing.value = player.value.isPlaying()
}

watch(() => props.audioURL, () => init())
watch(() => props.open, () => {
  if (!props.open && player.value) player.value.pause()
})
onUnmounted(() => destroy())
</script>

<template>
  <div>
    <div id="waveform" class="mb-4"></div>
    <el-button v-if="playing" type="primary" @click="playPause">
      <IconPlayerStopFilled class="mr-1" />暂停
    </el-button>
    <el-button v-else type="primary" @click="playPause">
      <IconPlayerPlayFilled class="mr-1" />播放
    </el-button>
  </div>
</template>

<style scoped>

</style>