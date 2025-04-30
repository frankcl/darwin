<script setup>
import { reactive, ref, useTemplateRef, watchEffect } from 'vue'
import {
  ElButton, ElCol, ElDialog, ElDivider, ElForm, ElFormItem,
  ElInput, ElInputNumber, ElOption, ElRadio, ElRadioGroup,
  ElRow, ElSelect, ElSpace, ElSwitch, ElText,
} from 'element-plus'
import { useUserStore } from '@/store'
import MutableTable from '@/components/data/MutableTable'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import { asyncGetSeed, asyncUpdateSeed } from '@/common/AsyncRequest'
import { seedFormRules, fillMap } from '@/views/seed/common'

const open = defineModel()
const emits = defineEmits(['close'])
const props = defineProps(['seedKey'])
const userStore = useUserStore()
const seedFormRef = useTemplateRef('seedForm')
const more = ref(true)
const seed = ref({})
const headers = reactive([])
const customOptions = reactive([])
const headerColumns = [{ name: '请求头名' }, { name: '请求头值' }]
const customOptionColumns = [{ name: '字段名' }, { name: '字段值' }]

const update = async formElement => {
  if (!await formElement.validate(v => v)) return
  fillMap(seed.value, 'headers', headers)
  fillMap(seed.value, 'custom_map', customOptions)
  if (!await asyncUpdateSeed(seed.value)) {
    showMessage('修改种子失败', ERROR)
    return
  }
  showMessage('修改种子成功', SUCCESS)
  emits('close')
  open.value = false
}

const initOptionsWithMap = (options, map) => {
  options.splice(0, options.length)
  if (!map) return
  for (const key in map) options.push([key, map[key]])
}

const resetSeedForm = async () => {
  if (props.seedKey) {
    seed.value = await asyncGetSeed(props.seedKey)
    initOptionsWithMap(headers, seed.value.headers)
    initOptionsWithMap(customOptions, seed.value.custom_map)
  }
}

watchEffect(() => resetSeedForm())
</script>

<template>
  <el-dialog v-model="open" width="850" align-center show-close>
    <el-space direction="vertical" :size="20" :fill="true" class="w100">
      <el-row align="middle">
        <span class="text-xl font-bold ml-2">编辑种子</span>
      </el-row>
      <el-form ref="seedForm" :model="seed" :rules="seedFormRules" label-width="100px" label-position="right">
        <el-form-item label="种子URL" prop="url">
          <el-input v-model.trim="seed.url" clearable />
        </el-form-item>
        <el-form-item label="更多选项" prop="more">
          <el-switch v-model="more" />
        </el-form-item>
        <el-divider v-if="more" content-position="left">高级选项</el-divider>
        <el-row v-if="more" :gutter="10">
          <el-col :span="12">
            <el-form-item label="数据分发" prop="allow_dispatch">
              <el-radio-group v-model="seed.allow_dispatch">
                <el-radio :value="true">允许</el-radio>
                <el-radio :value="false">禁止</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="URL正规化" prop="normalize">
              <el-radio-group v-model="seed.normalize">
                <el-radio :value="true">允许</el-radio>
                <el-radio :value="false">禁止</el-radio>
              </el-radio-group>
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
        <el-row v-if="more" :gutter="10">
          <el-col :span="12">
            <el-form-item label="全局抽链" prop="link_scope">
              <el-select v-model="seed.link_scope" clearable placeholder="请选择"
                         @clear="seed.link_scope = null" class="w180px">
                <el-option key="1" label="全部" :value="1" />
                <el-option key="2" label="DOMAIN" :value="2" />
                <el-option key="3" label="HOST" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="抓取超时" prop="timeout">
              <el-input-number v-model="seed.timeout" :min="0" :max="20000" :step="100" class="w180px" />
              <el-text class="ml-3" size="small">单位：毫秒</el-text>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="more" label="HTTP请求头">
          <mutable-table v-model="headers" :columns="headerColumns" />
        </el-form-item>
        <el-form-item v-if="more" label="自定义字段">
          <mutable-table v-model="customOptions" :columns="customOptionColumns" />
        </el-form-item>
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