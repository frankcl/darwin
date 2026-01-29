<script setup>
import { onMounted, ref } from 'vue'
import { ElLink, ElTable, ElTableColumn } from 'element-plus'
import { useRouter } from 'vue-router'
import { asyncHostFetchCount } from '@/common/AsyncRequest'
import DarwinCard from '@/components/data/Card'

const router = useRouter()
const hosts = ref([])

onMounted(async() => hosts.value = await asyncHostFetchCount())
</script>

<template>
  <darwin-card title="24小时抓取量TOP10站点">
    <el-table :data="hosts" max-height="183" table-layout="auto" stripe>
      <template #empty>暂无抓取数据</template>
      <el-table-column prop="host" label="站点" show-overflow-tooltip>
        <template #default="scope">
          <el-link @click="router.push({ path: '/record/search', query: { host: scope.row.host } })"
                   underline="never">{{ scope.row.host }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="count" label="抓取量" width="100" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.count }}</template>
      </el-table-column>
    </el-table>
  </darwin-card>
</template>

<style scoped>

</style>