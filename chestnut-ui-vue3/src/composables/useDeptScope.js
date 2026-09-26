import { computed, nextTick, ref } from 'vue'
import { deptTree } from '@/api/system/dept'
import useUserStore from '@/store/modules/user'

export function useDeptScope(queryParams, proxy, getList) {
  const deptOptions = ref([])
  const deptName = ref('')
  const defaultProps = { value: 'id', children: 'children', label: 'label' }
  const onlyCurrentDisabled = computed(() => !queryParams.value.deptId || String(queryParams.value.deptId) === '0')
  const filterNode = (value, data) => !value || data.label.includes(value)
  const handleNodeClick = (node) => {
    queryParams.value.deptId = String(node.id)
    queryParams.value.pageNum = 1
    getList()
  }
  async function getDeptTree() {
    const response = await deptTree()
    deptOptions.value = response.data
    const store = useUserStore()
    queryParams.value.deptId = store.deptId ? String(store.deptId) : undefined
    await nextTick()
    proxy.$refs.treeRef?.setCurrentKey(queryParams.value.deptId)
    await getList()
  }
  return { deptOptions, deptName, defaultProps, onlyCurrentDisabled, filterNode, handleNodeClick, getDeptTree }
}
