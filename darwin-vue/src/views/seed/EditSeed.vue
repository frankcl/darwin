<script setup>
import { reactive, ref, useTemplateRef, watchEffect } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElCol, ElDialog, ElDivider,
  ElForm, ElFormItem, ElInput, ElInputNumber, ElOption, ElPageHeader,
  ElRadio, ElRadioGroup, ElRow, ElSelect, ElSpace, ElSwitch, ElText,
} from 'element-plus'
import { useUserStore } from '@/store'
import DynamicMap from '@/components/data/DynamicMap'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import { asyncGetSeed, asyncUpdateSeed } from '@/common/AsyncRequest'
import { seedFormRules, fillSeedMapField } from '@/views/seed/common'

const open = defineModel()
const emits = defineEmits(['close'])
const props = defineProps(['seedKey'])
const userStore = useUserStore()
const seedFormRef = useTemplateRef('seedForm')
const more = ref(true)
const seed = ref({})
const headers = reactive([])
const customOptions = reactive([])

const update = async formElement => {
  if (!await formElement.validate(v => v)) return
  fillSeedMapField(seed.value, 'headers', headers)
  fillSeedMapField(seed.value, 'user_defined_map', customOptions)
  if (!await asyncUpdateSeed(seed.value)) {
    showMessage('修改种子失败', ERROR)
    return
  }
  showMessage('修改种子成功', SUCCESS)
  open.value = false
}

const initOptionsWithMap = (options, map) => {
  options.splice(0, options.length)
  if (!map) return
  for (const key in map) options.push({ key: key, value: map[key] })
}

const resetSeedForm = async () => {
  if (props.seedKey) {
    seed.value = await asyncGetSeed(props.seedKey)
    initOptionsWithMap(headers, seed.value.headers)
    initOptionsWithMap(customOptions, seed.value.user_defined_map)
  }
}

watchEffect(() => resetSeedForm())
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" width="800" align-center show-close>
    <el-space direction="vertical" :size="20" :fill="true" class="w100">
      <el-page-header @click="open = false">
        <template #breadcrumb>
          <el-breadcrumb :separator-icon="ArrowRight">
            <el-breadcrumb-item :to="{ name: 'Home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ name: 'PlanList' }">抓取计划</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ name: 'PlanTabs', query: { id: props.planId } }">完善计划</el-breadcrumb-item>
          </el-breadcrumb>
        </template>
        <template #content>
          <span class="font-600 mr-3">修改种子</span>
        </template>
      </el-page-header>
      <el-form ref="seedForm" :model="seed" :rules="seedFormRules" label-width="80px" label-position="right">
        <el-form-item label="种子URL" prop="url">
          <el-input v-model.trim="seed.url" clearable />
        </el-form-item>
        <el-form-item label="类型" prop="category">
          <el-radio-group v-model="seed.category">
            <el-radio :value="1">内容页</el-radio>
            <el-radio :value="2">列表页</el-radio>
            <el-radio :value="3">媒体资源</el-radio>
            <el-radio :value="4">视频流</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="更多选项" prop="more">
          <el-switch v-model="more" />
        </el-form-item>
        <el-divider v-if="more" content-position="left">高级选项</el-divider>
        <el-row v-if="more" :gutter="10">
          <el-col :span="12">
            <el-form-item label="并发级别" prop="concurrent_level">
              <el-radio-group v-model="seed.concurrent_level">
                <el-radio :value="0">DOMAIN</el-radio>
                <el-radio :value="1">HOST</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="抓取超时" prop="timeout">
              <el-input-number v-model="seed.timeout" :min="0" :step="100" class="w180px" />
              <el-text class="ml-3" size="small">单位：毫秒</el-text>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-if="more" :gutter="10">
          <el-col :span="12">
            <el-form-item label="抓取方式" prop="fetch_method">
              <el-select v-model="seed.fetch_method" placeholder="请选择" class="w180px">
                <el-option key="1" label="本地IP" :value="0" />
                <el-option key="2" label="代理IP" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="seed.priority" placeholder="请选择" class="w180px">
                <el-option key="1" label="高优先级" :value="0" />
                <el-option key="2" label="中优先级" :value="1" />
                <el-option key="2" label="低优先级" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-if="more">
          <el-form-item label="抽链范围" prop="scope">
            <el-select v-model="seed.scope" clearable placeholder="请选择"
                       @clear="seed.scope = null" class="w180px">
              <el-option key="1" label="所有" :value="1" />
              <el-option key="2" label="DOMAIN" :value="2" />
              <el-option key="3" label="HOST" :value="3" />
            </el-select>
            <el-text class="ml-2" type="danger" size="small">注意：选择抽链范围后规则脚本失效</el-text>
          </el-form-item>
        </el-row>
        <el-row v-if="more">
          <dynamic-map v-model="headers" title="HTTP Header" label="header" />
        </el-row>
        <el-row v-if="more">
          <dynamic-map v-model="customOptions" title="自定义数据" label="数据项" />
        </el-row>
        <el-form-item>
          <el-button type="primary" @click="update(seedFormRef)" :disabled="!userStore.injected">修改</el-button>
          <el-button type="info" @click="resetSeedForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-space>
  </el-dialog>
</template>

<style scoped>
</style>