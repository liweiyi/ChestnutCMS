<template>
  <div class="app-container">
    <el-row :gutter="24" class="mb8">
      <el-col :span="12">
        <el-row :gutter="10">
          <el-col :span="1.5">
            <el-button 
              plain 
              type="primary" 
              icon="Plus" 
              @click="handleAdd()"
              v-hasPermi="[ $p('cms:site:add') ]"
            >{{ $t("Common.Add") }}</el-button>
          </el-col>
        </el-row>
      </el-col>
      <el-col :span="12" style="text-align: right">
        <el-form
          :model="queryParams"
          ref="queryFormRef"
          :inline="true"
          :disabled="siteListLoading || siteOrderSubmitting"
          class="el-form-search"
        >
          <el-form-item prop="siteName">
            <el-input :placeholder="$t('CMS.Site.Name')" v-model="queryParams.siteName" />
          </el-form-item>
          <el-form-item>
            <el-button-group>
              <el-button
                type="primary"
                icon="Search"
                @click="handleQuery"
                >{{ $t("Common.Search") }}</el-button>
              <el-button icon="Refresh" @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
            </el-button-group>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
    <el-table
      :key="tableVersion"
      ref="siteTableRef"
      v-loading="siteListLoading"
      :data="siteList"
      row-key="siteId"
      :lazy="!activeSiteName"
      :load="loadChildSites"
      :tree-props="{ hasChildren: 'childCount' }"
      style="width: 100%; line-height: normal"
    >
      <el-table-column :label="$t('CMS.Site.Name')">
        <template #default="{ row }">
          <svg-icon
            v-if="!activeSiteName && canDragSite(row)"
            icon-class="drag"
            class="site-sort-handle cursor-move mr5"
            :title="$t('Common.Sort')"
            :data-site-id="row.siteId"
            :data-parent-id="row.parentId"
          />
          <el-link type="primary" class="link-type" @click="handleEdit(row)">
            {{ row.name }}
          </el-link>
          <span
            v-if="row.topFlag > 0"
            class="ml5"
            role="img"
            :aria-label="$t('CMS.Content.SetTop')"
            :title="row.topDate || $t('CMS.Content.SetTop')"
          ><svg-icon icon-class="top" /></span>
        </template>
      </el-table-column>
      <el-table-column label="ID" width="200" prop="siteId" />
      <el-table-column :label="$t('CMS.Site.SortFlag')" width="145" prop="sortFlag" />
      <el-table-column :label="$t('CMS.Site.Path')" prop="path" />
      <el-table-column
        :label="$t('Common.Operation')"
        align="center"
        width="560"
      >
        <template #default="scope" class="permi-wrap">
          <el-button
            link
            type="primary"
            icon="View"
            @click="handlePreview(scope.row)"
          >{{ $t("CMS.ContentCore.Preview") }}</el-button>
          <el-button
            link
            type="primary"
            icon="Promotion"
            v-hasPermi="[$p('Site:Publish:{0}', [scope.row.siteId])]"
            @click="handlePublish(scope.row)"
          >{{ $t("CMS.Site.PublishHome") }}</el-button>
          <el-button
            link
            type="success"
            icon="Edit"
            v-hasPermi="[$p('Site:Edit:{0}', [scope.row.siteId])]"
            @click="handleEdit(scope.row)"
          >{{ $t("Common.Edit") }}</el-button>
          <el-button
            v-if="canEditSite(scope.row)"
            link
            type="primary"
            icon="Plus"
            @click="handleAdd(scope.row)"
            v-hasPermi="[$p('cms:site:add')]"
          >{{ $t('Common.Add') }}</el-button>
          <el-button
            v-if="canEditSite(scope.row)"
            link
            type="primary"
            :disabled="siteOrderSubmitting || moveOptionsLoading"
            @click="handleMove(scope.row)"
          >{{ $t('CMS.Site.Move') }}</el-button>
          <el-button
            v-if="canEditSite(scope.row)"
            link
            type="primary"
            :disabled="siteOrderSubmitting"
            @click="handleSetTop(scope.row)"
          >{{ $t('CMS.Content.SetTop') }}</el-button>
          <el-button
            v-if="scope.row.topFlag > 0 && canEditSite(scope.row)"
            link
            type="primary"
            :disabled="siteOrderSubmitting"
            @click="handleCancelTop(scope.row)"
          >{{ $t('CMS.Content.CancelTop') }}</el-button>
          <el-button
            link
            type="danger"
            icon="Delete"
            :disabled="scope.row.childCount > 0"
            v-hasPermi="[$p('Site:Delete:{0}', [scope.row.siteId])]"
            @click="handleDelete(scope.row)"
          >{{ $t("Common.Delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 添加站点对话框 -->
    <el-dialog
      :title="$t('CMS.Site.Dialog.AddTitle')"
      v-model="open"
      width="600px"
      append-to-body
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="135px"
        class="el-form-dialog"
      >
        <el-form-item v-if="parentSite" :label="$t('CMS.Site.ParentSite')">
          <el-tag>{{ parentSite.name }}</el-tag>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Name')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Path')" prop="path">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.ResourceUrl')" prop="resourceUrl">
          <el-input v-model="form.resourceUrl" />
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Desc')" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :maxlength="300"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" :loading="siteAdding" @click="handleAddSave">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
    <el-dialog
      v-model="moveDialogVisible"
      :title="$t('CMS.Site.Move')"
      :close-on-click-modal="false"
      :close-on-press-escape="!siteOrderSubmitting"
      :show-close="!siteOrderSubmitting"
      width="480px"
      append-to-body
    >
      <el-form label-width="auto" :disabled="siteOrderSubmitting">
        <el-form-item :label="$t('CMS.Site.ParentSite')">
          <el-select
            v-model="moveParentId"
            :loading="moveOptionsLoading"
            :placeholder="$t('CMS.Site.MoveParentPlaceholder')"
            clearable
            filterable
          >
            <el-option
              v-for="site in moveOptions"
              :key="site.siteId"
              :label="site.name"
              :value="String(site.siteId)"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button
          type="primary"
          :loading="siteOrderSubmitting"
          :disabled="moveOptionsLoading || moveUnchanged"
          @click="submitMove"
        >{{ $t('Common.Confirm') }}</el-button>
        <el-button :disabled="siteOrderSubmitting" @click="moveDialogVisible = false">
          {{ $t('Common.Cancel') }}
        </el-button>
      </template>
    </el-dialog>
    <el-dialog
      v-model="topDialogVisible"
      :title="$t('CMS.Content.SetTop')"
      :close-on-click-modal="false"
      :close-on-press-escape="!siteOrderSubmitting"
      :show-close="!siteOrderSubmitting"
      width="460px"
      append-to-body
    >
      <el-form :model="topForm" label-width="auto" :disabled="siteOrderSubmitting">
        <el-form-item :label="$t('CMS.Content.TopEndTime')" prop="topEndTime">
          <el-date-picker
            v-model="topForm.topEndTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled-date="disablePastDate"
            clearable
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" :loading="siteOrderSubmitting" @click="submitTop">
          {{ $t('Common.Confirm') }}
        </el-button>
        <el-button :disabled="siteOrderSubmitting" @click="topDialogVisible = false">
          {{ $t('Common.Cancel') }}
        </el-button>
      </template>
    </el-dialog>
    <!-- 进度条 -->
    <cms-progress
      :title="$t('CMS.Site.DeleteProgressTitle')"
      v-model:open="openProgress"
      :taskId="taskId"
      @close="handleCloseProgress"
    ></cms-progress>
  </div>
</template>
<script setup name="CmsContentcoreSite">
import Sortable from 'sortablejs'
import { checkPermi } from '@/utils/permission'
import {
  cancelTopSite,
  delSite,
  addSite,
  listChildSites,
  listSite,
  moveSite,
  publishSite,
  setTopSite,
  sortSite,
} from "@/api/contentcore/site";
import CmsProgress from "@/views/components/Progress";

const { proxy } = getCurrentInstance();

const open = ref(false)
const siteList = ref([])
const siteListLoading = ref(true)
const siteOrderSubmitting = ref(false)
const siteAdding = ref(false)
const siteTableRef = ref()
const tableVersion = ref(0)
const activeSiteName = ref('')
const parentSite = ref(null)
const loadedBranches = new Map()
const moveDialogVisible = ref(false)
const moveOptionsLoading = ref(false)
const moveOptions = ref([])
const moveSource = ref(null)
const moveParentId = ref(null)
const moveUnchanged = computed(() => String(moveParentId.value || '0') === String(moveSource.value?.parentId))
const topDialogVisible = ref(false)
const topForm = ref({ siteId: undefined, topEndTime: null })
const openProgress = ref(false)
const taskId = ref("")
let sortable
const objects = reactive({
  queryParams: {
    siteName: undefined,
  },
  form: {},
  rules: {
    name: [
      { required: true, message: proxy.$t("Common.RuleTips.NotEmpty"), trigger: "blur" },
    ],
    path: [
      { required: true, message: proxy.$t("Common.RuleTips.NotEmpty"), trigger: "blur" },
    ]
  }
});
const { queryParams, form, rules } = toRefs(objects)

onMounted(() => loadSiteList())
watch(siteTableRef, table => {
  sortable?.destroy()
  sortable = undefined
  const tbody = table?.$el.querySelector('.el-table__body-wrapper tbody')
  if (tbody) {
    sortable = Sortable.create(tbody, {
      handle: '.site-sort-handle',
      draggable: '.el-table__row',
      ghostClass: 'sortable-ghost',
      onMove: canMoveSite,
      onEnd: handleDragEnd
    })
  }
}, { flush: 'post' })
onBeforeUnmount(() => sortable?.destroy())

async function loadSiteList(siteName = activeSiteName.value, resetTree = false) {
  siteListLoading.value = true
  try {
    const response = siteName ? await listSite({ siteName }) : await listChildSites()
    if (resetTree) {
      loadedBranches.clear()
      tableVersion.value++
    }
    activeSiteName.value = siteName
    siteList.value = response.data.rows
  } catch {
    // 请求层已提示加载失败。
  } finally {
    siteListLoading.value = false
  }
}

async function loadChildSites(row, node, resolve) {
  const version = tableVersion.value
  try {
    const { data } = await listChildSites(row.siteId)
    if (version === tableVersion.value) {
      loadedBranches.set(String(row.siteId), data.rows.length)
      resolve(data.rows)
    }
  } catch {
    node.loading = false
  }
}

async function refreshSiteSiblings(parentId) {
  if (activeSiteName.value || String(parentId) === '0') {
    await loadSiteList()
    return
  }
  try {
    const { data } = await listChildSites(parentId)
    siteTableRef.value?.updateKeyChildren(String(parentId), data.rows)
    loadedBranches.set(String(parentId), data.rows.length)
  } catch {
    // 请求层已提示加载失败。
  }
}

function canEditSite(row) {
  return checkPermi([proxy.$p('Site:Edit:{0}', [row.siteId])])
}

function canDragSite(row) {
  return !(row.topFlag > 0) && canEditSite(row)
}

function canMoveSite({ dragged, related }) {
  const source = dragged.querySelector('.site-sort-handle')
  const target = related.querySelector('.site-sort-handle')
  return !siteListLoading.value && !siteOrderSubmitting.value
    && !!source && !!target && source.dataset.parentId === target.dataset.parentId
}

async function handleDragEnd({ from, item, oldIndex, newIndex }) {
  if (oldIndex === newIndex) {
    return
  }
  // 恢复 Sortable 移动的 DOM，表格在请求完成后由 Vue 按后端顺序更新。
  from.removeChild(item)
  from.insertBefore(item, from.children[oldIndex] ?? null)

  const source = item.querySelector('.site-sort-handle')?.dataset
  const target = from.children[newIndex]?.querySelector('.site-sort-handle')?.dataset
  if (siteListLoading.value || siteOrderSubmitting.value || !source || !target
      || source.parentId !== target.parentId) {
    return
  }
  siteOrderSubmitting.value = true
  try {
    await sortSite({
      siteId: source.siteId,
      targetSiteId: target.siteId,
      beforeTarget: newIndex < oldIndex
    })
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'))
  } catch {
    // 请求层已提示排序失败。
  } finally {
    await refreshSiteSiblings(source.parentId)
    siteOrderSubmitting.value = false
  }
}

async function handleMove(row) {
  if (moveOptionsLoading.value || siteOrderSubmitting.value) return
  moveSource.value = row
  moveParentId.value = row.parentId > 0 ? String(row.parentId) : null
  moveOptions.value = []
  moveDialogVisible.value = true
  moveOptionsLoading.value = true
  try {
    const { data } = await listSite()
    moveOptions.value = data.rows.filter(site => String(site.siteId) !== String(row.siteId)
      && !site.ancestors?.startsWith(row.ancestors + ',') && canEditSite(site))
  } finally {
    moveOptionsLoading.value = false
  }
}

async function submitMove() {
  if (!moveSource.value || moveUnchanged.value || moveOptionsLoading.value || siteOrderSubmitting.value) return
  siteOrderSubmitting.value = true
  try {
    await moveSite({ siteId: moveSource.value.siteId, parentId: moveParentId.value || null })
    moveDialogVisible.value = false
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'))
    await loadSiteList(activeSiteName.value, true)
  } finally {
    siteOrderSubmitting.value = false
  }
}

function handleSetTop(row) {
  topForm.value = { siteId: row.siteId, parentId: row.parentId, topEndTime: row.topDate ?? null }
  topDialogVisible.value = true
}

function disablePastDate(date) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date < today
}

async function submitTop() {
  if (siteOrderSubmitting.value) return
  siteOrderSubmitting.value = true
  try {
    const { siteId, parentId, topEndTime } = topForm.value
    await setTopSite(siteId, { topEndTime: topEndTime || null })
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'))
    topDialogVisible.value = false
    await refreshSiteSiblings(parentId)
  } finally {
    siteOrderSubmitting.value = false
  }
}

async function handleCancelTop(row) {
  if (siteOrderSubmitting.value) return
  siteOrderSubmitting.value = true
  try {
    await cancelTopSite(row.siteId)
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'))
    await refreshSiteSiblings(row.parentId)
  } finally {
    siteOrderSubmitting.value = false
  }
}

const handleQuery = () => {
  loadSiteList(queryParams.value.siteName?.trim() || '', true)
}
const resetQuery = () => {
  proxy.resetForm("queryFormRef");
  handleQuery();
}
const cancel = () => {
  open.value = false;
}
const handleAdd = (parent = null) => {
  proxy.resetForm("formRef");
  parentSite.value = parent
  form.value = { parentId: parent?.siteId ?? 0 }
  open.value = true;
}
async function handleAddSave() {
  if (siteAdding.value) return
  const valid = await proxy.$refs.formRef.validate().catch(() => false)
  if (!valid || siteAdding.value) return
  siteAdding.value = true
  const parent = parentSite.value
  try {
    await addSite(form.value)
    proxy.$modal.msgSuccess(proxy.$t('Common.AddSuccess'))
    open.value = false
    if (!proxy.$cms.getCurrentSite()) {
      proxy.$router.go(0)
    } else if (!parent || activeSiteName.value) {
      await loadSiteList()
    } else {
      parent.childCount++
      const count = loadedBranches.get(String(parent.siteId))
      if (count > 0) {
        await refreshSiteSiblings(parent.siteId)
      } else if (count === 0) {
        await loadSiteList('', true)
      }
    }
  } finally {
    siteAdding.value = false
  }
}
const handleEdit = (row) => {
  const siteId = row.siteId;
  proxy.$router.push({ path: "/cms/site/tabs", query: { siteId: siteId } });
}
const handleDelete = (row) => {
  const siteId = row.siteId;
  proxy.$modal.confirm(proxy.$t("Common.ConfirmDelete")).then(() => {
    return delSite(siteId);
  }).then(response => {
    if (proxy.$cms.getCurrentSite() == siteId) {
      proxy.$cms.clearCurrentSite();
    }
    taskId.value = response.data;
    openProgress.value = true;
  }).catch(() => {});
}
const handleCloseProgress = () => {
  proxy.$router.go(0);
}
const handlePreview = (row) => {
  const siteId = row.siteId;
  let routeData = proxy.$router.resolve({
    path: "/cms/preview",
    query: { type: "site", dataId: siteId },
  });
  window.open(routeData.href, "_blank");
}
const handlePublish = (row) => {
  publishSite({ siteId: row.siteId, publishIndex: true }).then(response => {
      proxy.$modal.msgSuccess(proxy.$t("CMS.ContentCore.PublishSuccess"));
  });
}
</script>
