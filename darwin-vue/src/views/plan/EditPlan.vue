<script setup>
import { format } from 'date-fns'
import { reactive, ref, useTemplateRef, watchEffect } from 'vue'
import {
  ElButton, ElCol,
  ElForm,
  ElFormItem,
  ElInput,
  ElNotification,
  ElRadio,
  ElRadioGroup,
  ElRow,
} from 'element-plus'
import { asyncGetPlan, asyncUpdatePlan } from '@/common/service'
import { executeAsyncRequest } from '@/common/assortment'
import { planFormRules } from '@/views/plan/common'
import AppSearch from '@/components/app/AppSearch'

const props = defineProps(['id'])
const formRef = useTemplateRef('formRef')
const plan = ref()
const planForm = reactive({})
const formRules = { ... planFormRules }

const submit = async formEl => {
  const successHandle = () => ElNotification.success('更新计划成功')
  const failHandle = () => ElNotification.error('更新计划失败')
  if (await executeAsyncRequest(asyncUpdatePlan, planForm,
    successHandle, failHandle, undefined, formEl)) {
    await retrieveFill(props.id)
  }
}

const retrieveFill = async id => {
  plan.value = await asyncGetPlan(id)
  planForm.plan_id = id
  planForm.name = plan.value.name
  planForm.allow_repeat = plan.value.allow_repeat
  planForm.status = plan.value.status
  planForm.crontab_expression = plan.value.crontab_expression
  planForm.priority = plan.value.priority
  planForm.fetch_method = plan.value.fetch_method
  planForm.category = plan.value.category
  planForm.app_id = plan.value.app_id
  planForm.app_name = plan.value.app_name
}

watchEffect( async () => {
  if (props.id) await retrieveFill(props.id)
})
</script>

<template>
  <el-form ref="formRef" :model="planForm" :rules="formRules" label-width="100px" label-position="right" class="w100">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="计划名称" prop="name">
          <el-input v-model.trim="planForm.name" clearable></el-input>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="所属应用" prop="app_id">
          <app-search v-model="planForm.app_id" @change="app => planForm.app_name = app.name"></app-search>
        </el-form-item>
      </el-col>
    </el-row>
    <el-row v-if="plan" :gutter="20">
      <el-col :span="12">
        <el-form-item label="创建人" prop="creator">
          <el-input v-model="plan.creator" readonly></el-input>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="创建时间" prop="create_time">
          <el-input :value="format(new Date(plan['create_time']), 'yyyy-MM-dd HH:mm:ss')" readonly></el-input>
        </el-form-item>
      </el-col>
    </el-row>
    <el-row v-if="plan" :gutter="20">
      <el-col :span="12">
        <el-form-item label="修改人" prop="modifier">
          <el-input v-model="plan.modifier" readonly></el-input>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="修改时间" prop="update_time">
          <el-input :value="format(new Date(plan['update_time']), 'yyyy-MM-dd HH:mm:ss')" readonly></el-input>
        </el-form-item>
      </el-col>
    </el-row>
    <el-row v-if="planForm.category === 1" :gutter="20">
      <el-col :span="12">
        <el-form-item label="调度计划" prop="crontab_expression" required>
          <el-input v-model="planForm.crontab_expression" clearable placeholder="10分钟调度1次：0 0/10 * * * ?"></el-input>
        </el-form-item>
      </el-col>
      <el-col v-if="plan" :span="12">
        <el-form-item label="下次调度时间" prop="next_time">
          <el-input :value="plan['next_time'] ? format(new Date(plan['next_time']), 'yyyy-MM-dd HH:mm:ss') : ''" readonly></el-input>
        </el-form-item>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="优先级" prop="priority" required>
          <el-radio-group v-model="planForm.priority">
            <el-radio :value="0">高优先级</el-radio>
            <el-radio :value="1">中优先级</el-radio>
            <el-radio :value="2">低优先级</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="计划类型" prop="category" required>
          <el-radio-group v-model="planForm.category">
            <el-radio :value="0">单次型</el-radio>
            <el-radio :value="1">周期型</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="抓取方式" prop="fetch_method" required>
          <el-radio-group v-model="planForm.fetch_method">
            <el-radio :value="0">本地IP</el-radio>
            <el-radio :value="1">代理IP</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="重复抓取" prop="allow_repeat" required>
          <el-radio-group v-model="planForm.allow_repeat">
            <el-radio :value="true">允许</el-radio>
            <el-radio :value="false">避免</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-col>
    </el-row>
    <el-button @click="submit(formRef)">编辑</el-button>
    <el-button @click="retrieveFill(props.id)">重置</el-button>
  </el-form>
</template>

<style scoped>
</style>