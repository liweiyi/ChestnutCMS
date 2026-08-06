<template>
  <div id="catalog-selector">
    <el-dialog 
      :title="$t('CMS.Catalog.SelectCatalog')"
      v-model="props.open"
      width="450px"
      :close-on-click-modal="false"
      append-to-body
      class="catalog-selector"
      style="padding: 10px 20x;">
      <div v-if="props.showCopyToolbar" class="header-toolbar">
        <div v-if="showCopyToolbar" style="display: flex;align-items: center;">
          <el-radio-group v-model="copyType">
            <el-radio-button v-for="item in CMSContentCopyType" :key="item.value" :label="item.value">{{ item.label }}</el-radio-button>
          </el-radio-group>
          <el-tooltip placement="right">
            <template #content>
              {{ $t('CMS.Catalog.CopyContentTip') }}<br/>
              {{ $t('CMS.Catalog.MappingContentTip') }}<br/>
              {{ $t('CMS.Catalog.CrossSiteCopyTip') }}
            </template>
            <el-icon class="ml5"><InfoFilled /></el-icon>
          </el-tooltip>
        </div>
        <el-select
          v-if="isCrossSiteCopy"
          v-model="targetSiteId"
          class="site-selector"
          :placeholder="$t('CMS.Catalog.SelectTargetSite')"
          filterable
          remote
          clearable
          :remote-method="loadSiteOptions"
          :loading="siteLoading"
          @change="handleTargetSiteChange">
          <el-option
            v-for="site in siteOptions"
            :key="site.siteId"
            :label="site.name"
            :value="site.siteId"
            :disabled="isCurrentSite(site.siteId)" />
        </el-select>
      </div>
      <div class="search-toolbar">
        <el-input 
          :placeholder="$t('CMS.Catalog.CatalogNamePlaceholder')"
          v-model="filterCatalogName"
          clearable
          suffix-icon="Search">
        </el-input>
      </div>
      <div class="tree-container">
        <el-scrollbar style="height: 400px;">
          <el-button 
            v-if="showRootNode"
            type="text" 
            :class="'tree-root' + (rootSelected?' cc-current':'')"
            icon="HomeFilled"
            @click="handleTreeRootClick">{{ siteName }}</el-button>
          <el-tree 
            :data="catalogOptions" 
            :props="defaultProps" 
            :expand-on-click-node="false"
            :filter-node-method="filterNode"
            :show-checkbox="multiple"
            :check-strictly="checkStrictly"
            v-loading="loading"
            node-key="id"
            ref="tree"
            default-expand-all
            @node-click="handleNodeClick">
            <template #default="{ node, data }">
              <span :id="'tn-'+node.id" :class="node.disabled?'cc-disabled':''">{{ node.label }}</span>
            </template>
          </el-tree>
        </el-scrollbar>
      </div>
      <div class="dialog-footer">
        <el-button type="primary" @click="handleOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleCancel">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script setup name="CMSCatalogSelector">
import { getCatalogTreeData } from "@/api/contentcore/catalog";
import { getSelectSites } from "@/api/contentcore/site";

const { proxy } = getCurrentInstance();

const { CMSContentCopyType } = proxy.useDict('CMSContentCopyType');

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
    required: true
  },
  // 是否显示复制内容工具栏
  showCopyToolbar: {
    type: Boolean,
    default: false,
    required: false
  },
  // 是否显示站点根节点
  showRootNode: {
    type: Boolean,
    default: false,
    required: false
  },
  // 是否多选
  multiple: {
    type: Boolean,
    default: false,
    required: false
  },
  checkStrictly: {
    type: Boolean,
    default: true,
    required: false
  },
  // 是否不允许选择链接栏目
  disableLink: {
    type: Boolean,
    default: false,
    required: false
  }
})

const emit = defineEmits(['ok', 'close']);

const loading = ref(false);
const siteLoading = ref(false);
const filterCatalogName = ref(undefined);
const catalogOptions = ref([]);
const siteOptions = ref([]);
const siteName = ref("");
const rootSelected = ref(false);
const selectedCatalogs = ref([]);
const copyType = ref('1');
const targetSiteId = ref(undefined);
const currentSiteId = computed(() => proxy.$cms.getCurrentSite());
const isCrossSiteCopy = computed(() => copyType.value === '3');
let treeRequestId = 0;
let siteRequestId = 0;
const defaultProps = ref({
  children: "children",
  label: "label"
});


watch(() => props.open, (newVal) => {
  if (!newVal) {
    handleCancel();
  } else {
    loadCatalogTreeData();
  }
});

watch(filterCatalogName, (newVal) => {
  proxy.$refs.tree?.filter(newVal);
});

watch(copyType, (newVal) => {
  if (!props.open) {
    return;
  }
  resetCatalogSelection();
  if (newVal === '3') {
    targetSiteId.value = undefined;
    siteName.value = "";
    catalogOptions.value = [];
    treeRequestId++;
    loading.value = false;
    loadSiteOptions();
  } else {
    targetSiteId.value = undefined;
    loadCatalogTreeData();
  }
});

function loadCatalogTreeData (siteId) {
  resetCatalogSelection();
  const requestId = ++treeRequestId;
  loading.value = true;
  const params = {disableLink: props.disableLink};
  if (siteId) {
    params.siteId = siteId;
  }
  getCatalogTreeData(params).then(response => {
    if (requestId !== treeRequestId) {
      return;
    }
    catalogOptions.value = response.data.rows;
    siteName.value = response.data.siteName;
  }).finally(() => {
    if (requestId === treeRequestId) {
      loading.value = false;
    }
  });
}

function loadSiteOptions (keyword) {
  const requestId = ++siteRequestId;
  siteLoading.value = true;
  getSelectSites({
    siteName: keyword || undefined,
    pageNum: 1,
    pageSize: 100
  }).then(response => {
    if (requestId === siteRequestId) {
      siteOptions.value = response.data.rows || [];
    }
  }).finally(() => {
    if (requestId === siteRequestId) {
      siteLoading.value = false;
    }
  });
}

function isCurrentSite(siteId) {
  return String(siteId) === String(currentSiteId.value);
}

function handleTargetSiteChange(siteId) {
  catalogOptions.value = [];
  siteName.value = "";
  if (siteId && !isCurrentSite(siteId)) {
    loadCatalogTreeData(siteId);
  } else {
    treeRequestId++;
    loading.value = false;
    resetCatalogSelection();
  }
}

function resetCatalogSelection() {
  selectedCatalogs.value = [];
  rootSelected.value = false;
  filterCatalogName.value = undefined;
  proxy.$refs.tree?.setCurrentKey(null);
}
function filterNode (value, data) {
  if (!value) return true;
  return data.label.indexOf(value) > -1;
}

function setNodeHighlight(node) {
  document.querySelectorAll(".cc-current").forEach(item => item.classList.remove("cc-current"));
  if (node) {
    document.querySelector("#tn-"+node.id).classList.add("cc-current");
  }
}

function handleNodeClick (data, node) {
  if (!props.multiple) {
    if (!props.disableLink || !data.disabled) {
      setNodeHighlight(node)
      selectedCatalogs.value = [{ id: data.id, name: data.label, props: data.props }];
      rootSelected.value = false;
    } else {
      proxy.$refs.tree.setCurrentKey(null)
    }
  }
}

function handleTreeRootClick(e) {
  if (!props.multiple) {
    selectedCatalogs.value = [{ id: "0", name: siteName.value, props: {} }];
    proxy.$refs.tree.setCurrentKey(null);
    rootSelected.value = true;
  }
}

function handleOk (data) {
  if (isCrossSiteCopy.value && !targetSiteId.value) {
    proxy.$modal.alertWarning(proxy.$t('CMS.Catalog.SelectTargetSite'));
    return;
  }
  if (props.multiple) {
    selectedCatalogs.value = [];
    proxy.$refs.tree.getCheckedNodes().map(item => {
      selectedCatalogs.value.push({ id: item.id, name: item.label, props: data.props });
    })
  }
  if (selectedCatalogs.value.length == 0) {
    proxy.$modal.alertWarning(proxy.$t('CMS.Catalog.SelectCatalogFirst'));
    return;
  }
  console.log(selectedCatalogs.value);
  setNodeHighlight()
  emit("ok", { selectedCatalogs: selectedCatalogs.value, copyType: copyType.value });
}

function handleCancel () {
  emit("close");
  resetCatalogSelection();
  treeRequestId++;
  siteRequestId++;
  loading.value = false;
  siteLoading.value = false;
  targetSiteId.value = undefined;
  siteOptions.value = [];
  catalogOptions.value = [];
  siteName.value = "";
  copyType.value = '1';
}
</script>
<style scoped>
.catalog-selector .el-dialog__body {
  padding: 10px 20px;
}
.catalog-selector .header-toolbar {
  margin-bottom: 10px;
}
.catalog-selector .header-toolbar .site-selector {
  width: 100%;
  margin-top: 10px;
}
.catalog-selector .tree-container {
  margin: 10px 0;
}
.catalog-selector .tree-container .el-tree {
  height: 400px;
}
.catalog-selector .tree-container .el-scrollbar__wrap {
  overflow-x: hidden;
}
.catalog-selector .tree-container .tree-root {
  width: 100%;
  text-align: left;
}
.catalog-selector .tree-container .tree-root {
  width: 100%;
  text-align: left;
  border-radius: 0;
  padding: 5px;
}
.catalog-selector .tree-container .tree-root:hover {
  background-color: #F5F7FA;
}
.catalog-selector .tree-container .cc-current {
  color: #409EFF;
}
.catalog-selector .tree-container .cc-disabled {
  color: #C0C4CC;
  cursor: not-allowed;
}
</style>
