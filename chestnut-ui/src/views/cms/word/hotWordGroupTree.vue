<template>
  <div class="group-tree">
    <div class="head-container">
      <el-button type="text" icon="el-icon-plus" @click="handleAdd">{{ $t('WordMgr.HotWord.AddGroup') }}</el-button>
      <el-input 
        :placeholder="$t('WordMgr.HotWord.Placeholder.InputGroupName')"
        v-model="filterGroupName"
        clearable
        size="small"
        suffix-icon="el-icon-search">
      </el-input>
    </div>
    <div class="tree-container">
      <el-button type="text" class="tree-header" icon="el-icon-s-home" @click="handleTreeRootClick">{{ $t('APP.TITLE') }}</el-button>
      <el-tree 
        :data="groupOptions" 
        :props="defaultProps" 
        :expand-on-click-node="false"
        :default-expanded-keys="treeExpandedKeys"
        :filter-node-method="filterNode"
        node-key="id"
        ref="tree"
        highlight-current
        @node-click="handleNodeClick">
        <span class="tree-node" slot-scope="{ node, data }">
          <span>{{ node.label }}</span>
          <span class="node-tool">
            <el-button
              type="text"
              size="small"
              :title="$t('Common.Edit')"
              icon="el-icon-edit"
              @click="handleEdit(data)">
            </el-button>
            <el-button
              type="text"
              size="small"
              :title="$t('Common.Delete')"
              icon="el-icon-delete"
              @click="handleDelete(data)">
            </el-button>
          </span>
        </span>
      </el-tree>
    </div>
    <!-- 添加分组对话框 -->
    <el-dialog :title="diagTitle" :visible.sync="diagOpen" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item :label="$t('WordMgr.HotWord.GroupName')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="$t('WordMgr.HotWord.GroupCode')" prop="code">
          <el-input v-model="form.code" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleSave">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getHotWordGroupTreeData, addHotWordGroup } from "@/api/contentcore/word";
import { editHotWordGroup, deleteHotWordGroup } from "@/api/word/hotWord";
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";

export default {
  name: "CMSHotWordGroupTree",
  components: {
    Treeselect
  },
  computed: {
    theme() {
      return this.$store.state.settings.theme;
    }
  },
  data () {
    return {
      // 是否显示弹出层
      diagOpen: false,
      // 分组树过滤：名称
      filterGroupName: undefined,
      // 分组树数据
      groupOptions: undefined,
      // 当前选中分组ID
      selectedGroupId: undefined,
      treeExpandedKeys: [],
      defaultProps: {
        children: "children",
        label: "label"
      },
      diagTitle: "",
      form: {
      },
      rules: {
        name: [
          { required: true, message: this.$t('Common.NotEmpty'), trigger: "blur" }
        ],
        code: [
          { required: true, pattern: "^[A-Za-z0-9_]+$", message: this.$t('Common.RuleTips.Code'), trigger: "blur" }
        ]
      }
    };
  },
  watch: {
    filterGroupName(val) {
      this.$refs.tree.filter(val);
    }
  },
  created () {
    this.loadGroupTreeData();
  },
  methods: {
    loadGroupTreeData () {
      getHotWordGroupTreeData().then(response => {
        this.groupOptions = response.data;
        this.$nextTick(() => {
          this.selectedGroupId = this.$cache.local.get("LastSelectedHotWordGroupId");
          if (this.selectedGroupId && this.$refs.tree) {
            this.$refs.tree.setCurrentKey(this.selectedGroupId);
            this.treeExpandedKeys = [this.selectedGroupId];
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
      this.selectedGroupId = undefined;
      this.$cache.local.remove("LastSelectedHotWordGroupId");
      this.$refs.tree.setCurrentKey(null);
      this.$emit("node-click", null);
    },
    // 节点单击事件
    handleNodeClick (data) {
      this.selectedGroupId = data.id;
      this.$cache.local.set("LastSelectedHotWordGroupId", this.selectedGroupId);
      this.$emit("node-click", data);
    },
    // 取消按钮
    cancel () {
      this.diagOpen = false;
      this.reset();
    },
    // 表单重置
    reset () {
      this.resetForm("form");
    },
    /** 新增按钮操作 */
    handleAdd () {
      this.form = {};
      this.diagTitle = this.$t('WordMgr.HotWord.AddGroupTitle');
      this.diagOpen = true;
    },
    handleEdit (data) {
      this.form = { 
        groupId: data.id, 
        name: data.label, 
        code: data.props.code
      };
      this.diagTitle = this.$t('WordMgr.HotWord.EditGroupTitle');
      this.diagOpen = true;
    },
    /** 分组表单提交 */
    handleSave: function () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (!this.form.parentId || this.form.parentId.length == 0) {
            this.form.parentId = 0;
          }
          if (this.form.groupId) {
            editHotWordGroup(this.form).then(response => {
              this.diagOpen = false;
              this.$modal.msgSuccess(response.msg);
              this.loadGroupTreeData();
            });
          } else {
            addHotWordGroup(this.form).then(response => {
              this.$cache.local.set("LastSelectedHotWordGroupId", response.data.groupId);
              this.diagOpen = false;
              this.$modal.msgSuccess(response.msg);
              this.loadGroupTreeData();
            });
          }
        }
      });
    },
    handleDelete (data) {
      const groupIds = [ data.id ];
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function() {
        return deleteHotWordGroup(groupIds);
      }).then((response) => {
        this.$modal.msgSuccess(response.msg);
        this.loadGroupTreeData();
      }).catch(() => {});
    }
  }
};
</script>
<style scoped>
.group-tree .tree-header {
  font-size: 16px;
  font-weight: 700;
  line-height: 22px;
  color: #424242;
  width: 100%;
  text-align: left;
  padding-left: 4px;
}
.group-tree-header:hover {
  background-color: #F5F7FA;
}
.group-tree .tree-node {
  width: 100%;
  line-height: 30px;
}
.group-tree .tree-node .node-tool {
  display: none;
  position: absolute;
  right: 5px;
}
.group-tree .tree-node:hover .node-tool {
  display: inline-block;
}
.group-tree .tree-node .node-tool .el-button {
  font-size: 14px;
}
</style>