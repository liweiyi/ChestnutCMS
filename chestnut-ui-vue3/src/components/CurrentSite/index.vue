<template>
  <div v-if="showCurrentSite">
    <el-button type="primary" icon="HomeFilled" @click="handleOpenSiteList" plain>{{ currentSiteName }}</el-button>
    <el-dialog :title="$t('CMS.Site.CurrentSelectorTitle')" v-model="open" :modal-append-to-body='false' width="800px">
      <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="68px" class="el-form-search">
        <el-form-item prop="siteName">
          <el-input :placeholder="$t('CMS.Site.Name')" v-model="queryParams.siteName" :size="size"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" :size="size" @click="handleQuery">{{ $t("Common.Search")
            }}</el-button>
          <el-button icon="Refresh" :size="size" @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="siteList" height="460" highlight-current-row @current-change="handleSelectionChange"
        @row-dblclick="handleRowDblclick" style="width:100%;line-height: normal;">
        <el-table-column type="index" :label="$t('Common.RowNo')" width="50" />
        <el-table-column :label="$t('CMS.Site.Name')" prop="name">
          <template #default="scope">
            <div class="name-wrapper">
              <el-tag v-if="scope.row.siteId == currentSite" :size="size">{{ scope.row.name }}</el-tag>
              <span v-else>{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="$t('CMS.Site.Path')" width="260" prop="path" />
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="handleChangeCurrentSite" :size="size" :loading="changing">{{ $t("Common.Confirm") }}</el-button>
          <el-button @click="cancel" :size="size">{{ $t("Common.Cancel") }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import useAppStore from "@/store/modules/app"
import useSiteStore from "@/store/modules/site"
import { getSelectSites } from "@/api/contentcore/site"

const appStore = useAppStore()
const siteStore = useSiteStore()
const size = computed(() => appStore.size)

defineOptions({
  name: 'CurrentSite'
})

const { proxy } = getCurrentInstance()

const loading = ref(true)
const changing = ref(false)
const selectedRow = ref(undefined)
const total = ref(0)
const siteList = ref([])
const open = ref(false)
const currentSite = computed(() => siteStore.siteId)
const currentSiteName = computed(() => siteStore.siteName || proxy.$t('CMS.Site.CurrentSelectorTitle'))
const showCurrentSite = computed(() => siteStore.enabled === true)
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  siteName: undefined
})

const queryForm = ref(null)

const handleChangeCurrentSite = async () => {
  if (changing.value) {
    return
  }
  if (selectedRow.value == undefined) {
    proxy.$modal.msgError(proxy.$t('Common.SelectFirst'))
    return
  }
  if (currentSite.value == selectedRow.value.siteId) {
    proxy.$modal.msgError(proxy.$t('CMS.Site.AlreayCurrentSite'))
    return
  }
  changing.value = true
  try {
    if (!await siteStore.changeSite(selectedRow.value.siteId)) return
    open.value = false
    refreshView()
  } catch {
    // 请求失败时保留当前站点和选择弹窗，错误提示由请求层统一处理。
  } finally {
    changing.value = false
  }
}

const loadSiteList = () => {
  loading.value = true
  getSelectSites(queryParams).then(response => {
    siteList.value = response.data.rows
    total.value = parseInt(response.data.total)
  }).catch(() => {
    // 错误提示由请求层统一处理。
  }).finally(() => {
    loading.value = false
  })
}

const handleQuery = () => {
  queryParams.pageNum = 1
  loadSiteList()
}

const resetQuery = () => {
  proxy.resetForm("queryForm")
  handleQuery()
}

const handleSelectionChange = (currentRow) => {
  selectedRow.value = currentRow
}

const handleRowDblclick = (row) => {
  selectedRow.value = row
  handleChangeCurrentSite()
}

const handleOpenSiteList = () => {
  selectedRow.value = undefined
  open.value = true
  loadSiteList()
}

const cancel = () => {
  open.value = false
}

const refreshView = () => {
  setTimeout(() => window.location.reload(), 200)
}
</script>
<style lang="scss" scoped>
#current-site {
  :deep(.el-dialog__body) {
    padding-top: 0;
  }

  :deep(.el-form-item) {
    margin-bottom: 0;
  }
}
</style>
