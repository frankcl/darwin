import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

export const useUserStore = defineStore(
  'user',
  () => {
    const id = ref()
    const username = ref()
    const name = ref()
    const avatar = ref()
    const superAdmin = ref()
    const apps = ref()

    const injected = computed(() => id.value !== undefined)

    const inject = user => {
      if (!user) return
      id.value = user.id
      username.value = user.username
      name.value = user.name
      avatar.value = user.avatar
      superAdmin.value = user.super_admin
    }

    const injectApps = ownApps => {
      if (!injected.value) return
      apps.value = JSON.stringify(ownApps)
    }

    const $reset = () => {
      id.value = undefined
      username.value = undefined
      name.value = undefined
      avatar.value = undefined
      superAdmin.value = undefined
      apps.value = undefined
    }

    return { id, username, name, avatar, superAdmin, apps, injected, inject, injectApps, $reset }
  },
  {
    persist: {
      storage: sessionStorage,
      omit: ['inject', 'injectApps', '$reset', 'injected']
    }
  }
)