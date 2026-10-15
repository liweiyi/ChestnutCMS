import { reactive, ref } from 'vue'

export default function useMemberInteractionLogs({ getLikeLogs, getSignInLogs }) {
  const activeTab = ref('likes')
  const filters = reactive({ memberId: '', timeRange: [] })
  const pagination = reactive({ pageNum: 1, pageSize: 10 })
  const rows = ref([])
  const total = ref(0)
  const loading = ref(false)
  let requestId = 0
  let appliedFilters = {}

  function clearList() {
    requestId++
    rows.value = []
    total.value = 0
    loading.value = false
  }

  function selectTab(name) {
    if (!['likes', 'signIns'].includes(name) || activeTab.value === name) return false
    clearList()
    activeTab.value = name
    pagination.pageNum = 1
    return true
  }

  async function loadLogs() {
    const request = ++requestId
    const fetch = activeTab.value === 'likes' ? getLikeLogs : getSignInLogs
    loading.value = true
    try {
      const response = await fetch({ ...appliedFilters, ...pagination })
      if (request !== requestId) return
      rows.value = response.data.rows
      total.value = Number(response.data.total)
    } finally {
      if (request === requestId) loading.value = false
    }
  }

  function search() {
    pagination.pageNum = 1
    appliedFilters = {
      memberId: filters.memberId || undefined,
      beginTime: filters.timeRange?.[0],
      endTime: filters.timeRange?.[1]
    }
    clearList()
    return loadLogs()
  }

  function reset() {
    filters.memberId = ''
    filters.timeRange = []
    return search()
  }

  return { activeTab, filters, pagination, rows, total, loading, selectTab, loadLogs, search, reset }
}
