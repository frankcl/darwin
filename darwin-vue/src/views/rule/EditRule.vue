<script setup>
import { reactive, ref, useTemplateRef, watchEffect } from 'vue'
import {
  ElButton, ElCol, ElForm, ElFormItem, ElInput, ElNotification, ElOption, ElRow, ElSelect, ElSpace
} from 'element-plus'
import { executeAsyncRequest } from '@/common/assortment'
import { asyncGetRule, asyncUpdateRule } from '@/common/service'
import PlanRuleSelect from '@/components/rule/PlanRuleSelect'

const props = defineProps(['planId'])
const formRef = useTemplateRef('formRef')
const ruleId = ref()
const rule = ref({})
const refresh = ref(Date.now())
const debugURL = ref('')
const ruleForm = reactive({})
const formRules = { }

const submit = async formEl => {
  const successHandle = () => ElNotification.success('编辑规则成功')
  const failHandle = () => ElNotification.success('编辑规则失败')
  if (!await executeAsyncRequest(asyncUpdateRule, ruleForm, successHandle, failHandle, formEl)) return
  open.value = false
}

const clearSelectedRule = () => {
  ruleForm.id = null
  ruleForm.name = null
  ruleForm.regex = null
  ruleForm.script_type = null
  ruleForm.script = null
}

const debug = () => {
  if (!ruleForm.regex || !new RegExp(ruleForm.regex).test(debugURL.value)) {
    ElNotification.error('调试URL不匹配脚本规则')
  }
}

watchEffect(async () => {
  if (ruleId.value) {
    rule.value = await asyncGetRule(ruleId.value)
    ruleForm.id = rule.value.id
    ruleForm.name = rule.value.name
    ruleForm.regex = rule.value.regex
    ruleForm.script_type = rule.value.script_type
    ruleForm.script = rule.value.script
    ruleForm.creator = rule.value.creator
    ruleForm.modifier = rule.value.modifier
  }
})
</script>

<template>
  <el-space direction="vertical" :size="20" :fill="true" style="min-width: 100%">
    <el-row :gutter="20">
      <el-col :span="10">
        <el-form-item label="请选择规则">
          <plan-rule-select v-model="ruleId" :plan-id="props.planId" :force-refresh="refresh"
                            @clear="clearSelectedRule"></plan-rule-select>
        </el-form-item>
      </el-col>
      <el-col :span="14">
        <el-button>添加规则</el-button>
      </el-col>
    </el-row>
    <el-form v-if="ruleForm.id" ref="formRef" :model="ruleForm" :rules="formRules" label-position="top">
      <el-form-item label="规则名" prop="name" required>
        <el-input v-model.trim="ruleForm.name" clearable></el-input>
      </el-form-item>
      <el-form-item label="匹配规则" prop="regex" required>
        <el-input v-model.trim="ruleForm.regex" clearable></el-input>
      </el-form-item>
      <el-row>
        <el-col :span="8">
          <el-form-item label="脚本类型" prop="script_type" required>
            <el-select v-model="ruleForm.script_type" style="width: 250px">
              <el-option key="1" label="Groovy" :value="1"></el-option>
              <el-option key="2" label="JavaScript" :value="2"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="创建人" prop="creator">
            <el-input :value="rule.creator" style="width: 250px" readonly></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="修改人" prop="modifier">
            <el-input :value="rule.modifier" style="width: 250px" readonly></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="规则脚本" prop="script" required>
        <el-input type="textarea" :rows="20" v-model="ruleForm.script"></el-input>
      </el-form-item>
      <el-form-item label="调试URL" prop="debug_url">
        <el-input v-model="debugURL"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="submit(formRef)">编辑</el-button>
        <el-button>变更历史</el-button>
        <el-button @click="debug">脚本调试</el-button>
        <el-button @click="formRef.resetFields()">重置</el-button>
      </el-form-item>
    </el-form>
  </el-space>
</template>

<style scoped>
</style>