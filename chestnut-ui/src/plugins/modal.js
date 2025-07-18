import i18n from '@/i18n'
import { Message, MessageBox, Notification, Loading } from 'element-ui'

let loadingInstance;

export default {
  // 消息提示
  msg(content, customClass = "") {
    Message.info({
      message: content,
      customClass: customClass
    })
  },
  // 错误消息
  msgError(content, customClass = "") {
    Message.error({
      message: content,
      customClass: customClass
    })
  },
  // 成功消息
  msgSuccess(content, customClass = "") {
    Message.success({
      message: content,
      customClass: customClass
    })
  },
  // 警告消息
  msgWarning(content, customClass = "") {
    Message.warning({
      message: content,
      customClass: customClass
    })
  },
  // 弹出提示
  alert(content) {
    MessageBox.alert(content, i18n.t('Common.SystemTip'))
  },
  // 错误提示
  alertError(content) {
    MessageBox.alert(content, i18n.t('Common.SystemTip'), { type: 'error' })
  },
  // 成功提示
  alertSuccess(content) {
    MessageBox.alert(content, i18n.t('Common.SystemTip'), { type: 'success' })
  },
  // 警告提示
  alertWarning(content) {
    MessageBox.alert(content, i18n.t('Common.SystemTip'), { type: 'warning' })
  },
  // 通知提示
  notify(content) {
    Notification.info(content)
  },
  // 错误通知
  notifyError(content) {
    Notification.error(content);
  },
  // 成功通知
  notifySuccess(content) {
    Notification.success(content)
  },
  // 警告通知
  notifyWarning(content) {
    Notification.warning(content)
  },
  // 确认窗体
  confirm(content) {
    return MessageBox.confirm(content, i18n.t('Common.SystemTip'), {
      confirmButtonText: i18n.t('Common.Confirm'),
      cancelButtonText: i18n.t('Common.Cancel'),
      type: "warning",
    })
  },
  // 提交内容
  prompt(content) {
    return MessageBox.prompt(content, i18n.t('Common.SystemTip'), {
      confirmButtonText: i18n.t('Common.Confirm'),
      cancelButtonText: i18n.t('Common.Cancel'),
      type: "warning",
    })
  },
  // 打开遮罩层
  loading(content) {
    loadingInstance = Loading.service({
      lock: true,
      text: content,
      spinner: "el-icon-loading",
      background: "rgba(0, 0, 0, 0.7)",
    })
  },
  // 关闭遮罩层
  closeLoading() {
    loadingInstance.close();
  }
}
