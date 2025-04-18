<script setup>
import { ref, useTemplateRef, watchEffect } from 'vue'
import {
  ElButton, ElCol, ElDialog, ElForm, ElFormItem,
  ElInput, ElInputNumber, ElOption, ElRow, ElSelect, ElSpace
} from 'element-plus'
import { useUserStore } from '@/store'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import { asyncGetProxy, asyncUpdateProxy } from '@/common/AsyncRequest'
import { proxyFormRules } from '@/views/proxy/common'

const props = defineProps(['id'])
const open = defineModel()
const emits = defineEmits(['close'])
const userStore = useUserStore()
const proxyFormRef = useTemplateRef('proxyForm')
const proxy = ref({})

const resetProxyForm = async () => {
  if (props.id) proxy.value = await asyncGetProxy(props.id)
}

const update = async formElement => {
  if (!await formElement.validate(valid => valid)) return
  if (await asyncUpdateProxy(proxy.value)) showMessage('编辑代理成功', SUCCESS)
  else showMessage('编辑代理失败', ERROR)
  open.value = false
}

watchEffect(async () => await resetProxyForm())
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" width="680" align-center show-close>
    <el-space direction="vertical" :size="20" :fill="true" class="w100">
      <el-row align="middle">
        <span class="text-xl font-bold ml-2">编辑代理</span>
      </el-row>
      <el-form ref="proxyForm" :model="proxy" :rules="proxyFormRules" label-width="80px" label-position="right">
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
        <el-form-item>
          <el-button type="primary" @click="update(proxyFormRef)" :disabled="!userStore.superAdmin">编辑</el-button>
          <el-button type="info" @click="resetProxyForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-space>
  </el-dialog>
</template>

<style scoped>
</style>