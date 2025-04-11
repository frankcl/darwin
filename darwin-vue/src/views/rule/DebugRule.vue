<script setup>
import { onMounted, onUnmounted, reactive, ref, useTemplateRef, watch } from 'vue'
import { ElButton, ElCol, ElForm, ElFormItem, ElInput, ElRow } from 'element-plus'
import { basicSetup } from 'codemirror'
import { EditorState } from '@codemirror/state'
import { EditorView } from '@codemirror/view'
import { oneDark } from '@codemirror/theme-one-dark'
import JsonViewer from 'vue-json-viewer'
import { asyncCompileScript, asyncDebugScript } from '@/common/service'
import { sleep } from '@/common/assortment'
import { debugFormRules } from '@/views/rule/common'

const props = defineProps(['regex', 'script', 'script_type'])
const formRef = useTemplateRef('formRef')
const termRef = useTemplateRef('termRef')
const termView = ref()
const termOutput = ref('')
const debugForm = reactive({})
const parseSuccess = ref(false)
const parseResult = reactive({})

const clearParseResult = () => {
  parseSuccess.value = false
  delete parseResult.field_map
  delete parseResult.user_defined_map
  delete parseResult.child_urls
}

const checkURL = async () => {
  termOutput.value += '第一步：检测调试URL合法性 ...\n'
  await sleep(1000)
  if (!debugForm.url) {
    termOutput.value += '发生错误 -> 调试URL缺失，请输入调试URL\n'
    return false
  }
  if (!props.regex || !new RegExp(props.regex).test(debugForm.url)) {
    termOutput.value += '发生错误 -> URL: ' + debugForm.url + '不符合脚本规则\n'
    return false
  }
  termOutput.value += '检测结束：调试URL合法\n'
  return true
}

const checkScript = async () => {
  termOutput.value += '第二步：检测脚本合法性 ...\n'
  await sleep(1000)
  const response = await asyncCompileScript({
    script: props.script,
    script_type: props.script_type
  })
  if (!response.success) {
    termOutput.value += '发生错误 -> ' + response.message + '\n'
    termOutput.value += '异常堆栈 -> '
    termOutput.value += response.stack_trace + '\n'
    return false
  }
  termOutput.value += '检测结束：脚本合法\n'
  return true
}

const debug = async formEl => {
  clearParseResult()
  if (!await formEl.validate(valid => valid)) return
  termOutput.value = '开始调试 ...\n'
  let response
  try {
    if (!await checkURL()) return
    if (!await checkScript()) return
    termOutput.value += '第三步：开始抓取并进行结构化解析 ...\n'
    await sleep(1000)
    response = await asyncDebugScript({
      url: debugForm.url,
      script: props.script,
      script_type: props.script_type
    })
    if (response.success) {
      termOutput.value += '抓取和解析成功\n'
      parseSuccess.value = true
      if (response.field_map) parseResult.field_map = response.field_map
      if (response.user_defined_map) parseResult.user_defined_map = response.user_defined_map
      if (response.child_urls) parseResult.child_urls = response.child_urls
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
onMounted(() => initTerm())
onUnmounted(() => destroyTerm())
</script>

<template>
  <el-form :model="debugForm" ref="formRef" :rules="debugFormRules" label-width="80px">
    <el-row :gutter="20">
      <el-col :span="22">
        <el-form-item label="调试URL" prop="url" label-position="right">
          <el-input v-model="debugForm.url" clearable />
        </el-form-item>
      </el-col>
      <el-col :span="2">
        <el-button @click="debug(formRef)">调试</el-button>
      </el-col>
    </el-row>
    <el-form-item label-position="top">
      <div class="w100 terminal" ref="termRef" />
    </el-form-item>
    <el-form-item v-if="parseSuccess" label-position="top">
      <json-viewer class="w100" :value="parseResult" :expand-depth=2 boxed sort />
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