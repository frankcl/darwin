<script setup>
import { computed, ref, useTemplateRef, watchEffect } from 'vue'
import {
  ElButton, ElCol, ElForm, ElFormItem,
  ElInput, ElRadio, ElRadioGroup, ElRow,
} from 'element-plus'
import { useUserStore } from '@/store'
import { formatDate } from '@/common/Time'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import { asyncGetPlan, asyncUpdatePlan } from '@/common/AsyncRequest'
import { planFormRules } from '@/views/plan/common'
import AppSearch from '@/components/app/AppSearch'

const props = defineProps(['id'])
const userStore = useUserStore()
const plan = ref({})
const planFormRef = useTemplateRef('planForm')
const nextTime = computed(() => {
  if (!plan.value || !plan.value['next_time']) return '暂无'
  return formatDate(plan.value['next_time'])
})

const update = async formElement => {
  if (!await formElement.validate(v => v)) return
  if (!await asyncUpdatePlan(plan.value)) {
    showMessage('更新计划失败', ERROR)
    return
  }
  showMessage('更新计划成功', SUCCESS)
}

const resetPlanForm = async () => {
  if (props.id) plan.value = await asyncGetPlan(props.id)
}

watchEffect( async () => await resetPlanForm())
</script>

<template>
  <el-form ref="planForm" :model="plan" :rules="planFormRules"
           label-width="100px" label-position="right" class="w100">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="计划名称" prop="name">
          <el-input v-model.trim="plan.name" clearable></el-input>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="所属应用" prop="app_id">
          <app-search v-model="plan.app_id" @change="app => plan.app_name = app.name"></app-search>
        </el-form-item>
      </el-col>
    </el-row>
    <el-row v-if="plan" :gutter="20">
      <el-col :span="12">
        <el-form-item label="创建人" prop="creator">
          <el-input v-model="plan.creator" readonly />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="创建时间" prop="create_time">
          <el-input :value="formatDate(plan['create_time'])" readonly />
        </el-form-item>
      </el-col>
    </el-row>
    <el-row v-if="plan" :gutter="20">
      <el-col :span="12">
        <el-form-item label="修改人" prop="modifier">
          <el-input v-model="plan.modifier" readonly />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="修改时间" prop="update_time">
          <el-input :value="formatDate(plan['update_time'])" readonly />
        </el-form-item>
      </el-col>
    </el-row>
    <el-row v-if="plan.category === 1" :gutter="20">
      <el-col :span="12">
        <el-form-item label="调度计划" prop="crontab_expression" required>
          <el-input v-model="plan.crontab_expression" clearable placeholder="10分钟调度1次：0 0/10 * * * ?" />
        </el-form-item>
      </el-col>
      <el-col v-if="plan" :span="12">
        <el-form-item label="下次调度时间" prop="next_time">
          <el-input :value="nextTime" readonly />
        </el-form-item>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="优先级" prop="priority" required>
          <el-radio-group v-model="plan.priority">
            <el-radio :value="0">高优先级</el-radio>
            <el-radio :value="1">中优先级</el-radio>
            <el-radio :value="2">低优先级</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="计划类型" prop="category" required>
          <el-radio-group v-model="plan.category">
            <el-radio :value="0">单次型</el-radio>
            <el-radio :value="1">周期型</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="抓取方式" prop="fetch_method" required>
          <el-radio-group v-model="plan.fetch_method">
            <el-radio :value="0">本地IP</el-radio>
            <el-radio :value="1">代理IP</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="重复抓取" prop="allow_repeat" required>
          <el-radio-group v-model="plan.allow_repeat">
            <el-radio :value="true">允许</el-radio>
            <el-radio :value="false">避免</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item>
      <el-button type="primary" @click="update(planFormRef)" :disabled="!userStore.injected">编辑</el-button>
      <el-button type="info" @click="resetPlanForm()">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<style scoped>
</style>