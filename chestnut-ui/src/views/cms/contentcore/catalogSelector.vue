<template>
  <div id="catalog-selector">
    <el-dialog 
      :title="$t('CMS.Catalog.SelectCatalog')"
      :visible.sync="visible"
      width="450px"
      :close-on-click-modal="false"
      append-to-body
      class="catalog-selector"
      style="padding: 10px 20x;">
      <div v-if="showToolbar" class="header-toolbar">
        <div v-if="showCopyToolbar">
          <el-radio-group v-model="copyType" size="mini">
            <el-radio-button label="1">{{ $t('CMS.Catalog.CopyContent') }}</el-radio-button>
            <el-radio-button label="2">{{ $t('CMS.Catalog.MappingContent') }}</el-radio-button>
          </el-radio-group>
          <el-tooltip placement="right" style="margin-left:5px;">
            <div slot="content">
              {{ $t('CMS.Catalog.CopyContentTip') }}<br/>
              {{ $t('CMS.Catalog.MappingContentTip') }}
            </div>
            <i class="el-icon-info"></i>
          </el-tooltip>
        </div>
      </div>
      <div class="search-toolbar">
        <el-input 
          :placeholder="$t('CMS.Catalog.CatalogNamePlaceholder')"
          v-model="filterCatalogName"
          clearable
          size="small"
          suffix-icon="el-icon-search">
        </el-input>
      </div>
      <div class="tree-container">
        <el-scrollbar style="height: 400px;">
          <el-button 
            v-if="showRootNode"
            type="text" 
            :class="'tree-root' + (rootSelected?' cc-current':'')"
            icon="el-icon-s-home"
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
            <template slot-scope="{ node, data }">
              <span :id="'tn-'+node.id" :class="node.disabled?'cc-disabled':''">{{ node.label }}</span>
            </template>
          </el-tree>
        </el-scrollbar>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleCancel">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getCatalogTreeData } from "@/api/contentcore/catalog";

export default {
  name: "CMSCatalogSelector",
  props: {
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
  },
  computed: {
    showToolbar() {
      return this.showCopyToolbar;
    }
  },
  data () {
    return {
      loading: false,
      visible: this.open,
      // 栏目树过滤：栏目名称
      filterCatalogName: undefined,
      // 栏目树数据
      catalogOptions: undefined,
      // 站点名称
      siteName: undefined,
      rootSelected: false,
      // 当前选中栏目ID
      selectedCatalogs: [],
      copyType: 1,
      defaultProps: {
        children: "children",
        label: "label"
      }
    };
  },
  watch: {
    open () {
      this.visible = this.open;
    },
    visible (newVal) {
      if (!newVal) {
        this.handleCancel();
      } else {
        this.loadCatalogTreeData();
      }
    },
    filterCatalogName(val) {
      this.$refs.tree.filter(val);
    }
  },
  methods: {
    loadCatalogTreeData () {
      this.selectedCatalogs = [];
      this.rootSelected = false;
      this.loading = true;
      getCatalogTreeData({disableLink: this.disableLink}).then(response => {
        if (response.code == 200) {
          this.catalogOptions = response.data.rows;
          this.siteName = response.data.siteName; 
        }
        this.loading = false;
      });
    },
    filterNode (value, data) {
      if (!value) return true;
      return data.label.indexOf(value) > -1;
    },
    setNodeHighlight(node) {
      document.querySelectorAll(".cc-current").forEach(item => item.classList.remove("cc-current"));
      if (node) {
        document.querySelector("#tn-"+node.id).classList.add("cc-current");
      }
    },
    handleNodeClick (data, node) {
      if (!this.multiple) {
        if (!this.disableLink || !data.disabled) {
          this.setNodeHighlight(node)
          this.selectedCatalogs = [{ id: data.id, name: data.label, props: data.props }];
          this.rootSelected = false;
        } else {
          this.$refs.tree.setCurrentKey(null)
        }
      }
    },
    handleTreeRootClick(e) {
      if (!this.multiple) {
        this.selectedCatalogs = [{ id: "0", name: this.siteName, props: {} }];
        this.$refs.tree.setCurrentKey(null);
        this.rootSelected = true;
      }
    },
    handleOk (data) {
      if (this.multiple) {
        this.selectedCatalogs = [];
        this.$refs.tree.getCheckedNodes().map(item => {
          this.selectedCatalogs.push({ id: item.id, name: item.label, props: data.props });
        })
      }
      if (this.selectedCatalogs.length == 0) {
        this.$modal.alertWarning(this.$t('CMS.Catalog.SelectCatalogFirst'));
        return;
      }
      this.setNodeHighlight()
      this.$emit("ok", this.selectedCatalogs, this.copyType);
    },
    handleCancel () {
      this.$emit("close");
      this.selectedCatalogs = [];
      this.copyType = 1;
    }
  }
};
</script>
<style>
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