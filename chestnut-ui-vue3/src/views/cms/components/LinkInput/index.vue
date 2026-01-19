<template>
  <div :style="{'width':width}">
    <el-input v-model="model" placeholder="http(s)://">
      <template #append>
        <el-dropdown @command="handleLinkTo">
          <el-button>
            {{ $t('CMS.ContentCore.InternalUrl') }}<el-icon class="el-icon--right"><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-if="selects.includes('catalog')" command="catalog">{{ $t('CMS.ContentCore.SelectCatalog') }}</el-dropdown-item>
              <el-dropdown-item v-if="selects.includes('content')" command="content">{{ $t('CMS.ContentCore.SelectContent') }}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
    </el-input>
    <!-- 栏目选择组件 -->
    <cms-catalog-selector
      v-model:open="openCatalogSelector"
      :disableLink="true"
      @ok="handleCatalogSelectorOk"
      @close="handleCatalogSelectorClose"></cms-catalog-selector>
    <!-- 内容选择组件 -->
    <cms-content-selector
      v-model:open="openContentSelector"
      @ok="handleContentSelectorOk"
      @close="handleContentSelectorClose"></cms-content-selector>
  </div>
</template>
<script setup name="CmsContentSelector">
import CmsCatalogSelector from "@/views/cms/contentcore/catalogSelector";
import CmsContentSelector from "@/views/cms/contentcore/contentSelector";

const model = defineModel();

const props = defineProps({
  width: {
    type: String,
    default: "100%",
    required: false,
  },
  selects: {
    type: Array,
    default: () => ['content', 'catalog'],
    required: false,
  }
});

const openCatalogSelector = ref(false)
const openContentSelector = ref(false)
    
function handleLinkTo(type) {
  if (type === 'content') {
    openContentSelector.value = true;
  } else if (type === 'catalog') {
    openCatalogSelector.value = true;
  }
}
    
function handleCatalogSelectorOk(args) {
  var catalogs = args[0];
  if (catalogs && catalogs.length > 0) {
    model.value = catalogs[0].props.internalUrl;
  }
  openCatalogSelector.value = false;
}
    
function handleCatalogSelectorClose() {
  openCatalogSelector.value = false;
}

function handleContentSelectorOk(contents) {
  if (contents && contents.length > 0) {
    model.value = contents[0].internalUrl;
  }
  openContentSelector.value = false;
}

function handleContentSelectorClose() {
  openContentSelector.value = false;
}
</script>