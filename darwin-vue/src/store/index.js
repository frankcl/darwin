import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { asyncGetOwnApps } from '@/common/service'

export const useUserStore = defineStore(
  'user',
  () => {
    const id = ref()
    const username = ref()
    const name = ref()
    const openid = ref()
    const avatar = ref()
    const tenant = ref()
    const superAdmin = ref()
    const roles = ref()
    const apps = ref()

    const injected = computed(() => username.value !== undefined)

    const inject = async user => {
      if (!user) return
      id.value = user.id
      username.value = user.username
      name.value = user.name
      avatar.value = user.avatar
      openid.value = user.wx_openid
      superAdmin.value = user.super_admin
      tenant.value = user.tenant
      roles.value = user.roles
    }

    const fillApps = async () => {
      if (!injected.value) return
      const ownApps = await asyncGetOwnApps()
      if (ownApps) apps.value = JSON.stringify(ownApps)
    }

    const clear = () => {
      id.value = undefined
      username.value = undefined
      name.value = undefined
      avatar.value = undefined
      openid.value = undefined
      superAdmin.value = undefined
      tenant.value = undefined
      roles.value = undefined
      apps.value = undefined
    }

    return { id, username, name, openid, avatar, superAdmin, tenant, roles, apps, injected, inject, fillApps, clear }
  },
  {
    persist: {
      storage: sessionStorage,
      omit: ['inject', 'fillApps', 'clear', 'injected']
    }
  }
)