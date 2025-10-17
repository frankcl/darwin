<script setup>
import { IconEdit, IconRefresh } from '@tabler/icons-vue'
import { ref, useTemplateRef, watchEffect } from 'vue'
import { ElButton, ElDialog, ElForm, ElFormItem, ElInput } from 'element-plus'
import { useUserStore } from '@/store'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import { asyncGetApp, asyncUpdateApp } from '@/common/AsyncRequest'
import DarwinCard from '@/components/data/Card'
import { appFormRules } from '@/views/app/common'

const props = defineProps(['id'])
const open = defineModel()
const emits = defineEmits(['close'])
const userStore = useUserStore()
const formRef = useTemplateRef('form')
const app = ref({})

const resetAppForm = async () => {
  if (props.id) {
    app.value = await asyncGetApp(props.id)
  }
}

const update = async () => {
  if (!await formRef.value.validate(valid => valid)) return
  if (!await asyncUpdateApp(app.value)) {
    showMessage('编辑应用失败', ERROR)
    return
  }
  showMessage('编辑应用成功', SUCCESS)
  open.value = false
}

watchEffect(async () => await resetAppForm())
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" align-center show-close>
    <darwin-card :title="`编辑应用：${app.name}`">
      <el-form ref="form" :model="app" :rules="appFormRules" label-width="80px" label-position="top">
        <el-form-item label="应用名" prop="name">
          <el-input v-model.trim="app.name" clearable />
        </el-form-item>
        <el-form-item label="应用说明" prop="comment">
          <el-input type="textarea" :rows="5" v-model="app.comment" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="update" :disabled="!userStore.injected">
            <IconEdit size="20" class="mr-1" />
            <span>编辑</span>
          </el-button>
          <el-button type="info" @click="resetAppForm" :disabled="!userStore.injected">
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