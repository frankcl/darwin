<script setup>
import { IconEdit, IconHelp, IconRefresh, IconTrash } from '@tabler/icons-vue'
import { ref, useTemplateRef, watch, watchEffect } from 'vue'
import {
  ElButton, ElCol, ElForm, ElFormItem, ElInput, ElOption, ElRow, ElSelect, ElTooltip
} from 'element-plus'
import { useUserStore } from '@/store'
import TextEditor from '@/components/data/TextEditor'
import { asyncExecuteAfterConfirming, ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import { asyncGetRule, asyncRemoveRule, asyncUpdateRule } from '@/common/AsyncRequest'
import { langMap, ruleFormRules } from '@/views/rule/common'

const props = defineProps(['id', 'refresh'])
const emits = defineEmits(['update', 'change', 'remove'])
const userStore = useUserStore()
const formRef = useTemplateRef('form')
const rule = ref()
const refreshEditor = ref(Date.now())

const update = async () => {
  if (!await formRef.value.validate(v => v)) return
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

const handleReset = () => {
  formRef.value.resetFields()
  refreshEditor.value = Date.now()
}

watch(() => [props.id, props.refresh], async () => {
  if (props.id) {
    rule.value = await asyncGetRule(props.id)
    rule.value.change_log = ''
    refreshEditor.value = Date.now()
  }
}, { immediate: true })
watchEffect(() => emits('change', rule.value))
</script>

<template>
  <el-form v-if="rule" ref="form" :model="rule" :rules="ruleFormRules"
           class="ml-2 mr-2" label-width="100px" label-position="left">
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
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="脚本类型" prop="script_type">
          <el-select v-model="rule.script_type">
            <el-option key="1" label="Groovy" :value="1" />
            <el-option key="2" label="JavaScript" :value="2" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="变更人" prop="modifier" label-position="right">
          <el-input v-model="rule.modifier" disabled />
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item prop="script" label-position="top">
      <text-editor title="代码编辑器" v-model="rule.script" :lang="langMap[rule.script_type]" :refresh="refreshEditor" />
    </el-form-item>
    <el-form-item label="变更原因" prop="change_log" label-position="top">
      <el-input type="textarea" v-model="rule.change_log" :rows="3" />
    </el-form-item>
    <el-form-item label-position="top">
      <el-button type="primary" @click="update" :disabled="!userStore.injected">
        <IconEdit size="20" class="mr-1" />
        <span>编辑</span>
      </el-button>
      <el-button type="info" @click="handleReset" :disabled="!userStore.injected">
        <IconRefresh size="20" class="mr-1" />
        <span>重置</span>
      </el-button>
      <el-button type="danger" @click="remove(rule.id)" :disabled="!userStore.injected">
        <IconTrash size="20" class="mr-1" />
        <span>删除</span>
      </el-button>
    </el-form-item>
  </el-form>
</template>

<style scoped>
</style>