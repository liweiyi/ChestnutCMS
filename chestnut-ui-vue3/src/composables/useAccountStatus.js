import { computed, reactive, ref } from 'vue'
import { ACCOUNT_STATUS, ACCOUNT_STATUS_ACTION } from '../utils/accountStatus.js'
import { normalizePhoneNumber } from '../utils/phoneNumber.js'

export default function useAccountStatus({ idField, changeStatus, batchChangeStatus, batchAccountLabel, onSuccess }) {
  const pendingIds = reactive(new Set())
  const batchPending = ref(false)
  const statusBusy = computed(() => batchPending.value || pendingIds.size > 0)
  const lockOpen = ref(false)
  const lockAccount = ref(null)
  const lockBatchAccounts = ref(null)
  const lockAccountEndTime = computed(() => lockBatchAccounts.value ? '' : lockAccount.value?.lockEndTime || '')
  const lockAccountLabel = computed(() => {
    if (lockBatchAccounts.value) {
      const count = lockBatchAccounts.value.length
      return batchAccountLabel?.(count) || String(count)
    }
    const account = lockAccount.value
    if (!account) return ''
    const name = account.userName || account.email
      || normalizePhoneNumber(account.phoneNumber, account.phoneCallingCode)
    return name ? `${name} (${account[idField]})` : String(account[idField])
  })

  const isStatusPending = account => pendingIds.has(String(account[idField]))

  async function updateStatus(account, status, lockEndTime) {
    if (!account || batchPending.value || isStatusPending(account)) return false
    const id = String(account[idField])
    pendingIds.add(id)
    try {
      const payload = { [idField]: account[idField], status }
      if (status === ACCOUNT_STATUS.LOCKED) payload.lockEndTime = lockEndTime
      await changeStatus(payload)
      try {
        await onSuccess?.()
      } catch {
        // A failed list refresh must not undo a successful status update.
      }
      return true
    } catch {
      // The shared request interceptor reports status update failures.
      return false
    } finally {
      pendingIds.delete(id)
    }
  }

  function openLockDialog(account) {
    if (!account || lockOpen.value || batchPending.value || isStatusPending(account)) return
    lockBatchAccounts.value = null
    lockAccount.value = account
    lockOpen.value = true
  }

  function toggleDisabled(account) {
    const status = account?.status === ACCOUNT_STATUS.DISABLED ? ACCOUNT_STATUS.ENABLED : ACCOUNT_STATUS.DISABLED
    return updateStatus(account, status)
  }

  function toggleLocked(account) {
    if (account?.status === ACCOUNT_STATUS.LOCKED) return updateStatus(account, ACCOUNT_STATUS.ENABLED)
    openLockDialog(account)
  }

  function selectedAccountsSnapshot(accounts) {
    if (!Array.isArray(accounts) || accounts.length === 0
      || accounts.some(account => !account || account[idField] == null)) return []
    const unique = new Map(accounts.map(account => [String(account[idField]), { [idField]: account[idField] }]))
    return [...unique.values()]
  }

  async function updateBatchStatus(accounts, action, lockEndTime) {
    const snapshot = selectedAccountsSnapshot(accounts)
    if (!batchChangeStatus || statusBusy.value || snapshot.length === 0) return false
    const ids = snapshot.map(account => account[idField])
    batchPending.value = true
    ids.forEach(id => pendingIds.add(String(id)))
    try {
      const payload = { ids, action }
      if (action === ACCOUNT_STATUS_ACTION.LOCK) payload.lockEndTime = lockEndTime
      await batchChangeStatus(payload)
      try {
        await onSuccess?.()
      } catch {
        // Keep the successful batch result even when refreshing the list fails.
      }
      return true
    } catch {
      // The shared request interceptor reports failures; keep a lock dialog open for retry.
      return false
    } finally {
      ids.forEach(id => pendingIds.delete(String(id)))
      batchPending.value = false
    }
  }

  function handleBatchCommand(action, accounts) {
    if (!Object.values(ACCOUNT_STATUS_ACTION).includes(action) || statusBusy.value || lockOpen.value) return false
    const snapshot = selectedAccountsSnapshot(accounts)
    if (snapshot.length === 0) return false
    if (action === ACCOUNT_STATUS_ACTION.LOCK) {
      lockAccount.value = null
      lockBatchAccounts.value = snapshot
      lockOpen.value = true
      return true
    }
    return updateBatchStatus(snapshot, action)
  }

  async function submitLock(lockEndTime) {
    const success = lockBatchAccounts.value
      ? await updateBatchStatus(lockBatchAccounts.value, ACCOUNT_STATUS_ACTION.LOCK, lockEndTime)
      : await updateStatus(lockAccount.value, ACCOUNT_STATUS.LOCKED, lockEndTime)
    if (success) {
      lockOpen.value = false
    }
  }

  return { lockOpen, lockAccountLabel, lockAccountEndTime, isStatusPending, updateStatus, openLockDialog,
    batchPending, statusBusy, handleBatchCommand, toggleDisabled, toggleLocked, submitLock }
}
