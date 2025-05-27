<script setup>
import 'video.js/dist/video-js.css'
import VideoJS from 'video.js'
import { onUnmounted, ref, useTemplateRef, watch } from 'vue'

const props = defineProps(['videoURL', 'open'])
const playerRef = useTemplateRef('videoPlayer')
const player = ref()

const videoMediaType = requestURL => {
  const url = new URL(requestURL)
  const index = url.pathname.lastIndexOf('.')
  const videoType = index === -1 ? 'mp4' : url.pathname.substring(index + 1).toLowerCase()
  return 'video/' + videoType
}

const close = () => {
  if (player.value) {
    player.value.dispose()
    player.value = undefined
  }
}

const open = () => {
  if (props.videoURL) {
    close()
    const mediaType = videoMediaType(props.videoURL)
    player.value = VideoJS(playerRef.value, {
      autoplay: false,
      aspectRatio: '16:9',
      controls: true,
      fluid: true,
      preload: 'auto',
      sources: [{
        src: props.videoURL,
        type: mediaType
      }]
    }, () => {
      // const player = document.querySelector('.vjs-tech')
      // player.style.maxHeight = '600px'
    })
  }
}

watch(() => props.videoURL, () => open())
watch(() => props.open, () => {
  if (!props.open && player.value) player.value.pause()
})
onUnmounted(() => close())
</script>

<template>
  <div ref="videoContainer" class="video-container">
    <video ref="videoPlayer" class="video-js vjs-default-skin" />
  </div>
</template>

<style scoped>
.video-container {
  width: 100%;
  max-width: 1200px;
  overflow: hidden;
  margin: 0 auto;
}
.video-js {
  width: 100%;
  height: auto;
}
</style>