import { markRaw } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'

export const SUCCESS = Symbol()
export const ERROR = Symbol()
export const WARNING = Symbol()
export const INFO = Symbol()

export const showMessage = (message, type = INFO) => {
  switch (type) {
    case SUCCESS: ElMessage.success({ message: message, grouping: true }); break
    case ERROR: ElMessage.error({ message: message, grouping: true }); break
    case WARNING: ElMessage.warning({ message: message, grouping: true }); break
    case INFO: ElMessage.info({ message: message, grouping: true }); break
    default: ElMessage.info({ message: message, grouping: true })
  }
}

export const asyncExecuteAfterConfirming =
  async (asyncRequestFunction, args, message = '是否确定执行此操作？', title = '警告') => {
    return ElMessageBox.confirm(
      message,
      title,
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        icon: markRaw(Warning),
        type: 'warning',
      }
    ).then(async () => await asyncRequestFunction(args)).catch(() => {})
  }