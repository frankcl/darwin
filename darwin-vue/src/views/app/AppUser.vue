<script setup>
import { IconEdit } from '@tabler/icons-vue'
import { ref, watch } from 'vue'
import { ElButton, ElDialog, ElRow, ElTransfer } from 'element-plus'
import { useUserStore } from '@/store'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import {
  asyncAllUsers,
  asyncBatchUpdateAppUser,
  asyncGetAppUsers
} from '@/common/AsyncRequest'
import DarwinCard from '@/components/data/Card'

const open = defineModel()
const props = defineProps(['id', 'name'])
const emits = defineEmits(['close'])
const userStore = useUserStore()
const appUsers = ref([])
const users = ref([])
const userMap = new Map()

const search = (query, user) => user.label.includes(query)

const batchUpdate = async () => {
  const request = {
    app_id: props.id,
    users: []
  }
  appUsers.value.forEach(id => { request.users.push({ id: id, name: userMap.get(id) })})
  if (!await asyncBatchUpdateAppUser(request)) {
    showMessage('更新应用成员失败', ERROR)
    return
  }
  showMessage('更新应用成员成功', SUCCESS)
  open.value = false
}

watch(() => props.id, async () => {
  if (props.id) {
    userMap.clear()
    users.value.splice(0, users.value.length)
    appUsers.value.splice(0, appUsers.value.length)
    const tempAppUsers = await asyncGetAppUsers(props.id)
    tempAppUsers.forEach(user => appUsers.value.push(user.user_id))
    const tempUsers = await asyncAllUsers()
    tempUsers.forEach(user => {
      users.value.push({ key: user.id, label: user.name })
      userMap.set(user.id, user.name)
    })
  }
}, { immediate: true })
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" align-center show-close>
    <darwin-card :title="`应用成员：${name}`">
      <el-transfer v-model="appUsers" :data="users" filterable
                   filter-placeholder="根据用户名搜索" :filter-method="search"
                   :titles="['非应用成员', '应用成员']" :button-texts="['撤销', '选取']" />
      <el-row align="middle" class="mt-4">
        <el-button type="primary" @click="batchUpdate" :disabled="!userStore.injected">
          <IconEdit size="20" class="mr-1" />
          <span>批量更新</span>
        </el-button>
      </el-row>
    </darwin-card>
  </el-dialog>
</template>

<style scoped>
:deep(.el-transfer-panel .el-transfer-panel__header .el-checkbox .el-checkbox__label) {
  font-size: 14px;
  font-weight: 500;
}
</style>