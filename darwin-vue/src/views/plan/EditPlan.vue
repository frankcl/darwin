<script setup>
import { IconEdit, IconHelp, IconRefresh } from '@tabler/icons-vue'
import { computed, ref, useTemplateRef, watchEffect } from 'vue'
import { useRouter } from 'vue-router'
import {
  ElButton, ElCol, ElForm, ElFormItem, ElInput,
  ElInputNumber, ElRadio, ElRadioGroup, ElRow, ElTooltip,
} from 'element-plus'
import { useUserStore } from '@/store'
import { formatDate } from '@/common/Time'
import { planCategoryMap } from '@/common/Constants'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import { asyncGetPlan, asyncUpdatePlan } from '@/common/AsyncRequest'
import { planFormRules } from '@/views/plan/common'
import AppSearch from '@/components/app/AppSearch'

const props = defineProps(['id'])
const router = useRouter()
const userStore = useUserStore()
const plan = ref({})
const formRef = useTemplateRef('form')
const nextTime = computed(() => {
  if (!plan.value || !plan.value['next_time']) return '暂无'
  return formatDate(plan.value['next_time'])
})

const update = async () => {
  if (!await formRef.value.validate(v => v)) return
  if (!await asyncUpdatePlan(plan.value)) {
    showMessage('更新计划失败', ERROR)
    return
  }
  showMessage('更新计划成功', SUCCESS)
  await router.push({ path: '/plan/search'})
}

const resetPlanForm = async () => {
  if (props.id) plan.value = await asyncGetPlan(props.id)
}

watchEffect( async () => await resetPlanForm())
</script>

<template>
  <el-form ref="form" :model="plan" :rules="planFormRules" class="mt-4" label-width="100px" label-position="top">
    <el-form-item label="计划名称" prop="name">
      <el-input v-model.trim="plan.name" clearable></el-input>
    </el-form-item>
    <el-form-item label="所属应用" prop="app_id">
      <app-search v-model="plan.app_id" @change="app => plan.app_name = app.name"></app-search>
    </el-form-item>
    <el-row v-if="plan.category === 1" :gutter="20">
      <el-col :span="12">
        <el-form-item label="调度计划" prop="crontab_expression" required>
          <template #label>
            <span>调度计划</span>
            <el-tooltip effect="dark" placement="top"
                        content="crontab表达式，系统根据表达式计算下次调度时间进行调度">
              <IconHelp size="12" class="ml-2"/>
            </el-tooltip>
          </template>
          <el-input v-model="plan.crontab_expression" clearable placeholder="0 0/10 * * * ?   从0分开始每10分钟调度1次" />
        </el-form-item>
      </el-col>
      <el-col v-if="plan" :span="12">
        <el-form-item label="下次调度时间" prop="next_time">
          <el-input :value="nextTime" disabled />
        </el-form-item>
      </el-col>
    </el-row>
    <el-row v-if="plan" :gutter="20">
      <el-col :span="12">
        <el-form-item label="创建人" prop="creator">
          <el-input v-model="plan.creator" disabled />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="创建时间" prop="create_time">
          <el-input :value="formatDate(plan['create_time'])" disabled />
        </el-form-item>
      </el-col>
    </el-row>
    <el-row v-if="plan" :gutter="20">
      <el-col :span="12">
        <el-form-item label="变更人" prop="modifier">
          <el-input v-model="plan.modifier" disabled />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="变更时间" prop="update_time">
          <el-input :value="formatDate(plan['update_time'])" disabled />
        </el-form-item>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-form-item prop="category" required>
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
          <el-input-number :min="1" :max="6" v-model="plan.max_depth" clearable />
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item>
      <el-button type="primary" @click="update" :disabled="!userStore.injected">
        <IconEdit size="20" class="mr-1" />
        <span>编辑</span>
      </el-button>
      <el-button type="info" @click="resetPlanForm" :disabled="!userStore.injected">
        <IconRefresh size="20" class="mr-1" />
        <span>重置</span>
      </el-button>
    </el-form-item>
  </el-form>
</template>

<style scoped>
</style>