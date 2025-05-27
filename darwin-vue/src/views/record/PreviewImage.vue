<script setup>
import { ref, watchEffect } from 'vue'
import { ElDialog, ElImage } from 'element-plus'
import { asyncPreview } from '@/common/AsyncRequest'
import DarwinCard from '@/components/data/Card'

const props = defineProps(['recordKey'])
const open = defineModel()
const previewImageURL = ref()

watchEffect(async () => {
  try {
    if (props.recordKey) previewImageURL.value = await asyncPreview(props.recordKey)
  } catch {
    open.value = false
  }
})
</script>

<template>
  <el-dialog v-model="open" align-center show-close>
    <darwin-card title="图片预览">
      <el-image v-if="previewImageURL" :src="previewImageURL" class="image" />
    </darwin-card>
  </el-dialog>
</template>

<style scoped>
.image {
  display: flex;
  justify-self: center;
}
</style>