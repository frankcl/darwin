import globals from 'globals'
import pluginJs from '@eslint/js'
import pluginVue from 'eslint-plugin-vue'


export default [
  {
    files: ['**/*.{js,mjs,cjs,vue}']
  },
  {
    languageOptions: {
      globals: {
        ...globals.browser,
        ...globals.node
      },
      ecmaVersion: 'latest',                      // 使用最新的 ECMAScript 语法
      sourceType: 'module',                       // 代码是 ECMAScript 模块
    }
  },
  pluginJs.configs.recommended,
  ...pluginVue.configs['flat/essential'],
  {
    rules: {
      indent: ['warn', 2, { SwitchCase : 1 }],    // 缩进使用2个空格
      quotes: ['warn', 'single'],                 // 使用单引号
      semi: ['warn', 'never'],                    // 语句末尾不加分号
      'prefer-const': 'warn',                     // 常量使用 const
      'linebreak-style': ['error', 'unix'],       // 使用 Unix 风格的换行符
      'vue/multi-word-component-names': 'off',    // 关闭多组件名检测
      'no-unused-vars': 'warn',                   // 开启未使用变量警告
      'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',     // 生产环境中警告 console 使用，开发环境中关闭规则
      'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off',    // 生产环境中警告 debugger 使用，开发环境中关闭规则
    },
  },
  {
    ignores: [
      '**/dist',
      '.vscode',
      '.idea',
      '*.sh',
      '**/node_modules',
      '*.md',
      '*.woff',
      '*.woff',
      '*.ttf',
      'yarn.lock',
      'package-lock.json',
      '/public',
      '/docs',
      '**/output',
      '.husky',
      '.local',
      '/bin',
      'Dockerfile'
    ],
  }
]
