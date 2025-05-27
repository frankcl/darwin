<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { ElDialog } from 'element-plus'
import DarwinCard from '@/components/data/Card'
import TextEditor from '@/components/data/TextEditor'
import { pause } from '@/common/Time'
import { asyncDebugURL, asyncGetSeed } from '@/common/AsyncRequest'
import DebugResult from '@/views/debug/DebugResult'

const props = defineProps(['seedKey', 'planId'])
const open = defineModel()
const termOutput = ref('')
const refresh = ref(Date.now())
const debugging = ref(false)
const parseResult = reactive({})

const reset = () => {
  termOutput.value = ''
  delete parseResult.field_map
  delete parseResult.custom_map
  delete parseResult.children
}

const refreshTerm = async () => {
  refresh.value = Date.now()
  await pause(1000)
}

const debug = async () => {
  reset()
  termOutput.value = '获取种子信息 ...\n'
  await refreshTerm()
  const seed = await asyncGetSeed(props.seedKey)
  termOutput.value += `成功获取种子信息: ${seed.url}\n\n`
  termOutput.value += '开始调试 ...\n'
  await refreshTerm()
  let response
  try {
    debugging.value = true
    termOutput.value += '抓取种子并进行结构化解析 ...\n'
    await refreshTerm()
    response = await asyncDebugURL({
      key: props.seedKey,
      plan_id: props.planId,
    })
    if (response.success) {
      termOutput.value += '抓取和解析成功\n'
      if (response.field_map) parseResult.field_map = response.field_map
      if (response.custom_map) parseResult.custom_map = response.custom_map
      if (response.children) parseResult.children = response.children
      return
    }
    termOutput.value += '发生错误 -> ' + response.message + '\n'
    termOutput.value += '异常堆栈 -> '
    termOutput.value += response.stack_trace
  } finally {
    if (response && response.debug_log) {
      termOutput.value += '\n调试日志\n'
      termOutput.value += response.debug_log
    }
    if (response && response.stdout) {
      termOutput.value += '\n标准输出\n'
      termOutput.value += response.stdout
    }
    if (response && response.stderr) {
      termOutput.value += '\n标准错误\n'
      termOutput.value += response.stderr
    }
    termOutput.value += '\n调试结束\n'
    debugging.value = false
    await refreshTerm()
  }
}

watch(() => [props.seedKey, props.planId], () => debug())
onMounted(() => debug())
</script>

<template>
  <el-dialog v-model="open" width="850" align-center show-close>
    <darwin-card title="种子调试">
      <text-editor class="mb-4" title="调试终端" v-model="termOutput" :refresh="refresh"
                   :height="300" :read-only="true" />
      <debug-result :children="parseResult.children" :custom-map="parseResult.custom_map"
                    :field-map="parseResult.field_map" />
    </darwin-card>
  </el-dialog>
</template>

<style scoped>
</style>