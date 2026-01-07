<template>
  <div class="catalog-tree">
    <div class="head-container">
      <el-dropdown v-if="showNewBtn" placement="bottom-start" v-hasPermi="[ $p('Site:AddCatalog:{0}', [ siteId ]) ]" class="btn-add-catalog">
        <el-button 
          type="primary"
          link
          @click="handleAdd"
          icon="Plus">
          {{ $t('CMS.Catalog.AddCatalog') }}<el-icon class="el-icon--right"><ArrowDown /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click.native="handleAdd">{{ $t('CMS.Catalog.AddCatalog') }}</el-dropdown-item>
            <el-dropdown-item @click.native="handleBatchAdd">{{ $t('CMS.Catalog.BatchAddCatalog') }}</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <el-button type="text" icon="Refresh" @click="loadCatalogTreeData" style="float:right;margin-top:2px;">{{ $t("Common.Refresh") }}</el-button>
      <el-input 
        :placeholder="$t('CMS.Catalog.CatalogNamePlaceholder')"
        v-model="filterCatalogName"
        clearable
        suffix-icon="Search"
        style="margin-top: 3px;">
      </el-input>
    </div>
    <div class="tree-container">
      <el-button 
        :loading="loading"
        :title="siteName"
        type="text" 
        class="tree-header"
        icon="HomeFilled"
        @click="handleTreeRootClick">{{ siteName }}</el-button>
      <el-tree 
        :data="catalogOptions" 
        :props="defaultProps" 
        :expand-on-click-node="false"
        :default-expanded-keys="treeExpandedKeys"
        :filter-node-method="filterNode"
        :accordion="expandMode=='accordion'"
        node-key="id"
        ref="treeRef"
        highlight-current
        @node-click="handleNodeClick">
        <template #default="{ node, data }">
          <div class="tree-node">
            <div class="node-text" :title="node.label">{{ node.label }}</div>
            <div class="node-tool">
              <el-dropdown type="primary">
                <el-link :underline="false" class="row-more-btn" icon="More"></el-link>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click.native="handlePreview(data)"><svg-icon icon-class="eye-open" class="mr5"></svg-icon>{{ $t('CMS.ContentCore.Preview') }}</el-dropdown-item>
                    <div class="btn-permi" v-hasPermi="[ $p('Catalog:Publish:{0}', [ data.catalogId ]) ]">
                      <el-dropdown-item icon="Promotion" @click.native="handlePublish(data)">{{ $t('CMS.ContentCore.Publish') }}</el-dropdown-item>
                    </div>
                    <div class="btn-permi" v-hasPermi="[ $p('Catalog:Sort:{0}', [ data.catalogId ]) ]">
                      <el-dropdown-item icon="SortUp" @click.native="handleSortUp(data)">{{ $t('CMS.Catalog.SortUp') }}</el-dropdown-item>
                    </div>
                    <div class="btn-permi" v-hasPermi="[ $p('Catalog:Sort:{0}', [ data.catalogId ]) ]">
                      <el-dropdown-item icon="SortDown" @click.native="handleSortDown(data)">{{ $t('CMS.Catalog.SortDown') }}</el-dropdown-item>
                    </div>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </template>
      </el-tree>
    </div>
    <!-- 添加栏目对话框 -->
    <el-dialog 
      :title="$t('CMS.Catalog.AddCatalog')"
      v-model="diagOpen"
      :close-on-click-modal="false"
      width="600px"
      append-to-body>
      <el-form 
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px">
        <el-form-item :label="$t('CMS.Catalog.ParentCatalog')" prop="parentId">
          <el-tree-select v-model="form.parentId" :data="catalogOptions" :props="defaultProps" />
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.Name')"  prop="name">
          <el-input v-model="form.name" @blur="handleNameBlur" />
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.Alias')" prop="alias">
          <el-input v-model="form.alias" />
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.Path')" prop="path">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.CatalogType')"  prop="catalogType">
          <el-select v-model="form.catalogType">
            <el-option 
              v-for="ct in catalogTypeOptions"
              :key="ct.id"
              :label="ct.name"
              :value="ct.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleAddSave">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
    
    <el-dialog 
      :title="$t('CMS.Catalog.BatchAddCatalog')"
      v-model="openBatchAdd"
      :close-on-click-modal="false"
      :loading="loading"
      width="500px"
      append-to-body>
      <el-form 
        ref="formBatchRef"
        :model="formBatch"
        :rules="rulesBatch"
        label-position="top"
        label-width="70px">
        <el-form-item :label="$t('CMS.Catalog.ParentCatalog')" prop="parentId">
          <el-tree-select v-model="formBatch.parentId" :data="catalogOptions" :props="defaultProps" />
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.CatalogTree')" prop="catalogs">
          <el-input v-model="formBatch.catalogs" type="textarea" :rows="10" />
        </el-form-item>
      </el-form>
      <div style="background-color: #f4f4f5;color: #909399;font-size:12px;line-height: 30px;padding-left:10px;">
        <i class="el-icon-info mr5"></i>{{ $t("CMS.Catalog.BatchAddTip") }}
      </div>
      <template #footer>
        <el-button type="primary" @click="handleBatchAddSave">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="openBatchAdd=false">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
    
    <el-dialog 
      :title="$t('CMS.Catalog.PublishDialogTitle')"
      v-model="publishDialogVisible"
      width="500px"
      class="publish-dialog">
      <div>
        <p>{{ $t('Common.Tips') }}</p>
        <p>{{ $t('CMS.Catalog.PublishTips') }}</p>
        <el-checkbox v-model="publishChild">{{ $t('CMS.Catalog.ContainsChildren') }}</el-checkbox>
      </div>
      <template #footer>
        <el-button @click="publishDialogVisible = false">{{ $t("Common.Cancel") }}</el-button>
        <el-button type="primary" @click="handleDoPublish">{{ $t("Common.Confirm") }}</el-button>
      </template>
    </el-dialog>
    <!-- 进度条 -->
    <cms-progress :title="$t('CMS.Catalog.PublishProgressTitle')" v-model:open="openProgress" :taskId="taskId" @close="handleCloseProgress"></cms-progress>
  </div>
</template>
<script setup name="CMSCatalogTree">
import { codeValidator, pathValidator } from '@/utils/validate';
import { getCatalogTypes, getCatalogTreeData, addCatalog, batchAddCatalog, publishCatalog, sortCatalog, generateAliasAndPath } from "@/api/contentcore/catalog";
import CmsProgress from '@/views/components/Progress';

const { proxy } = getCurrentInstance();

const props = defineProps({
  newBtn: {
    type: Boolean,
    default: false,
    required: false
  }
})
const emit = defineEmits(['node-click']);

const siteId = ref(proxy.$cms.getCurrentSite());
const loading = ref(false);
// 是否显示新增栏目按钮
const showNewBtn = ref(props.newBtn);
// 是否显示弹出层
const diagOpen = ref(false);
// 栏目类型
const catalogTypeOptions = ref([]);
// 栏目树过滤：栏目名称
const filterCatalogName = ref("");
// 栏目树数据
const catalogOptions = ref([]);
// 站点名称
const siteName = ref("");
const expandMode = ref("");
// 当前选中栏目ID
const selectedCatalogId = ref("");
const treeExpandedKeys = ref([]);
const defaultProps = ref({
  value: "id",
  children: "children",
  label: "label"
});
const objects = reactive({
  form: {},
  formBatch: {},
});
const { form, formBatch } = toRefs(objects);
const rules = {
  name: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    { max: 100, messag: proxy.$t('Common.RuleTips.MaxLength', [ 100 ]), trigger: "change" }
  ],
  alias: [
    { required: true, message: proxy.$t("Common.RuleTips.NotEmpty"), trigger: "blur" },
    { validator: codeValidator, trigger: "change" },
    { max: 100, messag: proxy.$t('Common.RuleTips.MaxLength', [ 100 ]), trigger: "change" }
  ],
  path: [
    { required: true, message: proxy.$t("Common.RuleTips.NotEmpty"), trigger: "blur" },
    { validator: pathValidator, trigger: "change" },
    { max: 255, messag: proxy.$t('Common.RuleTips.MaxLength', [ 255 ]), trigger: "change" }
  ],
  catalogType: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ]
}
const openBatchAdd = ref(false);
const rulesBatch = {
  catalogs: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ]
}
const publishCatalogId = ref(0);
const publishDialogVisible = ref(false);
const publishChild = ref(false);
const openProgress = ref(false);
const taskId = ref("");


watch(filterCatalogName, (newVal) => {
  proxy.$refs.treeRef.filter(newVal);
});

onMounted(() => {
  loadCatalogTreeData();
  // 加载栏目类型数据
  getCatalogTypes().then(response => {
    catalogTypeOptions.value = response.data;
  });
})

const loadCatalogTreeData = () => {
  loading.value = true;
  getCatalogTreeData().then(response => {
    catalogOptions.value = response.data.rows;
    if (catalogOptions.value.length == 0) {
      proxy.$cms.clearLastSelectedCatalog();
    }
    siteName.value = response.data.siteName;
    expandMode.value = response.data.expandMode;
    loading.value = false;
    nextTick(() => {
      selectedCatalogId.value = proxy.$cms.getLastSelectedCatalog();
      if (selectedCatalogId.value && proxy.$refs.treeRef) {
        proxy.$refs.treeRef.setCurrentKey(selectedCatalogId.value);
        treeExpandedKeys.value = [selectedCatalogId.value];
        emit("node-click", proxy.$refs.treeRef.getCurrentNode());
      } else {
        emit("node-click", null);
      }
    })
  });
}

const filterNode = (value, data) => {
  if (!value) return true;
  return data.label.indexOf(value) > -1;
}

const handleTreeRootClick = () => {
  selectedCatalogId.value = undefined;
  proxy.$cms.clearLastSelectedCatalog();
  proxy.$refs.treeRef.setCurrentKey(null);
  emit("node-click", null);
}

const handleNodeClick = (data) => {
  selectedCatalogId.value = data.id;
  proxy.$cms.setLastSelectedCatalog(selectedCatalogId.value);
  emit("node-click", data);
}

const cancel = () => {
  diagOpen.value = false;
  resetAddForm();
}

const resetAddForm = () => {
  proxy.resetForm("form");
}

const handleNameBlur = () => {
  let data = openBatchAdd.value ? formBatch.value : form.value;
  if (!data.name || data.name.length == 0) {
    return;
  }
  if (!data.alias || data.alias.length == 0 || !data.path || data.path.length == 0) {
    generateAliasAndPath({ parentId: data.parentId, name: data.name }).then(response => {
      if (!data.alias || data.alias.length == 0) {
        data.alias = response.data.alias;
      }
      if (!data.path || data.path.length == 0) {
        data.path = response.data.path;
      }
    })
  }
}

const handleAdd = () => {
  resetAddForm();
  form.value = { parentId: selectedCatalogId.value, alias: "", path: "", catalogType: catalogTypeOptions.value[0].id };
  diagOpen.value = true;
}

const handleAddSave = () => {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      if (!selectedCatalogId.value) {
        form.value.parentId = 0;
      }
      addCatalog(form.value).then(response => {
          proxy.$cms.setLastSelectedCatalog(response.data.catalogId);
          diagOpen.value = false;
          proxy.$modal.msgSuccess(proxy.$t('Common.AddSuccess'));
          loadCatalogTreeData();
      });
    }
  });
}

const handleBatchAdd = () => {
  formBatch.value = { parentId: selectedCatalogId.value };
  openBatchAdd.value = true;
}

const handleBatchAddSave = () => {
  proxy.$refs.formBatchRef.validate(valid => {
    if (valid) {
      loading.value = true;
      batchAddCatalog(formBatch.value).then(response => {
        openBatchAdd.value = false;
        loading.value = false;
        proxy.$modal.msgSuccess(proxy.$t('Common.AddSuccess'));
        loadCatalogTreeData();
      });
    }
  });
}

const handlePreview = (data) => {
  let routeData = proxy.$router.resolve({
    path: "/cms/preview",
    query: { type: "catalog", dataId: data.id },
  });
  window.open(routeData.href, '_blank');
}

const handleSortUp = (nodeData) => {
  let data = { catalogId: nodeData.id, sort: -1 }
  sortCatalog(data).then(response => {
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
    loadCatalogTreeData();
  });
}

const handleSortDown = (nodeData) => {
  let data = { catalogId: nodeData.id, sort: 1 }
  sortCatalog(data).then(response => {
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
    loadCatalogTreeData();
  });
}

const handlePublish = (nodeData) => {
  publishCatalogId.value = nodeData.id;
  publishDialogVisible.value = true;
}

const handleDoPublish = () => {
  const data = {
    catalogId: publishCatalogId.value,
    publishChild: publishChild.value,
    publishDetail: false,
    publishStatus: 30
  };
  publishCatalog(data).then(response => {
    taskId.value = response.data;
    openProgress.value = true;
    proxy.$cms.setPublishFlag("true")
  }); 
  publishDialogVisible.value = false;
  publishChild.value = false;
}

const handleCloseProgress = () => {
}

defineExpose({
  loadCatalogTreeData
});
</script>
<style scoped>
.catalog-tree .head-container {
  margin-bottom: 10px;
}
.catalog-tree .btn-add-catalog {
  padding: 8px 0;
}
.catalog-tree .tree-header {
  font-size: 16px;
  font-weight: 700;
  line-height: 22px;
  color: #424242;
  padding-left: 4px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.catalog-tree-header:hover {
  background-color: #F5F7FA;
}
.catalog-tree .tree-node {
  width: 100%;
  line-height: 30px;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
}
.catalog-tree .tree-node .node-text {
  display: block;
  text-overflow: ellipsis;
  overflow: hidden;
}
.catalog-tree .tree-node .node-tool {
  display: none;
  position: absolute;
  right: 5px;
  top: 7px;
  height: 26px;
}
.catalog-tree .tree-node:hover .node-tool {
  display: inline-block;
}
.catalog-tree .tree-node .node-tool .el-button {
  font-size: 14px;
}
</style>