<script setup>
import {
  ElButton, ElCol, ElDivider, ElForm,
  ElFormItem, ElIcon, ElInput, ElSpace, ElText
} from 'element-plus'
import { Minus, Plus, Right } from '@element-plus/icons-vue'

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  label: {
    type: String,
    default: '数据项'
  }
})
const fields = defineModel()

const append = () => {
  fields.value.push({
    key: null,
    value: null
  })
}

const remove = field => {
  const index = fields.value.indexOf(field)
  if (index !== -1) fields.value.splice(index, 1)
}
</script>

<template>
  <el-divider content-position="left">
    <el-text class="mr-2">{{ title }}</el-text>
    <el-button @click="append" size="small" :icon="Plus" circle />
  </el-divider>
  <el-space direction="vertical" :size="20" :fill="true" class="w100">
    <el-form v-model="fields" v-if="fields" class="w100" label-width="80px" label-position="right">
      <el-form-item v-for="(field, index) in fields" :key="index" :label="label">
        <el-col :span="9">
          <el-input v-model="field.key" placeholder="please input key" />
        </el-col>
        <el-col class="flex justify-center" :span="2">
          <el-icon><Right /></el-icon>
        </el-col>
        <el-col :span="9">
          <el-input v-model="field.value" placeholder="please input value" />
        </el-col>
        <el-col :span="1"></el-col>
        <el-col :span="3">
          <el-button :icon="Minus" size="small" circle @click="remove(field)" />
        </el-col>
      </el-form-item>
    </el-form>
  </el-space>
</template>

<style scoped>

</style>