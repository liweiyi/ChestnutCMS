import { reactive, ref } from 'vue'

export default function useMemberLevels({ getMembers, getLevels, getLogs, getOperations }) {
  const members = ref([])
  const memberTotal = ref(0)
  const memberLoading = ref(false)
  const selectedMember = ref()
  const levels = ref([])
  const levelLoading = ref(false)
  const memberQuery = reactive({ memberId: '', keyword: '', pageNum: 1, pageSize: 10 })
  const logOpen = ref(false)
  const logContext = ref()
  const logs = ref([])
  const logTotal = ref(0)
  const logLoading = ref(false)
  const operations = ref([])
  const logQuery = reactive({ opType: '', dateRange: [], pageNum: 1, pageSize: 10 })
  let appliedLogFilters = {}
  let memberRequest = 0
  let levelRequest = 0
  let logRequest = 0
  let operationRequest = 0

  function closeLogs() {
    logRequest++
    operationRequest++
    logOpen.value = false
    logContext.value = undefined
    logLoading.value = false
    logs.value = []
    logTotal.value = 0
    operations.value = []
  }

  function clearSelection() {
    levelRequest++
    selectedMember.value = undefined
    levels.value = []
    levelLoading.value = false
    closeLogs()
  }

  async function selectMember(member) {
    if (!member || selectedMember.value?.memberId === member.memberId) return
    clearSelection()
    selectedMember.value = { ...member }
    const request = ++levelRequest
    levelLoading.value = true
    try {
      const response = await getLevels(member.memberId)
      if (request === levelRequest) levels.value = response.data
    } finally {
      if (request === levelRequest) levelLoading.value = false
    }
  }

  async function loadMembers() {
    const request = ++memberRequest
    clearSelection()
    members.value = []
    memberTotal.value = 0
    memberLoading.value = true
    try {
      const response = await getMembers({
        ...memberQuery, memberId: memberQuery.memberId || undefined, keyword: memberQuery.keyword || undefined
      })
      if (request !== memberRequest) return
      members.value = response.data.rows
      memberTotal.value = Number(response.data.total)
      memberLoading.value = false
      if (members.value.length) await selectMember(members.value[0])
    } finally {
      if (request === memberRequest) memberLoading.value = false
    }
  }

  async function loadLogs() {
    if (!logOpen.value || !logContext.value) return
    const request = ++logRequest
    const { memberId, levelType } = logContext.value
    logLoading.value = true
    try {
      const response = await getLogs({
        memberId, levelType, ...appliedLogFilters, pageNum: logQuery.pageNum, pageSize: logQuery.pageSize
      })
      if (request !== logRequest) return
      logs.value = response.data.rows
      logTotal.value = Number(response.data.total)
    } finally {
      if (request === logRequest) logLoading.value = false
    }
  }

  async function loadOperations() {
    const request = ++operationRequest
    const { memberId, levelType } = logContext.value
    const response = await getOperations({ memberId, levelType })
    if (request === operationRequest) operations.value = response.data
  }

  function searchLogs() {
    logQuery.pageNum = 1
    appliedLogFilters = {
      opType: logQuery.opType || undefined,
      beginDate: logQuery.dateRange?.[0],
      endDate: logQuery.dateRange?.[1]
    }
    return loadLogs()
  }

  function resetLogs() {
    logQuery.opType = ''
    logQuery.dateRange = []
    return searchLogs()
  }

  async function openLogs(level) {
    if (!selectedMember.value) return
    closeLogs()
    logContext.value = {
      memberId: selectedMember.value.memberId,
      memberName: selectedMember.value.nickName || selectedMember.value.userName || selectedMember.value.memberId,
      levelType: level.levelType,
      levelTypeName: level.levelTypeName
    }
    logQuery.opType = ''
    logQuery.dateRange = []
    logQuery.pageNum = 1
    appliedLogFilters = {}
    logOpen.value = true
    await Promise.all([loadLogs(), loadOperations()])
  }

  return {
    members, memberTotal, memberLoading, selectedMember, levels, levelLoading, memberQuery,
    logOpen, logContext, logs, logTotal, logLoading, operations, logQuery,
    loadMembers, selectMember, openLogs, closeLogs, loadLogs, searchLogs, resetLogs
  }
}
