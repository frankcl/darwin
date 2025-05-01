<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { ElDialog, ElForm, ElFormItem, ElRow } from 'element-plus'
import JsonViewer from 'vue-json-viewer'
import CodeEditor from '@/components/data/CodeEditor'
import { pause } from '@/common/Time'
import { asyncDebugURL, asyncGetSeed } from '@/common/AsyncRequest'

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
    <el-row align="middle" class="mb-2">
      <span class="text-xl font-bold ml-2">调试终端</span>
    </el-row>
    <el-form label-width="80px">
      <el-form-item label-position="top">
        <code-editor :code="termOutput" :refresh="refresh" lang="xml" :height="300" :read-only="true" />
      </el-form-item>
      <el-form-item v-if="parseResult.children && parseResult.children.length > 0" label="抽链列表" label-position="top">
        <json-viewer class="w100" :value="parseResult.children" :expand-depth=0 boxed sort />
      </el-form-item>
      <el-form-item v-if="parseResult.field_map && Object.keys(parseResult.field_map).length > 0"
                    label="结构化数据" label-position="top">
        <json-viewer class="w100" :value="parseResult.field_map" :expand-depth=0 boxed sort />
      </el-form-item>
      <el-form-item v-if="parseResult.custom_map && Object.keys(parseResult.custom_map).length > 0"
                    label="自定义数据" label-position="top">
        <json-viewer class="w100" :value="parseResult.custom_map" :expand-depth=0 boxed sort />
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<style scoped>
</style>