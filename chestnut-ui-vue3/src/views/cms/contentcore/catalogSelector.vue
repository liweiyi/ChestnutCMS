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
            <el-radio-button :label="1">{{ $t('CMS.Catalog.CopyContent') }}</el-radio-button>
            <el-radio-button :label="2">{{ $t('CMS.Catalog.MappingContent') }}</el-radio-button>
          </el-radio-group>
          <el-tooltip placement="right">
            <template #content>
              {{ $t('CMS.Catalog.CopyContentTip') }}<br/>
              {{ $t('CMS.Catalog.MappingContentTip') }}
            </template>
            <el-icon class="ml5"><InfoFilled /></el-icon>
          </el-tooltip>
        </div>
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

const { proxy } = getCurrentInstance();

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
const filterCatalogName = ref(undefined);
const catalogOptions = ref([]);
const siteName = ref("");
const rootSelected = ref(false);
const selectedCatalogs = ref([]);
const copyType = ref(1);
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
  proxy.$refs.tree.filter(newVal);
});

function loadCatalogTreeData () {
  selectedCatalogs.value = [];
  rootSelected.value = false;
  loading.value = true;
  getCatalogTreeData({disableLink: props.disableLink}).then(response => {
    catalogOptions.value = response.data.rows;
    siteName.value = response.data.siteName; 
    loading.value = false;
  });
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
  setNodeHighlight()
  emit("ok", [ selectedCatalogs.value, copyType.value ]);
}

function handleCancel () {
  emit("close");
  selectedCatalogs.value = [];
  copyType.value = 1;
}
</script>
<style scoped>
.catalog-selector .el-dialog__body {
  padding: 10px 20px;
}
.catalog-selector .header-toolbar {
  margin-bottom: 10px;
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