<script setup>
import { reactive, ref, useTemplateRef, watch, watchEffect } from 'vue'
import {
  ElButton, ElCol, ElForm, ElFormItem,
  ElInput, ElOption, ElRow, ElSelect
} from 'element-plus'
import { useUserStore } from '@/store'
import CodeEditor from '@/components/data/CodeEditor'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import { asyncAddRule, asyncGetTemplate } from '@/common/AsyncRequest'
import { langMap, ruleFormRules } from '@/views/rule/common'

const props = defineProps(['planId'])
const emits = defineEmits(['change'])
const userStore = useUserStore()
const ruleFormRef = useTemplateRef('ruleForm')
const rule = reactive({ plan_id : props.planId, script_type: 1 })
const refreshEditor = ref(Date.now())

const add = async formElement => {
  if (!await formElement.validate(v => v)) return
  if (await asyncAddRule(rule)) {
    showMessage('添加规则成功', SUCCESS)
    return
  }
  showMessage('添加规则失败', ERROR)
}

const handleScriptChange = script => rule.script = script
const handleReset = formElement => {
  formElement.resetFields()
  refreshEditor.value = Date.now()
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
  <el-form ref="ruleForm" :model="rule" :rules="ruleFormRules" label-width="80px" label-position="right">
    <el-row :gutter="20">
      <el-col :span="14">
        <el-form-item label="规则名称" prop="name">
          <el-input v-model.trim="rule.name" clearable />
        </el-form-item>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="14">
        <el-form-item label="匹配规则" prop="regex">
          <el-input v-model.trim="rule.regex" clearable />
        </el-form-item>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-form-item label="脚本类型" prop="script_type">
          <el-select v-model="rule.script_type" class="w150px">
            <el-option key="1" label="Groovy" :value="1" />
            <el-option key="2" label="JavaScript" :value="2" />
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item prop="script" label-position="top">
      <code-editor :code="rule.script" :lang="langMap[rule.script_type]"
                   :refresh="refreshEditor" @change="handleScriptChange" />
    </el-form-item>
    <el-form-item label="变更原因" prop="change_log" label-position="top">
      <el-input type="textarea" v-model="rule.change_log" :rows="3" />
    </el-form-item>
    <el-form-item label-position="top">
      <el-button type="primary" @click="add(ruleFormRef)" :disabled="!userStore.injected">添加</el-button>
      <el-button type="info" @click="handleReset(ruleFormRef)">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<style scoped>
</style>