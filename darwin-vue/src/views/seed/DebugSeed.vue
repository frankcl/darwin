<script setup>
import { onMounted, onUnmounted, reactive, ref, useTemplateRef, watch } from 'vue'
import { ElDialog, ElForm, ElFormItem, ElLoading, ElRow } from 'element-plus'
import { basicSetup } from 'codemirror'
import { EditorState } from '@codemirror/state'
import { EditorView } from '@codemirror/view'
import { oneDark } from '@codemirror/theme-one-dark'
import JsonViewer from 'vue-json-viewer'
import { pause } from '@/common/Time'
import { asyncDebugURL } from '@/common/AsyncRequest'

const props = defineProps(['seedKey', 'planId'])
const vLoading = ElLoading.directive
const open = defineModel()
const termRef = useTemplateRef('term')
const termView = ref()
const termOutput = ref('')
const debugging = ref(false)
const parseResult = reactive({})

const reset = () => {
  termOutput.value = ''
  delete parseResult.field_map
  delete parseResult.custom_map
  delete parseResult.children
}

const debug = async () => {
  reset()
  termOutput.value = '开始调试 ...\n'
  let response
  try {
    debugging.value = true
    termOutput.value += '开始抓取并进行结构化解析 ...\n'
    await pause(1000)
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

watch(() => [props.seedKey, props.planId], () => {
  initTerm()
  debug()
})
watch(() => termOutput.value, () => initTerm())
onMounted(() => {
  initTerm()
  debug()
})
onUnmounted(() => destroyTerm())
</script>

<template>
  <el-dialog v-model="open" width="850" align-center show-close>
    <el-row align="middle" class="mb-2">
      <span class="text-xl font-bold ml-2">调试终端</span>
    </el-row>
    <el-form label-width="80px">
      <el-form-item label-position="top">
        <div v-loading="debugging" class="w100 terminal" ref="term" />
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
.terminal {
  max-width: 980px;
  height: 300px;
  background-color: #292C34;
  overflow: scroll;
}
</style>