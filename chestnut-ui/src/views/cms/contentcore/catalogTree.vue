<template>
  <div class="catalog-tree">
    <div class="head-container">
      <el-dropdown v-if="showNewBtn" placement="bottom-start" style="margin-top:2px;">
        <el-button 
          type="text"
          @click="handleAdd"
          icon="el-icon-plus">
          {{ $t('CMS.Catalog.AddCatalog') }}<i class="el-icon-arrow-down el-icon--right"></i>
        </el-button>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item @click.native="handleAdd">{{ $t('CMS.Catalog.AddCatalog') }}</el-dropdown-item>
          <el-dropdown-item @click.native="handleBatchAdd">{{ $t('CMS.Catalog.BatchAddCatalog') }}</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
      <el-button type="text" icon="el-icon-refresh" @click="loadCatalogTreeData" style="float:right;margin-top:2px;">{{ $t("Common.Refresh") }}</el-button>
      <el-input 
        :placeholder="$t('CMS.Catalog.CatalogNamePlaceholder')"
        v-model="filterCatalogName"
        clearable
        size="small"
        suffix-icon="el-icon-search">
      </el-input>
    </div>
    <div class="tree-container">
      <el-button 
        :loading="loading"
        :title="siteName"
        type="text" 
        class="tree-header"
        icon="el-icon-s-home"
        @click="handleTreeRootClick">{{ siteName }}</el-button>
      <el-tree 
        :data="catalogOptions" 
        :props="defaultProps" 
        :expand-on-click-node="false"
        :default-expanded-keys="treeExpandedKeys"
        :filter-node-method="filterNode"
        :accordion="expandMode=='accordion'"
        node-key="id"
        ref="tree"
        highlight-current
        @node-click="handleNodeClick">
        <span class="tree-node" slot-scope="{ node, data }">
          <span class="node-text" :title="node.label">{{ node.label }}</span>
          <span class="node-tool">
            <el-dropdown size="small" type="primary">
              <el-link :underline="false" class="row-more-btn" icon="el-icon-more"></el-link>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item @click.native="handlePreview(data)"><svg-icon icon-class="eye-open" class="mr5"></svg-icon>{{ $t('CMS.ContentCore.Preview') }}</el-dropdown-item>
                <el-dropdown-item icon="el-icon-s-promotion" @click.native="handlePublish(data)" v-hasPermi="[ $p('Catalog:Publish:{0}', [ data.catalogId ]) ]">{{ $t('CMS.ContentCore.Publish') }}</el-dropdown-item>
                <el-dropdown-item icon="el-icon-sort-up" @click.native="handleSortUp(data)" v-hasPermi="[ $p('Catalog:Sort:{0}', [ data.catalogId ]) ]">{{ $t('CMS.Catalog.SortUp') }}</el-dropdown-item>
                <el-dropdown-item icon="el-icon-sort-down" @click.native="handleSortDown(data)" v-hasPermi="[ $p('Catalog:Sort:{0}', [ data.catalogId ]) ]">{{ $t('CMS.Catalog.SortDown') }}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </span>
        </span>
      </el-tree>
    </div>
    <!-- 添加栏目对话框 -->
    <el-dialog 
      :title="$t('CMS.Catalog.AddCatalog')"
      :visible.sync="diagOpen"
      :close-on-click-modal="false"
      width="600px"
      append-to-body>
      <el-form 
        ref="form"
        :model="form"
        :rules="rules"
        label-width="120px">
        <el-form-item :label="$t('CMS.Catalog.ParentCatalog')" prop="parentId">
          <treeselect v-model="form.parentId" :options="catalogOptions" />
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
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleAddSave">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
    
    <el-dialog 
      :title="$t('CMS.Catalog.BatchAddCatalog')"
      :visible.sync="openBatchAdd"
      :close-on-click-modal="false"
      :loading="loading"
      width="500px"
      style="padding-top:0;padding-bottom:0;"
      append-to-body>
      <el-form 
        ref="formBatch"
        :model="formBatch"
        :rules="rulesBatch"
        label-position="top"
        label-width="70px">
        <el-form-item :label="$t('CMS.Catalog.ParentCatalog')" prop="parentId">
          <treeselect v-model="formBatch.parentId" :options="catalogOptions" />
        </el-form-item>
        <el-form-item :label="$t('CMS.Catalog.CatalogTree')" prop="catalogs">
          <el-input v-model="formBatch.catalogs" type="textarea" :rows="10" />
        </el-form-item>
      </el-form>
      <div style="background-color: #f4f4f5;color: #909399;font-size:12px;line-height: 30px;padding-left:10px;">
        <i class="el-icon-info mr5"></i>{{ $t("CMS.Catalog.BatchAddTip") }}
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleBatchAddSave">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="openBatchAdd=false">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
    
    <el-dialog 
      :title="$t('CMS.Catalog.PublishDialogTitle')"
      :visible.sync="publishDialogVisible"
      width="500px"
      class="publish-dialog">
      <div>
        <p>{{ $t('Common.Tips') }}</p>
        <p>{{ $t('CMS.Catalog.PublishTips') }}</p>
        <el-checkbox v-model="publishChild">{{ $t('CMS.Catalog.ContainsChildren') }}</el-checkbox>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="publishDialogVisible = false">{{ $t("Common.Cancel") }}</el-button>
        <el-button type="primary" @click="handleDoPublish">{{ $t("Common.Confirm") }}</el-button>
      </span>
    </el-dialog>
    <!-- 进度条 -->
    <cms-progress :title="$t('CMS.Catalog.PublishProgressTitle')" :open.sync="openProgress" :taskId="taskId" @close="handleCloseProgress"></cms-progress>
  </div>
</template>
<script>
import { codeValidator, pathValidator } from '@/utils/validate';
import { getCatalogTypes, getCatalogTreeData, addCatalog, batchAddCatalog, publishCatalog, sortCatalog, generateAliasAndPath } from "@/api/contentcore/catalog";
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";
import CMSProgress from '@/views/components/Progress';

export default {
  name: "CMSCatalogTree",
  components: {
    Treeselect,
    'cms-progress': CMSProgress
  },
  props: {
    newBtn: {
      type: Boolean,
      default: false,
      required: false
    }
  },
  computed: {
    theme() {
      return this.$store.state.settings.theme;
    }
  },
  data () {
    return {
      loading: false,
      // 是否显示新增栏目按钮
      showNewBtn: this.newBtn,
      // 是否显示弹出层
      diagOpen: false,
      // 栏目类型
      catalogTypeOptions: [],
      // 栏目树过滤：栏目名称
      filterCatalogName: "",
      // 栏目树数据
      catalogOptions: [],
      // 站点名称
      siteName: "",
      expandMode: "",
      // 当前选中栏目ID
      selectedCatalogId: "",
      treeExpandedKeys: [],
      defaultProps: {
        children: "children",
        label: "label"
      },
      // 新增栏目表单参数
      form: {
      },
      // 表单校验
      rules: {
        name: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
          { max: 100, messag: this.$t('Common.RuleTips.MaxLength', [ 100 ]), trigger: "change" }
        ],
        alias: [
          { required: true, message: this.$t("Common.RuleTips.NotEmpty"), trigger: "blur" },
          { validator: codeValidator, trigger: "change" },
          { max: 100, messag: this.$t('Common.RuleTips.MaxLength', [ 100 ]), trigger: "change" }
        ],
        path: [
          { required: true, message: this.$t("Common.RuleTips.NotEmpty"), trigger: "blur" },
          { validator: pathValidator, trigger: "change" },
          { max: 255, messag: this.$t('Common.RuleTips.MaxLength', [ 255 ]), trigger: "change" }
        ],
        catalogType: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
        ]
      },
      openBatchAdd: false,
      formBatch: {},
      rulesBatch: {
        catalogs: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
        ]
      },
      publishCatalogId: 0,
      publishDialogVisible: false,
      publishChild: false,
      openProgress: false,
      taskId: "",
    };
  },
  watch: {
    filterCatalogName(val) {
      this.$refs.tree.filter(val);
    }
  },
  created () {
    this.loadCatalogTreeData();
    // 加载栏目类型数据
    getCatalogTypes().then(response => {
      this.catalogTypeOptions = response.data;
    });
  },
  methods: {
    /** 查询栏目树结构 */
    loadCatalogTreeData () {
      this.loading = true;
      getCatalogTreeData().then(response => {
        this.catalogOptions = response.data.rows;
        if (this.catalogOptions.length == 0) {
          this.$cache.local.remove("LastSelectedCatalogId");
        }
        this.siteName = response.data.siteName;
        this.expandMode = response.data.expandMode;
        this.loading = false;
        this.$nextTick(() => {
          this.selectedCatalogId = this.$cache.local.get("LastSelectedCatalogId");
          if (this.selectedCatalogId && this.$refs.tree) {
            this.$refs.tree.setCurrentKey(this.selectedCatalogId);
            this.treeExpandedKeys = [this.selectedCatalogId];
            this.$emit("node-click", this.$refs.tree.getCurrentNode());
          } else {
            this.$emit("node-click", null);
          }
        })
      });
    },
    // 筛选节点
    filterNode (value, data) {
      if (!value) return true;
      return data.label.indexOf(value) > -1;
    },
    // 根节点点击事件
    handleTreeRootClick() {
      this.selectedCatalogId = undefined;
      this.$cache.local.remove("LastSelectedCatalogId");
      this.$refs.tree.setCurrentKey(null);
      this.$emit("node-click", null);
    },
    // 节点单击事件
    handleNodeClick (data) {
      this.selectedCatalogId = data.id;
      this.$cache.local.set("LastSelectedCatalogId", this.selectedCatalogId);
      this.$emit("node-click", data);
    },
    // 取消按钮
    cancel () {
      this.diagOpen = false;
      this.resetAddForm();
    },
    // 表单重置
    resetAddForm () {
      this.resetForm("form");
    },
    handleNameBlur() {
      let data = this.openBatchAdd ? this.formBatch : this.form;
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
    },
    /** 新增按钮操作 */
    handleAdd () {
      this.resetAddForm();
      this.form = { parentId: this.selectedCatalogId, alias: "", path: "", catalogType: this.catalogTypeOptions[0].id };
      this.diagOpen = true;
    },
    /** 新增栏目提交 */
    handleAddSave: function () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (!this.selectedCatalogId) {
            this.form.parentId = 0;
          }
          addCatalog(this.form).then(response => {
              this.$cache.local.set("LastSelectedCatalogId", response.data.catalogId);
              this.diagOpen = false;
              this.$modal.msgSuccess(this.$t('Common.AddSuccess'));
              this.loadCatalogTreeData();
          });
        }
      });
    },
    handleBatchAdd () {
      this.formBatch = { parentId: this.selectedCatalogId }
      this.openBatchAdd = true;
    },
    handleBatchAddSave() {
      this.$refs["formBatch"].validate(valid => {
        if (valid) {
          this.loading = true;
          batchAddCatalog(this.formBatch).then(response => {
              this.openBatchAdd = false;
              this.loading = false;
              this.$modal.msgSuccess(this.$t('Common.AddSuccess'));
              this.loadCatalogTreeData();
          });
        }
      });
    },
    handlePreview (data) {
      let routeData = this.$router.resolve({
        path: "/cms/preview",
        query: { type: "catalog", dataId: data.id },
      });
      window.open(routeData.href, '_blank');
    },
    handleSortUp (nodeData) {
      let data = { catalogId: nodeData.id, sort: -1 }
      sortCatalog(data).then(response => {
          this.$modal.msgSuccess(this.$t('Common.OpSuccess'));
          this.loadCatalogTreeData();
      });
    },
    handleSortDown (nodeData) {
      let data = { catalogId: nodeData.id, sort: 1 }
      sortCatalog(data).then(response => {
          this.$modal.msgSuccess(this.$t('Common.OpSuccess'));
          this.loadCatalogTreeData();
      });
    },
    handlePublish (nodeData) {
      this.publishCatalogId = nodeData.id;
      this.publishDialogVisible = true;
    },
    handleDoPublish() {
      const data = {
        catalogId: this.publishCatalogId,
        publishChild: this.publishChild,
        publishDetail: false,
        publishStatus: 30
      };
      publishCatalog(data).then(response => {
        this.taskId = response.data;
        this.progressTitle = this.$t('CMS.Catalog.PublishProgressTitle');
        this.progressType = "Publish";
        this.openProgress = true;
        this.$cache.local.set('publish_flag', "true")
      }); 
      this.publishDialogVisible = false;
      this.publishChild = false;
    },
    handleCloseProgress() {
    },
  }
};
</script>
<style scoped>
.catalog-tree .head-container {
  margin-bottom: 10px;
}
.catalog-tree .tree-header {
  font-size: 16px;
  font-weight: 700;
  line-height: 22px;
  color: #424242;
  width: 100%;
  text-align: left;
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
  top: 0;
}
.catalog-tree .tree-node:hover .node-tool {
  display: inline-block;
}
.catalog-tree .tree-node .node-tool .el-button {
  font-size: 14px;
}
</style>