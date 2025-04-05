<script setup>
import { ref, watch } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElDialog,
  ElNotification, ElPageHeader, ElRow, ElSpace, ElTransfer
} from 'element-plus'
import {
  asyncBatchUpdateAppUser, asyncGetAllUsers,
  asyncGetAppUsers
} from '@/common/service'
import {
  executeAsyncRequest
} from '@/common/assortment'

const open = defineModel()
const props = defineProps(['id', 'name'])
const emits = defineEmits(['close'])
const appUsers = ref([])
const users = ref([])
const userMap = new Map()

const search = (query, user) => user.label.includes(query)

const save = async () => {
  const request = {
    app_id: props.id,
    users: []
  }
  appUsers.value.forEach(id => { request.users.push({ id: id, name: userMap.get(id) })})
  const successHandle = () => ElNotification.success('调整应用成员成功')
  const failHandle = () => ElNotification.success('调整应用成员失败')
  if (!await executeAsyncRequest(asyncBatchUpdateAppUser, request, successHandle, failHandle)) return
  open.value = false
}

watch(() => props.id, async () => {
  if (props.id) {
    users.value.splice(0, users.value.length)
    appUsers.value.splice(0, appUsers.value.length)
    userMap.clear()
    const tempAppUsers = await asyncGetAppUsers(props.id)
    tempAppUsers.forEach(user => appUsers.value.push(user.user_id))
    const tempUsers = await asyncGetAllUsers()
    tempUsers.forEach(user => {
      users.value.push({ label: user.name, key: user.id })
      userMap.set(user.id, user.name)
    })
  }
}, { immediate: true })
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" width="650" align-center show-close>
    <el-space direction="vertical" :size="20" :fill="true" style="min-width: 100%">
      <el-page-header @back="open = false">
        <template #breadcrumb>
          <el-breadcrumb :separator-icon="ArrowRight">
            <el-breadcrumb-item :to="{ name: 'Home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ name: 'AppList' }">爬虫应用</el-breadcrumb-item>
          </el-breadcrumb>
        </template>
        <template #content>
          <span class="text-large font-600 mr-3">应用成员</span>
          <span class="text-sm mr-2" style="color: var(--el-text-color-regular)">{{ name }}</span>
        </template>
      </el-page-header>
      <el-row justify="start">
        <el-transfer v-model="appUsers" :data="users" filterable
                     filter-placeholder="根据用户名称搜索" :filter-method="search" :titles="['非应用成员', '应用成员']"
                     :button-texts="['撤销', '选取']"></el-transfer>
      </el-row>
      <el-row align="middle">
        <el-button @click="save">保存</el-button>
      </el-row>
    </el-space>
  </el-dialog>
</template>

<style scoped>

</style>