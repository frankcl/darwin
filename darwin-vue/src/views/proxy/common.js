export const proxyFormRules = {
  address: [
    { required: true, message: '请输入代理IP地址', trigger: 'change' }
  ],
  port: [
    { required: true, message: '请输入代理端口', trigger: 'change' }
  ]
}