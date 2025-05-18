<script setup>
import JsonViewer from 'vue-json-viewer'
import TableHead from '@/components/data/TableHead'

defineProps(['children', 'fieldMap', 'customMap'])
</script>

<template>
  <div v-if="children && children.length > 0" class="mb-4">
    <table-head title="抽链列表" />
    <json-viewer class="w-100p" :value="children" :expand-depth=0 boxed sort />
  </div>
  <div v-if="fieldMap && Object.keys(fieldMap).length > 0" class="mb-4">
    <table-head title="结构化数据" />
    <json-viewer class="w-100p" :value="fieldMap" :expand-depth=0 boxed sort />
  </div>
  <div v-if="customMap && Object.keys(customMap).length > 0" class="mb-4">
    <table-head title="自定义数据" />
    <json-viewer class="w-100p" :value="customMap" :expand-depth=0 boxed sort />
  </div>
</template>

<style scoped>

</style>