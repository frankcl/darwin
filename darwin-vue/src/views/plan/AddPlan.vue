<script setup>
import { IconHelp, IconPlus, IconRefresh } from '@tabler/icons-vue'
import { reactive, useTemplateRef } from 'vue'
import {
  ElButton, ElCol, ElDialog, ElForm, ElFormItem, ElInput,
  ElInputNumber, ElRadio, ElRadioGroup, ElRow, ElTooltip,
} from 'element-plus'
import { useUserStore } from '@/store'
import { planCategoryMap } from '@/common/Constants'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import { asyncAddPlan } from '@/common/AsyncRequest'
import { planFormRules } from '@/views/plan/common'
import DarwinCard from '@/components/data/Card'
import AppSearch from '@/components/app/AppSearch'

const open = defineModel()
const emits = defineEmits(['close'])
const userStore = useUserStore()
const formRef = useTemplateRef('form')
const plan = reactive({
  allow_dispatch_fail: false,
  max_depth: 3,
  category: 0
})

const add = async () => {
  if (!await formRef.value.validate(valid => valid)) return
  if (!await asyncAddPlan(plan)) {
    showMessage('新增计划失败', ERROR)
    return
  }
  showMessage('新增计划成功', SUCCESS)
  open.value = false
}
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" align-center show-close>
    <darwin-card title="新增计划">
      <el-form ref="form" :model="plan" :rules="planFormRules"
               label-width="80px" label-position="top">
        <el-form-item label="计划名称" prop="name">
          <el-input v-model.trim="plan.name" clearable />
        </el-form-item>
        <el-form-item label="所属应用" prop="app_id">
          <app-search v-model="plan.app_id" :permission-check="true" @change="app => plan.app_name = app.name" />
        </el-form-item>
        <el-form-item v-if="plan.category === 1" label="调度计划" prop="crontab_expression">
          <template #label>
            <span>调度计划</span>
            <el-tooltip effect="dark" placement="top"
                        content="crontab表达式，系统根据表达式计算下次调度时间进行调度">
              <IconHelp size="12" class="ml-2"/>
            </el-tooltip>
          </template>
          <el-input v-model="plan.crontab_expression" clearable placeholder="0 0/10 * * * ?   从0分开始每10分钟调度1次" />
        </el-form-item>
        <el-row>
          <el-col :span="8">
            <el-form-item prop="category">
              <template #label>
                <span>计划类型</span>
                <el-tooltip effect="dark" placement="top"
                            content="单次型：手动执行；周期型：支持手动执行和系统调度执行，周期型计划需配合调度计划使用">
                  <IconHelp size="12" class="ml-2"/>
                </el-tooltip>
              </template>
              <el-radio-group v-model="plan.category">
                <el-radio v-for="key in Object.keys(planCategoryMap)" :value="parseInt(key)" :key="key">
                  {{ planCategoryMap[key] }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item prop="allow_dispatch_fail">
              <template #label>
                <span>允许分发失败数据</span>
                <el-tooltip effect="dark" placement="top"
                            content="默认只分发抓取成功数据，允许则抓取失败数据也会分发">
                  <IconHelp size="12" class="ml-2"/>
                </el-tooltip>
              </template>
              <el-radio-group v-model="plan.allow_dispatch_fail">
                <el-radio :value="true">允许</el-radio>
                <el-radio :value="false">禁止</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item prop="max_depth">
              <template #label>
                <span class="d-flex align-items-center">
                  <span>最大抓取深度</span>
                  <el-tooltip effect="dark" placement="top" content="超过最大抓取深度的数据将被丢弃">
                    <IconHelp size="12" class="ml-2" />
                  </el-tooltip>
                </span>
              </template>
              <el-input-number :min="1" :max="10" v-model="plan.max_depth" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <el-button type="primary" @click="add" :disabled="!userStore.injected">
            <IconPlus size="20" class="mr-1" />
            <span>新增</span>
          </el-button>
          <el-button type="info" @click="formRef.resetFields()" :disabled="!userStore.injected">
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