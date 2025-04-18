<script setup>
import { reactive, useTemplateRef, watchEffect } from 'vue'
import {
  ElButton, ElDialog, ElForm,
  ElFormItem, ElInput, ElRow, ElSpace
} from 'element-plus'
import { useUserStore } from '@/store'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import { asyncGetApp, asyncUpdateApp } from '@/common/AsyncRequest'
import { appFormRules } from '@/views/app/common'

const props = defineProps(['id'])
const open = defineModel()
const emits = defineEmits(['close'])
const userStore = useUserStore()
const appFormRef = useTemplateRef('appForm')
const app = reactive({})

const resetAppForm = async () => {
  if (props.id) {
    const tempApp = await asyncGetApp(props.id)
    app.id = props.id
    app.name = tempApp.name
    app.comment = tempApp.comment
  }
}

const update = async formElement => {
  if (!await formElement.validate(valid => valid)) return
  if (await asyncUpdateApp(app)) showMessage('编辑应用成功', SUCCESS)
  else showMessage('编辑应用失败', ERROR)
  open.value = false
}

watchEffect(async () => await resetAppForm())
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" width="680" align-center show-close>
    <el-space direction="vertical" :size="20" :fill="true" class="w100">
      <el-row align="middle">
        <span class="text-xl font-bold ml-2">编辑应用</span>
      </el-row>
      <el-form ref="appForm" :model="app" :rules="appFormRules" label-width="80px" label-position="right">
        <el-form-item label="应用名" prop="name">
          <el-input v-model.trim="app.name" clearable />
        </el-form-item>
        <el-form-item label="应用说明" prop="comment">
          <el-input type="textarea" :rows="5" v-model="app.comment" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="update(appFormRef)" :disabled="!userStore.injected">编辑</el-button>
          <el-button type="info" @click="resetAppForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-space>
  </el-dialog>
</template>

<style scoped>
</style>