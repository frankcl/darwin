import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPersistedState from 'pinia-plugin-persistedstate'
import router from '@/router'
import '@fontsource/plus-jakarta-sans'
import '@fontsource/plus-jakarta-sans/400.css'
import '@fontsource/plus-jakarta-sans/400-italic.css'
import '@/style/index.css'
import App from './App'

const pinia = createPinia()
pinia.use(piniaPersistedState)
createApp(App).use(pinia).use(router).mount('#app')
