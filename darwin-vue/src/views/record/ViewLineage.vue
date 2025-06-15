<script setup>
import { ref, watch } from 'vue'
import { ElDialog } from 'element-plus'
import { asyncGetLineageNode } from '@/common/AsyncRequest'
import DarwinCard from '@/components/data/Card'
import LineageGraph from '@/components/graph/LineageGraph'

const props = defineProps(['recordKey'])
const open = defineModel()
const node = ref()

watch(() => props.recordKey, async () => {
  if (props.recordKey) {
    const n = await asyncGetLineageNode(props.recordKey)
    node.value = { key: n.key, text: n.url, parent_key: n.parent_key }
  }
}, { immediate: true })
</script>

<template>
  <el-dialog v-model="open" align-center show-close>
    <darwin-card title="血源关系">
      <lineage-graph :node="node" />
    </darwin-card>
  </el-dialog>
</template>

<style scoped>
</style>