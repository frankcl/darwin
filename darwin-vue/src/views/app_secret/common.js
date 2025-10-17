export const appSecretFormRules = {
  name: [
    { required: true, message: '请输入应用秘钥名称', trigger: 'change' }
  ],
  app_id: [
    { required: true, message: '请选择应用', trigger: 'change' }
  ],
  access_key: [
    { required: true, message: '请输入应用AccessKey', trigger: 'change' }
  ],
  secret_key: [
    { required: true, message: '请输入应用SecretKey', trigger: 'change' }
  ]
}