<template>
  <div class="group-tree">
    <div class="head-container">
      <el-button type="text" icon="Plus" @click="handleAdd">{{ $t('WordMgr.HotWord.AddGroup') }}</el-button>
      <el-input 
        :placeholder="$t('WordMgr.HotWord.Placeholder.InputGroupName')"
        v-model="filterGroupName"
        clearable
        suffix-icon="Search">
      </el-input>
    </div>
    <div class="tree-container">
      <el-button type="text" class="tree-header" icon="HomeFilled" @click="handleTreeRootClick">{{ $t('APP.TITLE') }}</el-button>
      <el-tree 
        :data="groupOptions" 
        :props="defaultProps" 
        :expand-on-click-node="false"
        :default-expanded-keys="treeExpandedKeys"
        :filter-node-method="filterNode"
        node-key="id"
        ref="treeRef"
        highlight-current
        @node-click="handleNodeClick">
        <template #default="{ node, data }">
          <span class="tree-node">
            <span>{{ node.label }}</span>
            <span class="node-tool">
              <el-button
                type="text"
                :title="$t('Common.Edit')"
                icon="Edit"
                @click="handleEdit(data)">
              </el-button>
              <el-button
                type="text"
                :title="$t('Common.Delete')"
                icon="Delete"
                @click="handleDelete(data)">
              </el-button>
            </span>
          </span>
        </template>
      </el-tree>
    </div>
    <!-- 添加分组对话框 -->
    <el-dialog :title="diagTitle" v-model="diagOpen" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item :label="$t('WordMgr.HotWord.GroupName')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="$t('WordMgr.HotWord.GroupCode')" prop="code">
          <el-input v-model="form.code" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleSave">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="HotWordGroupTree">
import { codeValidator } from '@/utils/validate';
import { getHotWordGroupTreeData, addHotWordGroup, editHotWordGroup, deleteHotWordGroup } from "@/api/word/hotWord";

const { proxy } = getCurrentInstance();

const diagOpen = ref(false);
const filterGroupName = ref(undefined);
const groupOptions = ref(undefined);
const selectedGroupId = ref(undefined);
const treeExpandedKeys = ref([]);
const defaultProps = ref({
  children: "children",
  label: "label"
});
const diagTitle = ref("");
const objects = reactive({
  form: {}
});
const { form } = toRefs(objects);
const rules = {
  name: [
    { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  code: [
    { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    { validator: codeValidator, trigger: "change" },
  ]
}
watch(filterGroupName, (val) => {
  proxy.$refs.treeRef.filter(val);
});

onMounted(() => {
  loadGroupTreeData();
});

function loadGroupTreeData () {
  getHotWordGroupTreeData().then((response) => {
  groupOptions.value = response.data;
  proxy.nextTick(() => {
    selectedGroupId.value = proxy.$cms.getLastSelectedHotWordGroup();
    if (selectedGroupId.value && proxy.$refs.treeRef) {
      proxy.$refs.treeRef.setCurrentKey(selectedGroupId.value);
      treeExpandedKeys.value = [selectedGroupId.value];
      emit("node-click", proxy.$refs.treeRef.getCurrentNode());
    } else {
      emit("node-click", null);
    }
    });
  });
}
// 筛选节点
function filterNode (value, data) {
  if (!value) return true;
  return data.label.indexOf(value) > -1;
}
// 根节点点击事件
function handleTreeRootClick () {
  selectedGroupId.value = undefined;
  proxy.$cms.clearLastSelectedHotWordGroup();
  proxy.$refs.treeRef.setCurrentKey(null);
  emit("node-click", null);
}
// 节点单击事件
function handleNodeClick (data) {
  selectedGroupId.value = data.id;
  proxy.$cms.setLastSelectedHotWordGroup(data.id);
  emit("node-click", data);
}
// 取消按钮
function cancel () {
  diagOpen.value = false;
  reset();
}
// 表单重置
function reset () {
  proxy.resetForm("formRef");
}
/** 新增按钮操作 */
function handleAdd () {
  form.value = {};
  diagTitle.value = proxy.$t('WordMgr.HotWord.AddGroupTitle');
  diagOpen.value = true;
}
function handleEdit (data) {
  form.value = { 
    groupId: data.id, 
    name: data.label, 
    code: data.props.code
  }
  diagTitle.value = proxy.$t('WordMgr.HotWord.EditGroupTitle');
  diagOpen.value = true;
}
    /** 分组表单提交 */
function handleSave () {
  proxy.$refs.formRef.validate((valid) => {
    if (valid) {
      if (!form.value.parentId || form.value.parentId.length == 0) {
        form.value.parentId = 0;
      }
      if (form.value.groupId) {
        editHotWordGroup(form.value).then(response => {
          diagOpen.value = false;
          proxy.$modal.msgSuccess(response.msg);
          loadGroupTreeData();
        });
      } else {
        addHotWordGroup(form.value).then(response => {
          proxy.$cms.setLastSelectedHotWordGroup(response.data.groupId);
          diagOpen.value = false;
          proxy.$modal.msgSuccess(response.msg);
          loadGroupTreeData();
        });
      }
    }
  });
}
function handleDelete (data) {
  const groupIds = [ data.id ];
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function() {
    return deleteHotWordGroup(groupIds);
  }).then((response) => {
    proxy.$modal.msgSuccess(response.msg);
    loadGroupTreeData();
  }).catch(() => {});
}
</script>
<style scoped>
.group-tree .tree-header {
  font-size: 16px;
  font-weight: 700;
  line-height: 22px;
  color: #424242;
  width: 100%;
  justify-content: flex-start;
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