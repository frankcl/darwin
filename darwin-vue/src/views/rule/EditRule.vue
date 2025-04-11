<script setup>
import { ref, useTemplateRef, watch, watchEffect } from 'vue'
import {
  ElButton, ElCol, ElForm, ElFormItem, ElInput, ElNotification, ElOption, ElRow, ElSelect
} from 'element-plus'
import { checkUserLogin, executeAsyncRequest, executeAsyncRequestAfterConfirm } from '@/common/assortment'
import { asyncDeleteRule, asyncGetRule, asyncUpdateRule } from '@/common/service'
import CodeEditor from '@/components/data/CodeEditor'
import { langMap, ruleFormRules } from '@/views/rule/common'

const props = defineProps(['id', 'forceRefresh'])
const emits = defineEmits(['refresh', 'change', 'remove'])
const formRef = useTemplateRef('formRef')
const rule = ref()
const refreshEditor = ref(Date.now())

const update = async formEl => {
  const updateRule = {
    id: rule.value.id,
    name: rule.value.name,
    regex: rule.value.regex,
    script: rule.value.script,
    script_type: rule.value.script_type,
    change_log: rule.value.change_log
  }
  const successHandle = () => ElNotification.success('编辑规则成功')
  const failHandle = () => ElNotification.success('编辑规则失败')
  if (!await executeAsyncRequest(asyncUpdateRule, updateRule, successHandle, failHandle,
    undefined, formEl)) return
  refreshEditor.value = Date.now()
  emits('refresh')
}

const remove = async id => {
  if (!checkUserLogin()) return
  const successHandle = () => ElNotification.success('删除规则成功')
  const failHandle = () => ElNotification.error('删除规则失败')
  if (!await executeAsyncRequestAfterConfirm(
    '删除提示', '是否确定删除该规则？', asyncDeleteRule, id, successHandle, failHandle)) return
  emits('remove')
}

const handleScriptChange = script => rule.value.script = script
const handleReset = formEl => {
  formEl.resetFields()
  refreshEditor.value = Date.now()
}

watch(() => [props.id, props.forceRefresh], async () => {
  if (props.id) {
    rule.value = await asyncGetRule(props.id)
    refreshEditor.value = Date.now()
  }
}, { immediate: true })
watchEffect(() => emits('change', rule.value))
</script>

<template>
  <el-form v-if="rule" ref="formRef" :model="rule" :rules="ruleFormRules" label-width="80px" label-position="right">
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
      <el-button @click="update(formRef)">编辑</el-button>
      <el-button @click="remove(rule.id)">删除</el-button>
      <el-button @click="handleReset(formRef)">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<style scoped>
</style>