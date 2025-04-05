import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPersistedState from 'pinia-plugin-persistedstate'
import router from '@/router'
import '@/style/style.css'
import App from './App'

const pinia = createPinia()
pinia.use(piniaPersistedState)
createApp(App).use(pinia).use(router).mount('#app')
