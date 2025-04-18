<script setup>
import { ref, useTemplateRef, watch, watchEffect } from 'vue'
import {
  ElButton, ElCol, ElForm, ElFormItem, ElInput, ElOption, ElRow, ElSelect
} from 'element-plus'
import { useUserStore } from '@/store'
import CodeEditor from '@/components/data/CodeEditor'
import { asyncExecuteAfterConfirming, ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import { asyncGetRule, asyncRemoveRule, asyncUpdateRule } from '@/common/AsyncRequest'
import { langMap, ruleFormRules } from '@/views/rule/common'

const props = defineProps(['id', 'refresh'])
const emits = defineEmits(['update', 'change', 'remove'])
const userStore = useUserStore()
const ruleFormRef = useTemplateRef('ruleForm')
const rule = ref()
const refreshEditor = ref(Date.now())

const update = async formElement => {
  if (!await formElement.validate(v => v)) return
  if (!await asyncUpdateRule(rule.value)) {
    showMessage('编辑规则失败', ERROR)
    return
  }
  showMessage('编辑规则成功', SUCCESS)
  refreshEditor.value = Date.now()
  emits('update')
}

const remove = async id => {
  const success = await asyncExecuteAfterConfirming(asyncRemoveRule, id)
  if (success === undefined) return
  if (!success) {
    showMessage('删除规则失败', ERROR)
    return
  }
  showMessage('删除规则成功', SUCCESS)
  emits('remove')
}

const handleScriptChange = script => rule.value.script = script
const handleReset = formElement => {
  formElement.resetFields()
  refreshEditor.value = Date.now()
}

watch(() => [props.id, props.refresh], async () => {
  if (props.id) {
    rule.value = await asyncGetRule(props.id)
    refreshEditor.value = Date.now()
  }
}, { immediate: true })
watchEffect(() => emits('change', rule.value))
</script>

<template>
  <el-form v-if="rule" ref="ruleForm" :model="rule" :rules="ruleFormRules" label-width="80px" label-position="right">
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
      <el-button type="primary" @click="update(ruleFormRef)" :disabled="!userStore.injected">编辑</el-button>
      <el-button type="info" @click="handleReset(ruleFormRef)">重置</el-button>
      <el-button type="danger" @click="remove(rule.id)" :disabled="!userStore.injected">删除</el-button>
    </el-form-item>
  </el-form>
</template>

<style scoped>
</style>