<template>
  <div class="menu-perms-container" v-loading="loading">
    <el-row class="mb10">
      <el-col>
        <el-button 
          plain 
          type="success" 
          icon="Edit" 
          @click="handleSave"
          v-hasPermi="['system:user:grant', 'system:role:grant']"
        >{{ $t("Common.Save") }}</el-button>
        <el-button 
          plain 
          type="primary" 
          icon="Check" 
          @click="handleSelectAll"
        >{{ selectAll ? $t('Common.CheckInverse') : $t('Common.CheckAll') }}</el-button>
        <el-button 
          plain 
          type="primary" 
          icon="BottomRight" 
          @click="handleExpandAll"
        >{{ expandAll ? $t('Common.Collapse') : $t('Common.Expand') }}</el-button>
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
            ref="menuRef"
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
<script setup name="MenuPermission">
import { savePermissions, getMenuPerms } from "@/api/system/perms";

const props = defineProps({
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
});

watch(() => props.ownerType, (newV, oldV) => {
  if (newV && newV != '') {
    form.value.ownerType = newV;
    loadData();
  }
});

watch(() => props.owner, (newV, oldV) => {
  if (newV && newV != '') {
    form.value.owner = newV;
    loadData();
  }
});

const { proxy } = getCurrentInstance();

const loading = ref(false);
const selectAll = ref(false);
const expandAll = ref(true);
const menuTreeData = ref([]);
const perms = ref([]);
const disabledPerms = ref([]);
const defaultProps = {
  children: 'children',
  label: 'menuName'
};

const data = reactive({
  form: {}
});

const { form } = toRefs(data);

function loadData() {
  loading.value = true;
  const params = { ownerType: props.ownerType, owner: props.owner }
  getMenuPerms(params).then(response => {
    menuTreeData.value = proxy.handleTree(response.data.menus, '0', "menuId");
    perms.value = response.data.perms;
    disabledPerms.value = response.data.disabledPerms;
    nextTick(() => {
      setCheckedMenu(menuTreeData.value, disabledPerms.value);
      loading.value = false;
    })
  });
}
    
function setCheckedMenu(menus, disabledPerms) {
  menus.forEach(m => {
    if (perms.value.includes(m.perms)) {
      proxy.$refs.menuRef.setChecked(m.menuId, true);
    }
    if (disabledPerms.includes(m.perms)) {
      proxy.$refs.menuRef.setChecked(m.menuId, true);
      m.disabled = true;
    }
    if (m.children && m.children.length > 0) {
      setCheckedMenu(m.children, disabledPerms);
    }
  });
}

function handleTreeNodeCheckChange(node, checked) {
  if (node.perms && node.perms.length > 0) {
    if (checked) {
        perms.value.push(node.perms);
        if (node.parentId > 0) {
          proxy.$refs.menuRef.setChecked(node.parentId, true);
        }
    } else {
      for(let i = 0; i < perms.value.length; i++) {
        if(perms.value[i] == node.perms) {
          perms.value.splice(i, 1);
          break;
        }
      }
      if (node.children && node.children.length > 0) {
        node.children.forEach(child => proxy.$refs.menuRef.setChecked(child.menuId, false))
      }
    }
  }
}

function handleSelectAll() {
  checkAll(menuTreeData.value, !selectAll.value)
  selectAll.value = !selectAll.value;
}

function checkAll(menus, checked) {
  menus.forEach(m => {
    if (disabledPerms.value.includes(m.perms)) {
      proxy.$refs.menuRef.setChecked(m.menuId, true);
      m.disabled = true;
    } else {
      proxy.$refs.menuRef.setChecked(m.menuId, checked);
    }
    if (m.children && m.children.length > 0) {
      checkAll(m.children, checked);
    }
  });
}

function handleExpandAll() {
  let treeList = menuTreeData.value;
  for (let i = 0; i < treeList.length; i++) {
    proxy.$refs.menuRef.store.nodesMap[treeList[i].menuId].expanded = !expandAll.value;
  }
  expandAll.value = !expandAll.value;
}

function getMenuAllCheckedKeys() {
  let checkedKeys = proxy.$refs.menuRef.getCheckedNodes();
  let halfCheckedKeys = proxy.$refs.menuRef.getHalfCheckedNodes();
  checkedKeys.unshift.apply(checkedKeys, halfCheckedKeys);
  return checkedKeys;
}

function handleSave() {
  let permissions = [];
  getMenuAllCheckedKeys().forEach(node => {
    if (node.perms && node.perms != '' && !node.disabled) {
      permissions.push(node.perms);
    }
  });
  const _data = {
    ownerType: props.ownerType,
    owner: props.owner,
    permType: 'Menu',
    permissions: permissions
  };
  savePermissions(_data).then(response => {
    proxy.$modal.notifySuccess(proxy.$t('Common.SaveSuccess'));
  });
}

loadData();
</script>
