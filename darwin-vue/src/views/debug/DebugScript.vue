<script setup>
import { IconBug } from '@tabler/icons-vue'
import { reactive, ref, useTemplateRef, watch } from 'vue'
import { ElButton, ElCol, ElForm, ElFormItem, ElInput, ElRow } from 'element-plus'
import { useUserStore } from '@/store'
import { pause } from '@/common/Time'
import { asyncCompileScript, asyncDebugScript } from '@/common/AsyncRequest'
import TextEditor from '@/components/data/TextEditor'
import DebugResult from '@/views/debug/DebugResult'

const props = defineProps(['regex', 'script', 'script_type'])
const userStore = useUserStore()
const formRef = useTemplateRef('form')
const termOutput = ref('')
const refresh = ref(Date.now())
const debugging = ref(false)
const request = reactive({})
const parseResult = reactive({})
const formRules = { url: [{ required: true, message: '请输入调试URL', trigger: 'change'}] }

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
    if (response && response.stdout) {
      termOutput.value += '\n标准输出\n'
      termOutput.value += response.stdout
    }
    if (response && response.stderr) {
      termOutput.value += '\n标准错误\n'
      termOutput.value += response.stderr
    }
    return false
  }
  termOutput.value += '检测结束：脚本合法\n\n'
  return true
}

const debug = async () => {
  if (!await formRef.value.validate(v => v)) return
  reset()
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

watch(() => props.regex, () => reset())
</script>

<template>
  <el-form :model="request" ref="form" :rules="formRules" label-width="80px" class="ml-2 mr-2 mt-2">
    <el-row>
      <el-col :span="21">
        <el-form-item label="调试URL" prop="url" label-position="right">
          <el-input v-model="request.url" clearable />
        </el-form-item>
      </el-col>
      <el-col :span="3" class="d-flex justify-content-end">
        <el-button type="success" @click="debug" :loading="debugging" :disabled="!userStore.injected">
          <IconBug size="20" class="mr-1" />
          <span>调试</span>
        </el-button>
      </el-col>
    </el-row>
    <el-form-item label-position="top">
      <text-editor title="调试终端" v-model="termOutput" :refresh="refresh" :height="350" :read-only="true" />
    </el-form-item>
    <debug-result :children="parseResult.children" :custom-map="parseResult.custom_map"
                  :field-map="parseResult.field_map" />
  </el-form>
</template>

<style scoped>
</style>