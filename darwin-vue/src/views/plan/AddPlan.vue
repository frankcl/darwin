<script setup>
import { reactive, useTemplateRef } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import {
  ElBreadcrumb,
  ElBreadcrumbItem,
  ElButton,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElNotification, ElPageHeader,
  ElRadio,
  ElRadioGroup,
  ElRow,
  ElSpace,
} from 'element-plus'
import { asyncAddPlan } from '@/common/service'
import { executeAsyncRequest } from '@/common/assortment'
import { planFormRules } from '@/views/plan/common'
import AppSearch from '@/components/app/AppSearch'

const open = defineModel()
const emits = defineEmits(['close'])
const formRef = useTemplateRef('formRef')
const planForm = reactive({
  name: null,
  priority: 1,
  crontab_expression: null,
  allow_repeat: 'false',
  fetch_method: 0,
  category: 0,
  app_id: null,
  app_name: null
})
const formRules = { ... planFormRules }

const submit = async formEl => {
  const successHandle = () => ElNotification.success('新增计划成功')
  const failHandle = () => ElNotification.error('新增计划失败')
  if (!await executeAsyncRequest(asyncAddPlan, planForm, successHandle, failHandle, undefined, formEl)) return
  open.value = false
}
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" width="600" align-center show-close>
    <el-space direction="vertical" :size="20" :fill="true" style="min-width: 100%">
      <el-page-header @click="open = false">
        <template #breadcrumb>
          <el-breadcrumb :separator-icon="ArrowRight">
            <el-breadcrumb-item :to="{ name: 'Home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ name: 'PlanList' }">抓取计划</el-breadcrumb-item>
          </el-breadcrumb>
        </template>
        <template #content>
          <span class="text-large font-600 mr-3">新增计划</span>
        </template>
      </el-page-header>
      <el-row>
        <el-form ref="formRef" :model="planForm" :rules="formRules"
                 label-width="auto" label-position="right" style="min-width: 100%">
          <el-form-item label="计划名称" prop="name">
            <el-input v-model.trim="planForm.name" clearable></el-input>
          </el-form-item>
          <el-form-item label="所属应用" prop="app_id">
            <app-search v-model="planForm.app_id" @change="app => planForm.app_name = app.name"></app-search>
          </el-form-item>
          <el-form-item v-if="planForm.category === 1" label="调度时间" prop="crontab_expression" required>
            <el-input v-model="planForm.crontab_expression" clearable placeholder="10分钟调度1次：0 0/10 * * * ?"></el-input>
          </el-form-item>
          <el-form-item label="优先级" prop="priority" required>
            <el-radio-group v-model="planForm.priority">
              <el-radio :value="0">高优先级</el-radio>
              <el-radio :value="1">中优先级</el-radio>
              <el-radio :value="2">低优先级</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="计划类型" prop="category" required>
            <el-radio-group v-model="planForm.category">
              <el-radio :value="0">单次型</el-radio>
              <el-radio :value="1">周期型</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="抓取方式" prop="fetch_method" required>
            <el-radio-group v-model="planForm.fetch_method">
              <el-radio :value="0">本地IP</el-radio>
              <el-radio :value="1">代理IP</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="重复抓取" prop="allow_repeat" required>
            <el-radio-group v-model="planForm.allow_repeat">
              <el-radio value="true">允许</el-radio>
              <el-radio value="false">避免</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item>
            <el-button @click="submit(formRef)">新增</el-button>
            <el-button @click="formRef.resetFields()">重置</el-button>
          </el-form-item>
        </el-form>
      </el-row>
    </el-space>
  </el-dialog>
</template>

<style scoped>
</style>