<script setup>
import { IconPlus, IconRefresh } from '@tabler/icons-vue'
import { reactive, useTemplateRef } from 'vue'
import {
  ElButton, ElCol, ElDialog, ElForm, ElFormItem,
  ElInput, ElInputNumber, ElOption, ElRow, ElSelect
} from 'element-plus'
import { useUserStore } from '@/store'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import { asyncAddProxy } from '@/common/AsyncRequest'
import DarwinCard from '@/components/data/Card'
import { proxyFormRules } from '@/views/proxy/common'

const open = defineModel()
const emits = defineEmits(['close'])
const userStore = useUserStore()
const formRef = useTemplateRef('form')
const proxy = reactive({ category: 1, port: 888 })

const add = async () => {
  if (!await formRef.value.validate(valid => valid)) return
  if (await asyncAddProxy(proxy)) showMessage('新增代理成功', SUCCESS)
  else showMessage('新增代理失败', ERROR)
  open.value = false
}
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" align-center show-close>
    <darwin-card title="新增代理">
      <el-form ref="form" :model="proxy" :rules="proxyFormRules" label-width="80px" label-position="left">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="代理IP" prop="address">
              <el-input v-model.trim="proxy.address" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="代理端口" prop="port">
              <el-input-number v-model="proxy.port" :min="1" :max="65535" class="w150px" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="proxy.username" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="代理类型" prop="category" required>
              <el-select v-model="proxy.category" placeholder="请选择" class="w150px">
                <el-option key="1" label="长效代理" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input v-model="proxy.password" type="password" clearable show-password />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label-position="top">
          <el-button type="primary" @click="add" :disabled="!userStore.superAdmin">
            <IconPlus size="20" class="mr-1" />
            <span>新增</span>
          </el-button>
          <el-button type="info" @click="formRef.resetFields()" :disabled="!userStore.superAdmin">
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