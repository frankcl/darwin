<script setup>
import { onMounted, onUnmounted, reactive, ref, useTemplateRef, watch } from 'vue'
import { ElButton, ElCol, ElForm, ElFormItem, ElInput, ElRow } from 'element-plus'
import { basicSetup } from 'codemirror'
import { EditorState } from '@codemirror/state'
import { EditorView } from '@codemirror/view'
import { oneDark } from '@codemirror/theme-one-dark'
import JsonViewer from 'vue-json-viewer'
import { useUserStore } from '@/store'
import { pause } from '@/common/Time'
import { asyncCompileScript, asyncDebugScript } from '@/common/AsyncRequest'
import { debugFormRules } from '@/views/rule/common'

const props = defineProps(['regex', 'script', 'script_type'])
const userStore = useUserStore()
const debugFormRef = useTemplateRef('debugForm')
const termRef = useTemplateRef('term')
const termView = ref()
const termOutput = ref('')
const debugging = ref(false)
const request = reactive({})
const parseResult = reactive({})

const resetParseResult = () => {
  delete parseResult.field_map
  delete parseResult.user_defined_map
  delete parseResult.children
}

const checkURL = async () => {
  termOutput.value += '第一步：检测调试URL合法性 ...\n'
  await pause(1000)
  if (!request.url) {
    termOutput.value += '发生错误 -> 调试URL缺失，请输入调试URL\n'
    return false
  }
  if (!props.regex || !new RegExp(props.regex).test(request.url)) {
    termOutput.value += '发生错误 -> URL: ' + request.url + '不符合脚本规则\n'
    return false
  }
  termOutput.value += '检测结束：调试URL合法\n'
  return true
}

const checkScript = async () => {
  termOutput.value += '第二步：检测脚本合法性 ...\n'
  await pause(1000)
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
  termOutput.value += '检测结束：脚本合法\n'
  return true
}

const debug = async formElement => {
  resetParseResult()
  if (!await formElement.validate(v => v)) return
  termOutput.value = '开始调试 ...\n'
  let response
  try {
    debugging.value = true
    if (!await checkURL()) return
    if (!await checkScript()) return
    termOutput.value += '第三步：开始抓取并进行结构化解析 ...\n'
    await pause(1000)
    response = await asyncDebugScript({
      url: request.url,
      script: props.script,
      script_type: props.script_type
    })
    if (response.success) {
      termOutput.value += '抓取和解析成功\n'
      if (response.field_map) parseResult.field_map = response.field_map
      if (response.user_defined_map) parseResult.user_defined_map = response.user_defined_map
      if (response.children) parseResult.children = response.children
      return
    }
    termOutput.value += '发生错误 -> ' + response.message + '\n'
    termOutput.value += '异常堆栈 -> '
    termOutput.value += response.stack_trace + '\n'
  } finally {
    if (response && response.debug_log) {
      termOutput.value += '\n调试日志\n'
      termOutput.value += response.debug_log
    }
    termOutput.value += '调试结束\n'
    debugging.value = false
  }
}

const initTerm = () => {
  destroyTerm()
  const state = EditorState.create({
    doc: termOutput.value,
    extensions: [
      basicSetup,
      EditorState.readOnly.of(true),
      oneDark
    ]
  })
  if (termRef) {
    termView.value = new EditorView({
      state: state,
      parent: termRef.value
    })
  }
}

const destroyTerm = () => {
  if (termView.value) termView.value.destroy()
}

watch(() => termOutput.value, () => initTerm())
watch(() => props.regex, () => {
  request.url = undefined
  termOutput.value = ''
  resetParseResult()
  initTerm()
})
onMounted(() => initTerm())
onUnmounted(() => destroyTerm())
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
      <div class="w100 terminal" ref="term" />
    </el-form-item>
    <el-form-item v-if="parseResult.children && parseResult.children.length > 0" label="抽链列表" label-position="top">
      <json-viewer class="w100" :value="parseResult.children" :expand-depth=0 boxed sort />
    </el-form-item>
    <el-form-item v-if="parseResult.field_map && Object.keys(parseResult.field_map).length > 0"
                  label="结构化数据" label-position="top">
      <json-viewer class="w100" :value="parseResult.field_map" :expand-depth=0 boxed sort />
    </el-form-item>
    <el-form-item v-if="parseResult.user_defined_map && Object.keys(parseResult.user_defined_map).length > 0"
                  label="自定义数据" label-position="top">
      <json-viewer class="w100" :value="parseResult.user_defined_map" :expand-depth=0 boxed sort />
    </el-form-item>
  </el-form>
</template>

<style scoped>
.terminal {
  max-width: 980px;
  height: 300px;
  background-color: #292C34;
  overflow: scroll;
}
</style>