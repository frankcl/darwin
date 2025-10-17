<script setup>
import { IconEdit, IconRefresh } from '@tabler/icons-vue'
import { ref, useTemplateRef, watchEffect } from 'vue'
import { ElButton, ElDialog, ElForm, ElFormItem, ElInput } from 'element-plus'
import { useUserStore } from '@/store'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import {
  asyncGetAppSecret,
  asyncRandomAccessKey,
  asyncRandomSecretKey,
  asyncUpdateAppSecret
} from '@/common/AsyncRequest'
import DarwinCard from '@/components/data/Card'
import { appSecretFormRules } from '@/views/app_secret/common'
import AppSearch from '@/components/app/AppSearch'

const props = defineProps(['id'])
const open = defineModel()
const emits = defineEmits(['close'])
const userStore = useUserStore()
const formRef = useTemplateRef('form')
const appSecret = ref({})

const refreshAccessKey = async () => appSecret.value.access_key = await asyncRandomAccessKey()
const refreshSecretKey = async () => appSecret.value.secret_key = await asyncRandomSecretKey()

const resetAppSecretForm = async () => {
  if (props.id) {
    appSecret.value = await asyncGetAppSecret(props.id)
  }
}

const update = async () => {
  if (!await formRef.value.validate(valid => valid)) return
  if (!await asyncUpdateAppSecret(appSecret.value)) {
    showMessage('编辑应用秘钥失败', ERROR)
    return
  }
  showMessage('编辑应用秘钥成功', SUCCESS)
  open.value = false
}

watchEffect(async () => await resetAppSecretForm())
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" align-center show-close>
    <darwin-card :title="`编辑应用秘钥`">
      <el-form ref="form" :model="appSecret" :rules="appSecretFormRules" label-width="80px" label-position="top">
        <el-form-item label="秘钥名称" prop="name">
          <el-input v-model.trim="appSecret.name" clearable />
        </el-form-item>
        <el-form-item label="所属应用" prop="app_id">
          <app-search v-model="appSecret.app_id" placeholder="根据应用名搜索" />
        </el-form-item>
        <el-form-item label="AccessKey" prop="access_key">
          <div class="d-flex flex-grow-1">
            <el-input class="mr-4" v-model="appSecret.access_key" clearable />
            <el-button type="primary" plain @click="refreshAccessKey">
              <IconRefresh size="20" class="mr-2" />
              <span>刷新</span>
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="SecretKey" prop="secret_key">
          <div class="d-flex flex-grow-1">
            <el-input class="mr-4" v-model="appSecret.secret_key" clearable />
            <el-button type="primary" plain @click="refreshSecretKey">
              <IconRefresh size="20" class="mr-2" />
              <span>刷新</span>
            </el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="update" :disabled="!userStore.superAdmin">
            <IconEdit size="20" class="mr-1" />
            <span>编辑</span>
          </el-button>
          <el-button type="info" @click="resetAppSecretForm" :disabled="!userStore.superAdmin">
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