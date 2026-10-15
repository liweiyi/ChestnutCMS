import { reactive, ref } from 'vue'

export default function useMemberFavorites({ getMembers, getFavorites }) {
  const members = ref([])
  const memberTotal = ref(0)
  const memberLoading = ref(false)
  const selectedMember = ref()
  const memberQuery = reactive({ memberId: '', keyword: '', pageNum: 1, pageSize: 10 })
  const favorites = ref([])
  const favoriteTotal = ref(0)
  const favoriteLoading = ref(false)
  const favoriteQuery = reactive({ pageNum: 1, pageSize: 10 })
  let memberRequest = 0
  let favoriteRequest = 0

  function clearSelection() {
    favoriteRequest++
    selectedMember.value = undefined
    favorites.value = []
    favoriteTotal.value = 0
    favoriteLoading.value = false
    favoriteQuery.pageNum = 1
  }

  async function loadFavorites() {
    if (!selectedMember.value) return
    const request = ++favoriteRequest
    const memberId = selectedMember.value.memberId
    favoriteLoading.value = true
    try {
      const response = await getFavorites({ memberId, ...favoriteQuery })
      if (request !== favoriteRequest) return
      favorites.value = response.data.rows
      favoriteTotal.value = Number(response.data.total)
    } finally {
      if (request === favoriteRequest) favoriteLoading.value = false
    }
  }

  async function selectMember(member) {
    if (!member || selectedMember.value?.memberId === member.memberId) return
    clearSelection()
    selectedMember.value = { ...member }
    await loadFavorites()
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

  return {
    members, memberTotal, memberLoading, selectedMember, memberQuery,
    favorites, favoriteTotal, favoriteLoading, favoriteQuery,
    loadMembers, selectMember, loadFavorites
  }
}
