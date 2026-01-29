<script setup>
import { IconHelp, IconPlus, IconRefresh } from '@tabler/icons-vue'
import { reactive, ref, useTemplateRef, watch, watchEffect } from 'vue'
import {
  ElButton, ElCol, ElDialog, ElForm, ElFormItem, ElInput, ElInputNumber, ElOption,
  ElRadio, ElRadioGroup, ElRow, ElScrollbar, ElSelect, ElSwitch, ElTooltip
} from 'element-plus'
import { useUserStore } from '@/store'
import DarwinCard from '@/components/data/Card'
import MutableTable from '@/components/data/MutableTable'
import {
  fetchMethodMap, httpRequestMap,
  linkScopeMap, postMediaTypeMap, priorityMap
} from '@/common/Constants'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import { asyncAddSeed } from '@/common/AsyncRequest'
import { fieldTypes, fillMap, fillRequestBody, seedFormRules } from '@/views/seed/common'

const open = defineModel()
const emits = defineEmits(['close'])
const props = defineProps(['planId'])
const userStore = useUserStore()
const formRef = useTemplateRef('form')
const more = ref(false)
const seed = reactive({
  allow_dispatch: false,
  normalize: true,
  priority: 1,
  fetch_method: 0,
  http_request: 'GET',
  timeout: 3000
})
const headers = reactive([])
const customOptions = reactive([])
const requestBody = reactive([])
const headerColumns = [{ name: '请求头名' }, { name: '请求头值' }]
const customOptionColumns = [{ name: '字段名' }, { name: '字段值' }]
const requestBodyColumns = [
  { name: '字段名' },
  { name: '字段值' },
  { name: '类型', type: 'select', default: 'string', items: fieldTypes }]

const add = async () => {
  if (!await formRef.value.validate(v => v)) return
  fillMap(seed, 'headers', headers)
  fillMap(seed, 'custom_map', customOptions)
  if (seed.http_request === 'POST') {
    if (!fillRequestBody(seed,  requestBody)) return
  }
  if (seed.http_request === 'GET') {
    seed.post_media_type = null
    seed.request_body = {}
  }
  if (!await asyncAddSeed(seed)) {
    showMessage('新增种子失败', ERROR)
    return
  }
  showMessage('新增种子成功', SUCCESS)
  emits('close')
  open.value = false
}

const resetSeedForm = () => {
  formRef.value.resetFields()
  more.value = false
  headers.splice(0, headers.length)
  customOptions.splice(0, customOptions.length)
  requestBody.splice(0, requestBody.length)
}

watch(() => seed.http_request, () => {
  if (seed.http_request === 'POST' && !seed.post_media_type) seed.post_media_type = 'JSON'
})
watchEffect(() => seed.plan_id = props.planId)
</script>

<template>
  <el-dialog v-model="open" align-center show-close width="60%">
    <el-form ref="form" :model="seed" :rules="seedFormRules" label-width="100px" label-position="top">
      <el-scrollbar max-height="800px">
        <darwin-card>
          <template #title>
            <span class="fs-14px">基本信息</span>
          </template>
          <el-form-item label="种子URL" prop="url">
            <el-input v-model.trim="seed.url" clearable />
          </el-form-item>
          <el-row>
            <el-col :span="8">
              <el-form-item prop="allow_dispatch">
                <template #label>
                  <span class="d-flex align-items-center">
                    <span>数据分发</span>
                    <el-tooltip effect="dark" placement="top" content="抓取结果通过消息队列分发，默认禁止分发">
                      <IconHelp size="12" class="ml-2"/>
                    </el-tooltip>
                  </span>
                </template>
                <el-radio-group v-model="seed.allow_dispatch">
                  <el-radio :value="true">允许</el-radio>
                  <el-radio :value="false">禁止</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item prop="normalize">
                <template #label>
                  <span class="d-flex align-items-center">
                    <span>正规化</span>
                    <el-tooltip effect="dark" placement="top" content="正规化会改写链接，对参数进行排序，抹去锚信息，默认进行正规化">
                      <IconHelp size="12" class="ml-2"/>
                    </el-tooltip>
                  </span>
                </template>
                <el-radio-group v-model="seed.normalize">
                  <el-radio :value="true">允许</el-radio>
                  <el-radio :value="false">禁止</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item prop="timeout">
                <template #label>
                  <span class="d-flex align-items-center">
                    <span>抓取超时</span>
                    <el-tooltip effect="dark" placement="top" content="单位：毫秒，系统默认超时时间6秒">
                      <IconHelp size="12" class="ml-2"/>
                    </el-tooltip>
                  </span>
                </template>
                <el-input-number v-model="seed.timeout" :min="0" :max="60000" :step="100" class="w-100p" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="8">
              <el-form-item label="抓取方式" prop="fetch_method">
                <el-radio-group v-model="seed.fetch_method">
                  <el-radio v-for="key in Object.keys(fetchMethodMap)" :value="parseInt(key)" :key="key">
                    {{ fetchMethodMap[key] }}
                  </el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="优先级" prop="priority">
                <el-radio-group v-model="seed.priority">
                  <el-radio v-for="key in Object.keys(priorityMap)" :key="key"
                            :value="parseInt(key)">
                    {{ priorityMap[key] }}
                  </el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item prop="link_scope">
                <template #label>
                  <span class="d-flex align-items-center">
                    <span>范围抽链</span>
                    <el-tooltip effect="dark" placement="top" content="注意：选择范围抽链后规则脚本失效">
                      <IconHelp size="12" class="ml-2"/>
                    </el-tooltip>
                  </span>
                </template>
                <el-select v-model="seed.link_scope" clearable placeholder="请选择"
                           @clear="seed.link_scope = null">
                  <el-option v-for="key in Object.keys(linkScopeMap)" :key="key"
                             :value="parseInt(key)" :label="linkScopeMap[key]" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="8">
              <el-form-item label="HTTP请求" prop="http_request">
                <el-radio-group v-model="seed.http_request">
                  <el-radio v-for="key in Object.keys(httpRequestMap)" :value="key" :key="key">
                    {{ httpRequestMap[key] }}
                  </el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item v-if="seed.http_request === 'POST'" prop="post_media_type">
                <template #label>
                  <span class="d-flex align-items-center">
                    <span>POST媒体类型</span>
                    <el-tooltip effect="dark" placement="top" raw-content
                                content="FORM：application/x-www-form-urlencoded <br/> JSON：application/json">
                      <IconHelp size="12" class="ml-2"/>
                    </el-tooltip>
                  </span>
                </template>
                <el-radio-group v-model="seed.post_media_type">
                  <el-radio v-for="key in Object.keys(postMediaTypeMap)" :value="key" :key="key">
                    {{ postMediaTypeMap[key] }}
                  </el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item prop="system_cookie">
                <template #label>
                  <span class="d-flex align-items-center">
                    <span>系统设置Cookie</span>
                    <el-tooltip effect="dark" placement="top"
                                content="开启系统设置Cookie，系统根据Cookie管理配置为抓取URL设置Cookie">
                      <IconHelp size="12" class="ml-2"/>
                    </el-tooltip>
                  </span>
                </template>
                <el-select v-model="seed.system_cookie" clearable placeholder="请选择"
                           @clear="seed.system_cookie = false">
                  <el-option :value="true" label="开启" />
                  <el-option :value="false" label="关闭" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item v-if="seed.http_request === 'POST'" prop="request_body" label-position="top">
            <template #label>
              <span class="d-flex align-items-center">
                <span>POST请求体</span>
                <el-tooltip effect="dark" placement="top" content="POST请求参数定义">
                  <IconHelp size="12" class="ml-2"/>
                </el-tooltip>
              </span>
            </template>
            <mutable-table v-model="requestBody" :columns="requestBodyColumns" />
          </el-form-item>
          <el-form-item label="高级选项" prop="more" label-position="left">
            <el-switch v-model="more" />
          </el-form-item>
        </darwin-card>
        <darwin-card v-if="more">
          <template #title>
            <span class="fs-14px d-flex align-items-center">
              <span>HTTP请求头</span>
              <el-tooltip effect="dark" placement="top" content="自定义HTTP请求头解决数据爬取问题">
                <IconHelp size="12" class="ml-2"/>
              </el-tooltip>
            </span>
          </template>
          <mutable-table v-model="headers" :columns="headerColumns" />
        </darwin-card>
        <darwin-card v-if="more">
          <template #title>
            <span class="fs-14px d-flex align-items-center">
              <span>自定义数据</span>
              <el-tooltip effect="dark" placement="top" content="通过自定义数据进行用户数据透传">
                <IconHelp size="12" class="ml-2"/>
              </el-tooltip>
            </span>
          </template>
          <mutable-table v-model="customOptions" :columns="customOptionColumns" />
        </darwin-card>
      </el-scrollbar>
      <el-form-item>
        <el-button type="primary" @click="add" :disabled="!userStore.injected">
          <IconPlus size="20" class="mr-1" />
          <span>新增</span>
        </el-button>
        <el-button type="info" @click="resetSeedForm" :disabled="!userStore.injected">
          <IconRefresh size="20" class="mr-1" />
          <span>重置</span>
        </el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<style scoped>
</style>