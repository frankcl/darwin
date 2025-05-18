<script setup>
import { IconHelp, IconPlus, IconRefresh } from '@tabler/icons-vue'
import { reactive, ref, useTemplateRef, watch, watchEffect } from 'vue'
import { ElButton, ElForm, ElFormItem, ElInput, ElOption, ElSelect, ElTooltip } from 'element-plus'
import { useUserStore } from '@/store'
import TextEditor from '@/components/data/TextEditor'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import { asyncAddRule, asyncGetTemplate } from '@/common/AsyncRequest'
import { langMap, ruleFormRules } from '@/views/rule/common'

const props = defineProps(['planId'])
const emits = defineEmits(['add', 'change'])
const userStore = useUserStore()
const formRef = useTemplateRef('form')
const rule = reactive({ plan_id : props.planId, script_type: 1 })
const refreshEditor = ref(Date.now())

const add = async () => {
  if (!await formRef.value.validate(v => v)) return
  if (await asyncAddRule(rule)) {
    showMessage('新增规则成功', SUCCESS)
    emits('add')
    return
  }
  showMessage('新增规则失败', ERROR)
}

const handleReset = async () => {
  const prevType = rule.script_type
  formRef.value.resetFields()
  if (prevType === rule.script_type) {
    rule.script = await asyncGetTemplate(rule.script_type)
    refreshEditor.value = Date.now()
  }
}

watch(() => rule.script_type, async () => {
  if (rule.script_type !== undefined) {
    rule.script = await asyncGetTemplate(rule.script_type)
    refreshEditor.value = Date.now()
  }
}, { immediate: true})
watchEffect(async () => {
  if (props.planId) rule.plan_id = props.planId
})
watchEffect(() => emits('change', rule))
</script>

<template>
  <el-form ref="form" :model="rule" :rules="ruleFormRules" class="ml-2 mr-2"
           label-width="100px" label-position="left">
    <el-form-item label="规则名称" prop="name">
      <el-input v-model.trim="rule.name" clearable />
    </el-form-item>
    <el-form-item prop="regex">
      <template #label>
        <span class="d-flex align-items-center">
          <span>匹配规则</span>
          <el-tooltip effect="dark" placement="top" content="定义正则表达式，只有匹配表达式的链接进行脚本解析">
            <IconHelp size="12" class="ml-2"/>
          </el-tooltip>
        </span>
      </template>
      <el-input v-model.trim="rule.regex" clearable />
    </el-form-item>
    <el-form-item label="脚本类型" prop="script_type">
      <el-select v-model="rule.script_type">
        <el-option key="1" label="Groovy" :value="1" />
        <el-option key="2" label="JavaScript" :value="2" />
      </el-select>
    </el-form-item>
    <el-form-item prop="script" label-position="top">
      <text-editor title="代码编辑器" v-model="rule.script" :lang="langMap[rule.script_type]" :refresh="refreshEditor" />
    </el-form-item>
    <el-form-item label="变更原因" prop="change_log" label-position="top">
      <el-input type="textarea" v-model="rule.change_log" :rows="3" />
    </el-form-item>
    <el-form-item label-position="top">
      <el-button type="primary" @click="add" :disabled="!userStore.injected">
        <IconPlus size="20" class="mr-1" />
        <span>新增</span>
      </el-button>
      <el-button type="info" @click="handleReset">
        <IconRefresh size="20" class="mr-1" />
        <span>重置</span>
      </el-button>
    </el-form-item>
  </el-form>
</template>

<style scoped>
</style>