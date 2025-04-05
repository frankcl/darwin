<script setup>
import { useTemplateRef } from 'vue'
import { ElButton, ElCol, ElDivider, ElForm, ElFormItem, ElIcon, ElInput, ElRow, ElSpace, ElText } from 'element-plus'
import { DArrowRight, Delete, Plus } from '@element-plus/icons-vue'

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  fieldName: {
    type: String,
    default: 'field'
  }
})
const fields = defineModel()
const formRef = useTemplateRef('formRef')

const add = () => {
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
    <el-text>{{ title }}</el-text>&nbsp;&nbsp;
    <el-button @click.prevent="add()" size="small" :icon="Plus" circle></el-button>
  </el-divider>
  <el-space direction="vertical" :size="20" :fill="true" style="min-width: 100%">
    <el-form v-model="fields" v-if="fields" ref="formRef" style="min-width: 100%">
      <el-form-item v-for="(field, index) in fields" :key="index" :label="props.fieldName"
                    :prop="'fields.' + index + '.key'">
        <el-row :gutter="20" style="min-width: 100%">
          <el-col :span="10">
            <el-input v-model="field.key" placeholder="key"></el-input>
          </el-col>
          <el-col :span="1">
            <el-icon><DArrowRight /></el-icon>
          </el-col>
          <el-col :span="10">
            <el-input v-model="field.value" placeholder="value"></el-input>
          </el-col>
          <el-col :span="3">
            <el-button :icon="Delete" size="small" circle @click.prevent="remove(field)"></el-button>
          </el-col>
        </el-row>
      </el-form-item>
    </el-form>
  </el-space>
</template>

<style scoped>

</style>