<script setup>
import { ElCol, ElRow } from 'element-plus'

const props = defineProps(['title'])
</script>

<template>
  <el-row align="middle" class="mb-2">
    <el-col :span="8">
      <span class="fs-m fw-500 ml-2">{{ props.title }}</span>
    </el-col>
    <el-col :span="16">
      <el-row align="middle" justify="end">
        <slot name="right"></slot>
      </el-row>
    </el-col>
  </el-row>
</template>

<style scoped>

</style>