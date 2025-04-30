<script setup>
import { onUpdated, ref, watch} from 'vue'
import { ERROR, showMessage } from '@/common/Feedback'
import { ElButton, ElInput, ElInputNumber, ElTable, ElTableColumn, ElText } from 'element-plus'

const props = defineProps({
  columns: {
    required: true,
    type: Array,
    validator: value => {
      if (!Array.isArray(value)) return false
      value.forEach(v => {
        if (v.name === undefined) return false
      })
      return true
    }
  },
  maxHeight: { default: 500 },
  filterColumn: { default: 0, type: Number }
})
const rows = defineModel()
const prepareIndex = ref()
const query = ref()
const shows = ref([])

const isNotCompleted = index => {
  for (let i = 0; i< rows.value[index].length; i++) {
    if (rows.value[index][i] === undefined) {
      showMessage('尚有未完成的数据输入', ERROR)
      return true
    }
  }
  return false
}

const prepare = index => {
  if (prepareIndex.value !== undefined && isNotCompleted(prepareIndex.value)) return
  prepareIndex.value = index
}

const add = index => {
  if (prepareIndex.value !== undefined && isNotCompleted(prepareIndex.value)) return
  const row = new Array(props.columns.length)
  for (let i = 0; i < row.length; i++) {
    if (props.columns[i].default !== undefined) row[i] = props.columns[i].default
  }
  rows.value.splice(index + 1, 0, row)
  shows.value.splice(index + 1, 0, true)
  prepareIndex.value = index + 1
}

const remove = index => {
  if (prepareIndex.value && prepareIndex.value !== index && isNotCompleted(prepareIndex.value)) return
  rows.value.splice(index, 1)
  shows.value.splice(index, 1)
  if (prepareIndex.value === index) prepareIndex.value = undefined
}

const save = index => {
  if (isNotCompleted(index)) return
  prepareIndex.value = undefined
}

const handleRowClassName = o => shows.value[o.rowIndex] ? '' : 'hidden-row'

watch(() => query.value, () => {
  rows.value.forEach((row, index) => {
    if (!query.value) shows.value[index] = true
    else shows.value[index] = row[props.filterColumn] === query.value
  })
})
onUpdated(() => {
  shows.value = []
  rows.value.forEach(() => shows.value.push(true))
})
</script>

<template>
  <el-table :data="rows" :max-height="maxHeight" table-layout="auto" stripe :row-class-name="handleRowClassName">
    <template #empty>暂无数据</template>
    <el-table-column v-for="(column, index) in columns" :key="index" :label="column.name" show-overflow-tooltip>
      <template #default="scope">
        <el-input v-if="scope.$index === prepareIndex && column.type === 'password'" type="password"
                  v-model="scope.row[index]" clearable :placeholder="`请输入${columns[index].name}`" />
        <el-input-number v-else-if="scope.$index === prepareIndex && column.type === 'number'"
                  :min="column.min" :max="column.max" v-model="scope.row[index]" clearable />
        <el-input v-else-if="scope.$index === prepareIndex && (column.type === 'input' || column.type === undefined)"
                  v-model="scope.row[index]" clearable :placeholder="`请输入${columns[index].name}`" />
        <el-text v-else>{{ scope.row[index] }}</el-text>
      </template>
    </el-table-column>
    <el-table-column width="250" align="center">
      <template #header>
        <el-input v-model="query" :placeholder="`根据${columns[filterColumn].name}搜索`"
                  @clear="query = undefined" clearable />
      </template>
      <template #default="scope">
        <el-button v-if="scope.$index === prepareIndex" type="success" @click="save(scope.$index)">保存</el-button>
        <el-button v-else type="success" @click="prepare(scope.$index)">修改</el-button>
        <el-button type="danger" @click="remove(scope.$index)">删除</el-button>
        <el-button type="primary" @click="add(scope.$index)">新增</el-button>
      </template>
    </el-table-column>
  </el-table>
  <el-button v-if="rows.length === 0" type="primary" class="w100 mt-2" @click="add(-1)">新增</el-button>
</template>

<style scoped>
:deep(.el-table__body .hidden-row) {
  display: none !important;
  width: 123px;
}
</style>