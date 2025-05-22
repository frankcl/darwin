<script setup>
import { IconX } from '@tabler/icons-vue'
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElImage, ElLink } from 'element-plus'
import ImageLogo from '@/assets/images/logo.jpg'
import DarwinMenu from '@/views/main/Menu'
import DarwinNav from '@/views/main/Nav'

const router = useRouter()
const sidebarType = ref('full')
const isMiniSidebar = ref(false)
const isShowSidebar = ref(false)

const setSidebarType = () => {
  const width = window.innerWidth > 0 ? window.innerWidth : this.screen.width
  if (width < 1199) {
    sidebarType.value = 'mini-sidebar'
    isMiniSidebar.value = true
  } else {
    sidebarType.value = 'full'
    isMiniSidebar.value = false
  }
}

const showSidebar = () => isShowSidebar.value = true
const hideSidebar = () => isShowSidebar.value = false

onMounted(async () => window.addEventListener('resize', setSidebarType))
onUnmounted(() => window.removeEventListener('resize', setSidebarType))
</script>

<template>
  <div id="main-wrapper" class="page-wrapper" data-layout="vertical"
       :class="{ 'mini-sidebar': isMiniSidebar, 'show-sidebar': isShowSidebar }"
       :data-sidebartype="sidebarType" data-sidebar-position="fixed" data-header-position="fixed">
    <aside class="left-sidebar">
      <div class="brand-logo align-items-center justify-content-between d-flex">
        <el-link @click="router.push({ path: '/home' })" :underline="false" class="cursor-pointer">
          <el-image :src="ImageLogo" fit="fill" />
        </el-link>
        <div class="d-xl-none cursor-pointer ml-4" @click="hideSidebar"><IconX color="#5a6a85" /></div>
      </div>
      <darwin-menu />
    </aside>
    <div class="body-wrapper">
      <header class="app-header">
        <darwin-nav @show-sidebar="showSidebar" />
      </header>
      <div class="container-fluid">
        <div class="container-fluid">
          <RouterView></RouterView>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-wrapper {
  position: relative;
}
.left-sidebar {
  width: 220px;
  height: 100vh;
  background-color: #fff;
  position: absolute;
  -webkit-transition: 0.2s ease-in;
  transition: 0.2s ease-in;
  z-index: 11;
  border-right: 1px solid rgb(229, 234, 239);
}
#main-wrapper[data-layout=vertical][data-sidebar-position=fixed] .left-sidebar {
  position: fixed;
  top: 0;
}
@media (max-width: 1279px) {
  #main-wrapper[data-layout=vertical][data-sidebartype=full] .left-sidebar,
  #main-wrapper[data-layout=vertical][data-sidebartype=mini-sidebar] .left-sidebar {
    left: -220px;
  }
  #main-wrapper[data-layout=vertical][data-sidebartype=full].show-sidebar .left-sidebar,
  #main-wrapper[data-layout=vertical][data-sidebartype=mini-sidebar].show-sidebar .left-sidebar {
    left: 0;
  }
}
.brand-logo {
  min-height: 70px;
  padding: 27px 24px 0 24px;
}
.body-wrapper {
  position: relative;
}
.app-header {
  position: relative;
  z-index: 50;
  width: 100%;
  background: #fff;
  padding: 0 25px;
}
#main-wrapper[data-layout=vertical][data-header-position=fixed] .app-header {
  position: fixed;
  z-index: 10;
}
@media (min-width: 1280px) {
  #main-wrapper[data-layout=vertical][data-sidebartype=full] .body-wrapper {
    margin-left: 220px;
  }
  #main-wrapper[data-layout=vertical][data-header-position=fixed] .app-header {
    width: calc(100% - 220px);
  }
  .d-xl-none {
    display: none !important;
  }
}
#main-wrapper[data-layout=vertical][data-header-position=fixed] .body-wrapper>.container-fluid {
  padding-top: calc(70px + 15px);
}
.body-wrapper>.container-fluid {
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px;
  -webkit-transition: 0.2s ease-in;
  transition: 0.2s ease-in;
}
.container-fluid {
  width: 100%;
  padding-right: 12px;
  padding-left: 12px;
  margin-right: auto;
  margin-left: auto;
}
@media (max-width: 848px) {
  .body-wrapper>.container-fluid {
    padding: 30px 20px;
  }
}
</style>
