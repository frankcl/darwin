<script setup>
import { reactive, ref, useTemplateRef, watch } from 'vue'
import { ElButton, ElCol, ElForm, ElFormItem, ElInput, ElRow } from 'element-plus'
import JsonViewer from 'vue-json-viewer'
import { useUserStore } from '@/store'
import { pause } from '@/common/Time'
import { asyncCompileScript, asyncDebugScript } from '@/common/AsyncRequest'
import CodeEditor from '@/components/data/CodeEditor'


const props = defineProps(['regex', 'script', 'script_type'])
const userStore = useUserStore()
const debugFormRef = useTemplateRef('debugForm')
const termOutput = ref('')
const refresh = ref(Date.now())
const debugging = ref(false)
const request = reactive({})
const parseResult = reactive({})
const debugFormRules = {
  url: [
    { required: true, message: '请输入调试URL', trigger: 'change'}
  ]
}

const refreshTerm = async () => {
  refresh.value = Date.now()
  await pause(1000)
}

const reset = () => {
  termOutput.value = ''
  refresh.value = Date.now()
  delete parseResult.field_map
  delete parseResult.custom_map
  delete parseResult.children
}

const checkURL = async () => {
  termOutput.value += '第一步：检测调试URL合法性 ...\n'
  await refreshTerm()
  if (!request.url) {
    termOutput.value += '发生错误 -> 调试URL缺失，请输入调试URL\n'
    return false
  }
  if (!props.regex || !new RegExp(props.regex).test(request.url)) {
    termOutput.value += '发生错误 -> URL: ' + request.url + '不符合脚本规则\n'
    return false
  }
  termOutput.value += '检测结束：调试URL合法\n\n'
  return true
}

const checkScript = async () => {
  termOutput.value += '第二步：检测脚本合法性 ...\n'
  await refreshTerm()
  const response = await asyncCompileScript({
    script: props.script,
    script_type: props.script_type
  })
  if (!response.status) {
    termOutput.value += '发生错误 -> ' + response.message + '\n'
    termOutput.value += '异常堆栈 -> '
    termOutput.value += response.stack_trace + '\n'
    return false
  }
  termOutput.value += '检测结束：脚本合法\n\n'
  return true
}

const debug = async formElement => {
  reset()
  if (!await formElement.validate(v => v)) return
  termOutput.value = '开始调试 ...\n\n'
  let response
  try {
    debugging.value = true
    if (!await checkURL()) return
    if (!await checkScript()) return
    termOutput.value += '第三步：开始抓取并进行结构化解析 ...\n'
    await refreshTerm()
    response = await asyncDebugScript({
      url: request.url,
      script: props.script,
      script_type: props.script_type
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

watch(() => props.regex, () => {
  request.url = undefined
  reset()
})
</script>

<template>
  <el-form :model="request" ref="debugForm" :rules="debugFormRules" label-width="80px">
    <el-row :gutter="20">
      <el-col :span="15">
        <el-form-item label="调试URL" prop="url" label-position="right">
          <el-input v-model="request.url" clearable />
        </el-form-item>
      </el-col>
      <el-col :span="3">
        <el-button type="success" @click="debug(debugFormRef)" :loading="debugging"
                   :disabled="!userStore.injected">调试</el-button>
      </el-col>
    </el-row>
    <el-form-item label-position="top">
      <code-editor :code="termOutput" :refresh="refresh" lang="xml" :height="350" :read-only="true" />
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
</template>

<style scoped>
</style>