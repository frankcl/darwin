<script setup>
import { reactive, useTemplateRef } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElDialog, ElForm,
  ElFormItem, ElInput, ElNotification, ElPageHeader, ElSpace
} from 'element-plus'
import { asyncAddApp } from '@/common/service'
import { executeAsyncRequest } from '@/common/assortment'
import { appFormRules } from '@/views/app/common'

const open = defineModel()
const emits = defineEmits(['close'])
const formRef = useTemplateRef('formRef')
const appForm = reactive({
  name: null,
  comment: null
})
const formRules = { ... appFormRules }

const submit = async formEl => {
  const successHandle = () => ElNotification.success('新增应用成功')
  const failHandle = () => ElNotification.success('新增应用失败')
  if (!await executeAsyncRequest(asyncAddApp, appForm,
    successHandle, failHandle, undefined, formEl)) return
  open.value = false
}
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" width="680" align-center show-close>
    <el-space direction="vertical" :size="20" :fill="true" class="w100">
      <el-page-header @back="open = false">
        <template #breadcrumb>
          <el-breadcrumb :separator-icon="ArrowRight">
            <el-breadcrumb-item :to="{ name: 'Home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ name: 'AppList' }">爬虫应用</el-breadcrumb-item>
          </el-breadcrumb>
        </template>
        <template #content>
          <span class="text-large font-600">新增应用</span>
        </template>
      </el-page-header>
      <el-form ref="formRef" :model="appForm" :rules="formRules" label-width="80px" label-position="right">
        <el-form-item label="应用名" prop="name">
          <el-input v-model.trim="appForm.name" clearable></el-input>
        </el-form-item>
        <el-form-item label="应用说明" prop="comment">
          <el-input type="textarea" :rows="5" v-model="appForm.comment"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button @click="submit(formRef)">新增</el-button>
          <el-button @click="formRef.resetFields()">重置</el-button>
        </el-form-item>
      </el-form>
    </el-space>
  </el-dialog>
</template>

<style scoped>
</style>