<script setup>
import { reactive, useTemplateRef } from 'vue'
import {
  ElButton, ElDialog, ElForm, ElFormItem,
  ElInput, ElRadio, ElRadioGroup, ElRow, ElSpace,
} from 'element-plus'
import { useUserStore } from '@/store'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import { asyncAddPlan } from '@/common/AsyncRequest'
import { planFormRules } from '@/views/plan/common'
import AppSearch from '@/components/app/AppSearch'

const open = defineModel()
const emits = defineEmits(['close'])
const userStore = useUserStore()
const planFormRef = useTemplateRef('planForm')
const plan = reactive({
  priority: 1,
  allow_repeat: 'false',
  fetch_method: 0,
  category: 0
})

const add = async formElement => {
  if (!await formElement.validate(valid => valid)) return
  if (!await asyncAddPlan(plan)) {
    showMessage('新增计划失败', ERROR)
    return
  }
  showMessage('新增计划成功', SUCCESS)
  open.value = false
}
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" width="800" align-center show-close>
    <el-space direction="vertical" :size="20" :fill="true" class="w100">
      <el-row align="middle">
        <span class="text-xl font-bold ml-2">新增计划</span>
      </el-row>
      <el-form ref="planForm" :model="plan" :rules="planFormRules"
               label-width="80px" label-position="right" class="w100">
        <el-form-item label="计划名称" prop="name">
          <el-input v-model.trim="plan.name" clearable />
        </el-form-item>
        <el-form-item label="所属应用" prop="app_id">
          <app-search v-model="plan.app_id" @change="app => plan.app_name = app.name" />
        </el-form-item>
        <el-form-item v-if="plan.category === 1" label="调度时间" prop="crontab_expression" required>
          <el-input v-model="plan.crontab_expression" clearable placeholder="10分钟调度1次：0 0/10 * * * ?" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority" required>
          <el-radio-group v-model="plan.priority">
            <el-radio :value="0">高优先级</el-radio>
            <el-radio :value="1">中优先级</el-radio>
            <el-radio :value="2">低优先级</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="计划类型" prop="category" required>
          <el-radio-group v-model="plan.category">
            <el-radio :value="0">单次型</el-radio>
            <el-radio :value="1">周期型</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="抓取方式" prop="fetch_method" required>
          <el-radio-group v-model="plan.fetch_method">
            <el-radio :value="0">本地IP</el-radio>
            <el-radio :value="1">代理IP</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="重复抓取" prop="allow_repeat" required>
          <el-radio-group v-model="plan.allow_repeat">
            <el-radio value="true">允许</el-radio>
            <el-radio value="false">禁止</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="add(planFormRef)" :disabled="!userStore.injected">新增</el-button>
          <el-button type="info" @click="planFormRef.resetFields()">重置</el-button>
        </el-form-item>
      </el-form>
    </el-space>
  </el-dialog>
</template>

<style scoped>
</style>