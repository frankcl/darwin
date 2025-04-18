<script setup>
import { reactive, useTemplateRef } from 'vue'
import {
  ElButton, ElDialog, ElForm,
  ElFormItem, ElInput, ElRow, ElSpace
} from 'element-plus'
import { useUserStore } from '@/store'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import { asyncAddApp } from '@/common/AsyncRequest'
import { appFormRules } from '@/views/app/common'

const open = defineModel()
const emits = defineEmits(['close'])
const userStore = useUserStore()
const appFormRef = useTemplateRef('appForm')
const app = reactive({})

const add = async formElement => {
  if (!await formElement.validate(valid => valid)) return
  if (await asyncAddApp(app)) showMessage('新增应用成功', SUCCESS)
  else showMessage('新增应用失败', ERROR)
  open.value = false
}
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" width="680" align-center show-close>
    <el-space direction="vertical" :size="20" :fill="true" class="w100">
      <el-row align="middle">
        <span class="text-xl font-bold ml-2">新增应用</span>
      </el-row>
      <el-form ref="appForm" :model="app" :rules="appFormRules" label-width="80px" label-position="right">
        <el-form-item label="应用名" prop="name">
          <el-input v-model.trim="app.name" clearable />
        </el-form-item>
        <el-form-item label="应用说明" prop="comment">
          <el-input type="textarea" :rows="5" v-model="app.comment" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="add(appFormRef)" :disabled="!userStore.injected">新增</el-button>
          <el-button type="info" @click="appFormRef.resetFields()">重置</el-button>
        </el-form-item>
      </el-form>
    </el-space>
  </el-dialog>
</template>

<style scoped>
</style>