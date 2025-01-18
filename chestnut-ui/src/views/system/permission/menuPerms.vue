<template>
  <div class="menu-perms-container" v-loading="loading">
    <el-row class="mb10">
      <el-col>
        <el-button plain type="success" icon="el-icon-edit" size="mini" @click="handleSave">{{ $t("Common.Save") }}</el-button>
        <el-button plain type="primary" icon="el-icon-check" size="mini" @click="handleSelectAll">{{ this.selectAll ? $t('Common.CheckInverse') : $t('Common.CheckAll') }}</el-button>
        <el-button plain type="primary" icon="el-icon-bottom-right" size="mini" @click="handleExpandAll">{{ this.expandAll ? $t('Common.Collapse') : $t('Common.Expand') }}</el-button>
      </el-col>
    </el-row>
    <el-row>
      <el-col>
        <el-card shadow="never">
          <el-tree
            class="tree-menu" 
            :data="menuTreeData"
            :check-strictly="true"
            default-expand-all
            show-checkbox
            ref="menu"
            node-key="menuId"
            empty-text="Loading..."
            @check-change="handleTreeNodeCheckChange"
            :props="defaultProps">
          </el-tree>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
<script>
import { savePermissions, getMenuPerms } from "@/api/system/perms";

export default {
  name: "MenuPermission",
  props: {
    ownerType: {
      type: String,
      require: false,
      default: ""
    },
    owner: {
      type: String,
      require: false,
      default: ""
    }
  },
  watch: {
    ownerType: {
      handler(newV, oldV) {
        if (newV && newV != '') {
          this.form.ownerType = newV;
          this.loadData();
        }
      }
    },
    owner: {
      handler(newV, oldV) {
        if (newV && newV != '') {
          this.form.owner = newV;
          this.loadData();
        }
      }
    },
  },
  mounted() {
    this.loadData();
  },
  data() {
    return {
      loading: true,
      selectAll: false,
      expandAll: true,
      menuTreeData: [],
      perms: [],
      disabledPerms: [],
      form: {},
      defaultProps: {
        children: 'children',
        label: 'menuName'
      }
    };
  }, 
  methods: {
    loadData() {
      this.loading = true;
      const params = { ownerType: this.ownerType, owner: this.owner }
      getMenuPerms(params).then(response => {
        this.menuTreeData = this.handleTree(response.data.menus, '0', "menuId");
        this.perms = response.data.perms;
        this.disabledPerms = response.data.disabledPerms;
        this.$nextTick(() => {
          this.setCheckedMenu(this.menuTreeData, this.disabledPerms);
          this.loading = false;
        })
      });
    },
    setCheckedMenu(menus, disabledPerms) {
      menus.forEach(m => {
        if (this.perms.includes(m.perms)) {
          this.$refs.menu.setChecked(m.menuId, true);
        }
        if (disabledPerms.includes(m.perms)) {
          this.$refs.menu.setChecked(m.menuId, true);
          this.$set(m, "disabled", true);
        }
        if (m.children && m.children.length > 0) {
          this.setCheckedMenu(m.children, disabledPerms);
        }
      });
    },
    handleTreeNodeCheckChange(node, checked) {
      if (node.perms && node.perms.length > 0) {
        if (checked) {
            this.perms.push(node.perms);
            if (node.parentId > 0) {
              this.$refs.menu.setChecked(node.parentId, true);
            }
        } else {
          for(let i = 0; i < this.perms.length; i++) {
            if(this.perms[i] == node.perms) {
              this.perms.splice(i, 1);
              break;
            }
          }
          console.log(node)
          if (node.children && node.children.length > 0) {
            node.children.forEach(child => this.$refs.menu.setChecked(child.menuId, false))
          }
        }
      }
    },
    handleSelectAll() {
      this.checkAll(this.menuTreeData, !this.selectAll)
      this.selectAll = !this.selectAll;
    },
    checkAll(menus, checked) {
      menus.forEach(m => {
          if (this.disabledPerms.includes(m.perms)) {
            this.$refs.menu.setChecked(m.menuId, true);
            this.$set(m, "disabled", true);
          } else {
            this.$refs.menu.setChecked(m.menuId, checked);
          }
          if (m.children && m.children.length > 0) {
            this.checkAll(m.children, checked);
          }
        });
    },
    handleExpandAll() {
      let treeList = this.menuTreeData;
      for (let i = 0; i < treeList.length; i++) {
        this.$refs.menu.store.nodesMap[treeList[i].menuId].expanded = !this.expandAll;
      }
      this.expandAll = !this.expandAll;
    },
    getMenuAllCheckedKeys() {
      let checkedKeys = this.$refs.menu.getCheckedNodes();
      let halfCheckedKeys = this.$refs.menu.getHalfCheckedNodes();
      checkedKeys.unshift.apply(checkedKeys, halfCheckedKeys);
      return checkedKeys;
    },
    handleSave() {
      let permissions = [];
      this.getMenuAllCheckedKeys().forEach(node => {
        console.log(node)
        if (node.perms && node.perms != '' && !node.disabled) {
          permissions.push(node.perms);
        }
      });
      const data = {
        ownerType: this.ownerType,
        owner: this.owner,
        permType: 'Menu',
        permissions: permissions
      };
      savePermissions(data).then(response => {
        this.$modal.notifySuccess(this.$t('Common.SaveSuccess'));
      });
    }
  }
};
</script>
