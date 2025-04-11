<script setup>
import { ref, watchEffect } from 'vue'
import { ElDescriptions, ElDescriptionsItem, ElDialog } from 'element-plus'
import { asyncGetHistory } from '@/common/service'
import { langMap } from './common'
import CodeEditor from "@/components/data/CodeEditor.vue";

const props = defineProps(['id', 'name'])
const open = defineModel()
const emits = defineEmits(['close'])
const history = ref()
watchEffect(async () => {
  if (props.id) history.value = await asyncGetHistory(props.id)
})
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" width="1000" align-center show-close>
    <el-descriptions v-if="history" :title="name" direction="vertical" :column="3" border>
      <el-descriptions-item label="版本号">{{ history.version }}</el-descriptions-item>
      <el-descriptions-item label="匹配规则">{{ history.regex }}</el-descriptions-item>
      <el-descriptions-item label="脚本类型">{{ langMap[history.script_type] }}</el-descriptions-item>
      <el-descriptions-item label="脚本代码" :span="3">
        <code-editor :code="history.script" :lang="langMap[history.script_type]" :read-only="true" />
      </el-descriptions-item>
    </el-descriptions>
  </el-dialog>
</template>

<style scoped>

</style>