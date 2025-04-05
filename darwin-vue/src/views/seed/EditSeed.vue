<script setup>
import { reactive, ref, useTemplateRef, watchEffect } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import {
  ElBreadcrumb,
  ElBreadcrumbItem,
  ElButton, ElCol,
  ElDialog, ElDivider,
  ElForm,
  ElFormItem,
  ElInput, ElInputNumber,
  ElNotification, ElOption, ElPageHeader,
  ElRadio,
  ElRadioGroup,
  ElRow, ElSelect,
  ElSpace, ElSwitch, ElText,
} from 'element-plus'
import { asyncGetSeed, asyncUpdateSeed } from '@/common/service'
import { executeAsyncRequest } from '@/common/assortment'
import { seedFormRules, transferFieldArray } from '@/views/seed/common'
import DynamicMap from '@/components/data/DynamicMap'

const open = defineModel()
const emits = defineEmits(['close'])
const props = defineProps(['seedKey'])
const formRef = useTemplateRef('formRef')
const more = ref(true)
const seed = ref({})
const seedForm = reactive({})
const headerOptions = reactive([])
const userOptions = reactive([])
const formRules = { ... seedFormRules }

const submit = async formEl => {
  const successHandle = () => ElNotification.success('修改种子成功')
  const failHandle = () => ElNotification.error('修改种子失败')
  transferFieldArray(headerOptions, seedForm, 'headers')
  transferFieldArray(userOptions, seedForm, 'user_defined_map')
  if (!await executeAsyncRequest(asyncUpdateSeed, seedForm,
    successHandle, failHandle, undefined, formEl)) return
  open.value = false
}

const transferFieldMapToArray = (fieldMap, fieldArray) => {
  fieldArray.splice(0, fieldArray.length)
  if (fieldMap) {
    for (const key in fieldMap) {
      fieldArray.push({ key: key, value: fieldMap[key] })
    }
  }
}

const retrieve = async key => {
  seed.value = await asyncGetSeed(key)
  seedForm.key = seed.value.key
  seedForm.url = seed.value.url
  seedForm.concurrent_level = seed.value.concurrent_level
  seedForm.priority = seed.value.priority
  seedForm.fetch_method = seed.value.fetch_method
  seedForm.category = seed.value.category
  seedForm.timeout = seed.value.timeout
  seedForm.scope = seed.value.scope
  transferFieldMapToArray(seed.value.headers, headerOptions)
  transferFieldMapToArray(seed.value.user_defined_map, userOptions)
}

watchEffect(() => {
  if (props.seedKey) retrieve(props.seedKey)
})
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" width="800" align-center show-close>
    <el-space direction="vertical" :size="20" :fill="true" style="min-width: 100%">
      <el-page-header @click="open = false">
        <template #breadcrumb>
          <el-breadcrumb :separator-icon="ArrowRight">
            <el-breadcrumb-item :to="{ name: 'Home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ name: 'PlanList' }">抓取计划</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ name: 'PlanPanel', query: { id: props.planId } }">完善计划</el-breadcrumb-item>
          </el-breadcrumb>
        </template>
        <template #content>
          <span class="text-large font-600 mr-3">修改种子</span>
        </template>
      </el-page-header>
      <el-row>
        <el-form ref="formRef" :model="seedForm" :rules="formRules"
                 label-width="auto" label-position="right" style="min-width: 100%">
          <el-form-item label="URL" prop="url">
            <el-input v-model.trim="seedForm.url" clearable></el-input>
          </el-form-item>
          <el-form-item label="类型" prop="category" required>
            <el-radio-group v-model="seedForm.category">
              <el-radio :value="1">内容页</el-radio>
              <el-radio :value="2">列表页</el-radio>
              <el-radio :value="3">媒体资源</el-radio>
              <el-radio :value="4">视频流</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="更多选项" prop="more">
            <el-switch v-model="more"></el-switch>
          </el-form-item>
          <el-divider v-if="more" content-position="left">高级选项</el-divider>
          <el-row v-if="more" :gutter="10">
            <el-col :span="12">
              <el-form-item label="并发级别" prop="concurrent_level">
                <el-radio-group v-model="seedForm.concurrent_level">
                  <el-radio :value="0">DOMAIN</el-radio>
                  <el-radio :value="1">HOST</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="抓取超时" prop="timeout">
                <el-input-number v-model="seedForm.timeout" :min="0" :step="100" style="width: 180px" />
                &nbsp;&nbsp;&nbsp;&nbsp;
                <el-text size="small">单位：毫秒</el-text>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row v-if="more" :gutter="10">
            <el-col :span="12">
              <el-form-item label="抓取方式" prop="fetch_method">
                <el-select v-model="seedForm.fetch_method" clearable placeholder="请选择"
                           @clear="seedForm.fetch_method = null" style="width: 180px">
                  <el-option key="1" label="本地IP" :value="0"></el-option>
                  <el-option key="2" label="代理IP" :value="1"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="优先级" prop="priority">
                <el-select v-model="seedForm.priority" clearable placeholder="请选择"
                           @clear="seedForm.priority = null" style="width: 180px">
                  <el-option key="1" label="高优先级" :value="0"></el-option>
                  <el-option key="2" label="中优先级" :value="1"></el-option>
                  <el-option key="2" label="低优先级" :value="2"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row v-if="more">
            <el-form-item label="抽链范围" prop="scope">
              <el-select v-model="seedForm.scope" clearable placeholder="请选择"
                         @clear="seedForm.scope = null" style="width: 180px">
                <el-option key="1" label="所有" :value="1"></el-option>
                <el-option key="2" label="DOMAIN" :value="2"></el-option>
                <el-option key="3" label="HOST" :value="3"></el-option>
              </el-select>
              &nbsp;&nbsp;&nbsp;&nbsp;
              <el-text type="danger" size="small">注意：选择抽链范围后规则脚本失效</el-text>
            </el-form-item>
          </el-row>
          <el-row v-if="more">
            <dynamic-map v-model="headerOptions" title="HTTP Header" option-name="header"></dynamic-map>
          </el-row>
          <el-row v-if="more">
            <dynamic-map v-model="userOptions" title="自定义数据" option-name="option"></dynamic-map>
          </el-row>
          <el-form-item>
            <el-button @click="submit(formRef)">修改</el-button>
            <el-button @click="retrieve(props.key)">重置</el-button>
          </el-form-item>
        </el-form>
      </el-row>
    </el-space>
  </el-dialog>
</template>

<style scoped>
</style>