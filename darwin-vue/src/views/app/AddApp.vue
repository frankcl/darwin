<script setup>
import { IconPlus, IconRefresh } from '@tabler/icons-vue'
import { reactive, useTemplateRef } from 'vue'
import {
  ElButton, ElDialog, ElForm,
  ElFormItem, ElInput
} from 'element-plus'
import { useUserStore } from '@/store'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import { asyncAddApp } from '@/common/AsyncRequest'
import DarwinCard from '@/components/data/Card'
import { appFormRules } from '@/views/app/common'

const open = defineModel()
const emits = defineEmits(['close'])
const userStore = useUserStore()
const formRef = useTemplateRef('form')
const app = reactive({})

const add = async () => {
  if (!await formRef.value.validate(valid => valid)) return
  if (!await asyncAddApp(app)) {
    showMessage('新增应用失败', ERROR)
    return
  }
  showMessage('新增应用成功', SUCCESS)
  open.value = false
}
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" align-center show-close>
    <darwin-card title="新增应用">
      <el-form ref="form" :model="app" :rules="appFormRules" label-width="80px" label-position="top">
        <el-form-item label="应用名" prop="name">
          <el-input v-model.trim="app.name" clearable />
        </el-form-item>
        <el-form-item label="应用说明" prop="comment">
          <el-input type="textarea" :rows="5" v-model="app.comment" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="add" :disabled="!userStore.injected">
            <IconPlus size="20" class="mr-1" />
            <span>新增</span>
          </el-button>
          <el-button type="info" @click="formRef.resetFields()" :disabled="!userStore.injected">
            <IconRefresh size="20" class="mr-1" />
            <span>重置</span>
          </el-button>
        </el-form-item>
      </el-form>
    </darwin-card>
  </el-dialog>
</template>

<style scoped>
</style>