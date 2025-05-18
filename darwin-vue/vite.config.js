import { resolve } from 'path'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import eslintPlugin from 'vite-plugin-eslint'
import ElementPlus from 'unplugin-element-plus/vite'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    ElementPlus(),
    eslintPlugin({
      include: ['src/**/*.js', 'src/**/*.vue', 'src/**/*.ts'],
      exclude: ['node_modules/**', 'dist/**'],
      fix: false,
      cache: false
    })
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    },
    extensions: ['.js', '.jsx', '.ts', '.tsx', '.vue']
  },
  test: {
    globals: true,              // 启用类似 jest 的全局测试 API
    environment: 'happy-dom'    // 使用 happy-dom 模拟 DOM
  },
  server: {
    port: 8088,
    host: '127.0.0.1',
    allowedHosts: ['darwin.manong.xin'],
    cors: true,
    proxy: {
      '^/api': {
        target: 'https://darwin.manong.xin:10001',
        changeOrigin: true,
        ws: true,
        https: true,
        secure: false
      },
    }
  }
})
