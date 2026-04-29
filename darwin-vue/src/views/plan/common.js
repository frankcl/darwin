import CronExpressionValidator from 'cron-expression-validator'

export const planFormRules = {
  name: [
    { required: true, message: '请输入计划名', trigger: 'change' }
  ],
  app_id: [
    { required: true, message: '请选择所属应用', trigger: 'change' }
  ],
  category: [
    { required: true, message: '请选择计划类型', trigger: 'change' }
  ],
  allow_dispatch_fail: [
    { required: true, message: '请选择失败数据分发方式', trigger: 'change' }
  ],
  crontab_expression: [
    {
      required: true,
      trigger: 'change',
      validator: (rule, value, callback) => {
        if (!value || value === '') callback(new Error('请输入调度时间表达式'))
        else if (CronExpressionValidator.isValidCronExpression(value.trim())) callback()
        else callback(new Error('非法调度时间表达式'))
      }
    }
  ]
}