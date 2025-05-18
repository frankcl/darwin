<script setup>
import { ref, watchEffect } from 'vue'
import { ElDescriptions, ElDescriptionsItem, ElDialog } from 'element-plus'
import { asyncGetHistory } from '@/common/AsyncRequest'
import { scriptLangMap } from '@/common/Constants'
import TextEditor from '@/components/data/TextEditor'
import DarwinCard from '@/components/data/Card'

const props = defineProps(['id', 'name'])
const open = defineModel()
const emits = defineEmits(['close'])
const history = ref()
const refreshEditor = ref(Date.now())

watchEffect(async () => {
  if (props.id) {
    history.value = await asyncGetHistory(props.id)
    refreshEditor.value = Date.now()
  }
})
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" align-center show-close>
    <darwin-card :title="`规则历史：${name}`">
      <el-descriptions v-if="history" direction="vertical" :column="3" border>
        <el-descriptions-item label="版本号">{{ history.version }}</el-descriptions-item>
        <el-descriptions-item label="匹配规则">{{ history.regex }}</el-descriptions-item>
        <el-descriptions-item label="脚本类型">{{ scriptLangMap[history.script_type] }}</el-descriptions-item>
        <el-descriptions-item label="脚本代码" :span="3" class-name="wide-column">
          <text-editor v-model="history.script" title="代码编辑器" :refresh="refreshEditor"
                       :lang="scriptLangMap[history.script_type]" :read-only="true" />
        </el-descriptions-item>
      </el-descriptions>
    </darwin-card>
  </el-dialog>
</template>

<style scoped>
:deep(.wide-column) {
  max-width: 40vw;
}
</style>